<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../../stores/authStore'
import AppLogo from '../../AppLogo.vue'
import ThemeToggle from '../../ThemeToggle.vue'
import NavSearch from '../NavSearch.vue'

const authStore = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <nav class="navbar">
    <div class="navbar-inner">
      <AppLogo />
      <NavSearch />
      <ul class="links">
        <li><RouterLink :to="{ path: '/', hash: '#solve' }">Solve</RouterLink></li>
        <li><RouterLink :to="{ path: '/', hash: '#compete' }">Compete</RouterLink></li>
        <li><RouterLink :to="{ path: '/', hash: '#communities' }">Communities</RouterLink></li>
      </ul>
      <div class="actions">
        <ThemeToggle />
        <span class="member-name hide-on-phone">{{ authStore.profile?.name }}</span>
        <button class="btn" type="button" @click="handleLogout">Log out</button>
      </div>
    </div>
  </nav>
</template>

<style scoped src="../navbar.css"></style>
