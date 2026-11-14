import { useAuthStore } from '../../stores/authStore'

// Người dùng đã đăng nhập không cần xem các trang chỉ dành cho khách (meta.guestOnly) như /login.
export async function redirectAuthenticatedGuests(destination) {
  if (!destination.meta.guestOnly) return true
  const authStore = useAuthStore()
  await authStore.initialize()
  return authStore.status === 'authenticated' ? '/' : true
}
