import http from '../../../config/http'

export async function getCommunityBySlug(slug) {
  const { data } = await http.get(`/communities/by-slug/${encodeURIComponent(slug)}`)
  return data
}

export async function getCommunityById(id) {
  const { data } = await http.get(`/communities/${id}`)
  return data
}
