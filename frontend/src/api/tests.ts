import { http } from './http'
import type { KnowledgeTest } from './types'

export async function listTests() {
  const { data } = await http.get<KnowledgeTest[]>('/api/knowledge_tests')
  return data
}
