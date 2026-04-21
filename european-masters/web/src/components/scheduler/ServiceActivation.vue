<script setup lang="ts">
import { ref } from 'vue';
import apiClient from '../../services/apiClient';

const status = ref<'idle' | 'processing' | 'success' | 'error'>('idle');
const progress = ref(0);
const errorMessage = ref('');
const loadingText = ref('Iniciando...');

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
    <div v-else-if="status === 'success'" class="state-container">
      <h1 class="main-title">¡Activación Exitosa!</h1>
      <p class="activation-subtitle">
        El servicio de gestión académica ha sido inicializado<br />
        correctamente en el cluster de producción.
      </p>
      
      <div class="circle-wrapper">
        <div class="success-ring">
          <div class="success-content">
            <span class="success-title">ACTIVO</span>
            <span class="success-subtitle">EN SERVICIO</span>
          </div>
        </div>
      </div>
      
      <button class="btn-primary mt-8" @click="reset">Volver al Inicio</button>
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
  </div>
</template>

<style scoped>
/* Estilos específicos de este componente que no se pueden globalizar fácilmente o que dependen de animaciones locales */
</style>
