<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import ErrorBanner from '@/components/ErrorBanner.vue'
import { getApiErrorMessage } from '@/api/errors'
import { createSubject, deleteSubject, listSubjects, updateSubject } from '@/api/subjects'
import type { Subject } from '@/api/types'

const subjects = ref<Subject[]>([])
const error = ref('')
const notice = ref('')
const loading = ref(false)
const createName = ref('')
const editingId = ref<number | null>(null)
const editName = ref('')
const editSnapshotName = ref<string | null>(null)

async function load() {
  loading.value = true
  error.value = ''
  try {
    subjects.value = await listSubjects()
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
    await createSubject({ name: createName.value.trim() })
    createName.value = ''
    notice.value = 'Subject created successfully'
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

function startEdit(subject: Subject) {
  editingId.value = subject.id
  editName.value = subject.name
  editSnapshotName.value = subject.name
}

function cancelEdit() {
  editingId.value = null
  editSnapshotName.value = null
}

async function onSaveEdit() {
  if (editingId.value == null || editSnapshotName.value == null) return
  error.value = ''
  notice.value = ''

  const nextName = editName.value.trim()
  if (nextName === editSnapshotName.value) {
    notice.value = 'No changes to save'
    return
  }

  try {
    await updateSubject(editingId.value, { name: nextName })
    notice.value = 'Subject updated successfully'
    cancelEdit()
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

async function onDelete(id: number) {
  if (!confirm('Delete this subject?')) return
  error.value = ''
  notice.value = ''
  try {
    const result = await deleteSubject(id)
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
        <h1>Subjects</h1>
        <p>Create and manage subjects used by knowledge tests.</p>
      </header>

      <ErrorBanner :message="error" />
      <p v-if="notice" class="notice">{{ notice }}</p>
      <p v-if="loading" class="muted">Loading…</p>

      <form class="panel form" @submit.prevent="onCreate">
        <h2>Add subject</h2>
        <div class="grid">
          <label>Name <input v-model="createName" required /></label>
        </div>
        <button type="submit">Create subject</button>
      </form>

      <div class="panel table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!loading && subjects.length === 0">
              <td colspan="3" class="muted">No subjects found.</td>
            </tr>
            <tr v-for="subject in subjects" :key="subject.id">
              <template v-if="editingId === subject.id">
                <td>{{ subject.id }}</td>
                <td colspan="2">
                  <form class="inline-form" @submit.prevent="onSaveEdit">
                    <input v-model="editName" required />
                    <button type="submit">Save</button>
                    <button type="button" class="ghost" @click="cancelEdit">Cancel</button>
                  </form>
                </td>
              </template>
              <template v-else>
                <td>{{ subject.id }}</td>
                <td>{{ subject.name }}</td>
                <td class="actions">
                  <button type="button" class="ghost" @click="startEdit(subject)">Edit</button>
                  <button type="button" class="danger" @click="onDelete(subject.id)">Delete</button>
                </td>
              </template>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </AppShell>
</template>
