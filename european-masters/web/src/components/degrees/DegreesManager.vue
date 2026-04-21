<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import apiClient from '../../services/apiClient';
import { formatDate, formatId } from '../../utils/formatters';
import BaseModal from '../common/BaseModal.vue';
import ErrorModal from '../common/ErrorModal.vue';
import DataTable from '../common/DataTable.vue';
import type { Column } from '../common/DataTable.vue';

interface Degree {
  id: number;
  name: string;
  pdfFileName: string;
  pdfCurriculumPath: string;
  subjectImageName: string;
  subjectImagePath: string;
  createdAt: string;
}

interface DegreeFormData {
  name: string;
  file: File | null;
  subjectImage: File | null;
}

// State
const degrees = ref<Degree[]>([]);
const totalRecords = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchQuery = ref('');
const isEditMode = ref(false);
const selectedDegree = ref<Degree | null>(null);
const showDeleteModal = ref(false);
const degreeToDelete = ref<Degree | null>(null);
const isLoading = ref(false);
const formError = ref('');

// Global Error Modal State
const apiError = ref<any>(null);
const showErrorModal = ref(false);

const handleApiError = (err: any) => {
  apiError.value = err;
  showErrorModal.value = true;
};

const formData = ref<DegreeFormData>({
  name: '',
  file: null,
  subjectImage: null
});

// Drag & Drop state
const isDragging = ref(false);
const isDraggingImage = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const imageInput = ref<HTMLInputElement | null>(null);

// Fetching data
const fetchDegrees = async () => {
  isLoading.value = true;
  try {
    const response = await apiClient.get('/v1/degrees', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        search: searchQuery.value,
        sortBy: 'pdfCurriculumPath',  
        direction: 'desc'
      }
    });
    degrees.value = response.data.content;
    totalRecords.value = response.data.totalElements;
  } catch (error) {
    console.error('Error fetching degrees:', error);
    handleApiError(error);
  } finally {
    isLoading.value = false;
  }
};

// Debounce search
let searchTimeout: any;
watch(searchQuery, () => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    currentPage.value = 1;
    fetchDegrees();
  }, 300);
});

// Form Handlers
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    formData.value.file = target.files[0];
  }
};

const onDrop = (event: DragEvent) => {
  isDragging.value = false;
  if (event.dataTransfer?.files && event.dataTransfer.files[0]) {
    const file = event.dataTransfer.files[0];
    if (file.type === 'application/pdf') {
      formData.value.file = file;
    }
  }
};

const validateForm = () => {
  if (!formData.value.name.trim()) {
    formError.value = 'El nombre es requerido.';
    return false;
  }
  
  // Si estamos creando, el archivo PDF es obligatorio
  if (!isEditMode.value && !formData.value.file) {
    formError.value = 'El archivo PDF es obligatorio para crear el programa.';
    return false;
  }

  // Si estamos editando y no hay un archivo nuevo seleccionado, 
  // verificamos que al menos exista uno previamente cargado
  if (isEditMode.value && !formData.value.file && !selectedDegree.value?.pdfFileName) {
    formError.value = 'El programa debe tener un archivo PDF asociado.';
    return false;
  }

  formError.value = '';
  return true;
};

const handleImageSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    formData.value.subjectImage = target.files[0];
  }
};

const onDropImage = (event: DragEvent) => {
  isDraggingImage.value = false;
  if (event.dataTransfer?.files && event.dataTransfer.files[0]) {
    const file = event.dataTransfer.files[0];
    if (file.type.startsWith('image/')) {
      formData.value.subjectImage = file;
    }
  }
};

