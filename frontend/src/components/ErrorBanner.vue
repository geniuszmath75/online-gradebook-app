<script setup lang="ts">
defineProps<{
  message: string
}>()
</script>

<template>
  <p v-if="message" class="error-banner" role="alert">{{ message }}</p>
</template>

<style scoped>
.error-banner {
  margin: 0;
  color: #8a2f2f;
  background: rgba(138, 47, 47, 0.08);
  border-radius: 0.4rem;
  padding: 0.65rem 0.75rem;
  font-size: 0.92rem;
}
</style>
