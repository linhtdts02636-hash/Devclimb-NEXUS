import { createRouter, createWebHistory } from 'vue-router'
import { redirectAuthenticatedGuests } from './guards/authGuard'
import { redirectToCanonicalCommunitySlug } from './guards/communitySlugGuard'
import { getCommunityById } from '../features/community/api/communityApi'

async function redirectLegacyCommunityFeed(to) {
  const communityId = Number(to.params.communityId)

  if (!Number.isInteger(communityId) || communityId <= 0) {
    return { path: '/' }
  }

  try {
    const community = await getCommunityById(communityId)
    return { name: 'community-feed', params: { slug: community.slug }, replace: true }
  } catch {
    return { path: '/' }
  }
}

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('../pages/guest/LandingPage.vue') },
    { path: '/login', component: () => import('../pages/guest/LoginPage.vue'), meta: { guestOnly: true } },
    {
      name: 'community-feed',
      path: '/community/:slug',
      component: () => import('../pages/user/CommunityFeedPage.vue'),
      props: true,
    },
    {
      // Route cũ chỉ còn nhiệm vụ chuyển hướng sang URL slug mới.
      path: '/communities/:communityId/posts',
      component: () => import('../pages/user/CommunityFeedPage.vue'),
      beforeEnter: redirectLegacyCommunityFeed,
    },
    // Trang kiểm tra giao diện Post.vue bằng dữ liệu giả; chỉ tồn tại ở development.
    ...(import.meta.env.DEV
      ? [
          {
            name: 'post-mock-gallery',
            path: '/dev/post-mock-gallery',
            component: () => import('../pages/dev/PostMockGallery.vue'),
          },
        ]
      : []),
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior(destination) {
    return destination.hash ? { el: destination.hash } : { top: 0 }
  },
})

router.beforeEach(redirectToCanonicalCommunitySlug)
router.beforeEach(redirectAuthenticatedGuests)
