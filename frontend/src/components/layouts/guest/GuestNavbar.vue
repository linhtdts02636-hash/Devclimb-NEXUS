<script setup>
import { useRoute } from 'vue-router'
import AppLogo from '../../AppLogo.vue'
import ThemeToggle from '../../ThemeToggle.vue'
import NavSearch from '../NavSearch.vue'

const route = useRoute()
</script>

<template>
  <nav class="navbar">
    <div class="navbar-inner">
      <AppLogo />
      <NavSearch />
      <ul class="links">
        <li><RouterLink :to="{ path: '/', hash: '#solve' }">Solve</RouterLink></li>
        <li><RouterLink :to="{ path: '/', hash: '#judge' }">Judge</RouterLink></li>
        <li><RouterLink :to="{ path: '/', hash: '#compete' }">Compete</RouterLink></li>
        <li><RouterLink :to="{ path: '/', hash: '#communities' }">Communities</RouterLink></li>
        <li><RouterLink :to="{ path: '/', hash: '#faq' }">FAQ</RouterLink></li>
      </ul>
      <div class="actions">
        <ThemeToggle />
        <!-- Đang ở trang đăng nhập thì không cần hai nút dẫn tới chính trang này. -->
        <template v-if="route.path !== '/login'">
          <RouterLink class="btn hide-on-phone" to="/login">Log in</RouterLink>
          <RouterLink class="btn p" to="/login">Start climbing</RouterLink>
        </template>
      </div>
    </div>
  </nav>
</template>

<style scoped src="../navbar.css"></style>
