import { LANDING_PAGE_PATH } from '../../config/applicationPaths'
import { AuthStatus, useAuthStore } from '../../stores/authStore'

export async function redirectAuthenticatedGuests(destination) {
  if (!destination.meta.guestOnly) {
    return true
  }

  const authStore = useAuthStore()
  await authStore.initialize()
  return authStore.status === AuthStatus.AUTHENTICATED ? LANDING_PAGE_PATH : true
}
