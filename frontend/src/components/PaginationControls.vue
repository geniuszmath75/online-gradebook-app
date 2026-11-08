<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  page: number
  totalPages: number
  totalElements: number
}>()

const emit = defineEmits<{
  change: [page: number]
}>()

const pageNumbers = computed(() => {
  const visibleCount = 5
  const start = Math.max(0, Math.min(props.page - 2, props.totalPages - visibleCount))
  const end = Math.min(props.totalPages, start + visibleCount)
  return Array.from({ length: Math.max(0, end - start) }, (_, index) => start + index)
})

function goTo(page: number) {
  if (page >= 0 && page < props.totalPages && page !== props.page) {
    emit('change', page)
  }
}
</script>

<template>
  <nav v-if="totalPages > 0" class="pagination" aria-label="Pagination">
    <button type="button" class="ghost" :disabled="page === 0" @click="goTo(page - 1)">
      Previous
    </button>
    <button
      v-for="pageNumber in pageNumbers"
      :key="pageNumber"
      type="button"
      class="pagination__page"
      :class="{ 'pagination__page--active': pageNumber === page }"
      :aria-current="pageNumber === page ? 'page' : undefined"
      @click="goTo(pageNumber)"
    >
      {{ pageNumber + 1 }}
    </button>
    <button type="button" class="ghost" :disabled="page >= totalPages - 1" @click="goTo(page + 1)">
      Next
    </button>
    <span class="pagination__summary">
      Page {{ page + 1 }} of {{ totalPages }} · {{ totalElements }} records
    </span>
  </nav>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.4rem;
}

.pagination__page {
  min-width: 2.25rem;
  padding: 0.45rem 0.65rem;
  border: 1px solid #9bb5a8;
  background: rgba(255, 255, 255, 0.72);
  color: #1f5a45;
}

.pagination__page--active {
  background: #174836;
  color: #f4faf7;
}

.pagination__page:hover {
  background: rgba(31, 90, 69, 0.12);
}

.pagination__page--active:hover {
  background: #174836;
}

.pagination__summary {
  color: #66776f;
  font-size: 0.9rem;
  margin-left: 0.35rem;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}
</style>
