<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import ErrorBanner from '@/components/ErrorBanner.vue'
import PaginationControls from '@/components/PaginationControls.vue'
import { getApiErrorMessage } from '@/api/errors'
import { createGrade, deleteGrade, listGrades, updateGrade } from '@/api/grades'
import { listStudents } from '@/api/students'
import { listTests } from '@/api/tests'
import type { Grade, GradeUpdateRequest, KnowledgeTest, Student } from '@/api/types'
import { hasChanges, pickChanged } from '@/utils/pickChanged'

const grades = ref<Grade[]>([])
const students = ref<Student[]>([])
const tests = ref<KnowledgeTest[]>([])
const error = ref('')
const notice = ref('')
const loading = ref(false)
const editingId = ref<number | null>(null)
const page = ref(0)
const pageSize = 5
const totalPages = ref(0)
const totalElements = ref(0)

const createForm = reactive({
  grade: '4.0',
  description: '',
  studentId: '',
  testId: '',
})

type GradeEditSnapshot = {
  grade: string
  description: string
  studentId: string
  testId: string
}

const editForm = reactive<GradeEditSnapshot>({
  grade: '',
  description: '',
  studentId: '',
  testId: '',
})

const editSnapshot = ref<GradeEditSnapshot | null>(null)

const studentLabel = computed(() => {
  const map = new Map<number, string>()
  for (const s of students.value) map.set(s.id, `${s.firstName} ${s.lastName}`)
  return map
})

const testLabel = computed(() => {
  const map = new Map<number, string>()
  for (const t of tests.value) map.set(t.id, t.name)
  return map
})

async function load(requestedPage = page.value) {
  loading.value = true
  error.value = ''
  try {
    const [gradePage, studentPage, testList] = await Promise.all([
      listGrades({ page: requestedPage, size: pageSize }),
      listStudents({ size: 100 }),
      listTests(),
    ])
    grades.value = gradePage.content
    page.value = gradePage.number
    totalPages.value = gradePage.totalPages
    totalElements.value = gradePage.totalElements
    students.value = studentPage.content
    tests.value = testList
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
    await createGrade({
      grade: Number(createForm.grade),
      description: createForm.description.trim() || undefined,
      studentId: Number(createForm.studentId),
      testId: Number(createForm.testId),
    })
    createForm.grade = '4.0'
    createForm.description = ''
    createForm.studentId = ''
    createForm.testId = ''
    notice.value = 'Grade created successfully'
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

function startEdit(grade: Grade) {
  editingId.value = grade.id
  const snapshot: GradeEditSnapshot = {
    grade: String(grade.grade),
    description: grade.description || '',
    studentId: grade.studentId != null ? String(grade.studentId) : '',
    testId: grade.testId != null ? String(grade.testId) : '',
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

  const current: GradeEditSnapshot = {
    grade: editForm.grade,
    description: editForm.description.trim(),
    studentId: editForm.studentId,
    testId: editForm.testId,
  }

  const changed = pickChanged(editSnapshot.value, current, [
    'grade',
    'description',
    'studentId',
    'testId',
  ])

  if (!hasChanges(changed)) {
    notice.value = 'No changes to save'
    return
  }

  const payload: GradeUpdateRequest = {}
  if (changed.grade !== undefined) payload.grade = Number(changed.grade)
  if (changed.description !== undefined) payload.description = changed.description
  if (changed.studentId !== undefined) payload.studentId = Number(changed.studentId)
  if (changed.testId !== undefined) payload.testId = Number(changed.testId)

  try {
    await updateGrade(editingId.value, payload)
    notice.value = 'Grade updated successfully'
    editingId.value = null
    editSnapshot.value = null
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

async function onDelete(id: number) {
  if (!confirm('Delete this grade?')) return
  error.value = ''
  notice.value = ''
  try {
    const result = await deleteGrade(id)
    notice.value = result.message
    const nextPage = grades.value.length === 1 && page.value > 0 ? page.value - 1 : page.value
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
        <h1>Grades</h1>
        <p>Assign and manage grades for knowledge tests.</p>
      </header>

      <ErrorBanner :message="error" />
      <p v-if="notice" class="notice">{{ notice }}</p>
      <p v-if="loading" class="muted">Loading…</p>

      <form class="panel form" @submit.prevent="onCreate">
        <h2>Add grade</h2>
        <div class="grid">
          <label>
            Student
            <select v-model="createForm.studentId" required>
              <option disabled value="">Select student</option>
              <option v-for="s in students" :key="s.id" :value="String(s.id)">
                {{ s.firstName }} {{ s.lastName }}
              </option>
            </select>
          </label>
          <label>
            Test
            <select v-model="createForm.testId" required>
              <option disabled value="">Select test</option>
              <option v-for="t in tests" :key="t.id" :value="String(t.id)">{{ t.name }}</option>
            </select>
          </label>
          <label>Grade <input v-model="createForm.grade" type="number" min="1" max="6" step="0.5" required /></label>
          <label>Description <input v-model="createForm.description" /></label>
        </div>
        <button type="submit">Create grade</button>
      </form>

      <div class="panel table-wrap">
        <table>
          <thead>
            <tr>
              <th>Student</th>
              <th>Test</th>
              <th>Grade</th>
              <th>Description</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && grades.length === 0">
              <td colspan="5" class="muted">No grades found.</td>
            </tr>
            <template v-for="grade in grades" :key="grade.id">
              <tr v-if="editingId === grade.id">
                <td colspan="5">
                  <form class="inline-form" @submit.prevent="onSaveEdit">
                    <select v-model="editForm.studentId" required>
                      <option v-for="s in students" :key="s.id" :value="String(s.id)">
                        {{ s.firstName }} {{ s.lastName }}
                      </option>
                    </select>
                    <select v-model="editForm.testId" required>
                      <option v-for="t in tests" :key="t.id" :value="String(t.id)">{{ t.name }}</option>
                    </select>
                    <input v-model="editForm.grade" type="number" min="1" max="6" step="0.5" required />
                    <input v-model="editForm.description" placeholder="Description" />
                    <button type="submit">Save</button>
                    <button type="button" class="ghost" @click="cancelEdit">Cancel</button>
                  </form>
                </td>
              </tr>
              <tr v-else>
                <td>{{ grade.studentId != null ? studentLabel.get(grade.studentId) || grade.studentId : '—' }}</td>
                <td>{{ grade.testId != null ? testLabel.get(grade.testId) || grade.testId : '—' }}</td>
                <td>{{ grade.grade }}</td>
                <td>{{ grade.description || '—' }}</td>
                <td class="actions">
                  <button type="button" class="ghost" @click="startEdit(grade)">Edit</button>
                  <button type="button" class="danger" @click="onDelete(grade.id)">Delete</button>
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
