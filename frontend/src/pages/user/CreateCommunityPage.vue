<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { describeApiError } from '../../config/apiClient'
import CommunityForm from '../../features/community/components/CommunityForm.vue'
import { createCommunity } from '../../features/community/communityApi'

const NAME_ERROR_CODES = ['COMMUNITY_NAME_TAKEN', 'COMMUNITY_NAME_INVALID']

const router = useRouter()
const isBusy = ref(false)
const nameError = ref('')
const formError = ref('')

function clearErrors() {
  nameError.value = ''
  formError.value = ''
}

async function handleSubmit(community) {
  isBusy.value = true
  clearErrors()
  try {
    const createdCommunity = await createCommunity(community)
    router.push(`/community/${createdCommunity.name}`)
  } catch (error) {
    const { code, message } = describeApiError(error)
    if (NAME_ERROR_CODES.includes(code)) nameError.value = message
    else formError.value = message
  } finally {
    isBusy.value = false
  }
}
</script>

<template>
  <UserNavbar />
  <main class="page">
    <RouterLink class="back" to="/communities">&larr; Communities</RouterLink>
    <h1>Create a community</h1>
    <p class="lead">You become its owner and can edit or delete it later.</p>
    <div class="card">
      <CommunityForm
        submit-label="Create community"
        :busy="isBusy"
        :name-error="nameError"
        :form-error="formError"
        @submit="handleSubmit"
        @edit="clearErrors"
      />
    </div>
  </main>
</template>

<style scoped>
.page {
  max-width: 640px;
  margin: 0 auto;
  padding: var(--s-6) var(--s-5) var(--s-8);
}

.back {
  font-size: var(--t-sm);
  color: var(--tx2);
}

.back:hover {
  color: var(--tx);
}

h1 {
  margin-top: var(--s-4);
  font: 600 26px / 1.2 var(--f-head);
  letter-spacing: -0.025em;
}

.lead {
  margin: var(--s-1) 0 var(--s-5);
  color: var(--tx2);
}

.card {
  padding: var(--s-5);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-lg);
}
</style>