const createDegree = async () => {
  if (!validateForm()) return;
  isLoading.value = true;
  try {
    const data = new FormData();
    data.append('data',  new Blob([JSON.stringify({ name: formData.value.name, areaId: 1 })], { type: "application/json" }));
    if (formData.value.file) data.append('curriculum', formData.value.file);
    if (formData.value.subjectImage) data.append('subjectImage', formData.value.subjectImage);

    await apiClient.post('/v1/degrees', data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    resetForm();
    fetchDegrees();
  } catch (error) {
    handleApiError(error);
  } finally {
    isLoading.value = false;
  }
};

const updateDegree = async () => {
  if (!validateForm() || !selectedDegree.value) return;
  isLoading.value = true;
  try {
    const data = new FormData();
    data.append('data',  new Blob([JSON.stringify({ name: formData.value.name, areaId: 1 })], { type: "application/json" }));
    if (formData.value.file) data.append('curriculum', formData.value.file);
    if (formData.value.subjectImage) data.append('subjectImage', formData.value.subjectImage);

    await apiClient.put(`/v1/degrees/${selectedDegree.value.id}`, data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    resetForm();
    fetchDegrees();
  } catch (error) {
    handleApiError(error);
  } finally {
    isLoading.value = false;
  }
};

const enterEditMode = (degree: Degree) => {
  isEditMode.value = true;
  selectedDegree.value = degree;
  formData.value = {
    name: degree.name,
    file: null,
    subjectImage: null
  };
  formError.value = '';
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const resetForm = () => {
  isEditMode.value = false;
  selectedDegree.value = null;
  formData.value = { name: '', file: null, subjectImage: null };
  formError.value = '';
};

const confirmDelete = (degree: Degree) => {
  degreeToDelete.value = degree;
  showDeleteModal.value = true;
};

const deleteDegree = async () => {
  if (!degreeToDelete.value) return;
  isLoading.value = true;
  try {
    await apiClient.delete(`/v1/degrees/${degreeToDelete.value.id}`);
    showDeleteModal.value = false;
    degreeToDelete.value = null;
    fetchDegrees();
  } catch (error) {
    handleApiError(error);
  } finally {
    isLoading.value = false;
  }
};

const downloadPdf = async (id: number) => {
  isLoading.value = true;
  try {
    const response = await apiClient.get(`/v1/degrees/${id}/pdf`, {
      responseType: 'blob'
    });
    
    const blob = new Blob([response.data], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    window.open(url, '_blank');
    
    // Cleanup URL after some time
    setTimeout(() => window.URL.revokeObjectURL(url), 100);
  } catch (error) {
    handleApiError(error);
  } finally {
    isLoading.value = false;
  }
};

// Pagination helpers
const totalPages = computed(() => Math.ceil(totalRecords.value / pageSize.value));
const paginatedRange = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value + 1;
  const end = Math.min(currentPage.value * pageSize.value, totalRecords.value);
  return { start, end };
});

const tableColumns: Column[] = [
  { key: 'id', label: '# ID', slot: true },
  { key: 'name', label: 'Nombre de la Maestría', slot: true },
  { key: 'subjectImage', label: 'Imagen Asunto', slot: true },
  { key: 'pdf', label: 'Archivo PDF', slot: true },
  { key: 'createdAt', label: 'Fecha de Registro', slot: true },
  { key: 'actions', label: 'Acciones', slot: true }
];

const tableData = computed(() => {
  return degrees.value.map(degree => ({
    ...degree,
    _isEditing: selectedDegree.value?.id === degree.id
  }));
});

const downloadImage = async (id: number) => {
  isLoading.value = true;
  try {
    const response = await apiClient.get(`/v1/degrees/${id}/image`, {
      responseType: 'blob'
    });
    
    const blob = new Blob([response.data], { type: 'image/png' });
    const url = window.URL.createObjectURL(blob);
    window.open(url, '_blank');
    
    setTimeout(() => window.URL.revokeObjectURL(url), 100);
  } catch (error) {
    handleApiError(error);
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchDegrees);
</script>

<template>
  <div class="panel-container">
    <!-- Form Panel -->
    <section class="glass-surface glass-panel">
      <header class="panel-header">
        <div v-if="isEditMode" class="edit-badge">
          <span class="badge-icon">✏</span> Editando ID: {{ formatId(selectedDegree?.id || 0) }}
        </div>
        <h2 class="panel-title">{{ isEditMode ? formData.name : 'Nueva Maestría' }}</h2>
        <p class="panel-subtitle">
          {{ isEditMode 
            ? 'Modifica los detalles del programa académico y actualiza el documento PDF asociado.' 
            : 'Complete los detalles a continuación para registrar un nuevo programa académico.' 
          }}
        </p>
      </header>

      <div class="form-grid">
        <div class="form-group full-width">
          <label class="form-label">Nombre de la Maestría</label>
          <input 
            v-model="formData.name"
            type="text" 
            class="form-input" 
            placeholder="Ej. Maestría en Ciencias de la Computación"
            :class="{ 'input-error': formError && !formData.name }"
          />
          <p v-if="formError && !formData.name" class="error-text">{{ formError }}</p>
        </div>

        <div class="form-group">
          <label class="form-label">Archivo PDF del Programa</label>
          <div 
            class="upload-zone"
            :class="{ 
              'dragging': isDragging, 
              'has-file': formData.file || (isEditMode && selectedDegree?.pdfFileName),
              'input-error': formError && !formData.file && (!isEditMode || !selectedDegree?.pdfFileName)
            }"
            @dragover.prevent="isDragging = true"
            @dragleave.prevent="isDragging = false"
            @drop.prevent="onDrop"
            @click="fileInput?.click()"
          >
            <input 
              ref="fileInput"
              type="file" 
              accept=".pdf" 
              hidden 
              @change="handleFileSelect"
            />
            
            <div v-if="!formData.file && !(isEditMode && selectedDegree?.pdfFileName)" class="upload-placeholder">
              <div class="upload-icon">📤</div>
              <p class="upload-text">Archivo PDF</p>
              <p class="upload-subtext">Arrastre o haga clic</p>
              <span class="upload-badge">PDF</span>
            </div>

            <div v-else class="file-info">
              <div class="file-icon">📄</div>
              <div class="file-details">
                <p class="file-name">{{ formData.file ? formData.file.name : selectedDegree?.pdfFileName }}</p>
              </div>
              <button class="remove-file" @click.stop="formData.file = null">×</button>
            </div>
          </div>
          <p v-if="formError && !formData.file && (!isEditMode || !selectedDegree?.pdfFileName)" class="error-text">
            {{ formError }}
          </p>
          <button v-if="isEditMode && selectedDegree?.pdfFileName && !formData.file" class="replace-btn" @click="fileInput?.click()">
            Reemplazar PDF
          </button>
        </div>

        <div class="form-group">
          <label class="form-label">Imagen del Asunto</label>
          <div 
            class="upload-zone"
            :class="{ 'dragging': isDraggingImage, 'has-file': formData.subjectImage || (isEditMode && selectedDegree?.subjectImageName) }"
            @dragover.prevent="isDraggingImage = true"
            @dragleave.prevent="isDraggingImage = false"
            @drop.prevent="onDropImage"
            @click="imageInput?.click()"
          >
            <input 
              ref="imageInput"
              type="file" 
              accept="image/*" 
              hidden 
              @change="handleImageSelect"
            />
            
            <div v-if="!formData.subjectImage && !(isEditMode && selectedDegree?.subjectImageName)" class="upload-placeholder">
              <div class="upload-icon">🖼️</div>
              <p class="upload-text">Imagen Asunto</p>
              <p class="upload-subtext">Arrastre o haga clic</p>
              <span class="upload-badge">PNG/JPG</span>
            </div>

            <div v-else class="file-info">
              <div class="file-icon">🖼️</div>
              <div class="file-details">
                <p class="file-name">{{ formData.subjectImage ? formData.subjectImage.name : selectedDegree?.subjectImageName }}</p>
              </div>
              <button class="remove-file" @click.stop="formData.subjectImage = null">×</button>
            </div>
          </div>
          <button v-if="isEditMode && selectedDegree?.subjectImageName && !formData.subjectImage" class="replace-btn" @click="imageInput?.click()">
            Reemplazar Imagen
          </button>
        </div>
      </div>

      <div class="form-actions">
        <button v-if="isEditMode" class="btn-secondary" @click="resetForm">Cancelar</button>
        <button 
          class="btn-primary" 
          :disabled="!formData.name || isLoading"
          @click="isEditMode ? updateDegree() : createDegree()"
        >
          <span class="btn-icon">{{ isEditMode ? '💾' : '+' }}</span>
          {{ isEditMode ? 'Guardar cambios' : 'Crear Maestría' }}
        </button>
      </div>
    </section>

    <!-- Table Section -->
    <DataTable
      title="Listado de Programas"
      icon="📑"
      :subtitle="isEditMode ? `Total: ${totalRecords} programas registrados` : undefined"
      :columns="tableColumns"
      :data="tableData"
      :totalRecords="totalRecords"
      v-model:currentPage="currentPage"
      v-model:pageSize="pageSize"
      v-model:searchQuery="searchQuery"
      searchPlaceholder="Buscar por nombre de maestría..."
      :isLoading="isLoading"
      emptyMessage="No se encontraron programas."
      @fetch="fetchDegrees"
    >
      <template #cell-id="{ row }">
        <span class="id-cell">{{ formatId(row.id) }}</span>
      </template>

      <template #cell-name="{ row }">
        <div class="name-container" :title="row.name">
          <span class="degree-name">{{ row.name }}</span>
          <div v-if="selectedDegree?.id === row.id" class="editing-label">ACTUALMENTE EDITANDO</div>
        </div>
      </template>

      <template #cell-subjectImage="{ row }">
        <div 
          class="image-status-cell" 
          :class="{ 'has-image': row.subjectImagePath }"
          @click="row.subjectImagePath ? downloadImage(row.id) : null"
          :title="row.subjectImagePath ? 'Ver imagen' : 'Sin imagen'"
        >
          <div v-if="row.subjectImagePath" class="status-icon-wrapper">
            <span class="status-icon">🖼️</span>
            <span class="status-badge-small">Existe</span>
          </div>
          <div v-else class="status-icon-wrapper disabled">
            <span class="status-icon">🚫</span>
            <span class="status-badge-small">Falta</span>
          </div>
        </div>
      </template>

      <template #cell-pdf="{ row }">
        <button class="pdf-link-btn" @click="downloadPdf(row.id)">
          <span class="pdf-icon">📄</span> {{ row.pdfCurriculumPath?.substring(row.pdfCurriculumPath.lastIndexOf('/') + 1) }}
        </button>
      </template>

      <template #cell-createdAt="{ row }">
        {{ formatDate(row.createdAt) }}
      </template>

      <template #cell-actions="{ row }">
        <div class="actions-cell">
          <button class="action-btn edit" title="Editar" @click="enterEditMode(row)">
            ✏️
          </button>
          <button class="action-btn delete" title="Eliminar" @click="confirmDelete(row)">
            🗑️
          </button>
        </div>
      </template>
    </DataTable>

    <!-- Delete Confirmation Modal -->
    <BaseModal
      :show="showDeleteModal"
      title="¿Eliminar la maestría?"
      type="danger"
      confirm-text="Eliminar permanentemente"
      @confirm="deleteDegree"
      @cancel="showDeleteModal = false"
    >
      ¿Estás seguro de que deseas eliminar la <strong>"{{ degreeToDelete?.name }}"</strong>? 
      Esta acción es permanente y no se puede deshacer.
    </BaseModal>

    <!-- Global Error Modal -->
    <ErrorModal
      :show="showErrorModal"
      :error="apiError"
      @close="showErrorModal = false"
    />

    <!-- Loading Overlay -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<style scoped>
/* Estilos específicos del formulario de maestrías */
.edit-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: var(--color-active-surface);
  color: var(--color-primary);
  padding: 4px 12px;
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  margin-bottom: 24px;
}

.full-width {
  grid-column: span 2;
}

.input-error {
  border-color: #ff4d4f;
}

.error-text {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

/* Upload Zone */
.upload-zone {
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-input);
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: var(--transition-base);
  background: rgba(255, 255, 255, 0.3);
  min-height: 140px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.upload-zone.dragging {
  border-color: var(--color-primary);
  background: var(--color-active-surface);
}

.upload-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.upload-text {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 14px;
}

.upload-subtext {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}

.upload-badge {
  background: var(--color-divider);
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  font-size: 10px;
  font-weight: 600;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  width: 100%;
}

.file-icon {
  font-size: 24px;
}

.file-name {
  font-weight: 600;
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.file-status {
  font-size: 12px;
  color: #52c41a;
}

.remove-file {
  margin-left: auto;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: var(--color-text-muted);
}

.replace-btn {
  align-self: center;
  background: none;
  border: 1px solid var(--color-border);
  padding: 6px 12px;
  border-radius: var(--radius-button);
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
}

/* Custom Table Cells */
.image-status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  padding: 4px;
  border-radius: 8px;
  transition: var(--transition-base);
}

.image-status-cell.has-image {
  cursor: pointer;
}

.image-status-cell.has-image:hover {
  background: var(--color-active-surface);
}

.status-icon-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.status-icon-wrapper.disabled {
  opacity: 0.5;
}

.status-icon {
  font-size: 18px;
}

.status-badge-small {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.has-image .status-badge-small {
  color: #52c41a;
}

.disabled .status-badge-small {
  color: var(--color-text-muted);
}

.degree-name {
  font-weight: 600;
  display: block;
  max-width: 500px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.editing-label {
  color: var(--color-primary);
  font-size: 11px;
  font-weight: 600;
  margin-top: 4px;
}

.pdf-link-btn {
  background: none;
  border: none;
  color: var(--color-primary);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 0;
  font-family: inherit;
  font-size: inherit;
  transition: var(--transition-base);
}

.pdf-link-btn:hover {
  opacity: 0.8;
  text-decoration: underline;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>