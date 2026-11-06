<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getApiErrorMessage } from '@/api/errors'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function onSubmit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(email.value.trim(), password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect || '/')
  } catch (err) {
    error.value = getApiErrorMessage(err, 'Invalid email or password')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login">
    <div class="login__panel">
      <p class="brand">Online Gradebook</p>
      <h1>Sign in to continue</h1>
      <p class="lead">Use your teacher or admin account to access the gradebook.</p>

      <form class="form" @submit.prevent="onSubmit">
        <label class="field">
          <span>Email</span>
          <input
            v-model="email"
            type="email"
            name="email"
            autocomplete="username"
            required
            placeholder="admin@gmail.com"
          />
        </label>

        <label class="field">
          <span>Password</span>
          <input
            v-model="password"
            type="password"
            name="password"
            autocomplete="current-password"
            required
            placeholder="••••••••"
          />
        </label>

        <p v-if="error" class="error" role="alert">{{ error }}</p>

        <button class="submit" type="submit" :disabled="loading">
          {{ loading ? 'Signing in…' : 'Sign in' }}
        </button>
      </form>
    </div>
  </main>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 2rem 1.25rem;
  animation: fade-in 480ms ease-out;
}

.login__panel {
  width: min(100%, 26rem);
  display: grid;
  gap: 0.85rem;
}

.brand {
  margin: 0;
  font-family: "Iowan Old Style", "Palatino Linotype", Palatino, Georgia, serif;
  font-size: clamp(2.4rem, 7vw, 3.4rem);
  line-height: 1.05;
  letter-spacing: -0.03em;
  color: #16352b;
  animation: rise 560ms ease-out;
}

h1 {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 600;
  color: #24352e;
}

.lead {
  margin: 0 0 0.5rem;
  color: #4d5c55;
}

.form {
  display: grid;
  gap: 0.9rem;
  margin-top: 0.35rem;
  animation: rise 700ms ease-out;
}

.field {
  display: grid;
  gap: 0.35rem;
}

.field span {
  font-size: 0.9rem;
  color: #31443c;
}

.field input {
  width: 100%;
  border: 1px solid #9bb5a8;
  background: rgba(255, 255, 255, 0.72);
  color: #1f2a25;
  border-radius: 0.45rem;
  padding: 0.7rem 0.8rem;
  font: inherit;
  transition: border-color 160ms ease, box-shadow 160ms ease;
}

.field input:focus {
  outline: none;
  border-color: #2f6b54;
  box-shadow: 0 0 0 3px rgba(47, 107, 84, 0.18);
}

.error {
  margin: 0;
  color: #8a2f2f;
  background: rgba(138, 47, 47, 0.08);
  border-radius: 0.4rem;
  padding: 0.65rem 0.75rem;
  font-size: 0.92rem;
}

.submit {
  border: 0;
  border-radius: 0.45rem;
  padding: 0.8rem 1rem;
  background: #1f5a45;
  color: #f4faf7;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  transition: background 160ms ease, transform 160ms ease;
}

.submit:hover:not(:disabled) {
  background: #174836;
}

.submit:active:not(:disabled) {
  transform: translateY(1px);
}

.submit:disabled {
  opacity: 0.7;
  cursor: wait;
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
