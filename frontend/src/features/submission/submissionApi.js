import axios from 'axios'
import { auth } from '../../config/firebase'

const apiClient = axios.create({ baseURL: '/api' })

// Backend yêu cầu Firebase ID token cho mọi request chấm bài.
apiClient.interceptors.request.use(async (requestConfig) => {
  const firebaseUser = auth.currentUser
  if (firebaseUser) {
    const idToken = await firebaseUser.getIdToken()
    requestConfig.headers.Authorization = `Bearer ${idToken}`
  }
  return requestConfig
})

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
