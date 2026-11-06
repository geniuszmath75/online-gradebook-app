import axios from 'axios'
import type { ApiErrorBody } from './types'

export function getApiErrorMessage(error: unknown, fallback = 'Something went wrong'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiErrorBody | string | undefined
    if (data && typeof data === 'object' && typeof data.message === 'string' && data.message.trim()) {
      return data.message
    }
    if (typeof data === 'string' && data.trim()) {
      return data
    }
    if (error.message) {
      return error.message
    }
  }
  if (error instanceof Error && error.message) {
    return error.message
  }
  return fallback
}
