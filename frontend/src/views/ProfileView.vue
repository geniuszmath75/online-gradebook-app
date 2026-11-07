<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import ErrorBanner from '@/components/ErrorBanner.vue'
import { getApiErrorMessage } from '@/api/errors'
import { getStudent, updateStudent } from '@/api/students'
import type { Grade, Student, StudentUpdateRequest } from '@/api/types'
import { useAuthStore } from '@/stores/auth'
import { hasChanges, pickChanged } from '@/utils/pickChanged'

const auth = useAuthStore()
const student = ref<Student | null>(null)
const error = ref('')
const notice = ref('')
const loading = ref(false)

type ProfileSnapshot = {
  email: string
  firstName: string
  lastName: string
  password: string
}

const form = reactive<ProfileSnapshot>({
  email: '',
  firstName: '',
  lastName: '',
  password: '',
})

const snapshot = ref<ProfileSnapshot | null>(null)

async function load() {
  if (auth.userId == null) {
    error.value = 'Missing user id in session. Please sign in again.'
    return
  }
  loading.value = true
  error.value = ''
  try {
    student.value = await getStudent(auth.userId)
    const next: ProfileSnapshot = {
      email: student.value.email,
      firstName: student.value.firstName,
      lastName: student.value.lastName,
      password: '',
    }
    snapshot.value = { ...next }
    Object.assign(form, next)
  } catch (err) {
    error.value = getApiErrorMessage(err)
  } finally {
    loading.value = false
  }
}

async function onSave() {
  if (auth.userId == null || snapshot.value == null) return
  error.value = ''
  notice.value = ''

  const current: ProfileSnapshot = {
    email: form.email.trim(),
    firstName: form.firstName.trim(),
    lastName: form.lastName.trim(),
    password: form.password,
  }

  const changed = pickChanged(snapshot.value, current, [
    'email',
    'firstName',
    'lastName',
    'password',
  ])

  if (changed.password === '') {
    delete changed.password
  }

  if (!hasChanges(changed)) {
    notice.value = 'No changes to save'
    return
  }

  const payload: StudentUpdateRequest = {}
  if (changed.email !== undefined) payload.email = changed.email
  if (changed.firstName !== undefined) payload.firstName = changed.firstName
  if (changed.lastName !== undefined) payload.lastName = changed.lastName
  if (changed.password !== undefined) payload.password = changed.password

  try {
    student.value = await updateStudent(auth.userId, payload)
    notice.value = 'Profile updated successfully'
    const next: ProfileSnapshot = {
      email: student.value.email,
      firstName: student.value.firstName,
      lastName: student.value.lastName,
      password: '',
    }
    snapshot.value = { ...next }
    Object.assign(form, next)
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

onMounted(load)
</script>

<template>
  <AppShell>
    <section class="page">
      <header class="page__head">
        <h1>My profile</h1>
        <p>View and update your student account details.</p>
      </header>

      <ErrorBanner :message="error" />
      <p v-if="notice" class="notice">{{ notice }}</p>
      <p v-if="loading" class="muted">Loading…</p>

      <form v-if="student" class="panel form" @submit.prevent="onSave">
        <div class="grid">
          <label>Email <input v-model="form.email" type="email" required /></label>
          <label>First name <input v-model="form.firstName" required /></label>
          <label>Last name <input v-model="form.lastName" required /></label>
          <label>New password <input v-model="form.password" type="password" minlength="6" placeholder="Optional" /></label>
        </div>
        <button type="submit">Save profile</button>
      </form>

      <div v-if="student?.grades?.length" class="panel table-wrap">
        <h2>My grades</h2>
        <table>
          <thead>
            <tr>
              <th>Grade</th>
              <th>Description</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="grade in student.grades as Grade[]" :key="grade.id">
              <td>{{ grade.grade }}</td>
              <td>{{ grade.description || '—' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-else-if="student" class="muted">No grades assigned yet.</p>
    </section>
  </AppShell>
</template>
