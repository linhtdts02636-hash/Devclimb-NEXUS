import { useAuthStore } from '../../stores/authStore'

// meta.guestOnly: người đã đăng nhập không cần xem /login. meta.requiresAuth: khách phải đăng nhập trước.
// meta.public: khách được xem nhưng vẫn khởi tạo auth để biết trạng thái đăng nhập.
export async function checkRouteAccess(destination) {
  const { guestOnly, requiresAuth, public: isPublicRoute } = destination.meta
  if (!guestOnly && !requiresAuth && !isPublicRoute) return true

  const authStore = useAuthStore()
  await authStore.initialize()
  if (isPublicRoute) return true

  const isAuthenticated = authStore.status === 'authenticated'
  if (guestOnly) return isAuthenticated ? '/' : true
  return isAuthenticated ? true : '/login'
}
