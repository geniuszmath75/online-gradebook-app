import { http } from './http'
import type { User, UserCreateRequest } from './types'

export async function listUsers() {
  const { data } = await http.get<User[]>('/api/users')
  return data
}

export async function createUser(payload: UserCreateRequest) {
  const { data } = await http.post<User>('/api/auth/register/user', payload)
  return data
}
