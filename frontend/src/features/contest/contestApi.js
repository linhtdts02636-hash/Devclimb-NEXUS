import { apiClient } from '../../config/apiClient'

// Lấy danh sách tất cả các cuộc thi từ backend (/api/contests)
export async function fetchContests() {
  const response = await apiClient.get('/contests')
  return response.data
}

// Lấy thông tin chi tiết một cuộc thi theo id (/api/contests/:id)
export async function fetchContest(id) {
  const response = await apiClient.get(`/contests/${id}`)
  return response.data
}

// Tạo cuộc thi mới (/api/contests)
export async function createContest(contest) {
  const response = await apiClient.post('/contests', contest)
  return response.data
}

// Cập nhật thông tin cuộc thi (/api/contests/:id)
export async function updateContest(id, contest) {
  const response = await apiClient.patch(`/contests/${id}`, contest)
  return response.data
}

// Lấy danh sách bài toán của cuộc thi kèm điểm số (/api/contests/:id/puzzles)
export async function fetchContestPuzzles(contestId) {
  const response = await apiClient.get(`/contests/${contestId}/puzzles`)
  return response.data
}

// Lấy danh sách thí sinh và điểm số bảng xếp hạng (/api/contests/:id/contestants)
export async function fetchContestants(contestId) {
  const response = await apiClient.get(`/contests/${contestId}/contestants`)
  return response.data
}

// Đăng ký tham gia cuộc thi (/api/contests/:id/contestants)
export async function joinContest(contestId) {
  const response = await apiClient.post(`/contests/${contestId}/contestants`)
  return response.data
}
