<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Avatar from '../../components/common/Avatar.vue'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { describeApiError } from '../../config/apiClient'
import CommunityForm from '../../features/community/components/CommunityForm.vue'
import DeleteCommunityDialog from '../../features/community/components/DeleteCommunityDialog.vue'
import RoleManager from '../../features/community/components/RoleManager.vue'
import { deleteCommunity, fetchCommunity, updateCommunity } from '../../features/community/communityApi'

const SECTIONS = [
  { id: 'general', label: 'General' },
  { id: 'roles', label: 'Roles' },
  { id: 'danger', label: 'Danger zone' },
]

const route = useRoute()
const router = useRouter()
const communityName = route.params.name

const activeSection = ref('general')
const isDirty = ref(false)
const formVersion = ref(0)
const community = ref(null)
const loadError = ref('')
const isSaving = ref(false)
const formError = ref('')
const isDeleteDialogOpen = ref(false)
const isDeleting = ref(false)
const deleteError = ref('')

onMounted(async () => {
  try {
    const loadedCommunity = await fetchCommunity(communityName)
    if (!loadedCommunity.isOwner) {
      router.replace(`/community/${communityName}`)
      return
    }
    community.value = loadedCommunity
  } catch (error) {
    loadError.value = describeApiError(error).message
  }
})

function markDirty() {
  formError.value = ''
  isDirty.value = true
}

// Đổi key để dựng lại form từ giá trị đã lưu, bỏ mọi thay đổi chưa lưu.
function discardChanges() {
  formVersion.value++
  isDirty.value = false
  formError.value = ''
}

async function handleSave({ name, shoutout, isPublic, imageUrl, bannerUrl }) {
  isSaving.value = true
  formError.value = ''
  try {
    const savedCommunity = await updateCommunity(communityName, { name, shoutout, isPublic, imageUrl, bannerUrl })
    router.push(`/community/${savedCommunity.name}`)
  } catch (error) {
    formError.value = describeApiError(error).message
  } finally {
    isSaving.value = false
  }
}

async function handleDelete(confirmedName) {
  isDeleting.value = true
  deleteError.value = ''
  try {
    await deleteCommunity(communityName, confirmedName)
    router.push('/communities')
  } catch (error) {
    deleteError.value = describeApiError(error).message
    isDeleting.value = false
  }
}

function closeDeleteDialog() {
  isDeleteDialogOpen.value = false
  deleteError.value = ''
}
</script>

<template>
  <UserNavbar />
  <main class="page">
    <RouterLink class="back" :to="`/community/${communityName}`">&larr; Back to c/{{ communityName }}</RouterLink>
    <div class="title">
      <Avatar :src="community?.imageUrl" :name="communityName" :size="48" :show-name="false" />
      <div>
        <h1><span>c/</span>{{ communityName }}</h1>
        <p>Community settings</p>
      </div>
    </div>

    <p v-if="loadError" class="load-error" role="alert">{{ loadError }}</p>

    <div v-else-if="community" class="layout">
      <nav class="menu" aria-label="Settings sections">
        <template v-for="section in SECTIONS" :key="section.id">
          <div v-if="section.id === 'danger'" class="separator"></div>
          <button type="button" :class="{ bad: section.id === 'danger' }" :aria-current="activeSection === section.id" @click="activeSection = section.id">{{ section.label }}</button>
        </template>
      </nav>

      <div>
        <section v-show="activeSection === 'general'" class="panel" aria-labelledby="general-heading">
          <header>
            <h2 id="general-heading">General</h2>
            <p>Name, description, visibility, image and banner.</p>
          </header>
          <div class="card">
            <CommunityForm
              :key="formVersion"
              form-id="community-settings-form"
              :initial-values="community"
              submit-label="Save changes"
              hide-submit
              :busy="isSaving"
              :form-error="formError"
              @submit="handleSave"
              @edit="markDirty"
            />
          </div>
        </section>

        <RoleManager v-if="activeSection === 'roles'" :owner-count="1" />

        <section v-show="activeSection === 'danger'" class="panel" aria-labelledby="danger-heading">
          <header>
            <h2 id="danger-heading">Danger zone</h2>
            <p>These actions cannot be undone.</p>
          </header>
          <div class="card danger-zone">
            <div class="danger-row">
              <div>
                <h3>Delete this community</h3>
                <p>Permanently removes its members, posts, puzzles, contests and everything else in it.</p>
              </div>
              <button type="button" class="btn delete" @click="isDeleteDialogOpen = true">Delete community</button>
            </div>
          </div>
        </section>
      </div>
    </div>

    <div class="save-bar" :class="{ visible: isDirty && activeSection === 'general' }" role="region" aria-label="Unsaved changes" :inert="!(isDirty && activeSection === 'general')">
      <div class="save-bar-inner">
        <span>You have unsaved changes.</span>
        <button type="button" class="btn" :disabled="isSaving" @click="discardChanges">Discard</button>
        <button type="submit" form="community-settings-form" class="btn p" :disabled="isSaving" :aria-busy="isSaving">
          <span v-if="isSaving" class="spinner" aria-hidden="true"></span>
          Save changes
        </button>
      </div>
    </div>

    <DeleteCommunityDialog
      v-if="isDeleteDialogOpen"
      :community-name="communityName"
      :busy="isDeleting"
      :error-message="deleteError"
      @confirm="handleDelete"
      @cancel="closeDeleteDialog"
    />
  </main>
