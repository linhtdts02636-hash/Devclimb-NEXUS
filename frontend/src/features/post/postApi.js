import http from '../../../config/http'

// Tiêu đề không bắt buộc; backend sẽ trim và lưu null nếu để trống.
export async function createPost(communityId, { title, content }) {
  const { data } = await http.post(`/communities/${communityId}/posts`, { title, content })
  return data
}

export async function getPosts(communityId) {
  const { data } = await http.get(`/communities/${communityId}/posts`)
  return data
}

export async function updatePost(postId, { title, content }) {
  const { data } = await http.put(`/posts/${postId}`, { title, content })
  return data
}

export async function deletePost(postId) {
  await http.delete(`/posts/${postId}`)
}
