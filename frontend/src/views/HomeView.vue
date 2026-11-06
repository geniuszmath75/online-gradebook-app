<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

async function onLogout() {
  auth.logout()
  await router.push({ name: 'login' })
}
</script>

<template>
  <div class="shell">
    <header class="top">
      <p class="brand">Online Gradebook</p>
      <div class="session">
        <span class="email">{{ auth.email }}</span>
        <button type="button" class="logout" @click="onLogout">Sign out</button>
      </div>
    </header>

    <main class="content">
      <h1>Welcome back</h1>
      <p class="lead">
        You are signed in. Student, grade, and class screens will land here next.
      </p>
    </main>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: grid;
  grid-template-rows: auto 1fr;
}

.top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1.1rem 1.5rem;
  border-bottom: 1px solid rgba(22, 53, 43, 0.12);
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(6px);
}

.brand {
  margin: 0;
  font-family: "Iowan Old Style", "Palatino Linotype", Palatino, Georgia, serif;
  font-size: 1.55rem;
  letter-spacing: -0.02em;
  color: #16352b;
}

.session {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.email {
  color: #3f5249;
  font-size: 0.95rem;
}

.logout {
  border: 1px solid #7f9d8e;
  background: transparent;
  color: #1f5a45;
  border-radius: 0.4rem;
  padding: 0.4rem 0.75rem;
  font: inherit;
  cursor: pointer;
}

.logout:hover {
  background: rgba(31, 90, 69, 0.08);
}

.content {
  padding: 2rem 1.5rem;
  max-width: 42rem;
  animation: rise 420ms ease-out;
}

h1 {
  margin: 0 0 0.55rem;
  font-size: 1.5rem;
  color: #24352e;
}

.lead {
  margin: 0;
  color: #4d5c55;
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
