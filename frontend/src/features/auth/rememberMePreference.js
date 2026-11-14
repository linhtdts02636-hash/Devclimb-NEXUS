const STORAGE_KEY = 'dc-remember-me'

// Mặc định là ghi nhớ đăng nhập khi chưa có lựa chọn nào được lưu.
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
    // Trình duyệt ẩn danh có thể chặn localStorage; lựa chọn khi đó chỉ có hiệu lực trong lần truy cập này.
  }
}
