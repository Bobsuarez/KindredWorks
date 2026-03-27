<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import apiClient from '../../services/apiClient';
import BaseModal from '../common/BaseModal.vue';
import ErrorModal from '../common/ErrorModal.vue';

interface Degree {
  id: number;
  name: string;
  pdfFileName: string;
  pdfCurriculumPath: string;
  createdAt: string;
}

interface DegreeFormData {
  name: string;
  file: File | null;
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
  file: null
});

// Drag & Drop state
const isDragging = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);

// Fetching data
const fetchDegrees = async () => {
  isLoading.value = true;
  try {
    const response = await apiClient.get('/v1/degrees', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        search: searchQuery.value
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
    formError.value = 'El nombre es requerido para crear el registro.';
    return false;
  }
  formError.value = '';
  return true;
};

const createDegree = async () => {
  if (!validateForm()) return;
  isLoading.value = true;
  try {
    const data = new FormData();
    data.append('data',  new Blob([JSON.stringify({ name: formData.value.name, areaId: 1 })], { type: "application/json" }));
    if (formData.value.file) data.append('curriculum', formData.value.file);

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
    file: null
  };
  formError.value = '';
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const resetForm = () => {
  isEditMode.value = false;
  selectedDegree.value = null;
  formData.value = { name: '', file: null };
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

const formatId = (id: number) => `#${String(id).padStart(3, '0')}`;
const formatDate = (dateStr: string) => new Date(dateStr).toLocaleDateString('es-ES');

onMounted(fetchDegrees);
</script>

<template>
  <div class="manager-container">
    <!-- Form Panel -->
    <section class="glass-surface form-panel">
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
        <div class="form-group">
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
            :class="{ 'dragging': isDragging, 'has-file': formData.file || (isEditMode && selectedDegree?.pdfFileName) }"
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
              <p class="upload-text">Arrastre su archivo PDF aquí</p>
              <p class="upload-subtext">o haga clic para seleccionar desde su equipo</p>
              <span class="upload-badge">Máximo 10MB</span>
            </div>

            <div v-else class="file-info">
              <div class="file-icon">📄</div>
              <div class="file-details">
                <p class="file-name">{{ formData.file ? formData.file.name : selectedDegree?.pdfFileName }}</p>
                <p v-if="isEditMode && !formData.file" class="file-status">Archivo cargado correctamente</p>
              </div>
              <button class="remove-file" @click.stop="formData.file = null">×</button>
            </div>
          </div>
          <button v-if="isEditMode && selectedDegree?.pdfFileName && !formData.file" class="replace-btn" @click="fileInput?.click()">
            Reemplazar Archivo
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
    <section class="glass-surface table-section">
      <header class="table-header">
        <div class="header-left">
          <h3 class="table-title">
            <span class="header-icon">📑</span> Listado de Programas
          </h3>
          <p v-if="isEditMode" class="table-subtitle">Total: {{ totalRecords }} programas registrados</p>
        </div>
        <div class="header-right">
          <span class="count-badge">{{ totalRecords }} Registros Totales</span>
        </div>
      </header>

      <div class="table-toolbar">
        <div class="search-box">
          <span class="search-icon">🔍</span>
          <input 
            v-model="searchQuery"
            type="text" 
            placeholder="Buscar por nombre de maestría..." 
            class="search-input"
          />
        </div>
        <div class="table-info">
          Mostrando {{ paginatedRange.start }}–{{ paginatedRange.end }} de {{ totalRecords }} programas
        </div>
      </div>

      <div class="table-container">
        <table class="degrees-table">
          <thead>
            <tr>
              <th># ID</th>
              <th>Nombre de la Maestría</th>
              <th>Archivo PDF</th>
              <th>Fecha de Registro</th>
              <th class="actions-cell">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="degree in degrees" 
              :key="degree.id"
              :class="{ 'row-editing': selectedDegree?.id === degree.id }"
            >
              <td class="id-cell">{{ formatId(degree.id) }}</td>
              <td>
                <div class="name-container" :title="degree.name">
                  <span class="degree-name">{{ degree.name }}</span>
                  <div v-if="selectedDegree?.id === degree.id" class="editing-label">ACTUALMENTE EDITANDO</div>
                </div>
              </td>
              <td>
                <button class="pdf-link-btn" @click="downloadPdf(degree.id)">
                  <span class="pdf-icon">📄</span> {{ degree.pdfCurriculumPath.substring(degree.pdfCurriculumPath.lastIndexOf('/') + 1) }}
                </button>
              </td>
              <td>{{ formatDate(degree.createdAt) }}</td>
              <td class="actions-cell">
                <button class="action-btn edit" title="Editar" @click="enterEditMode(degree)">
                  ✏️
                </button>
                <button class="action-btn delete" title="Eliminar" @click="confirmDelete(degree)">
                  🗑️
                </button>
              </td>
            </tr>
            <tr v-if="degrees.length === 0 && !isLoading">
              <td colspan="5" class="empty-state">No se encontraron programas.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="table-footer">
        <div class="page-size">
          Filas por página: 
          <input v-model.number="pageSize" type="number" class="size-input" @change="fetchDegrees" />
        </div>
        <div class="pagination">
          <span class="page-info">Página {{ currentPage }} de {{ totalPages }}</span>
          <div class="page-controls">
            <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--; fetchDegrees()">‹</button>
            <button 
              v-for="p in totalPages" 
              :key="p" 
              class="page-btn" 
              :class="{ active: p === currentPage }"
              @click="currentPage = p; fetchDegrees()"
            >
              {{ p }}
            </button>
            <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++; fetchDegrees()">›</button>
          </div>
        </div>
      </footer>
    </section>

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
.manager-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
  position: relative;
}

/* Form Panel */
.form-panel {
  padding: 32px;
}

.panel-header {
  margin-bottom: 24px;
}

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

.panel-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 8px;
}

.panel-subtitle {
  color: var(--color-text-muted);
  font-size: 14px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-weight: 600;
  font-size: 13px;
}

.form-input {
  padding: 12px 16px;
  border-radius: var(--radius-input);
  border: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.5);
  font-family: inherit;
  transition: var(--transition-base);
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-active-surface);
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
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: var(--transition-base);
  background: rgba(255, 255, 255, 0.3);
}

