import { useAuthStore } from '../../stores/authStore'

// meta.guestOnly: người đã đăng nhập không cần xem /login. meta.requiresAuth: khách phải đăng nhập trước.
export async function checkRouteAccess(destination) {
  const { guestOnly, requiresAuth } = destination.meta
  if (!guestOnly && !requiresAuth) return true

  const authStore = useAuthStore()
  await authStore.initialize()
  const isAuthenticated = authStore.status === 'authenticated'
  if (guestOnly) return isAuthenticated ? '/' : true
  return isAuthenticated ? true : '/login'
}
