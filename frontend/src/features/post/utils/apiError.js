// Backend là nguồn thông báo lỗi nghiệp vụ; helper này chỉ thêm fallback cho lỗi mạng.
export function getApiErrorMessage(error, fallback = 'Không thể hoàn tất yêu cầu.') {
  const backendMessage = error?.response?.data?.message

  if (backendMessage) {
    return backendMessage
  }

  if (error?.code === 'ERR_NETWORK') {
    return 'Không thể kết nối tới máy chủ.'
  }

  return fallback
}
