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

export async function createSession() {
  const response = await apiClient.post('/auth/session')
  return response.data
}
