import { apiClient } from '../../config/apiClient'

export async function fetchPuzzles({ q = '', difficulty = '', page = 0, size = 10 } = {}) {
  const params = { page, size }
  if (q) {
    params.q = q
  }
  if (difficulty) {
    params.difficulty = difficulty
  }

  const response = await apiClient.get('/puzzles', { params })
  return response.data
}

export async function fetchPuzzle(id) {
  const response = await apiClient.get(`/puzzles/${id}`)
  return response.data
}

// Payload gồm community đích, tag đã chuẩn hóa, function header của từng ngôn ngữ
// và các testcase ẩn. Backend sẽ compile từng header qua Judge0 trước khi lưu.
export async function createPuzzle(puzzle) {
  const response = await apiClient.post('/puzzles', puzzle)
  return response.data
}

let tagCache = null
let tagPromise = null

// Cache nhỏ để trang tạo puzzle không gọi lại /tags mỗi lần mở menu.
export async function fetchTags({ force = false } = {}) {
  if (!force && tagCache) return tagCache
  if (!force && tagPromise) return tagPromise

  tagPromise = apiClient
    .get('/tags')
    .then((response) => {
      tagCache = response.data
      return tagCache
    })
    .finally(() => {
      tagPromise = null
    })

  return tagPromise
}

export function invalidateTags() {
  tagCache = null
}
