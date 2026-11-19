import { apiClient } from '../../config/apiClient'

// Đây là lang_id trong database, không phải language_id của Judge0.
const backendLanguageIds = {
  python: 1,
  javascript: 3,
  java: 2,
}

export function getBackendLanguageId(editorLanguageId) {
  return backendLanguageIds[editorLanguageId] ?? null
}

export async function submitSolution({ puzzleId, languageId, code }) {
  const response = await apiClient.post('/submissions', { puzzleId, languageId, code })
  return response.data
}
