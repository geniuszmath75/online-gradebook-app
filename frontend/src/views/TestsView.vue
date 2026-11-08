<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import ErrorBanner from '@/components/ErrorBanner.vue'
import { getApiErrorMessage } from '@/api/errors'
import { listClasses } from '@/api/classes'
import { createTest, deleteTest, listTests, updateTest } from '@/api/tests'
import { listSubjects } from '@/api/subjects'
import type {
  KnowledgeTest,
  KnowledgeTestCreateRequest,
  KnowledgeTestUpdateRequest,
  SchoolClass,
  Subject,
} from '@/api/types'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const tests = ref<KnowledgeTest[]>([])
const classes = ref<SchoolClass[]>([])
const subjects = ref<Subject[]>([])
const error = ref('')
const notice = ref('')
const loading = ref(false)
const editingId = ref<number | null>(null)

const categories = ['QUIZ', 'CLASS_TEST', 'HOMEWORK', 'ORAL_ANSWER', 'CLASSWORK', 'OTHER']
const minDate = new Date(Date.now() + 86_400_000).toISOString().slice(0, 10)

const createForm = reactive<KnowledgeTestCreateRequest>({
  name: '',
  category: 'QUIZ',
  testDate: '',
  classId: 0,
  subjectId: 0,
})

type TestForm = {
  name: string
  category: string
  testDate: string
  classId: string
  subjectId: string
}

const editForm = reactive<TestForm>({
  name: '',
  category: 'QUIZ',
  testDate: '',
  classId: '',
  subjectId: '',
})
const editSnapshot = ref<TestForm | null>(null)

const classLabel = computed(() => {
  const map = new Map<number, string>()
  for (const item of classes.value) map.set(item.id, item.name)
  return map
})

const subjectLabel = computed(() => {
  const map = new Map<number, string>()
  for (const item of subjects.value) map.set(item.id, item.name)
  return map
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [testList, classList, subjectList] = await Promise.all([
      listTests(),
      listClasses(),
      listSubjects(),
    ])
    tests.value = testList
    classes.value = classList
    subjects.value = subjectList
  } catch (err) {
    error.value = getApiErrorMessage(err)
  } finally {
    loading.value = false
  }
}

function resetCreateForm() {
  createForm.name = ''
  createForm.category = 'QUIZ'
  createForm.testDate = ''
  createForm.classId = 0
  createForm.subjectId = 0
}

async function onCreate() {
  error.value = ''
  notice.value = ''
  if (!createForm.classId || !createForm.subjectId) {
    error.value = 'Select a class and subject before creating the test'
    return
  }
  try {
    await createTest(createForm)
    resetCreateForm()
    notice.value = 'Test created successfully'
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

function startEdit(test: KnowledgeTest) {
  editingId.value = test.id
  const snapshot: TestForm = {
    name: test.name,
    category: test.category,
    testDate: test.testDate,
    classId: test.classId != null ? String(test.classId) : '',
    subjectId: test.subjectId != null ? String(test.subjectId) : '',
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

  const current: TestForm = {
    name: editForm.name.trim(),
    category: editForm.category,
    testDate: editForm.testDate,
    classId: editForm.classId,
    subjectId: editForm.subjectId,
  }
  if (JSON.stringify(current) === JSON.stringify(editSnapshot.value)) {
    notice.value = 'No changes to save'
    return
  }

  const payload: KnowledgeTestUpdateRequest = {
    name: current.name,
    category: current.category,
    testDate: current.testDate,
    classId: Number(current.classId),
    subjectId: Number(current.subjectId),
  }
  if (!payload.classId || !payload.subjectId) {
    error.value = 'Select a class and subject before saving the test'
    return
  }

  try {
    await updateTest(editingId.value, payload)
    notice.value = 'Test updated successfully'
    cancelEdit()
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

async function onDelete(id: number) {
  if (!confirm('Delete this test? Its grades will also be deleted.')) return
  error.value = ''
  notice.value = ''
  try {
    const result = await deleteTest(id)
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
        <h1>Knowledge tests</h1>
        <p>{{ auth.isAdmin ? 'Create and manage all knowledge tests.' : 'Manage your knowledge tests.' }}</p>
      </header>

      <ErrorBanner :message="error" />
      <p v-if="notice" class="notice">{{ notice }}</p>
      <p v-if="loading" class="muted">Loading…</p>

      <form class="panel form" @submit.prevent="onCreate">
        <h2>Add test</h2>
        <div class="grid">
          <label>
            Name
            <input v-model="createForm.name" required />
          </label>
          <label>
            Category
            <select v-model="createForm.category" required>
              <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
            </select>
          </label>
          <label>
            Test date
            <input v-model="createForm.testDate" :min="minDate" type="date" required />
          </label>
          <label>
            Class
            <select v-model.number="createForm.classId" required>
              <option :value="0" disabled>Select class</option>
              <option v-for="item in classes" :key="item.id" :value="item.id">{{ item.name }}</option>
            </select>
          </label>
          <label>
            Subject
            <select v-model.number="createForm.subjectId" required>
              <option :value="0" disabled>Select subject</option>
              <option v-for="item in subjects" :key="item.id" :value="item.id">{{ item.name }}</option>
            </select>
          </label>
        </div>
        <button type="submit">Create test</button>
      </form>

      <div class="panel table-wrap">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Category</th>
              <th>Date</th>
              <th>Class</th>
              <th>Subject</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && tests.length === 0">
              <td colspan="6" class="muted">No tests found.</td>
            </tr>
            <template v-for="test in tests" :key="test.id">
              <tr v-if="editingId === test.id">
                <td colspan="6">
                  <form class="inline-form" @submit.prevent="onSaveEdit">
                    <input v-model="editForm.name" required placeholder="Name" />
                    <select v-model="editForm.category" required>
                      <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
                    </select>
                    <input v-model="editForm.testDate" :min="minDate" type="date" required />
                    <select v-model="editForm.classId" required>
                      <option disabled value="">Select class</option>
                      <option v-for="item in classes" :key="item.id" :value="String(item.id)">{{ item.name }}</option>
                    </select>
                    <select v-model="editForm.subjectId" required>
                      <option disabled value="">Select subject</option>
                      <option v-for="item in subjects" :key="item.id" :value="String(item.id)">{{ item.name }}</option>
                    </select>
                    <button type="submit">Save</button>
                    <button type="button" class="ghost" @click="cancelEdit">Cancel</button>
                  </form>
                </td>
              </tr>
              <tr v-else>
                <td>{{ test.name }}</td>
                <td>{{ test.category }}</td>
                <td>{{ test.testDate }}</td>
                <td>{{ test.classId != null ? classLabel.get(test.classId) || test.classId : '—' }}</td>
                <td>{{ test.subjectId != null ? subjectLabel.get(test.subjectId) || test.subjectId : '—' }}</td>
                <td class="actions">
                  <button type="button" class="ghost" @click="startEdit(test)">Edit</button>
                  <button type="button" class="danger" @click="onDelete(test.id)">Delete</button>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </section>
  </AppShell>
</template>
