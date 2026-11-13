const STORAGE_KEY = 'dc-remember-me'

export function loadRememberMePreference() {
  try {
    return localStorage.getItem(STORAGE_KEY) !== 'false'
  } catch {
    return true
  }
}

export function saveRememberMePreference(isRemembered) {
  try {
    localStorage.setItem(STORAGE_KEY, String(isRemembered))
  } catch {
    // Storage is blocked in private windows; the choice then only lasts for this visit.
  }
}
