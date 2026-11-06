export interface ApiErrorBody {
  status: number
  message: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  email: string
  token: string
}
