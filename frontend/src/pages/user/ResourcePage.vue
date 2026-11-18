<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { RESOURCES } from '../../features/community/resources'

const route = useRoute()
const resource = computed(() => RESOURCES.find((item) => item.slug === route.params.slug))
</script>

<template>
  <UserNavbar />
  <main class="page">
    <h1>{{ resource ? resource.label : 'Page not found' }}</h1>
    <p>{{ resource ? 'This page is coming soon.' : 'There is no such page.' }}</p>
    <RouterLink class="btn" to="/communities">Back to communities</RouterLink>
  </main>
</template>

<style scoped>
.page {
  display: grid;
  gap: var(--s-3);
  justify-items: start;
  max-width: 640px;
  margin: 0 auto;
  padding: var(--s-8) var(--s-5);
}

p {
  color: var(--tx2);
}
</style>
