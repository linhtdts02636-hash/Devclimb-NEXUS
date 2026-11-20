<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Avatar from '../../components/common/Avatar.vue'
import BaseModal from '../../components/common/BaseModal.vue'
import PostComposerModal from '../../features/post/components/PostComposerModal.vue'
import PostList from '../../features/post/components/PostList.vue'
import GuestNavbar from '../../components/layouts/guest/GuestNavbar.vue'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { describeApiError } from '../../config/apiClient'
import { fetchCommunity, fetchMembers, joinCommunity, leaveCommunity } from '../../features/community/communityApi'
import CommunityHeaderActions from '../../features/community/components/CommunityHeaderActions.vue'
import CommunitySideCards from '../../features/community/components/CommunitySideCards.vue'
import CommunitySidebar from '../../features/community/components/CommunitySidebar.vue'
import PinnedPosts from '../../features/community/components/PinnedPosts.vue'
import RoleList from '../../features/community/components/RoleList.vue'
import { deletePost, getPosts } from '../../features/post/postApi'
import { samplePosts } from '../../sample/sampleCommunityData'
import { useAuthStore } from '../../stores/authStore'

const route = useRoute()
const authStore = useAuthStore()
const dateFormat = new Intl.DateTimeFormat('en', { year: 'numeric', month: 'short', day: 'numeric' })
const pinnedPosts = samplePosts.filter((post) => post.isPinned)

const ALL_TABS = [
  { id: 'posts', label: 'Posts' },
  { id: 'puzzles', label: 'Puzzles' },
  { id: 'contests', label: 'Contests' },
  { id: 'members', label: 'Members' },
]

const community = ref(null)
const members = ref([])
const loadStatus = ref('loading') // loading | ready | missing | failed
const loadError = ref('')
const activeTab = ref('posts')
const isMembershipBusy = ref(false)
const actionError = ref('')

const posts = ref([])
const postsLoading = ref(false)
const postsError = ref('')
const composerOpen = ref(false)
const editingPost = ref(null)
const deleteOpen = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)
const deleteError = ref('')
const toast = ref('')
let toastTimer = null

const owners = computed(() => members.value.filter((member) => member.isOwner))
const currentUserId = computed(() => authStore.profile?.id ?? null)
const isAuthenticated = computed(() => authStore.status === 'authenticated')
const isMember = computed(() => Boolean(community.value?.isMember))
const canPost = computed(() => isAuthenticated.value && isMember.value && Number.isInteger(community.value?.id))
// Guest chỉ xem bài post, không xem danh sách thành viên (API members vẫn yêu cầu đăng nhập).
const tabs = computed(() => (
  isAuthenticated.value ? ALL_TABS : ALL_TABS.filter((tab) => tab.id !== 'members')
))

async function loadFeed() {
  if (!Number.isInteger(community.value?.id)) {
    posts.value = []
    postsLoading.value = false
    return
  }

  postsLoading.value = true
  postsError.value = ''
  try {
    posts.value = await getPosts(community.value.id)
  } catch (error) {
    posts.value = []
    postsError.value = describeApiError(error).message
  } finally {
    postsLoading.value = false
  }
}

async function loadCommunity() {
  loadStatus.value = 'loading'
  try {
    // Hai yêu cầu độc lập nên gửi song song, không chờ nhau. Guest không gọi API members.
    const membersRequest = isAuthenticated.value ? fetchMembers(route.params.name) : Promise.resolve([])
    const [loadedCommunity, loadedMembers] = await Promise.all([fetchCommunity(route.params.name), membersRequest])
    community.value = loadedCommunity
    members.value = loadedMembers
    loadStatus.value = 'ready'
    await loadFeed()
  } catch (error) {
    loadStatus.value = error.response?.status === 404 ? 'missing' : 'failed'
    loadError.value = describeApiError(error).message
  }
}

function moveTabWithArrowKeys(event) {
  if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') return
  event.preventDefault()
  const step = event.key === 'ArrowRight' ? 1 : -1
  const currentIndex = tabs.value.findIndex((tab) => tab.id === activeTab.value)
  activeTab.value = tabs.value[(currentIndex + step + tabs.value.length) % tabs.value.length].id
  document.getElementById(`tab-${activeTab.value}`)?.focus()
}

