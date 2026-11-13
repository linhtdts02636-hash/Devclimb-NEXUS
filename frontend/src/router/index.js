import { createRouter, createWebHistory } from 'vue-router'
import { LANDING_PAGE_PATH, LOGIN_PAGE_PATH } from '../config/applicationPaths'
import { redirectAuthenticatedGuests } from './guards/authGuard'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: LANDING_PAGE_PATH, component: () => import('../pages/guest/LandingPage.vue') },
    { path: LOGIN_PAGE_PATH, component: () => import('../pages/guest/LoginPage.vue'), meta: { guestOnly: true } },
    { path: '/:pathMatch(.*)*', redirect: LANDING_PAGE_PATH },
  ],
  scrollBehavior(destination) {
    return destination.hash ? { el: destination.hash } : { top: 0 }
  },
})

router.beforeEach(redirectAuthenticatedGuests)