</template>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
  padding: var(--s-5) var(--s-5) 120px;
}

.back {
  font-size: var(--t-sm);
  color: var(--tx2);
}

.back:hover {
  color: var(--tx);
}

.title {
  display: flex;
  gap: var(--s-4);
  align-items: center;
  margin: var(--s-4) 0 var(--s-5);
}

h1 {
  font: 600 26px / 1.2 var(--f-head);
  letter-spacing: -0.025em;
}

h1 span {
  font-family: var(--f-mono);
  font-weight: 400;
  color: var(--tx2);
}

.title p {
  font-size: var(--t-sm);
  color: var(--tx2);
}

.layout {
  display: grid;
  grid-template-columns: 200px minmax(0, 1fr);
  gap: var(--s-6);
  align-items: start;
}

.menu {
  position: sticky;
  top: 84px;
  display: grid;
  gap: 2px;
}

.menu button {
  height: 38px;
  padding: 0 var(--s-3);
  font-size: var(--t-sm);
  font-weight: 500;
  color: var(--tx2);
  text-align: left;
  border-radius: var(--r-sm);
}

.menu button:hover {
  color: var(--tx);
  background: var(--s2);
}

.menu button[aria-current='true'] {
  color: var(--tx);
  background: var(--s2);
  box-shadow: inset 2px 0 0 var(--ac);
}

.menu button.bad:hover,
.menu button.bad[aria-current='true'] {
  color: var(--bad);
}

.separator {
  height: 1px;
  margin: var(--s-2) 0;
  background: var(--line);
}

.panel {
  display: grid;
  gap: var(--s-4);
}

.panel > header h2 {
  font: 600 20px / 1.3 var(--f-head);
  letter-spacing: -0.02em;
}

.panel > header p {
  margin-top: 2px;
  font-size: var(--t-sm);
  color: var(--tx2);
}

.card {
  padding: var(--s-5);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
}

.danger-zone {
  border-color: var(--bad);
}

.danger-row {
  display: flex;
  gap: var(--s-5);
  align-items: center;
  justify-content: space-between;
}

.danger-row h3 {
  font: 600 var(--t-md) var(--f-head);
  color: var(--bad);
}

.danger-row p {
  max-width: 46ch;
  font-size: var(--t-sm);
  color: var(--tx2);
}

.btn.delete {
  flex: none;
  color: var(--bad);
  background: transparent;
  border: 1px solid var(--bad);
}

.btn.delete:hover {
  color: var(--bg);
  background: var(--bad);
}

.load-error {
  color: var(--bad);
}

.save-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 30;
  background: color-mix(in srgb, var(--s1) 94%, transparent);
  border-top: 1px solid var(--line2);
  backdrop-filter: blur(8px);
  transform: translateY(110%);
  transition: transform 0.25s var(--ease);
}

.save-bar.visible {
  transform: none;
}

.save-bar-inner {
  display: flex;
  gap: var(--s-3);
  align-items: center;
  justify-content: flex-end;
  max-width: 1100px;
  margin: 0 auto;
  padding: var(--s-3) var(--s-5);
}

.save-bar-inner span {
  margin-right: auto;
  font-size: var(--t-sm);
  color: var(--tx2);
}

@media (max-width: 820px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .menu {
    position: static;
    display: flex;
    overflow-x: auto;
  }

  .menu button {
    flex: none;
  }

  .separator {
    display: none;
  }

  .danger-row {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (prefers-reduced-motion: reduce) {
  .save-bar {
    transition: none;
  }
}
</style>
