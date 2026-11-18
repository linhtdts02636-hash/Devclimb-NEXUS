import { toCommunitySlug } from '../../features/community/utils/communitySlug'

// Chuyển URL có dấu cách/viết hoa về đúng slug chuẩn, ví dụ:
// /community/Post%20Mock%20Test -> /community/post-mock-test
export function redirectToCanonicalCommunitySlug(to) {
  if (to.name !== 'community-feed') {
    return true
  }

  const rawSlug = to.params.slug
  if (typeof rawSlug !== 'string') {
    return true
  }

  const canonicalSlug = toCommunitySlug(rawSlug)

  if (!canonicalSlug || canonicalSlug === rawSlug) {
    return true
  }

  return {
    name: 'community-feed',
    params: { slug: canonicalSlug },
    replace: true,
  }
}
