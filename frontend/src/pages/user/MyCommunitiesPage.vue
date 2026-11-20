<script setup>
import { onMounted, ref } from 'vue'
import Avatar from '../../components/common/Avatar.vue'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { describeApiError } from '../../config/apiClient'
import { fetchMyCommunities } from '../../features/community/communityApi'

const communities = ref([])
const isLoading = ref(true)
const errorMessage = ref('')

onMounted(async () => {
  try {
    communities.value = await fetchMyCommunities()
  } catch (error) {
    errorMessage.value = describeApiError(error).message
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <UserNavbar />
  <main class="page">
    <header class="header">
      <div>
        <h1>Communities</h1>
        <p>The communities you belong to.</p>
      </div>
      <RouterLink class="btn p" to="/communities/new">Create community</RouterLink>
    </header>

    <ul v-if="isLoading" class="list" aria-busy="true" aria-label="Loading communities">
      <li v-for="placeholder in 3" :key="placeholder" class="skeleton"></li>
    </ul>

    <p v-else-if="errorMessage" class="state error" role="alert">{{ errorMessage }}</p>

    <div v-else-if="!communities.length" class="state empty">
      <h2>You are not in a community yet</h2>
      <p>Create one to start a space for your own problems, contests and members.</p>
      <RouterLink class="btn p" to="/communities/new">Create your first community</RouterLink>
    </div>

    <ul v-else class="list">
      <li v-for="community in communities" :key="community.id">
        <RouterLink class="card" :to="`/community/${community.name}`">
          <Avatar :src="community.imageUrl" :name="community.name" :size="48" :show-name="false" :clickable="false" />
          <span class="info">
            <b>{{ community.name }}</b>
            <small>{{ community.memberCount }} {{ community.memberCount === 1 ? 'member' : 'members' }}</small>
          </span>
          <span v-if="community.isOwner" class="badge">owner</span>
          <span v-if="!community.isPublic" class="badge">private</span>
        </RouterLink>
      </li>
    </ul>
  </main>
</template>

<style scoped>
.page {
  max-width: 880px;
  margin: 0 auto;
  padding: var(--s-6) var(--s-5) var(--s-8);
}

.header {
  display: flex;
  gap: var(--s-4);
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: var(--s-5);
}

h1 {
  font: 600 26px / 1.2 var(--f-head);
  letter-spacing: -0.025em;
}

.header p {
  color: var(--tx2);
}

.list {
  display: grid;
  gap: var(--s-2);
  margin: 0;
  padding: 0;
  list-style: none;
}

.card {
  display: flex;
  gap: var(--s-4);
  align-items: center;
  padding: var(--s-4);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
  transition: border-color 0.2s var(--ease);
}

.card:hover {
  border-color: var(--ac);
}

.info {
  display: grid;
  flex: 1;
  min-width: 0;
}

.info small {
  font: 400 var(--t-xs) var(--f-mono);
  color: var(--tx2);
}

.badge {
  padding: 1px var(--s-2);
  font: 400 var(--t-xs) var(--f-mono);
  color: var(--tx2);
  background: var(--s2);
  border-radius: 999px;
}

.skeleton {
  height: 82px;
  background: var(--s2);
  border-radius: var(--r);
}

.state {
  display: grid;
  gap: var(--s-3);
  justify-items: center;
  padding: var(--s-7) var(--s-5);
  text-align: center;
  color: var(--tx2);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
}

.state h2 {
  font: 500 var(--t-lg) var(--f-head);
  color: var(--tx);
}

.state.error {
  color: var(--bad);
}

@media (max-width: 560px) {
  .header {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
