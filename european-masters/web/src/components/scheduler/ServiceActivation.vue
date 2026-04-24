<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import apiClient from '../../services/apiClient';
import DataTable, { type Column } from '../common/DataTable.vue';

const status = ref<'idle' | 'processing' | 'success' | 'error'>('idle');
const progress = ref(0);
const errorMessage = ref('');
const loadingText = ref('Iniciando...');

// Programs Table State
const programs = ref<any[]>([]);
const originalPrograms = ref<any[]>([]); // To track changes
const totalRecords = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchQuery = ref('');
const isProgramsLoading = ref(false);
const isSaving = ref(false);
const globalChannels = ref<any[]>([]);

const tableColumns: Column[] = [
  { key: 'name', label: 'Nombre del Programa', slot: true },
  { key: 'contactsCount', label: 'Cantidad de Contactos' },
  { key: 'emailEnabled', label: 'Email', slot: true },
  { key: 'whatsappEnabled', label: 'WhatsApp', slot: true }
];

const fetchPrograms = async () => {
  isProgramsLoading.value = true;
  try {
    const dummyPrograms = [
      { id: 1, name: 'Maestría en Inteligencia Artificial Aplicada', contactsCount: 1250, emailEnabled: true, whatsappEnabled: true },
      { id: 2, name: 'Maestría en Energías Renovables y Sostenibilidad', contactsCount: 840, emailEnabled: false, whatsappEnabled: false },
      { id: 9, name: 'Maestría en Dirección de Proyectos Tecnológicos', contactsCount: 2100, emailEnabled: true, whatsappEnabled: false },
      { id: 10, name: 'Maestría en Ciberseguridad Global', contactsCount: 1560, emailEnabled: true, whatsappEnabled: true }
    ];

    try {
      const response = await apiClient.get('/master-programs', {
        params: {
          page: currentPage.value - 1,
          size: pageSize.value,
          search: searchQuery.value
        }
      });
      // Adapt API response to include channel flags if they don't exist
      const data = response.data.content || response.data;
      programs.value = data.map((p: any) => ({
        ...p,
        emailEnabled: p.emailEnabled ?? false,
        whatsappEnabled: p.whatsappEnabled ?? false
      }));
      originalPrograms.value = JSON.parse(JSON.stringify(programs.value));
      totalRecords.value = response.data.totalElements || programs.value.length;
    } catch (e) {
      console.warn('API /master-programs no disponible, usando dummy data');
      programs.value = JSON.parse(JSON.stringify(dummyPrograms));
      originalPrograms.value = JSON.parse(JSON.stringify(dummyPrograms));
      totalRecords.value = dummyPrograms.length;
    }
  } catch (error) {
    console.error('Error fetching programs:', error);
  } finally {
    isProgramsLoading.value = false;
  }
};

const hasChanges = computed(() => {
  return JSON.stringify(programs.value) !== JSON.stringify(originalPrograms.value);
});

const saveAllChanges = async () => {
  isSaving.value = true;
  try {
    // 1. Identificamos solo lo que cambió para no "contaminar" el JSON
    const payload = programs.value.map((p) => {
      const original = originalPrograms.value.find(op => op.id === p.id);
      const channels: Record<string, boolean> = {};
      
      // Solo agregamos al mapa de channels si el valor es diferente al original
      if (p.emailEnabled !== original?.emailEnabled) {
        channels.email = p.emailEnabled;
      }
      if (p.whatsappEnabled !== original?.whatsappEnabled) {
        channels.whatsapp = p.whatsappEnabled;
      }

      // Si no hay cambios en ningún canal para este programa, retornamos null
      if (Object.keys(channels).length === 0) return null;

      return {
        masterProgramId: p.id,
        channels: channels
      };
    }).filter(p => p !== null) as any[]; // Eliminamos los programas que no tuvieron cambios

    // 2. Si por alguna razón llegamos aquí sin cambios reales (extraño pero posible), salimos
    if (payload.length === 0) {
      alert('No se detectaron cambios para guardar.');
      isSaving.value = false;
      return;
    }

    await apiClient.patch('/v1/degrees/notification-channels', payload);
    
    // Actualizamos el estado original tras el éxito
    originalPrograms.value = JSON.parse(JSON.stringify(programs.value));
    alert('Configuración de canales actualizada correctamente');
  } catch (error: any) {
    console.error('Error al guardar la configuración:', error);
    const msg = error.response?.data?.message || 'Error al conectar con el servidor. Intente de nuevo.';
    alert(`Error: ${msg}`);
  } finally {
    isSaving.value = false;
  }
};

const fetchGlobalChannels = async () => {
  try {
    const response = await apiClient.get('/notification-channels/status');
    globalChannels.value = response.data;
  } catch (error) {
    console.error('Error fetching global channels:', error);
  }
};

