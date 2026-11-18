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

export async function createPuzzle(puzzle) {
  const response = await apiClient.post('/puzzles', puzzle)
  return response.data
}
