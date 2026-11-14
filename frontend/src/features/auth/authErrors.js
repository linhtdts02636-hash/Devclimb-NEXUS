const NETWORK_MESSAGE = 'Could not reach the server. Check your connection and try again.'
const DEFAULT_MESSAGE = 'Something went wrong. Try again in a moment.'
const WRONG_CREDENTIALS_MESSAGE = 'Email or password is incorrect. Check both and try again, or reset your password.'

// Lỗi do người dùng chủ động gây ra (ví dụ đóng cửa sổ đăng nhập) thì không hiển thị gì.
const SILENT_ERROR_CODES = ['auth/popup-closed-by-user', 'auth/cancelled-popup-request']

// Lỗi hiển thị ngay dưới một ô nhập thay vì phía trên form.
const FIELD_ERRORS = {
  'auth/email-already-in-use': {
    target: 'email',
    message: 'An account with this email already exists. Log in instead, or reset your password.',
  },
  'auth/weak-password': { target: 'password', message: 'Use at least 8 characters.' },
}

const FORM_MESSAGES = {
  'auth/invalid-credential': WRONG_CREDENTIALS_MESSAGE,
  'auth/wrong-password': WRONG_CREDENTIALS_MESSAGE,
  'auth/user-not-found': WRONG_CREDENTIALS_MESSAGE,
  'auth/too-many-requests': 'Too many attempts. Wait a few minutes, or reset your password.',
  'auth/network-request-failed': NETWORK_MESSAGE,
  ERR_NETWORK: NETWORK_MESSAGE,
  'auth/popup-blocked': 'Your browser blocked the sign-in window. Allow pop-ups for this site and try again.',
  'auth/account-exists-with-different-credential':
    'This email is already used with another sign-in method. Log in with that method instead.',
  'auth/user-disabled': 'This account has been disabled.',
}

/**
 * Chuyển lỗi từ Firebase hoặc backend thành { target, message }, trong đó target là 'form', 'email' hoặc 'password'.
 * Trả về null với những lỗi không cần hiển thị.
 */
export function describeAuthError(error) {
  const backendMessage = error?.response?.data?.message
  if (backendMessage) return { target: 'form', message: backendMessage }
  if (SILENT_ERROR_CODES.includes(error?.code)) return null
  return FIELD_ERRORS[error?.code] ?? { target: 'form', message: FORM_MESSAGES[error?.code] ?? DEFAULT_MESSAGE }
}
