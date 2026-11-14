import { createRouter, createWebHistory } from 'vue-router'
import { redirectAuthenticatedGuests } from './guards/authGuard'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('../pages/guest/LandingPage.vue') },
    { path: '/login', component: () => import('../pages/guest/LoginPage.vue'), meta: { guestOnly: true } },
    { path: '/puzzles', name: 'puzzles', component: () => import('../pages/user/PuzzlePage.vue') },
    { path: '/puzzle/:id', name: 'puzzle-solve', component: () => import('../pages/user/PuzzleSolvePage.vue') },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior(destination) {
    return destination.hash ? { el: destination.hash } : { top: 0 }
  },
})

router.beforeEach(redirectAuthenticatedGuests)
