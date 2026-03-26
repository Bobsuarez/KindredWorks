<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  show: boolean;
  error: any;
  title?: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: 'Error del Sistema'
});

const emit = defineEmits(['close']);

const errorMessage = computed(() => {
  if (!props.error) return '';
  if (typeof props.error === 'string') return props.error;
  
  // Manejo de errores de Axios/Backend
  const apiError = props.error.response?.data;
  if (apiError?.message) return apiError.message;
  if (apiError?.error) return apiError.error;
  
  return props.error.message || 'Ocurrió un error inesperado. Por favor, intente de nuevo.';
});

const errorCode = computed(() => {
  return props.error?.response?.status || props.error?.code || null;
});
</script>

<template>
  <Transition name="fade">
    <div v-if="show" class="modal-overlay" @click="emit('close')">
      <div class="modal-content glass-surface error-border" @click.stop>
        <div class="modal-header">
          <div class="error-icon">❌</div>
          <h3 class="modal-title">{{ title }}</h3>
        </div>
        
        <div class="modal-body">
          <p class="error-text">{{ errorMessage }}</p>
          <div v-if="errorCode" class="error-code">
            Código de error: <span>{{ errorCode }}</span>
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn-error" @click="emit('close')">Entendido</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  width: 100%;
  max-width: 400px;
  padding: 32px;
  text-align: center;
  border: 1px solid rgba(255, 77, 79, 0.3);
}

.error-icon {
  font-size: 54px;
  margin-bottom: 20px;
  filter: drop-shadow(0 0 10px rgba(255, 77, 79, 0.2));
}

.modal-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #cf1322;
}

.modal-body {
  margin-bottom: 28px;
}

.error-text {
  color: var(--color-text);
  line-height: 1.6;
  font-size: 15px;
  margin-bottom: 12px;
}

.error-code {
  font-size: 12px;
  color: var(--color-text-muted);
  background: var(--color-divider);
  padding: 4px 10px;
  border-radius: 6px;
  display: inline-block;
}

.error-code span {
  font-weight: 700;
  font-family: monospace;
}

.btn-error {
  width: 100%;
  background: #cf1322;
  color: white;
  border: none;
  padding: 14px;
  border-radius: var(--radius-button);
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition-base);
}

.btn-error:hover {
  background: #a8071a;
  transform: translateY(-1px);
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
