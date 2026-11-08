export interface ApiErrorBody {
  status: number
  message: string
}

export type UserRole = 'ADMIN' | 'TEACHER' | 'STUDENT' | 'UNKNOWN'

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  email: string
  token: string
  id: number
  role: UserRole
}

export interface PageMetadata {
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface PageResponse<T> extends PageMetadata {
  content: T[]
}

export interface PagePayload<T> {
  content: T[]
  totalElements?: number
  totalPages?: number
  number?: number
  size?: number
  page?: Partial<PageMetadata>
}

export function normalizePageResponse<T>(payload: PagePayload<T>): PageResponse<T> {
  const metadata = payload.page ?? {}
  return {
    content: payload.content,
    totalElements: metadata.totalElements ?? payload.totalElements ?? 0,
    totalPages: metadata.totalPages ?? payload.totalPages ?? 0,
    number: metadata.number ?? payload.number ?? 0,
    size: metadata.size ?? payload.size ?? payload.content.length,
  }
}

export interface MessageResponse {
  message: string
}

export interface SchoolClass {
  id: number
  name: string
}

export interface Subject {
  id: number
  name: string
}

export interface SubjectRequest {
  name: string
}

export interface User {
  id: number
  email: string
  firstName: string
  lastName: string
  role: Exclude<UserRole, 'STUDENT' | 'UNKNOWN'>
  classId?: number | null
}

export interface UserCreateRequest {
  email: string
  password: string
  firstName: string
  lastName: string
  role: 'ADMIN' | 'TEACHER'
  classId?: number | null
}

export interface Student {
  id: number
  email: string
  firstName: string
  lastName: string
  classId?: number | null
  grades?: Grade[]
}

export interface StudentCreateRequest {
  email: string
  password: string
  firstName: string
  lastName: string
  classId?: number | null
}

export interface StudentUpdateRequest {
  email?: string
  password?: string
  firstName?: string
  lastName?: string
  classId?: number | null
}

export interface Grade {
  id: number
  grade: number
  description?: string | null
  studentId?: number | null
  testId?: number | null
}

export interface GradeCreateRequest {
  grade: number
  description?: string
  studentId: number
  testId: number
}

export interface GradeUpdateRequest {
  grade?: number
  description?: string
  studentId?: number
  testId?: number
}

export interface KnowledgeTest {
  id: number
  name: string
  category: string
  testDate: string
  classId?: number | null
  subjectId?: number | null
}

export interface KnowledgeTestCreateRequest {
  name: string
  category: string
  testDate: string
  classId: number
  subjectId: number
}

export interface KnowledgeTestUpdateRequest {
  name?: string
  category?: string
  testDate?: string
  classId?: number
  subjectId?: number
}

export interface ClassCreateRequest {
  name: string
}
