import { apiClient } from '../../config/apiClient'

export async function createSession() {
  const response = await apiClient.post('/auth/session')
  return response.data
}
