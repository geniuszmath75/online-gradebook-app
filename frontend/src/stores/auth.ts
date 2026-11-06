import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { loginRequest } from '@/api/auth'

const TOKEN_KEY = 'token'
const EMAIL_KEY = 'email'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const email = ref<string | null>(localStorage.getItem(EMAIL_KEY))

  const isAuthenticated = computed(() => Boolean(token.value))

  function setSession(nextToken: string | null, nextEmail: string | null = null) {
    token.value = nextToken
    email.value = nextEmail

    if (nextToken) {
      localStorage.setItem(TOKEN_KEY, nextToken)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }

    if (nextEmail) {
      localStorage.setItem(EMAIL_KEY, nextEmail)
    } else {
      localStorage.removeItem(EMAIL_KEY)
    }
  }

  async function login(loginEmail: string, password: string) {
    const response = await loginRequest({ email: loginEmail, password })
    setSession(response.token, response.email)
    return response
  }

  function logout() {
    setSession(null, null)
  }

  return {
    token,
    email,
    isAuthenticated,
    login,
    logout,
  }
})
