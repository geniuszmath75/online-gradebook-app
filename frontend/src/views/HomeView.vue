<script setup lang="ts">
</script>

<template>
  <main class="home">
    <p class="brand">Online Gradebook</p>
    <h1>Frontend scaffold ready</h1>
    <p class="lead">
      Vue 3 + Vite + TypeScript. Login and CRUD screens come next.
    </p>
  </main>
</template>

<style scoped>
.home {
  min-height: 100vh;
  display: grid;
  align-content: center;
  gap: 0.75rem;
  padding: 2rem;
  max-width: 40rem;
}

.brand {
  margin: 0;
  font-family: "Iowan Old Style", "Palatino Linotype", Palatino, Georgia, serif;
  font-size: clamp(2rem, 5vw, 3rem);
  letter-spacing: -0.02em;
  color: #16352b;
}

h1 {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 600;
  color: #2c3a35;
}

.lead {
  margin: 0;
  color: #4d5c55;
  line-height: 1.5;
}
</style>
