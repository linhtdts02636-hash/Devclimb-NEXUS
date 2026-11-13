import { defineStore } from 'pinia'
import { ref } from 'vue'
import { onAuthStateChanged } from 'firebase/auth'
import { auth } from '../config/firebase'
import { createSession } from '../features/auth/authApi'
import { requiresEmailVerification, signOutCurrentUser } from '../features/auth/authService'

export const AuthStatus = Object.freeze({
  UNKNOWN: 'unknown',
  SIGNED_OUT: 'signedOut',
  NEEDS_EMAIL_VERIFICATION: 'needsEmailVerification',
  AUTHENTICATED: 'authenticated',
})

function isClientError(error) {
  const statusCode = error?.response?.status
  return statusCode >= 400 && statusCode < 500
}

export const useAuthStore = defineStore('auth', () => {
  const status = ref(AuthStatus.UNKNOWN)
  const profile = ref(null)
  const pendingVerificationEmail = ref('')
  let initialization = null

  function markSignedOut() {
    profile.value = null
    pendingVerificationEmail.value = ''
    status.value = AuthStatus.SIGNED_OUT
  }

  async function resolveSession() {
    const firebaseUser = auth.currentUser
    if (!firebaseUser) {
      markSignedOut()
      return
    }

    if (requiresEmailVerification(firebaseUser)) {
      profile.value = null
      pendingVerificationEmail.value = firebaseUser.email
      status.value = AuthStatus.NEEDS_EMAIL_VERIFICATION
      return
    }

    try {
      profile.value = await createSession()
      pendingVerificationEmail.value = ''
      status.value = AuthStatus.AUTHENTICATED
    } catch (error) {
      // A rejected session (disabled account, invalid token) must not leave a Firebase login behind.
      if (isClientError(error)) {
        await signOutCurrentUser()
      }
      markSignedOut()
      throw error
    }
  }

  function initialize() {
    initialization ??= new Promise((resolve) => {
      let isFirstCallback = true
      onAuthStateChanged(auth, async (firebaseUser) => {
        if (isFirstCallback) {
          isFirstCallback = false
          await resolveSession().catch(() => {})
          resolve()
        } else if (!firebaseUser) {
          markSignedOut()
        }
      })
    })
    return initialization
  }

  async function confirmEmailVerified() {
    const firebaseUser = auth.currentUser
    await firebaseUser.reload()
    if (requiresEmailVerification(firebaseUser)) {
      return false
    }
    // The backend reads email_verified from the token, so the cached token has to be replaced.
    await firebaseUser.getIdToken(true)
    await resolveSession()
    return true
  }

  async function logout() {
    await signOutCurrentUser()
    markSignedOut()
  }

  return { status, profile, pendingVerificationEmail, initialize, resolveSession, confirmEmailVerified, logout }
})
