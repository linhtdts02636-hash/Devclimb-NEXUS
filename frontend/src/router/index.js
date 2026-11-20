import { createRouter, createWebHistory } from 'vue-router'
import { checkRouteAccess } from './guards/authGuard'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('../pages/guest/LandingPage.vue') },
    { path: '/login', component: () => import('../pages/guest/LoginPage.vue'), meta: { guestOnly: true } },
    { path: '/puzzles', name: 'puzzles', component: () => import('../pages/user/PuzzlePage.vue'), meta: { requiresAuth: true } },
    { path: '/puzzle/:id', name: 'puzzle-solve', component: () => import('../pages/user/PuzzleSolvePage.vue'), meta: { requiresAuth: true } },
    { path: '/communities', component: () => import('../pages/user/MyCommunitiesPage.vue'), meta: { requiresAuth: true } },
    { path: '/communities/new', component: () => import('../pages/user/CreateCommunityPage.vue'), meta: { requiresAuth: true } },
    // Route public: guest xem được community public, guard vẫn khởi tạo auth.
    { path: '/community/:name', component: () => import('../pages/user/CommunityPage.vue'), meta: { public: true } },
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

router.beforeEach(checkRouteAccess)
