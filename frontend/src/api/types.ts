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

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface MessageResponse {
  message: string
}

export interface SchoolClass {
  id: number
  name: string
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
}

export interface ClassCreateRequest {
  name: string
}
