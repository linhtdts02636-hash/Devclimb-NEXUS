import { apiClient } from '../../config/apiClient'

const communityPath = (name) => `/communities/${encodeURIComponent(name)}`

export async function createCommunity(community) {
  const response = await apiClient.post('/communities', community)
  return response.data
}

export async function fetchMyCommunities() {
  const response = await apiClient.get('/communities/mine')
  return response.data
}

export async function fetchCommunitiesByIds(ids) {
  if (!ids.length) return []
  const response = await apiClient.get('/communities', { params: { ids: ids.join(',') } })
  return response.data
}

export async function fetchCommunity(name) {
  const response = await apiClient.get(communityPath(name))
  return response.data
}

export async function updateCommunity(name, changes) {
  const response = await apiClient.patch(communityPath(name), changes)
  return response.data
}

export async function deleteCommunity(name, confirmName) {
  await apiClient.delete(communityPath(name), { data: { confirmName } })
}

export async function fetchMembers(name) {
  const response = await apiClient.get(`${communityPath(name)}/members`)
  return response.data
}

export async function joinCommunity(name) {
  const response = await apiClient.post(`${communityPath(name)}/members`)
  return response.data
}

export async function leaveCommunity(name) {
  await apiClient.delete(`${communityPath(name)}/members/me`)
}

export async function fetchRoles(name) {
  const response = await apiClient.get(`${communityPath(name)}/roles`)
  return response.data
}

export async function createRole(name, role) {
  const response = await apiClient.post(`${communityPath(name)}/roles`, role)
  return response.data
}

export async function updateRole(name, roleId, role) {
  const response = await apiClient.patch(`${communityPath(name)}/roles/${roleId}`, role)
  return response.data
}

export async function deleteRole(name, roleId) {
  await apiClient.delete(`${communityPath(name)}/roles/${roleId}`)
}

export async function assignRole(name, userId, roleId) {
  await apiClient.put(`${communityPath(name)}/members/${userId}/roles/${roleId}`)
}

export async function unassignRole(name, userId, roleId) {
  await apiClient.delete(`${communityPath(name)}/members/${userId}/roles/${roleId}`)
}

export async function removeMember(name, userId) {
  await apiClient.delete(`${communityPath(name)}/members/${userId}`)
}

/** Xin link ký sẵn từ backend rồi đẩy thẳng file lên R2; trả về URL công khai để lưu vào cộng đồng. */
export async function uploadCommunityImage(purpose, file) {
  const response = await apiClient.post('/uploads/community-image', {
    purpose,
    contentType: file.type,
    size: file.size,
  })
  const { uploadUrl, publicUrl } = response.data
  // Dùng fetch thay vì apiClient vì link R2 đã tự mang chữ ký, không được gắn thêm header Authorization của Firebase.
  const uploadResponse = await fetch(uploadUrl, { method: 'PUT', headers: { 'Content-Type': file.type }, body: file })
  if (!uploadResponse.ok) {
    throw new Error('The image upload failed.')
  }
  return publicUrl
}
