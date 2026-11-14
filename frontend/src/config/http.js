import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    Accept: 'application/json',
  },
})

http.interceptors.request.use((config) => {
  const devUserId = import.meta.env.VITE_DEV_USER_ID

  // Development-only authentication. Production builds never send this header.
  if (import.meta.env.DEV && devUserId) {
    config.headers['X-Dev-User-Id'] = devUserId
  }

  return config
})

export default http
