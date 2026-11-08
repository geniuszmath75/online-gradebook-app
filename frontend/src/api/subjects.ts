import { http } from './http'
import type { MessageResponse, Subject, SubjectRequest } from './types'

export async function listSubjects() {
  const { data } = await http.get<Subject[]>('/api/subjects')
  return data
}

export async function createSubject(payload: SubjectRequest) {
  const { data } = await http.post<Subject>('/api/subjects', payload)
  return data
}

export async function updateSubject(id: number, payload: SubjectRequest) {
  const { data } = await http.put<Subject>(`/api/subjects/${id}`, payload)
  return data
}

export async function deleteSubject(id: number) {
  const { data } = await http.delete<MessageResponse>(`/api/subjects/${id}`)
  return data
}
