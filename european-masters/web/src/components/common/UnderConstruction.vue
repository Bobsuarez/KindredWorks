<script setup lang="ts">
</script>

<template>
  <div class="under-construction-container">
    <div class="under-construction-card glass-surface">
      <div class="icon">🚧</div>
      <h2 class="title">Módulo en Construcción</h2>
      <p class="subtitle">Esta sección estará disponible próximamente.</p>
    </div>
  </div>
</template>

<style scoped>
.under-construction-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}

.under-construction-card {
  padding: 48px;
  text-align: center;
  max-width: 500px;
}

.icon {
  font-size: 64px;
  margin-bottom: 24px;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 12px 0;
}

.subtitle {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0;
}
</style>
