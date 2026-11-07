import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { loginRequest } from '@/api/auth'
import type { UserRole } from '@/api/types'

const TOKEN_KEY = 'token'
const EMAIL_KEY = 'email'
const ID_KEY = 'userId'
const ROLE_KEY = 'role'

function readStoredId(): number | null {
  const raw = localStorage.getItem(ID_KEY)
  if (!raw) return null
  const value = Number(raw)
  return Number.isFinite(value) ? value : null
}

function readStoredRole(): UserRole | null {
  const raw = localStorage.getItem(ROLE_KEY)
  if (raw === 'ADMIN' || raw === 'TEACHER' || raw === 'STUDENT') {
    return raw
  }
  return null
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const email = ref<string | null>(localStorage.getItem(EMAIL_KEY))
  const userId = ref<number | null>(readStoredId())
  const role = ref<UserRole | null>(readStoredRole())

  const isAuthenticated = computed(() => Boolean(token.value))
  const isStaff = computed(() => role.value === 'ADMIN' || role.value === 'TEACHER')
  const isAdmin = computed(() => role.value === 'ADMIN')
  const isStudent = computed(() => role.value === 'STUDENT')

  function setSession(
    nextToken: string | null,
    nextEmail: string | null = null,
    nextId: number | null = null,
    nextRole: UserRole | null = null,
  ) {
    token.value = nextToken
    email.value = nextEmail
    userId.value = nextId
    role.value = nextRole

    if (nextToken) localStorage.setItem(TOKEN_KEY, nextToken)
    else localStorage.removeItem(TOKEN_KEY)

    if (nextEmail) localStorage.setItem(EMAIL_KEY, nextEmail)
    else localStorage.removeItem(EMAIL_KEY)

    if (nextId != null) localStorage.setItem(ID_KEY, String(nextId))
    else localStorage.removeItem(ID_KEY)

    if (nextRole) localStorage.setItem(ROLE_KEY, nextRole)
    else localStorage.removeItem(ROLE_KEY)
  }

  async function login(loginEmail: string, password: string) {
    const response = await loginRequest({ email: loginEmail, password })
    setSession(response.token, response.email, response.id, response.role)
    return response
  }

  function logout() {
    setSession(null, null, null, null)
  }

  function homeRouteName(): 'profile' | 'students' {
    return role.value === 'STUDENT' ? 'profile' : 'students'
  }

  return {
    token,
    email,
    userId,
    role,
    isAuthenticated,
    isStaff,
    isAdmin,
    isStudent,
    login,
    logout,
    homeRouteName,
  }
})
