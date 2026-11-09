<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import ErrorBanner from '@/components/ErrorBanner.vue'
import PaginationControls from '@/components/PaginationControls.vue'
import { getApiErrorMessage } from '@/api/errors'
import { listClasses } from '@/api/classes'
import { createStudent, deleteStudent, listStudents, updateStudent } from '@/api/students'
import type { SchoolClass, Student, StudentUpdateRequest } from '@/api/types'
import { useAuthStore } from '@/stores/auth'
import { hasChanges, pickChanged } from '@/utils/pickChanged'

const auth = useAuthStore()
const students = ref<Student[]>([])
const classes = ref<SchoolClass[]>([])
const error = ref('')
const notice = ref('')
const loading = ref(false)
const editingId = ref<number | null>(null)
const page = ref(0)
const pageSize = 3
const totalPages = ref(0)
const totalElements = ref(0)
const filters = reactive({
  email: '',
  classId: '',
  sort: 'lastName,asc',
})

const createForm = reactive({
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  classId: '',
})

type StudentEditSnapshot = {
  email: string
  firstName: string
  lastName: string
  classId: string
  password: string
}

const editForm = reactive<StudentEditSnapshot>({
  email: '',
  firstName: '',
  lastName: '',
  classId: '',
  password: '',
})

const editSnapshot = ref<StudentEditSnapshot | null>(null)

const classNameById = computed(() => {
  const map = new Map<number, string>()
  for (const item of classes.value) map.set(item.id, item.name)
  return map
})

async function load(requestedPage = page.value) {
  loading.value = true
  error.value = ''
  try {
    const [studentPage, classList] = await Promise.all([
      listStudents({
        page: requestedPage,
        size: pageSize,
        email: filters.email.trim() || undefined,
        classId: filters.classId ? Number(filters.classId) : undefined,
        sort: filters.sort,
      }),
      listClasses(),
    ])
    students.value = studentPage.content
    page.value = studentPage.number
    totalPages.value = studentPage.totalPages
    totalElements.value = studentPage.totalElements
    classes.value = classList
  } catch (err) {
    error.value = getApiErrorMessage(err)
  } finally {
    loading.value = false
  }
}

async function applyFilters() {
  page.value = 0
  await load(0)
}

async function clearFilters() {
  filters.email = ''
  filters.classId = ''
  filters.sort = 'lastName,asc'
  await applyFilters()
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
  const snapshot: StudentEditSnapshot = {
    email: student.email,
    firstName: student.firstName,
    lastName: student.lastName,
    classId: student.classId != null ? String(student.classId) : '',
    password: '',
  }
  editSnapshot.value = { ...snapshot }
  Object.assign(editForm, snapshot)
}

function cancelEdit() {
  editingId.value = null
  editSnapshot.value = null
}

async function onSaveEdit() {
  if (editingId.value == null || editSnapshot.value == null) return
  error.value = ''
  notice.value = ''

  const current: StudentEditSnapshot = {
    email: editForm.email.trim(),
    firstName: editForm.firstName.trim(),
    lastName: editForm.lastName.trim(),
    classId: editForm.classId,
    password: editForm.password,
  }

  const changed = pickChanged(editSnapshot.value, current, [
    'email',
    'firstName',
    'lastName',
    'classId',
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
  if (changed.classId !== undefined) {
    payload.classId = changed.classId ? Number(changed.classId) : null
  }
  if (changed.password !== undefined) payload.password = changed.password

  try {
    await updateStudent(editingId.value, payload)
    notice.value = 'Student updated successfully'
    editingId.value = null
    editSnapshot.value = null
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
    const nextPage = students.value.length === 1 && page.value > 0 ? page.value - 1 : page.value
    await load(nextPage)
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

function onPageChange(nextPage: number) {
  void load(nextPage)
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

      <form class="panel form" @submit.prevent="applyFilters">
        <h2>Filter and sort students</h2>
        <div class="grid">
          <label>
            Email
            <input v-model="filters.email" type="search" placeholder="Search by email" />
          </label>
          <label>
            Class
            <select v-model="filters.classId">
              <option value="">All classes</option>
              <option v-for="item in classes" :key="item.id" :value="String(item.id)">
                {{ item.name }}
              </option>
            </select>
          </label>
          <label>
            Sort
            <select v-model="filters.sort">
              <option value="lastName,asc">Last name, A–Z</option>
              <option value="lastName,desc">Last name, Z–A</option>
              <option value="firstName,asc">First name, A–Z</option>
              <option value="email,asc">Email, A–Z</option>
              <option value="createdAt,desc">Newest first</option>
            </select>
          </label>
        </div>
        <div class="inline-form">
          <button type="submit">Apply filters</button>
          <button type="button" class="ghost" @click="clearFilters">Clear</button>
        </div>
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
            <tr v-if="!loading && students.length === 0">
              <td colspan="4" class="muted">No students found.</td>
            </tr>
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
      <PaginationControls
        :page="page"
        :total-pages="totalPages"
        :total-elements="totalElements"
        @change="onPageChange"
      />
    </section>
  </AppShell>
</template>
