import axios from 'axios'
import { auth } from '../../config/firebase'

const apiClient = axios.create({ baseURL: '/api' })

apiClient.interceptors.request.use(async (requestConfig) => {
  const firebaseUser = auth.currentUser
  if (firebaseUser) {
    const idToken = await firebaseUser.getIdToken()
    requestConfig.headers.Authorization = `Bearer ${idToken}`
  }
  return requestConfig
})

export async function fetchPuzzles({ q = '', difficulty = '', page = 0, size = 10 } = {}) {
  const params = { page, size }
  if (q) {
    params.q = q
  }
  if (difficulty) {
    params.difficulty = difficulty
  }

  const response = await apiClient.get('/puzzle', { params })
  return response.data
}

export async function createPuzzle(puzzle) {
  const response = await apiClient.post('/puzzle', puzzle)
  return response.data
}
