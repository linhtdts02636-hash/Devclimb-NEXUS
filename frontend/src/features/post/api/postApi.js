import http from '../../../config/http'

export async function createPost(communityId, content) {
  const { data } = await http.post(`/communities/${communityId}/posts`, { content })
  return data
}

export async function getPosts(communityId) {
  const { data } = await http.get(`/communities/${communityId}/posts`)
  return data
}
