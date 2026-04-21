<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import apiClient from '../../services/apiClient';
import { formatDate } from '../../utils/formatters';
import DataTable from '../common/DataTable.vue';
import type { Column } from '../common/DataTable.vue';

interface ImportRecord {
  id: number;
  fileName: string;
  status: string;
  createdAt: string;
  // Añade otros campos relevantes según el backend
}

const fileInput = ref<HTMLInputElement | null>(null);
const selectedFile = ref<File | null>(null);
const isDragging = ref(false);
const isUploading = ref(false);
const uploadStatus = ref<'idle' | 'success' | 'error'>('idle');
const errorMessage = ref('');

const acceptedFormats = '.csv, .xlsx, .json';

// Table state
const imports = ref<ImportRecord[]>([]);
const totalRecords = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchQuery = ref('');
const isTableLoading = ref(false);

const tableColumns: Column[] = [
  { key: 'createdAt', label: 'Fecha de Carga', slot: true },
  { key: 'fileName', label: 'Nombre de Archivo', slot: true },
  { key: 'status', label: 'Estado', slot: true },
  { key: 'id', label: '# ID System', slot: true },
];

const fetchImports = async () => {
  isTableLoading.value = true;
  try {
    const response = await apiClient.get('/v1/imports', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        search: searchQuery.value,
        direction: 'desc',
        sort: 'createdAt'
      }
    });
    imports.value = response.data.content || [];
    totalRecords.value = response.data.totalElements || 0;
  } catch (error) {
    console.error('Error fetching imports:', error);
  } finally {
    isTableLoading.value = false;
  }
};

let searchTimeout: any;
watch(searchQuery, () => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    currentPage.value = 1;
    fetchImports();
  }, 300);
});

onMounted(() => {
  fetchImports();
});

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files[0]) {
    selectedFile.value = target.files[0];
    uploadStatus.value = 'idle';
  }
};

const handleDragEnter = (event: DragEvent) => {
  event.preventDefault();
  isDragging.value = true;
};

const handleDragLeave = (event: DragEvent) => {
  event.preventDefault();
  isDragging.value = false;
};

const handleDragOver = (event: DragEvent) => {
  event.preventDefault();
};

const handleDrop = (event: DragEvent) => {
  event.preventDefault();
  isDragging.value = false;

  if (event.dataTransfer?.files && event.dataTransfer.files[0]) {
    selectedFile.value = event.dataTransfer.files[0];
    uploadStatus.value = 'idle';
  }
};

const triggerFileInput = () => {
  fileInput.value?.click();
};

const removeFile = () => {
  selectedFile.value = null;
  uploadStatus.value = 'idle';
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
};

