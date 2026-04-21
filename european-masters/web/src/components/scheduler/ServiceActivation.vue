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
      <p class="subtitle">
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
      <p class="subtitle">
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
      <p class="subtitle">
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
      <p class="subtitle text-red-light">{{ errorMessage }}</p>
      
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
.activation-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 64px 32px;
  text-align: center;
  min-height: 600px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: var(--color-surface, rgba(255, 255, 255, 0.5));
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  color: var(--color-text);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.system-label {
  font-size: 13px;
  letter-spacing: 3px;
  text-transform: uppercase;
  color: var(--color-primary);
  font-weight: 700;
  margin-bottom: 16px;
}

.system-label.blue {
  color: var(--color-primary);
}

.main-title {
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
  color: var(--color-text);
}

.subtitle {
  font-size: 16px;
  color: var(--color-text-muted);
  line-height: 1.6;
  margin-bottom: 48px;
}

.circle-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 24px 0 48px 0;
}

/* IDLE STATE */
.circle-btn {
  width: 260px;
  height: 260px;
  border-radius: 50%;
  background: transparent;
  border: 2px solid var(--color-border);
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--color-text);
}

.circle-btn::before {
  content: '';
  position: absolute;
  inset: -20px;
  border: 1px solid var(--color-border);
  border-radius: 50%;
  transition: all 0.3s ease;
  opacity: 0.5;
}

.circle-btn:hover {
  border-color: var(--color-primary);
  box-shadow: 0 0 50px rgba(232, 82, 26, 0.15); /* Using orange primary color hint */
}

.circle-btn:hover::before {
  border-color: var(--color-primary);
  opacity: 0.3;
}

.btn-text {
  font-size: 36px;
  letter-spacing: 6px;
  font-weight: 300;
  margin-left: 6px;
}

.btn-indicator {
  width: 36px;
  height: 3px;
  background: var(--color-primary);
  margin-top: 16px;
  border-radius: 2px;
}

.footer-info {
  display: flex;
  justify-content: center;
  gap: 64px;
  margin-top: 32px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  text-align: left;
}

.icon-box {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--color-active-surface);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
  border: 1px solid var(--color-border);
}

.info-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-text strong {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.info-text span, .info-text a {
  font-size: 13px;
  color: var(--color-text-muted);
}

.info-text a {
  color: var(--color-primary);
  text-decoration: none;
  transition: color 0.2s;
}

.info-text a:hover {
  color: var(--color-primary-hover);
}

/* PROCESSING STATE */
.progress-ring {
  position: relative;
  width: 260px;
  height: 260px;
}

.progress-ring svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: var(--color-border);
  stroke-width: 6;
}

.ring-fill {
  fill: none;
  stroke: var(--color-primary);
  stroke-width: 6;
  stroke-dasharray: 565.48;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.3s linear;
}

.progress-content {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.percentage {
  font-size: 56px;
  font-weight: 800;
  line-height: 1;
  color: var(--color-text);
}

.percentage small {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-muted);
}

.status-text {
  font-size: 12px;
  letter-spacing: 3px;
  color: var(--color-primary);
  margin-top: 12px;
  font-weight: 700;
}

.loading-bar {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  background: var(--color-active-surface);
  padding: 14px 28px;
  border-radius: 30px;
  margin: 0 auto 32px;
  font-size: 14px;
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.spinner-small {
  width: 18px;
  height: 18px;
  border: 2px solid transparent;
  border-top-color: var(--color-primary);
  border-left-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.processing-icons {
  display: flex;
  justify-content: center;
  gap: 48px;
}

.p-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  font-size: 11px;
  color: var(--color-text-muted);
  letter-spacing: 1px;
  font-weight: 600;
}

.p-icon .icon {
  font-size: 24px;
  opacity: 0.4;
  filter: grayscale(1);
}

.p-icon.active {
  color: var(--color-primary);
}

.p-icon.active .icon {
  opacity: 1;
  filter: none;
}

/* SUCCESS STATE */
.success-ring {
  width: 260px;
  height: 260px;
  border-radius: 50%;
  border: 4px solid #10b981;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 0 80px rgba(16, 185, 129, 0.15);
  background: radial-gradient(circle, rgba(16, 185, 129, 0.05) 0%, transparent 70%);
}

.success-ring::before {
  content: '';
  position: absolute;
  inset: -20px;
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 50%;
}

.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.success-title {
  font-size: 40px;
  font-weight: 900;
  letter-spacing: 1px;
  color: var(--color-text);
}

.success-subtitle {
  font-size: 13px;
  letter-spacing: 3px;
  color: #10b981;
  margin-top: 8px;
  font-weight: 700;
}

/* ERROR STATE */
.error-ring {
  width: 260px;
  height: 260px;
  border-radius: 50%;
  border: 4px solid #ef4444;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 0 80px rgba(239, 68, 68, 0.15);
}

.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cross-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: 3px solid #ef4444;
  color: #ef4444;
  font-size: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  font-weight: bold;
}

.error-title {
  font-size: 40px;
  font-weight: 900;
  letter-spacing: 1px;
  color: var(--color-text);
}

.error-subtitle {
  font-size: 13px;
  letter-spacing: 3px;
  color: #ef4444;
  margin-top: 8px;
  font-weight: 700;
}

.text-red {
  color: #ef4444 !important;
}

.text-red-light {
  color: #f87171 !important;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
  border: none;
  padding: 14px 36px;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  margin: 0 auto;
  display: inline-block;
  transition: all 0.2s ease;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.btn-primary:hover {
  background: var(--color-primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(232, 82, 26, 0.2);
}

.mt-8 {
  margin-top: 32px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
