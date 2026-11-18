const NON_SLUG_CHARACTERS = /[^a-z0-9]+/g
const EDGE_HYPHENS = /^-+|-+$/g

// Đồng bộ với CommunitySlug.java để URL luôn khớp slug thật của community.
export function toCommunitySlug(value) {
  if (typeof value !== 'string') {
    return ''
  }

  return value
    .normalize('NFD')
    .replace(/\p{M}+/gu, '')
    .toLowerCase()
    .replace(/đ/g, 'd')
    .replace(NON_SLUG_CHARACTERS, '-')
    .replace(EDGE_HYPHENS, '')
}
