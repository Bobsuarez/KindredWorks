<script setup lang="ts">
interface Props {
  show: boolean;
  title: string;
  type?: 'warning' | 'info' | 'danger';
  confirmText?: string;
  cancelText?: string;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'warning',
  confirmText: 'Confirmar',
  cancelText: 'Cancelar'
});

const emit = defineEmits(['confirm', 'cancel']);

const handleConfirm = () => emit('confirm');
const handleCancel = () => emit('cancel');
</script>

<template>
  <Transition name="fade">
    <div v-if="show" class="modal-overlay" @click="handleCancel">
      <div class="modal-content glass-surface" @click.stop>
        <div class="modal-header">
          <div class="modal-icon" :class="type">
            <template v-if="type === 'warning'">⚠️</template>
            <template v-else-if="type === 'danger'">🛑</template>
            <template v-else>ℹ️</template>
          </div>
          <h3 class="modal-title">{{ title }}</h3>
        </div>
        <div class="modal-body">
          <slot />
        </div>
        <div class="modal-actions">
          <button class="btn-ghost" @click="handleCancel">{{ cancelText }}</button>
          <button 
            class="btn-action" 
            :class="type"
            @click="handleConfirm"
          >
            {{ confirmText }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  max-width: 420px;
  padding: 32px;
  text-align: center;
}

.modal-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.modal-icon.warning { color: #faad14; }
.modal-icon.danger { color: #ff4d4f; }
.modal-icon.info { color: var(--color-primary); }

.modal-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 12px;
  color: var(--color-text);
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
  border: 1px solid var(--color-border);
  padding: 12px;
  border-radius: var(--radius-button);
  font-weight: 600;
  cursor: pointer;
  color: var(--color-text);
  transition: var(--transition-base);
}

.btn-ghost:hover {
  background: var(--color-hover-surface);
}

.btn-action {
  flex: 2;
  color: white;
  border: none;
  padding: 12px;
  border-radius: var(--radius-button);
  font-weight: 600;
  cursor: pointer;
  transition: var(--transition-base);
}

.btn-action.warning { background: #faad14; }
.btn-action.warning:hover { background: #d48806; }

.btn-action.danger { background: #dc2626; }
.btn-action.danger:hover { background: #b91c1c; }

.btn-action.info { background: var(--color-primary); }
.btn-action.info:hover { background: var(--color-primary-hover); }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