const uploadFile = async () => {
  if (!selectedFile.value) return;

  isUploading.value = true;
  uploadStatus.value = 'idle';
  errorMessage.value = '';

  const formData = new FormData();
  formData.append('file', selectedFile.value);

  try {
    await apiClient.post('/v1/imports', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    uploadStatus.value = 'success';
    fetchImports(); // Refresh table
    setTimeout(() => {
      removeFile();
    }, 3000);
  } catch (error: any) {
    uploadStatus.value = 'error';
    errorMessage.value = error.response?.data?.message || 'Error al cargar el archivo. Inténtalo de nuevo.';
  } finally {
    isUploading.value = false;
  }
};
</script>

<template>
  <div class="panel-container">
    <div class="page-header">
      <h1 class="page-title">Carga de Datos</h1>
      <p class="page-subtitle">Sube tu archivo para procesarlo en el sistema.</p>
    </div>

    <!-- Upload Form -->
    <div class="upload-section glass-surface glass-panel">
      <div
        :class="['upload-area', { dragging: isDragging }]"
        @dragenter="handleDragEnter"
        @dragleave="handleDragLeave"
        @dragover="handleDragOver"
        @drop="handleDrop"
        @click="triggerFileInput"
      >
        <input
          ref="fileInput"
          type="file"
          :accept="acceptedFormats"
          @change="handleFileSelect"
          class="file-input"
        />

        <div v-if="!selectedFile" class="upload-placeholder">
          <div class="upload-icon">📁</div>
          <p class="upload-text">Arrastra tu archivo aquí</p>
          <p class="upload-subtext">o haz clic para seleccionar</p>
          <p class="accepted-formats">Formatos aceptados: {{ acceptedFormats }}</p>
        </div>

        <div v-else class="file-info">
          <div class="file-details">
            <span class="file-icon">📄</span>
            <div class="file-meta">
              <p class="file-name">{{ selectedFile.name }}</p>
              <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
            </div>
            <button @click.stop="removeFile" class="remove-button">×</button>
          </div>
        </div>
      </div>

      <button
        v-if="selectedFile"
        @click="uploadFile"
        :disabled="isUploading || !selectedFile"
        class="upload-button"
      >
        <span v-if="isUploading">Cargando archivo...</span>
        <span v-else>Cargar Archivo</span>
      </button>

      <div v-if="uploadStatus === 'success'" class="status-message success">
        <span class="status-icon">✓</span>
        <span>Archivo cargado exitosamente.</span>
      </div>

      <div v-if="uploadStatus === 'error'" class="status-message error">
        <span class="status-icon">✕</span>
        <span>{{ errorMessage }}</span>
      </div>
    </div>

    <!-- Imports Table -->
    <div class="table-section-wrapper">
      <DataTable
        title="Historial de Cargas"
        icon="📊"
        :columns="tableColumns"
        :data="imports"
        :totalRecords="totalRecords"
        v-model:currentPage="currentPage"
        v-model:pageSize="pageSize"
        v-model:searchQuery="searchQuery"
        searchPlaceholder="Buscar por nombre de archivo..."
        :isLoading="isTableLoading"
        emptyMessage="No se encontraron cargas previas."
        @fetch="fetchImports"
      >
        <template #cell-id="{ row }">
          <span class="id-cell">#{{ String(row.id).padStart(3, '0') }}</span>
        </template>

        <template #cell-fileName="{ row }">
          <span class="file-name-cell">{{ row.originalName || 'N/A' }}</span>
        </template>

        <template #cell-status="{ row }">
          <span :class="['status-badge', row.status?.toLowerCase()]">{{ row.status || 'Completado' }}</span>
        </template>

        <template #cell-createdAt="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </DataTable>
    </div>

    <div v-if="isUploading" class="loading-overlay">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<style scoped>
/* Estilos específicos de carga de archivos */
.file-name-cell {
  font-weight: 600;
  color: var(--color-primary);
}

.upload-area {
  padding: 48px;
  text-align: center;
  border: 2px dashed rgba(232, 82, 26, 0.3);
  cursor: pointer;
  transition: var(--transition-base);
  position: relative;
}

.upload-area.dragging {
  border-color: var(--color-primary);
  background: rgba(232, 82, 26, 0.05);
}

.file-input {
  display: none;
}

.upload-placeholder {
  pointer-events: none;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text);
  margin: 0 0 8px 0;
}

.upload-subtext {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0 0 16px 0;
}

.accepted-formats {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0;
}

.file-info {
  pointer-events: none;
}

.file-details {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: var(--radius-input);
  pointer-events: auto;
}

.file-icon {
  font-size: 24px;
}

.file-meta {
  flex-grow: 1;
  text-align: left;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  margin: 0 0 4px 0;
}

.file-size {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0;
}

.remove-button {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: 4px 8px;
  transition: var(--transition-base);
  pointer-events: auto;
}

.remove-button:hover {
  color: var(--color-primary);
}

.upload-button {
  margin-top: 24px;
  width: 100%;
  padding: 11px 0;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-button);
  font-size: 14px;
  font-weight: 600;
  font-family: var(--font-family);
  cursor: pointer;
  transition: var(--transition-base);
}

.upload-button:hover:not(:disabled) {
  background: var(--color-primary-hover);
  transform: scale(0.98);
}

.upload-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.status-message {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: var(--radius-input);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.status-message.success {
  background: rgba(52, 211, 153, 0.1);
  color: #059669;
  border: 1px solid rgba(52, 211, 153, 0.3);
}

.status-message.error {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.status-icon {
  font-weight: 700;
  font-size: 16px;
}
</style>