async function changeMembership(action) {
  isMembershipBusy.value = true
  actionError.value = ''
  try {
    await action(route.params.name)
    await loadCommunity()
  } catch (error) {
    actionError.value = describeApiError(error).message
  } finally {
    isMembershipBusy.value = false
  }
}

function openComposer() {
  if (!canPost.value) return
  editingPost.value = null
  composerOpen.value = true
}

function handleEdit(post) {
  if (post?.author?.userId !== currentUserId.value) return
  editingPost.value = post
  composerOpen.value = true
}

function handleCreated(post) {
  posts.value = [post, ...posts.value]
  showToast('Post created.')
}

function handleUpdated(updatedPost) {
  posts.value = posts.value.map((post) => (post.id === updatedPost.id ? updatedPost : post))
  showToast('Post updated.')
}

function requestDelete(post) {
  if (post?.author?.userId !== currentUserId.value) return
  deleteTarget.value = post
  deleteError.value = ''
  deleteOpen.value = true
}

function resetDeleteConfirm() {
  deleteOpen.value = false
  deleteTarget.value = null
  deleteError.value = ''
}

function closeDeleteConfirm() {
  if (deleting.value) return
  resetDeleteConfirm()
}

function handleDeleteOpenChange(open) {
  if (!open) closeDeleteConfirm()
}

async function confirmDelete() {
  if (!deleteTarget.value || deleting.value) return

  deleting.value = true
  deleteError.value = ''
  try {
    const postId = deleteTarget.value.id
    await deletePost(postId)
    posts.value = posts.value.filter((post) => post.id !== postId)
    resetDeleteConfirm()
    showToast('Post deleted.')
  } catch (error) {
    deleteError.value = describeApiError(error).message
  } finally {
    deleting.value = false
  }
}

function showToast(message) {
  toast.value = message
  if (toastTimer) window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toast.value = ''
  }, 5000)
}

watch(
  () => route.params.name,
  () => {
    activeTab.value = 'posts'
    actionError.value = ''
    composerOpen.value = false
    editingPost.value = null
    resetDeleteConfirm()
    loadCommunity()
  },
  { immediate: true },
)

onMounted(() => {
  authStore.initialize()
})

onBeforeUnmount(() => {
  if (toastTimer) window.clearTimeout(toastTimer)
})
</script>

