export function getPostErrorMessage(error, fallback = 'Không thể hoàn tất yêu cầu.') {
  const status = error?.response?.status
  const backendMessage = error?.response?.data?.message

  if (backendMessage) {
    return backendMessage
  }

  if (status === 401) {
    return 'Bạn cần đăng nhập để tiếp tục.'
  }

  if (status === 403) {
    return 'Bạn không phải là thành viên của cộng đồng này.'
  }

  if (status === 404) {
    return 'Cộng đồng không tồn tại.'
  }

  if (status >= 500) {
    return 'Đã xảy ra lỗi máy chủ. Vui lòng thử lại.'
  }

  if (error?.code === 'ERR_NETWORK') {
    return 'Không thể kết nối tới máy chủ.'
  }

  return fallback
}
