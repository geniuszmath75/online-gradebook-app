import { http } from './http'
import type { LoginRequest, LoginResponse } from './types'

export async function loginRequest(payload: LoginRequest): Promise<LoginResponse> {
  const { data } = await http.post<LoginResponse>('/api/auth/login', payload)
  return data
}