<template>
  <UserNavbar v-if="isAuthenticated" />
  <GuestNavbar v-else />
  <div class="shell">
    <CommunitySidebar
      class="left"
      :current-community="community"
      :is-authenticated="isAuthenticated"
    />

    <main class="content">
      <div v-if="loadStatus === 'loading'" aria-busy="true" aria-label="Loading community">
        <div class="skeleton banner"></div>
        <div class="skeleton line"></div>
      </div>

      <div v-else-if="loadStatus !== 'ready'" class="state" role="alert">
        <h1>{{ loadStatus === 'missing' ? 'Community not found' : 'Could not load this community' }}</h1>
        <p>{{ loadStatus === 'missing' ? 'It may not exist, or it may be private.' : loadError }}</p>
        <RouterLink class="btn" :to="isAuthenticated ? '/communities' : '/'">
          {{ isAuthenticated ? 'Back to communities' : 'Back to home' }}
        </RouterLink>
      </div>

      <template v-else>
        <div class="banner">
          <img v-if="community.bannerUrl" :src="community.bannerUrl" alt="" />
          <svg v-else viewBox="0 0 1200 176" preserveAspectRatio="none" aria-hidden="true">
            <path d="M0 176V124l160-52 140 42 180-72 200 84 150-42 190 62 180-32v62z" fill="var(--s3)" opacity=".55" />
            <path d="M0 176V146l200-42 160 32 220-62 180 72 170-32 270 52v10z" fill="var(--ac)" opacity=".16" />
          </svg>
        </div>

        <header class="header">
          <Avatar class="community-avatar" :src="community.imageUrl" :name="community.name" :size="96" :show-name="false" />
          <div class="identity">
            <h1><span class="prefix">c/</span>{{ community.name }}</h1>
            <p class="meta">
              <span class="badge">{{ community.isPublic ? 'public' : 'private' }}</span>
              <span><b>{{ community.memberCount }}</b> {{ community.memberCount === 1 ? 'member' : 'members' }}</span>
            </p>
          </div>
          <CommunityHeaderActions
            :community="community"
            :active-tab="activeTab"
            :busy="isMembershipBusy"
            :is-authenticated="isAuthenticated"
            @join="changeMembership(joinCommunity)"
            @leave="changeMembership(leaveCommunity)"
            @create="openComposer"
          />
        </header>
        <p v-if="actionError" class="action-error" role="alert">{{ actionError }}</p>

        <div class="layout">
          <section class="feed">
            <div class="tabs" role="tablist" aria-label="Community sections" @keydown="moveTabWithArrowKeys">
              <button v-for="tab in tabs" :id="`tab-${tab.id}`" :key="tab.id" type="button" role="tab" :aria-selected="activeTab === tab.id" :tabindex="activeTab === tab.id ? 0 : -1" @click="activeTab = tab.id">{{ tab.label }}</button>
            </div>

            <div v-if="activeTab === 'posts'" role="tabpanel" aria-labelledby="tab-posts" class="posts">
              <PinnedPosts :posts="pinnedPosts" />
              <PostList
                :posts="posts"
                :loading="postsLoading"
                :error="postsError"
                :current-user-id="currentUserId"
                @retry="loadFeed"
                @edit="handleEdit"
                @delete="requestDelete"
              />
            </div>

            <div v-else-if="activeTab === 'puzzles' || activeTab === 'contests'" role="tabpanel" :aria-labelledby="`tab-${activeTab}`" class="empty-tab">
              <p>{{ activeTab === 'puzzles' ? 'Puzzles' : 'Contests' }} for this community are coming soon.</p>
            </div>

            <ul v-else-if="isAuthenticated" class="members" role="tabpanel" aria-labelledby="tab-members">
              <li v-for="member in members" :key="member.userId">
                <Avatar :src="member.imageUrl" :name="member.name" :size="36" :round="true" :show-name="false" />
                <b>{{ member.name }}</b>
                <span v-if="member.isOwner" class="badge">owner</span>
                <small>Joined {{ dateFormat.format(new Date(member.joinedAt)) }}</small>
              </li>
            </ul>
          </section>

          <aside class="side" aria-label="About this community">
            <section class="card">
              <h2>About</h2>
              <p>{{ community.shoutout || 'This community has no description yet.' }}</p>
              <p class="created">Created {{ dateFormat.format(new Date(community.createdAt)) }}</p>
            </section>

            <section v-if="isAuthenticated" class="card">
              <h2>Moderators</h2>
              <ul class="mods">
                <li v-for="owner in owners" :key="owner.userId">
                  <Avatar :src="owner.imageUrl" :name="owner.name" :size="28" :round="true" :show-name="false" />
                  {{ owner.name }}
                  <svg class="crown" viewBox="0 0 24 24" aria-label="Owner"><path d="M3 18 5 7l5 5 2-7 2 7 5-5 2 11z" /></svg>
                </li>
              </ul>
            </section>

            <RoleList v-if="isAuthenticated" :owners="owners" />
            <CommunitySideCards />
            <p class="sample-note">Roles, contest, solvers and topics are sample data for illustration</p>
          </aside>
        </div>
      </template>
    </main>
  </div>

  <PostComposerModal
    v-if="community"
    v-model:open="composerOpen"
    :community-id="community.id"
    :post="editingPost"
    @created="handleCreated"
    @updated="handleUpdated"
  />

  <BaseModal
    :open="deleteOpen"
    title="Delete post"
    size="sm"
    :close-on-backdrop="!deleting"
    @update:open="handleDeleteOpenChange"
  >
    <p class="confirm-text">Are you sure you want to delete this post? This cannot be undone.</p>
    <p v-if="deleteTarget?.title" class="confirm-title">"{{ deleteTarget.title }}"</p>
    <p v-if="deleteError" class="confirm-error" role="alert">{{ deleteError }}</p>

    <template #footer>
      <button type="button" class="btn" :disabled="deleting" @click="closeDeleteConfirm">Cancel</button>
      <button type="button" class="btn btn--danger" :disabled="deleting" @click="confirmDelete">
        <span v-if="deleting" class="spinner" aria-hidden="true"></span>
        {{ deleting ? 'Deleting…' : 'Delete post' }}
      </button>
    </template>
  </BaseModal>

  <Transition name="toast">
    <div v-if="toast" class="toast" role="status">{{ toast }}</div>
  </Transition>
</template>

<style scoped src="./CommunityPage.css"></style>
