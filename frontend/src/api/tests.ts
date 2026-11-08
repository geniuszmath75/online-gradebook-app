import { http } from './http'
import type {
  KnowledgeTest,
  KnowledgeTestCreateRequest,
  KnowledgeTestUpdateRequest,
  MessageResponse,
} from './types'

export async function listTests() {
  const { data } = await http.get<KnowledgeTest[]>('/api/knowledge_tests')
  return data
}

export async function createTest(payload: KnowledgeTestCreateRequest) {
  const { data } = await http.post<KnowledgeTest>('/api/knowledge_tests', payload)
  return data
}

export async function updateTest(id: number, payload: KnowledgeTestUpdateRequest) {
  const { data } = await http.patch<KnowledgeTest>(`/api/knowledge_tests/${id}`, payload)
  return data
}

export async function deleteTest(id: number) {
  const { data } = await http.delete<MessageResponse>(`/api/knowledge_tests/${id}`)
  return data
}
