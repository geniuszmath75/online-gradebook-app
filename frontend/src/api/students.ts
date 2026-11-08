import { http } from './http'
import { normalizePageResponse } from './types'
import type {
  MessageResponse,
  Student,
  StudentCreateRequest,
  StudentUpdateRequest,
  PagePayload,
} from './types'

export async function listStudents(params: {
  page?: number
  size?: number
  classId?: number
  email?: string
  sort?: string
} = {}) {
  const { data } = await http.get<PagePayload<Student>>('/api/students', { params })
  return normalizePageResponse(data)
}

export async function getStudent(id: number) {
  const { data } = await http.get<Student>(`/api/students/${id}`)
  return data
}

export async function createStudent(payload: StudentCreateRequest) {
  const { data } = await http.post<Student>('/api/auth/register/student', payload)
  return data
}

export async function updateStudent(id: number, payload: StudentUpdateRequest) {
  const { data } = await http.patch<Student>(`/api/students/${id}`, payload)
  return data
}

export async function deleteStudent(id: number) {
  const { data } = await http.delete<MessageResponse>(`/api/students/${id}`)
  return data
}
