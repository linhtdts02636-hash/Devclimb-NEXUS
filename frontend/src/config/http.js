import axios from 'axios'
import { auth } from './firebase'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    Accept: 'application/json',
  },
})

http.interceptors.request.use(async (config) => {
  const firebaseUser = auth.currentUser

  // Gắn Firebase ID token để backend xác thực các API cần đăng nhập.
  if (firebaseUser) {
    try {
      const idToken = await firebaseUser.getIdToken()
      config.headers.Authorization = `Bearer ${idToken}`
    } catch {
      // Nếu lấy token thất bại, để backend trả 401 thay vì chặn request tại đây.
    }
  }

  const devUserId = import.meta.env.VITE_DEV_USER_ID

  // Header dev chỉ dùng khi chạy development; production không gửi header này.
  if (import.meta.env.DEV && devUserId) {
    config.headers['X-Dev-User-Id'] = devUserId
  }

  return config
})

export default http
