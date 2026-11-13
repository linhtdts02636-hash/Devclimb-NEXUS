export const ErrorTarget = Object.freeze({
  FORM: 'form',
  EMAIL: 'email',
  PASSWORD: 'password',
})

const NETWORK_MESSAGE = 'Could not reach the server. Check your connection and try again.'
const DEFAULT_MESSAGE = 'Something went wrong. Try again in a moment.'
const WRONG_CREDENTIALS_MESSAGE = 'Email or password is incorrect. Check both and try again, or reset your password.'

const SILENT_ERROR_CODES = ['auth/popup-closed-by-user', 'auth/cancelled-popup-request']

const FORM_MESSAGES = {
  'auth/invalid-credential': WRONG_CREDENTIALS_MESSAGE,
  'auth/wrong-password': WRONG_CREDENTIALS_MESSAGE,
  'auth/user-not-found': WRONG_CREDENTIALS_MESSAGE,
  'auth/too-many-requests': 'Too many attempts. Wait a few minutes, or reset your password.',
  'auth/network-request-failed': NETWORK_MESSAGE,
  'auth/popup-blocked': 'Your browser blocked the sign-in window. Allow pop-ups for this site and try again.',
  'auth/account-exists-with-different-credential':
    'This email is already used with another sign-in method. Log in with that method instead.',
  'auth/user-disabled': 'This account has been disabled.',
}

/**
 * Turns a Firebase or backend error into a message and the field it belongs to.
 * Returns null when the user caused it on purpose (for example closing the sign-in popup).
 */
export function describeAuthError(error) {
  const backendMessage = error?.response?.data?.message
  if (backendMessage) {
    return { target: ErrorTarget.FORM, message: backendMessage }
  }
  if (error?.code === 'ERR_NETWORK') {
    return { target: ErrorTarget.FORM, message: NETWORK_MESSAGE }
  }
  if (SILENT_ERROR_CODES.includes(error?.code)) {
    return null
  }
  if (error?.code === 'auth/email-already-in-use') {
    return {
      target: ErrorTarget.EMAIL,
      message: 'An account with this email already exists. Log in instead, or reset your password.',
    }
  }
  if (error?.code === 'auth/weak-password') {
    return { target: ErrorTarget.PASSWORD, message: 'Use at least 8 characters.' }
  }
  return { target: ErrorTarget.FORM, message: FORM_MESSAGES[error?.code] ?? DEFAULT_MESSAGE }
}
