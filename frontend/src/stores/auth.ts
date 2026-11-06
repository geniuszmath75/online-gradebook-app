import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))

  const isAuthenticated = computed(() => Boolean(token.value))

  function setToken(value: string | null) {
    token.value = value
    if (value) {
      localStorage.setItem('token', value)
    } else {
      localStorage.removeItem('token')
    }
  }

  function logout() {
    setToken(null)
  }

  return {
    token,
    isAuthenticated,
    setToken,
    logout,
  }
})
