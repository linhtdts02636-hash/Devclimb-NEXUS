import { apiClient } from '../../config/apiClient'

// Tiêu đề không bắt buộc; backend sẽ trim và lưu null nếu để trống.
export async function createPost(communityId, { title, content }) {
  const { data } = await apiClient.post(`/communities/${communityId}/posts`, { title, content })
  return data
}

// Trả về { posts, nextCursor }. Truyền nextCursor của trang trước làm `before` để lấy trang kế; null nghĩa là đã hết bài.
export async function getPosts(communityId, before = null) {
  const { data } = await apiClient.get(`/communities/${communityId}/posts`, { params: before === null ? {} : { before } })
  return data
}

export async function getPinnedPosts(communityId) {
  const { data } = await apiClient.get(`/communities/${communityId}/posts/pinned`)
  return data
}

export async function updatePost(postId, { title, content }) {
  const { data } = await apiClient.put(`/posts/${postId}`, { title, content })
  return data
}

export async function deletePost(postId) {
  await apiClient.delete(`/posts/${postId}`)
}

export async function setPostPinned(postId, pinned) {
  const { data } = pinned ? await apiClient.put(`/posts/${postId}/pin`) : await apiClient.delete(`/posts/${postId}/pin`)
  return data
}
