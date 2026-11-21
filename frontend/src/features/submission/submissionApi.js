import { apiClient } from '../../config/apiClient'

// Đây là lang_id trong database, không phải language_id của Judge0.
const backendLanguageIds = {
  python: 1,
  javascript: 3,
  java: 2,
}

// Map ngược để chuyển languageId của API puzzle về ID mà editor đang dùng.
const editorLanguageIds = Object.fromEntries(
  Object.entries(backendLanguageIds).map(([editorId, backendId]) => [backendId, editorId]),
)

export function getBackendLanguageId(editorLanguageId) {
  return backendLanguageIds[editorLanguageId] ?? null
}

export function getEditorLanguageId(backendLanguageId) {
  return editorLanguageIds[backendLanguageId] ?? null
}

export async function submitSolution({ puzzleId, languageId, code }) {
  const response = await apiClient.post('/submissions', { puzzleId, languageId, code })
  return response.data
}
