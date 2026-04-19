<script setup lang="ts">
import { useAuthStore } from '../../stores/authStore';

interface Props {
  currentPath: string;
}

const props = defineProps<Props>();
const authStore = useAuthStore();

const navItems = [
  {
    label: 'Carga de Datos',
    path: '/app/data-upload',
    icon: '📁',
  },
  {
    label: 'Crear Maestrías',
    path: '/app/degrees',
    icon: '🎓',
  },
  {
    label: 'Programación de Eventos',
    path: '/app/events',
    icon: '📅',
  }
];

const isActive = (path: string) => {
  return props.currentPath === path;
};

const handleLogout = () => {
  authStore.logout();
};
</script>

<template>
  <aside class="sidebar glass-surface">
    <div class="sidebar-header">
      <h1 class="logo">Sistema</h1>
    </div>

    <nav class="nav-section">
      <a v-for="item in navItems" :key="item.path" :href="item.path"
        :class="['nav-item', { active: isActive(item.path) }]">
        <span class="nav-icon">{{ item.icon }}</span>
        <span class="nav-label">{{ item.label }}</span>
      </a>
    </nav>

    <div class="sidebar-footer">
      <button @click="handleLogout" class="logout-button">
        Cerrar Sesión
      </button>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  width: var(--sidebar-width);
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 24px 16px;
  z-index: 100;
}

.sidebar-header {
  margin-bottom: 32px;
}

.logo {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary);
  margin: 0;
}

.nav-section {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-radius: 10px;
  text-decoration: none;
  color: var(--color-text-muted);
  transition: var(--transition-base);
  border-left: 3px solid transparent;
  font-weight: 400;
  font-size: 14px;
}

.nav-item:hover {
  background: var(--color-hover-surface);
  transform: translateX(2px);
}

.nav-item.active {
  background: var(--color-active-surface);
  color: var(--color-primary);
  border-left-color: var(--color-primary);
  font-weight: 600;
}

.nav-icon {
  font-size: 18px;
}

.nav-label {
  flex-grow: 1;
}

.sidebar-footer {
  padding-top: 16px;
  border-top: 1px solid var(--color-divider);
}

.logout-button {
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 10px 16px;
  border-radius: 10px;
  transition: var(--transition-base);
  width: 100%;
  text-align: left;
  font-family: var(--font-family);
}

.logout-button:hover {
  color: var(--color-primary);
  background: var(--color-hover-surface);
}
</style>