// Debounce search
let searchTimeout: any;
watch(searchQuery, () => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    currentPage.value = 1;
    fetchPrograms();
  }, 300);
});

const loadingMessages = [
  'Iniciando proceso...',
  'Sincronizando base de datos académica...',
  'Configurando entorno...',
  'Validando registros...',
  'Finalizando...'
];

const startService = async () => {
  status.value = 'processing';
  progress.value = 0;
  
  // Simular la barra de progreso
  const interval = setInterval(() => {
    if (progress.value < 90) {
      progress.value += Math.floor(Math.random() * 5) + 1;
      const step = Math.floor((progress.value / 100) * loadingMessages.length);
      loadingText.value = loadingMessages[Math.min(step, loadingMessages.length - 1)];
    }
  }, 300);

  try {
    await apiClient.post('/v1/scheduler/start');
    
    // Si la llamada fue exitosa, llevamos al 100%
    clearInterval(interval);
    progress.value = 100;
    loadingText.value = '¡Completado!';
    
    // Fetch programs before showing success
    await fetchPrograms();
    
    // Dar un pequeño tiempo para que se vea el 100%
    setTimeout(() => {
      status.value = 'success';
    }, 600);
  } catch (error: any) {
    clearInterval(interval);
    status.value = 'error';
    errorMessage.value = error.response?.data?.message || 'Error al iniciar el servicio. Verifique la conexión.';
  }
};

const reset = () => {
  status.value = 'idle';
  progress.value = 0;
};

onMounted(() => {
  fetchGlobalChannels();
  fetchPrograms();
});
</script>

