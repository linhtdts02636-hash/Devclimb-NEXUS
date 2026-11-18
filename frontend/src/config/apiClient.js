import axios from 'axios'
import { auth } from './firebase'

const NETWORK_MESSAGE = 'Could not reach the server. Check your connection and try again.'
const DEFAULT_MESSAGE = 'Something went wrong. Try again in a moment.'

export const apiClient = axios.create({ baseURL: '/api' })

apiClient.interceptors.request.use(async (requestConfig) => {
  const firebaseUser = auth.currentUser
  if (firebaseUser) {
    const idToken = await firebaseUser.getIdToken()
    requestConfig.headers.Authorization = `Bearer ${idToken}`
  }
  return requestConfig
})

// Backend trả { code, message }; lỗi mạng không có response nên cần một thông báo riêng.
export function describeApiError(error) {
  const data = error?.response?.data
  if (data?.message) return { code: data.code, message: data.message }
  return { code: undefined, message: error?.code === 'ERR_NETWORK' ? NETWORK_MESSAGE : DEFAULT_MESSAGE }
}
