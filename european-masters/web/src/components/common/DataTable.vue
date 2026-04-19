<script setup lang="ts">
import { computed } from 'vue';

export interface Column {
  key: string;
  label: string;
  slot?: boolean;
}

const props = defineProps<{
  title: string;
  icon?: string;
  subtitle?: string;
  columns: Column[];
  data: any[];
  totalRecords: number;
  currentPage: number;
  pageSize: number;
  searchQuery: string;
  searchPlaceholder?: string;
  isLoading?: boolean;
  emptyMessage?: string;
}>();

const emit = defineEmits<{
  (e: 'update:searchQuery', value: string): void;
  (e: 'update:pageSize', value: number): void;
  (e: 'update:currentPage', value: number): void;
  (e: 'fetch'): void;
}>();

const totalPages = computed(() => Math.max(1, Math.ceil(props.totalRecords / props.pageSize)));

const paginatedRange = computed(() => {
  const start = (props.currentPage - 1) * props.pageSize + 1;
  const end = Math.min(props.currentPage * props.pageSize, props.totalRecords);
  return { start: props.totalRecords === 0 ? 0 : start, end };
});

const onSearchInput = (e: Event) => {
  const target = e.target as HTMLInputElement;
  emit('update:searchQuery', target.value);
};

const onPageSizeChange = (e: Event) => {
  const target = e.target as HTMLInputElement;
  emit('update:pageSize', Number(target.value));
  emit('fetch');
};

const goToPage = (p: number) => {
  if (p >= 1 && p <= totalPages.value) {
    emit('update:currentPage', p);
    emit('fetch');
  }
};
</script>

<template>
  <section class="glass-surface table-section">
    <header class="table-header">
      <div class="header-left">
        <h3 class="table-title">
          <span v-if="icon" class="header-icon">{{ icon }}</span> {{ title }}
        </h3>
        <p v-if="subtitle" class="table-subtitle">{{ subtitle }}</p>
      </div>
      <div class="header-right">
        <span class="count-badge">{{ totalRecords }} Registros Totales</span>
      </div>
    </header>

    <div class="table-toolbar">
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input 
          :value="searchQuery"
          @input="onSearchInput"
          type="text" 
          :placeholder="searchPlaceholder || 'Buscar...'" 
          class="search-input"
        />
      </div>
      <div class="table-info">
        Mostrando {{ paginatedRange.start }}–{{ paginatedRange.end }} de {{ totalRecords }} registros
      </div>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th v-for="col in columns" :key="col.key">{{ col.label }}</th>
          </tr>
        </thead>
        <tbody>
          <tr 
            v-for="(row, index) in data" 
            :key="row.id || index"
            :class="{ 'row-editing': row._isEditing }"
          >
            <td v-for="col in columns" :key="col.key" :class="col.key + '-cell'">
              <slot v-if="col.slot" :name="`cell-${col.key}`" :row="row"></slot>
              <template v-else>{{ row[col.key] }}</template>
            </td>
          </tr>
          <tr v-if="data.length === 0 && !isLoading">
            <td :colspan="columns.length" class="empty-state">
              {{ emptyMessage || 'No se encontraron registros.' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <footer class="table-footer">
      <div class="page-size">
        Filas por página: 
        <input 
          :value="pageSize" 
          @change="onPageSizeChange" 
          type="number" 
          class="size-input" 
          min="1"
        />
      </div>
      <div class="pagination">
        <span class="page-info">Página {{ currentPage }} de {{ totalPages }}</span>
        <div class="page-controls">
          <button class="page-btn" :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">‹</button>
          <button 
            v-for="p in totalPages" 
            :key="p" 
            class="page-btn" 
            :class="{ active: p === currentPage }"
            @click="goToPage(p)"
          >
            {{ p }}
          </button>
          <button class="page-btn" :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">›</button>
        </div>
      </div>
    </footer>
  </section>
</template>

<style scoped>
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
  margin: 0 0 8px 0;
}

.table-subtitle {
  color: var(--color-text-muted);
  font-size: 14px;
  margin: 0;
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
  font-family: inherit;
}

.search-input:focus {
  outline: none;
  border-color: var(--color-primary);
  background: white;
}

.table-info {
  font-size: 12px;
  color: var(--color-text-muted);
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 16px;
  font-size: 12px;
  text-transform: uppercase;
  color: var(--color-text-muted);
  border-bottom: 1px solid var(--color-divider);
}

.data-table td {
  padding: 16px;
  border-bottom: 1px solid var(--color-divider);
}

.row-editing {
  background: var(--color-active-surface);
}

.empty-state {
  text-align: center;
  padding: 48px !important;
  color: var(--color-text-muted) !important;
  font-style: italic;
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

.page-info {
  font-size: 13px;
  color: var(--color-text-muted);
}

.page-controls {
  display: flex;
  gap: 4px;
}

.page-btn {
  min-width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  background: var(--color-surface);
}
</style>
