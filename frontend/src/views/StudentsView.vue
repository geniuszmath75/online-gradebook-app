<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import ErrorBanner from '@/components/ErrorBanner.vue'
import { getApiErrorMessage } from '@/api/errors'
import { listClasses } from '@/api/classes'
import { createStudent, deleteStudent, listStudents, updateStudent } from '@/api/students'
import type { SchoolClass, Student, StudentUpdateRequest } from '@/api/types'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const students = ref<Student[]>([])
const classes = ref<SchoolClass[]>([])
const error = ref('')
const notice = ref('')
const loading = ref(false)
const editingId = ref<number | null>(null)

const createForm = reactive({
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  classId: '',
})

const editForm = reactive({
  email: '',
  firstName: '',
  lastName: '',
  classId: '',
  password: '',
})

const classNameById = computed(() => {
  const map = new Map<number, string>()
  for (const item of classes.value) map.set(item.id, item.name)
  return map
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [page, classList] = await Promise.all([
      listStudents({ size: 100, sort: 'lastName,asc' }),
      listClasses(),
    ])
    students.value = page.content
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
    await createStudent({
      email: createForm.email.trim(),
      password: createForm.password,
      firstName: createForm.firstName.trim(),
      lastName: createForm.lastName.trim(),
      classId: createForm.classId ? Number(createForm.classId) : null,
    })
    createForm.email = ''
    createForm.password = ''
    createForm.firstName = ''
    createForm.lastName = ''
    createForm.classId = ''
    notice.value = 'Student created successfully'
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

function startEdit(student: Student) {
  editingId.value = student.id
  editForm.email = student.email
  editForm.firstName = student.firstName
  editForm.lastName = student.lastName
  editForm.classId = student.classId != null ? String(student.classId) : ''
  editForm.password = ''
}

function cancelEdit() {
  editingId.value = null
}

async function onSaveEdit() {
  if (editingId.value == null) return
  error.value = ''
  notice.value = ''
  try {
    const payload: StudentUpdateRequest = {
      email: editForm.email.trim(),
      firstName: editForm.firstName.trim(),
      lastName: editForm.lastName.trim(),
    }
    if (editForm.classId) payload.classId = Number(editForm.classId)
    if (editForm.password.trim()) payload.password = editForm.password
    await updateStudent(editingId.value, payload)
    notice.value = 'Student updated successfully'
    editingId.value = null
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

async function onDelete(id: number) {
  if (!confirm('Delete this student?')) return
  error.value = ''
  notice.value = ''
  try {
    const result = await deleteStudent(id)
    notice.value = result.message
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
        <h1>Students</h1>
        <p>List, register, update, and remove students.</p>
      </header>

      <ErrorBanner :message="error" />
      <p v-if="notice" class="notice">{{ notice }}</p>
      <p v-if="loading" class="muted">Loading…</p>

      <form class="panel form" @submit.prevent="onCreate">
        <h2>Add student</h2>
        <div class="grid">
          <label>Email <input v-model="createForm.email" type="email" required /></label>
          <label>Password <input v-model="createForm.password" type="password" minlength="6" required /></label>
          <label>First name <input v-model="createForm.firstName" required /></label>
          <label>Last name <input v-model="createForm.lastName" required /></label>
          <label>
            Class
            <select v-model="createForm.classId">
              <option value="">No class</option>
              <option v-for="item in classes" :key="item.id" :value="String(item.id)">{{ item.name }}</option>
            </select>
          </label>
        </div>
        <button type="submit">Create student</button>
      </form>

      <div class="panel table-wrap">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Class</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <template v-for="student in students" :key="student.id">
              <tr v-if="editingId === student.id">
                <td colspan="4">
                  <form class="inline-form" @submit.prevent="onSaveEdit">
                    <input v-model="editForm.firstName" required placeholder="First name" />
                    <input v-model="editForm.lastName" required placeholder="Last name" />
                    <input v-model="editForm.email" type="email" required placeholder="Email" />
                    <select v-model="editForm.classId">
                      <option value="">No class</option>
                      <option v-for="item in classes" :key="item.id" :value="String(item.id)">{{ item.name }}</option>
                    </select>
                    <input v-model="editForm.password" type="password" minlength="6" placeholder="New password (optional)" />
                    <button type="submit">Save</button>
                    <button type="button" class="ghost" @click="cancelEdit">Cancel</button>
                  </form>
                </td>
              </tr>
              <tr v-else>
                <td>{{ student.firstName }} {{ student.lastName }}</td>
                <td>{{ student.email }}</td>
                <td>{{ student.classId != null ? classNameById.get(student.classId) || student.classId : '—' }}</td>
                <td class="actions">
                  <button type="button" class="ghost" @click="startEdit(student)">Edit</button>
                  <button
                    v-if="auth.isAdmin"
                    type="button"
                    class="danger"
                    @click="onDelete(student.id)"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </section>
  </AppShell>
</template>
