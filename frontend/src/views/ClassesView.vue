<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppShell from '@/components/AppShell.vue'
import ErrorBanner from '@/components/ErrorBanner.vue'
import { getApiErrorMessage } from '@/api/errors'
import { createClass, deleteClass, listClasses, updateClass } from '@/api/classes'
import type { SchoolClass } from '@/api/types'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const classes = ref<SchoolClass[]>([])
const error = ref('')
const notice = ref('')
const loading = ref(false)
const editingId = ref<number | null>(null)
const createName = ref('')
const editName = ref('')
const editSnapshotName = ref<string | null>(null)

async function load() {
  loading.value = true
  error.value = ''
  try {
    classes.value = await listClasses()
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
    await createClass({ name: createName.value.trim() })
    createName.value = ''
    notice.value = 'Class created successfully'
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

function startEdit(item: SchoolClass) {
  editingId.value = item.id
  editName.value = item.name
  editSnapshotName.value = item.name
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
    await updateClass(editingId.value, { name: nextName })
    notice.value = 'Class updated successfully'
    editingId.value = null
    editSnapshotName.value = null
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err)
  }
}

async function onDelete(id: number) {
  if (!confirm('Delete this class?')) return
  error.value = ''
  notice.value = ''
  try {
    const result = await deleteClass(id)
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
        <h1>Classes</h1>
        <p>Browse school classes{{ auth.isAdmin ? ' and manage them as admin' : '' }}.</p>
      </header>

      <ErrorBanner :message="error" />
      <p v-if="notice" class="notice">{{ notice }}</p>
      <p v-if="loading" class="muted">Loading…</p>

      <form v-if="auth.isAdmin" class="panel form" @submit.prevent="onCreate">
        <h2>Add class</h2>
        <div class="grid">
          <label>Name <input v-model="createName" required placeholder="e.g. 2A" /></label>
        </div>
        <button type="submit">Create class</button>
      </form>

      <div class="panel table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th v-if="auth.isAdmin"></th>
            </tr>
          </thead>
          <tbody>
            <template v-for="item in classes" :key="item.id">
              <tr v-if="auth.isAdmin && editingId === item.id">
                <td>{{ item.id }}</td>
                <td colspan="2">
                  <form class="inline-form" @submit.prevent="onSaveEdit">
                    <input v-model="editName" required />
                    <button type="submit">Save</button>
                    <button type="button" class="ghost" @click="cancelEdit">Cancel</button>
                  </form>
                </td>
              </tr>
              <tr v-else>
                <td>{{ item.id }}</td>
                <td>{{ item.name }}</td>
                <td v-if="auth.isAdmin" class="actions">
                  <button type="button" class="ghost" @click="startEdit(item)">Edit</button>
                  <button type="button" class="danger" @click="onDelete(item.id)">Delete</button>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </section>
  </AppShell>
</template>
