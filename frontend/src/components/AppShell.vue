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
      <RouterLink class="brand" :to="{ name: auth.homeRouteName() }">Online Gradebook</RouterLink>

      <nav v-if="auth.isStaff" class="nav">
        <RouterLink :to="{ name: 'students' }">Students</RouterLink>
        <RouterLink :to="{ name: 'grades' }">Grades</RouterLink>
        <RouterLink :to="{ name: 'tests' }">Tests</RouterLink>
        <RouterLink :to="{ name: 'classes' }">Classes</RouterLink>
        <RouterLink v-if="auth.isAdmin" :to="{ name: 'subjects' }">Subjects</RouterLink>
        <RouterLink v-if="auth.isAdmin" :to="{ name: 'users' }">Users</RouterLink>
      </nav>
      <nav v-else class="nav">
        <RouterLink :to="{ name: 'profile' }">My profile</RouterLink>
      </nav>

      <div class="session">
        <span class="meta">{{ auth.email }} · {{ auth.role }}</span>
        <button type="button" class="logout" @click="onLogout">Sign out</button>
      </div>
    </header>

    <main class="content">
      <slot />
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
  gap: 1.25rem;
  flex-wrap: wrap;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid rgba(22, 53, 43, 0.12);
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(6px);
}

.brand {
  margin: 0;
  font-family: "Iowan Old Style", "Palatino Linotype", Palatino, Georgia, serif;
  font-size: 1.45rem;
  letter-spacing: -0.02em;
  color: #16352b;
  text-decoration: none;
}

.nav {
  display: flex;
  gap: 0.9rem;
  flex: 1;
}

.nav a {
  text-decoration: none;
  color: #31443c;
  font-weight: 600;
  font-size: 0.95rem;
}

.nav a.router-link-active {
  color: #1f5a45;
  text-decoration: underline;
  text-underline-offset: 0.25rem;
}

.session {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  margin-left: auto;
}

.meta {
  color: #3f5249;
  font-size: 0.9rem;
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
  padding: 1.5rem;
  animation: rise 360ms ease-out;
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

@media (max-width: 720px) {
  .session {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
