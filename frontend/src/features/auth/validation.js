const MAXIMUM_EMAIL_LENGTH = 50
const MAXIMUM_DISPLAY_NAME_LENGTH = 50
const MINIMUM_PASSWORD_LENGTH = 8
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function validateEmail(email) {
  const trimmedEmail = email.trim()
  if (!trimmedEmail) {
    return 'Enter your email address.'
  }
  if (!EMAIL_PATTERN.test(trimmedEmail)) {
    return 'Email address must include @ and a domain, like name@example.com.'
  }
  if (trimmedEmail.length > MAXIMUM_EMAIL_LENGTH) {
    return `Email addresses can be at most ${MAXIMUM_EMAIL_LENGTH} characters long.`
  }
  return ''
}

export function validateDisplayName(displayName) {
  const trimmedName = displayName.trim()
  if (!trimmedName) {
    return 'Enter a display name.'
  }
  if (trimmedName.length > MAXIMUM_DISPLAY_NAME_LENGTH) {
    return `Display names can be at most ${MAXIMUM_DISPLAY_NAME_LENGTH} characters long.`
  }
  return ''
}

export function validateExistingPassword(password) {
  return password ? '' : 'Enter your password.'
}

export function validateNewPassword(password) {
  if (!password) {
    return 'Enter a password.'
  }
  if (password.length < MINIMUM_PASSWORD_LENGTH) {
    return `Use at least ${MINIMUM_PASSWORD_LENGTH} characters.`
  }
  return ''
}

export function validatePasswordConfirmation(password, passwordConfirmation) {
  if (!passwordConfirmation) {
    return 'Enter your password again.'
  }
  return password === passwordConfirmation ? '' : 'Passwords do not match.'
}
