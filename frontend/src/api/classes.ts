import { http } from './http'
import type { ClassCreateRequest, MessageResponse, SchoolClass } from './types'

export async function listClasses() {
  const { data } = await http.get<SchoolClass[]>('/api/classes')
  return data
}

export async function createClass(payload: ClassCreateRequest) {
  const { data } = await http.post<SchoolClass>('/api/classes', payload)
  return data
}

export async function updateClass(id: number, payload: ClassCreateRequest) {
  const { data } = await http.put<SchoolClass>(`/api/classes/${id}`, payload)
  return data
}

export async function deleteClass(id: number) {
  const { data } = await http.delete<MessageResponse>(`/api/classes/${id}`)
  return data
}
