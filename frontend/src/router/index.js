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
import { checkRouteAccess } from './guards/authGuard'

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
    { path: '/puzzles', name: 'puzzles', component: () => import('../pages/user/PuzzlePage.vue') },
    { path: '/puzzle/:id', name: 'puzzle-solve', component: () => import('../pages/user/PuzzleSolvePage.vue') },
    { path: '/communities', component: () => import('../pages/user/MyCommunitiesPage.vue'), meta: { requiresAuth: true } },
    { path: '/communities/new', component: () => import('../pages/user/CreateCommunityPage.vue'), meta: { requiresAuth: true } },
    { path: '/community/:name', component: () => import('../pages/user/CommunityPage.vue'), meta: { requiresAuth: true } },
    { path: '/community/:name/settings', component: () => import('../pages/user/CommunitySettingsPage.vue'), meta: { requiresAuth: true } },
    { path: '/resources/:slug', component: () => import('../pages/user/ResourcePage.vue'), meta: { requiresAuth: true } },
    { path: '/c/:name', redirect: (destination) => `/community/${destination.params.name}` },
    { path: '/c/:name/settings', redirect: (destination) => `/community/${destination.params.name}/settings` },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior(destination) {
    return destination.hash ? { el: destination.hash } : { top: 0 }
  },
})

router.beforeEach(redirectToCanonicalCommunitySlug)
router.beforeEach(redirectAuthenticatedGuests)
router.beforeEach(checkRouteAccess)
