import { defineStore } from 'pinia'
import { ref } from 'vue'
import { onAuthStateChanged } from 'firebase/auth'
import { auth } from '../config/firebase'
import { createSession } from '../features/auth/authApi'
import { requiresEmailVerification, signOutCurrentUser } from '../features/auth/authService'

function isClientError(error) {
  const statusCode = error?.response?.status
  return statusCode >= 400 && statusCode < 500
}

export const useAuthStore = defineStore('auth', () => {
  // Trạng thái: 'unknown' | 'signedOut' | 'needsEmailVerification' | 'authenticated'
  const status = ref('unknown')
  const profile = ref(null)
  const pendingVerificationEmail = ref('')
  let initialization = null

  function markSignedOut() {
    profile.value = null
    pendingVerificationEmail.value = ''
    status.value = 'signedOut'
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
      status.value = 'needsEmailVerification'
      return
    }

    try {
      profile.value = await createSession()
      pendingVerificationEmail.value = ''
      status.value = 'authenticated'
    } catch (error) {
      // Phiên bị từ chối (tài khoản bị khóa, token không hợp lệ) thì không được để lại phiên đăng nhập Firebase.
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
    // Backend đọc email_verified từ token, nên phải thay token đang được lưu cache.
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