<template>
  <div class="activation-container glass-surface">
    <!-- ESTADO: IDLE -->
    <div v-if="status === 'idle'" class="state-container">
      <h3 class="system-label">SISTEMA DE GESTIÓN ACADÉMICA</h3>
      <h1 class="main-title">Activación de Servicio</h1>
      <p class="activation-subtitle">
        Inicie el proceso de sincronización para actualizar la base de datos de<br />
        maestrías y programas académicos globales.
      </p>
      
      <div class="circle-wrapper">
        <button class="circle-btn" @click="startService">
          <span class="btn-text">INICIO</span>
          <span class="btn-indicator"></span>
        </button>
      </div>
      
      <div class="footer-info">
        <div class="info-item">
          <div class="icon-box">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
          </div>
          <div class="info-text">
            <strong>AcademicCloud Admin</strong>
            <span>161.10.181.148</span>
          </div>
        </div>
        <div class="info-item">
          <div class="icon-box">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>
          </div>
          <div class="info-text">
            <strong>US-EAST-MASTER-01</strong>
            <a href="#" class="link">Cambiar servidor</a>
          </div>
        </div>
      </div>
    </div>

    <!-- ESTADO: PROCESANDO -->
    <div v-else-if="status === 'processing'" class="state-container">
      <h3 class="system-label blue">ACTIVACIÓN DE SERVICIO EN CURSO</h3>
      <p class="activation-subtitle">
        Estamos configurando su entorno de gestión de maestrías.<br />
        Este proceso puede tomar unos segundos.
      </p>
      
      <div class="circle-wrapper">
        <div class="progress-ring">
          <svg viewBox="0 0 200 200">
            <circle class="ring-bg" cx="100" cy="100" r="90"></circle>
            <circle class="ring-fill" cx="100" cy="100" r="90" :style="{ strokeDashoffset: 565.48 - (565.48 * progress) / 100 }"></circle>
          </svg>
          <div class="progress-content">
            <span class="percentage">{{ progress }}<small>%</small></span>
            <span class="status-text">PROCESANDO</span>
          </div>
        </div>
      </div>
      
      <div class="loading-bar">
        <div class="spinner-small"></div>
        <span>{{ loadingText }}</span>
      </div>
      
      <div class="processing-icons">
        <div class="p-icon"><span class="icon">💻</span>CPU</div>
        <div class="p-icon active"><span class="icon">🌐</span>RED</div>
        <div class="p-icon"><span class="icon">🗄️</span>STORAGE</div>
      </div>
    </div>

    <!-- ESTADO: ÉXITO -->
    <div v-else-if="status === 'success'" class="state-container success-view">
      <h1 class="main-title">¡Activación Exitosa!</h1>
      <p class="activation-subtitle">
        El servicio de gestión académica ha sido inicializado<br />
        correctamente en el cluster de producción.
      </p>
      
      <div class="table-wrapper">
        <DataTable
          title="Programas Sincronizados"
          icon="📊"
          :columns="tableColumns"
          :data="programs"
          :totalRecords="totalRecords"
          v-model:currentPage="currentPage"
          v-model:pageSize="pageSize"
          v-model:searchQuery="searchQuery"
          :isLoading="isProgramsLoading"
          emptyMessage="No se encontraron programas sincronizados."
          @fetch="fetchPrograms"
        >
          <template #cell-name="{ row }">
            <span class="program-name">{{ row.name }}</span>
          </template>

          <template #cell-emailEnabled="{ row }">
            <div class="switch-container">
              <label class="switch">
                <input 
                  type="checkbox" 
                  v-model="row.emailEnabled"
                >
                <span class="slider round"></span>
              </label>
              <span class="switch-label">{{ row.emailEnabled ? 'Activo' : 'Inactivo' }}</span>
            </div>
          </template>

          <template #cell-whatsappEnabled="{ row }">
            <div class="switch-container">
              <label class="switch">
                <input 
                  type="checkbox" 
                  v-model="row.whatsappEnabled"
                >
                <span class="slider round"></span>
              </label>
              <span class="switch-label">{{ row.whatsappEnabled ? 'Activo' : 'Inactivo' }}</span>
            </div>
          </template>
        </DataTable>

        <div class="save-actions" v-if="hasChanges">
          <p class="changes-hint">Hay cambios sin guardar en los canales de notificación.</p>
          <button 
            class="btn-primary" 
            :disabled="isSaving" 
            @click="saveAllChanges"
          >
            {{ isSaving ? 'Guardando...' : 'Guardar Cambios' }}
          </button>
        </div>
      </div>
      
      <button class="btn-ghost mt-8" @click="reset">Volver al Inicio</button>
    </div>

    <!-- ESTADO: ERROR -->
    <div v-else-if="status === 'error'" class="state-container">
      <h1 class="main-title text-red">¡Error en Activación!</h1>
      <p class="activation-subtitle text-red-light">{{ errorMessage }}</p>
      
      <div class="circle-wrapper">
        <div class="error-ring">
          <div class="error-content">
            <span class="error-title">ERROR</span>
            <span class="error-subtitle">SISTEMA DETENIDO</span>
          </div>
        </div>
      </div>
      
      <button class="btn-primary mt-8" @click="reset">Reintentar</button>
    </div>

    <!-- TABLA SIEMPRE VISIBLE SI NO HAY ÉXITO (PARA CONFIGURACIÓN PREVIA) -->
    <div v-if="status !== 'success' && status !== 'processing'" class="table-wrapper always-visible">
      <DataTable
        title="Configuración de Canales"
        icon="⚙️"
        :columns="tableColumns"
        :data="programs"
        :totalRecords="totalRecords"
        v-model:currentPage="currentPage"
        v-model:pageSize="pageSize"
        v-model:searchQuery="searchQuery"
        :isLoading="isProgramsLoading"
        emptyMessage="No se encontraron programas."
        @fetch="fetchPrograms"
      >
        <template #cell-name="{ row }">
          <span class="program-name">{{ row.name }}</span>
        </template>

        <template #cell-emailEnabled="{ row }">
          <div class="switch-container">
            <label class="switch">
              <input 
                type="checkbox" 
                v-model="row.emailEnabled"
              >
              <span class="slider round"></span>
            </label>
          </div>
        </template>

        <template #cell-whatsappEnabled="{ row }">
          <div class="switch-container">
            <label class="switch">
              <input 
                type="checkbox" 
                v-model="row.whatsappEnabled"
              >
              <span class="slider round"></span>
            </label>
          </div>
        </template>
      </DataTable>

      <div class="save-actions" v-if="hasChanges">
        <button 
          class="btn-primary" 
          :disabled="isSaving" 
          @click="saveAllChanges"
        >
          {{ isSaving ? 'Guardando...' : 'Guardar Cambios' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.success-view {
  width: 100%;
  max-width: 1000px;
}

.table-wrapper {
  width: 100%;
  margin: 32px 0;
  text-align: left;
}

.program-name {
  font-weight: 600;
  color: var(--color-text);
}

/* Switch Styles */
.switch-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #e2e8f0;
  transition: .4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

input:checked + .slider {
  background-color: var(--color-primary);
}

input:focus + .slider {
  box-shadow: 0 0 1px var(--color-primary);
}

input:checked + .slider:before {
  transform: translateX(20px);
}

.slider.round {
  border-radius: 34px;
}

.slider.round:before {
  border-radius: 50%;
}

.switch-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-muted);
  min-width: 60px;
}

.mt-8 {
  margin-top: 32px;
}

.save-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
  padding: 20px;
  background: rgba(var(--color-primary-rgb), 0.05);
  border-radius: var(--radius-card);
  border: 1px dashed var(--color-primary);
}

.changes-hint {
  font-size: 14px;
  color: var(--color-primary);
  font-weight: 600;
  margin: 0;
}

.always-visible {
  margin-top: 64px;
  border-top: 1px solid var(--color-divider);
  padding-top: 48px;
}

.btn-ghost {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text-muted);
  padding: 10px 24px;
  border-radius: var(--radius-pill);
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-ghost:hover {
  background: var(--color-active-surface);
  border-color: var(--color-text-muted);
}

/* Animaciones y transiciones */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
