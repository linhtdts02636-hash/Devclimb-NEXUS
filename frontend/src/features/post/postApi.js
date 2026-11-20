import { apiClient } from '../../config/apiClient'

// Tiêu đề không bắt buộc; backend sẽ trim và lưu null nếu để trống.
export async function createPost(communityId, { title, content }) {
  const { data } = await apiClient.post(`/communities/${communityId}/posts`, { title, content })
  return data
}

export async function getPosts(communityId) {
  const { data } = await apiClient.get(`/communities/${communityId}/posts`)
  return data
}

export async function updatePost(postId, { title, content }) {
  const { data } = await apiClient.put(`/posts/${postId}`, { title, content })
  return data
}

export async function deletePost(postId) {
  await apiClient.delete(`/posts/${postId}`)
}

// Dùng apiClient để interceptor tự gắn Firebase ID token.
export async function setPostReaction(postId, type) {
  const { data } = await apiClient.put(`/posts/${postId}/reaction`, { type })
  return data
}

export async function removePostReaction(postId) {
  const { data } = await apiClient.delete(`/posts/${postId}/reaction`)
  return data
}