.upload-zone.dragging {
  border-color: var(--color-primary);
  background: var(--color-active-surface);
}

.upload-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.upload-text {
  font-weight: 600;
  margin-bottom: 4px;
}

.upload-subtext {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 12px;
}

.upload-badge {
  background: var(--color-divider);
  padding: 4px 10px;
  border-radius: var(--radius-pill);
  font-size: 11px;
  font-weight: 600;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
}

.file-icon {
  font-size: 24px;
}

.file-name {
  font-weight: 600;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  align-self: flex-end;
  background: none;
  border: 1px solid var(--color-border);
  padding: 8px 16px;
  border-radius: var(--radius-button);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
}

/* Buttons */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: var(--radius-button);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: var(--transition-base);
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: none;
  border: 1px solid var(--color-border);
  padding: 12px 24px;
  border-radius: var(--radius-button);
  font-weight: 600;
  cursor: pointer;
}

/* Table Section */
.table-section {
  padding: 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.table-title {
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}

.count-badge {
  background: var(--color-divider);
  padding: 6px 12px;
  border-radius: var(--radius-pill);
  font-size: 12px;
  font-weight: 600;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-box {
  position: relative;
  width: 320px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-muted);
}

.search-input {
  width: 100%;
  padding: 10px 10px 10px 36px;
  border-radius: var(--radius-input);
  border: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.5);
}

.table-info {
  font-size: 12px;
  color: var(--color-text-muted);
}

.table-container {
  overflow-x: auto;
}

.degrees-table {
  width: 100%;
  border-collapse: collapse;
}

.degrees-table th {
  text-align: left;
  padding: 16px;
  font-size: 12px;
  text-transform: uppercase;
  color: var(--color-text-muted);
  border-bottom: 1px solid var(--color-divider);
}

.degrees-table td {
  padding: 16px;
  border-bottom: 1px solid var(--color-divider);
}

.id-cell {
  font-weight: 700;
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

.row-editing {
  background: var(--color-active-surface);
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

.actions-cell {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background: white;
  cursor: pointer;
  transition: var(--transition-base);
}

.action-btn.edit:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.action-btn.delete:hover {
  border-color: #ff4d4f;
  color: #ff4d4f;
}

/* Pagination */
.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
}

.size-input {
  width: 60px;
  padding: 6px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  margin-left: 8px;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-controls {
  display: flex;
  gap: 4px;
}

.page-btn {
  min-width: 32px;
  height: 32px;
  border: 1px solid var(--color-border);
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: var(--transition-base);
}

.page-btn.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  width: 100%;
  max-width: 420px;
  padding: 32px;
  text-align: center;
}

.warning-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.modal-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 12px;
}

.modal-body {
  color: var(--color-text-muted);
  line-height: 1.5;
  margin-bottom: 24px;
}

.modal-actions {
  display: flex;
  gap: 12px;
}

.btn-ghost {
  flex: 1;
  background: none;
  border: none;
  padding: 12px;
  font-weight: 600;
  cursor: pointer;
}

.btn-danger {
  flex: 2;
  background: #dc2626;
  color: white;
  border: none;
  padding: 12px;
  border-radius: var(--radius-button);
  font-weight: 600;
  cursor: pointer;
}

.btn-danger:hover {
  background: #b91c1c;
}

/* Loading Overlay */
.loading-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border-radius: var(--radius-card);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--color-active-surface);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>