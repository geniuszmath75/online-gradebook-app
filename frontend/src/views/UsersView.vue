<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import ErrorBanner from '@/components/ErrorBanner.vue'
import { getApiErrorMessage } from '@/api/errors'
import { listClasses } from '@/api/classes'
import { createUser, listUsers } from '@/api/users'
import type { SchoolClass, User, UserCreateRequest } from '@/api/types'

const users = ref<User[]>([])
const classes = ref<SchoolClass[]>([])
const error = ref('')
const notice = ref('')
const loading = ref(false)

const form = reactive<UserCreateRequest>({
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  role: 'TEACHER',
  classId: null,
})

const classLabel = (classId: number | null | undefined) => {
  if (classId == null) return '—'
  return classes.value.find((item) => item.id === classId)?.name || String(classId)
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [userList, classList] = await Promise.all([listUsers(), listClasses()])
    users.value = userList
    classes.value = classList
  } catch (err) {
    error.value = getApiErrorMessage(err)
  } finally {
    loading.value = false
  }
}

async function onCreate() {
  error.value = ''
  notice.value = ''
  try {
    await createUser({
      ...form,
      email: form.email.trim(),
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      classId: form.classId || null,
    })
    form.email = ''
    form.password = ''
    form.firstName = ''
    form.lastName = ''
    form.role = 'TEACHER'
    form.classId = null
    notice.value = 'User created successfully'
    await load()
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
        <h1>Users</h1>
        <p>Register teachers and demonstrate the single-ADMIN rule.</p>
      </header>

      <ErrorBanner :message="error" />
      <p v-if="notice" class="notice">{{ notice }}</p>
      <p v-if="loading" class="muted">Loading…</p>

      <form class="panel form" @submit.prevent="onCreate">
        <h2>Register user</h2>
        <div class="grid">
          <label>Email <input v-model="form.email" type="email" required /></label>
          <label>Password <input v-model="form.password" type="password" minlength="6" required /></label>
          <label>First name <input v-model="form.firstName" required /></label>
          <label>Last name <input v-model="form.lastName" required /></label>
          <label>
            Role
            <select v-model="form.role" required>
              <option value="TEACHER">Teacher</option>
              <option value="ADMIN">Admin</option>
            </select>
          </label>
          <label>
            Class
            <select v-model="form.classId">
              <option :value="null">No class</option>
              <option v-for="item in classes" :key="item.id" :value="item.id">{{ item.name }}</option>
            </select>
          </label>
        </div>
        <button type="submit">Register user</button>
      </form>

      <div class="panel table-wrap">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Class</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && users.length === 0">
              <td colspan="4" class="muted">No users found.</td>
            </tr>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.firstName }} {{ user.lastName }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.role }}</td>
              <td>{{ classLabel(user.classId) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </AppShell>
</template>
