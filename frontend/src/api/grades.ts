import { http } from './http'
import { normalizePageResponse } from './types'
import type {
  Grade,
  GradeCreateRequest,
  GradeUpdateRequest,
  MessageResponse,
  PagePayload,
} from './types'

export async function listGrades(params: {
  page?: number
  size?: number
  studentId?: number
  testId?: number
} = {}) {
  const { data } = await http.get<PagePayload<Grade>>('/api/grades', { params })
  return normalizePageResponse(data)
}

export async function createGrade(payload: GradeCreateRequest) {
  const { data } = await http.post<Grade>('/api/grades', payload)
  return data
}

export async function updateGrade(id: number, payload: GradeUpdateRequest) {
  const { data } = await http.patch<Grade>(`/api/grades/${id}`, payload)
  return data
}

export async function deleteGrade(id: number) {
  const { data } = await http.delete<MessageResponse>(`/api/grades/${id}`)
  return data
}
