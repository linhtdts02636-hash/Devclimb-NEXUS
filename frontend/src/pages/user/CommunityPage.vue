<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Avatar from '../../components/common/Avatar.vue'
import BaseModal from '../../components/common/BaseModal.vue'
import PostComposerModal from '../../features/post/components/PostComposerModal.vue'
import PostList from '../../features/post/components/PostList.vue'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { describeApiError } from '../../config/apiClient'
import { fetchCommunity, fetchMembers, joinCommunity, leaveCommunity } from '../../features/community/communityApi'
import CommunityHeaderActions from '../../features/community/components/CommunityHeaderActions.vue'
import CommunitySideCards from '../../features/community/components/CommunitySideCards.vue'
import CommunitySidebar from '../../features/community/components/CommunitySidebar.vue'
import PinnedPosts from '../../features/community/components/PinnedPosts.vue'
import RoleList from '../../features/community/components/RoleList.vue'
import { deletePost, getPinnedPosts, getPosts, setPostPinned } from '../../features/post/postApi'
import { useAuthStore } from '../../stores/authStore'

const route = useRoute()
const authStore = useAuthStore()
const dateFormat = new Intl.DateTimeFormat('en', { year: 'numeric', month: 'short', day: 'numeric' })

const TABS = [
  { id: 'posts', label: 'Posts' },
  { id: 'puzzles', label: 'Puzzles' },
  { id: 'contests', label: 'Contests' },
]

const community = ref(null)
const members = ref([])
const loadStatus = ref('loading') // loading | ready | missing | failed
const loadError = ref('')
const activeTab = ref('posts')
const isMembershipBusy = ref(false)
const actionError = ref('')

const posts = ref([])
const pinnedPosts = ref([])
const nextCursor = ref(null)
const postsLoading = ref(false)
const postsError = ref('')
const loadingMore = ref(false)
const loadMoreError = ref('')
const feedEnd = ref(null)
const composerOpen = ref(false)
const editingPost = ref(null)
const deleteOpen = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)
const deleteError = ref('')
const toast = ref('')
let toastTimer = null

// Chủ cộng đồng, cùng bất kỳ ai có vai trò cấp quyền Pin posts, Remove posts and comments hoặc Manage members.
const moderators = computed(() => members.value.filter((member) => member.isModerator))
const canPinPosts = computed(() => Boolean(community.value?.permissions?.includes('PIN_POSTS')))
const canRemoveContent = computed(() => Boolean(community.value?.permissions?.includes('REMOVE_CONTENT')))
const canManageMembers = computed(() => Boolean(community.value?.permissions?.includes('MANAGE_MEMBERS')))
const currentUserId = computed(() => authStore.profile?.id ?? null)
const isAuthenticated = computed(() => authStore.status === 'authenticated')
const isMember = computed(() => Boolean(community.value?.isMember))
const canPost = computed(() => isAuthenticated.value && isMember.value && Number.isInteger(community.value?.id))

const byNewest = (first, second) => second.id - first.id

async function loadFeed() {
  posts.value = []
  pinnedPosts.value = []
  nextCursor.value = null
  loadMoreError.value = ''
  if (!Number.isInteger(community.value?.id)) {
    postsLoading.value = false
    return
  }

  postsLoading.value = true
  postsError.value = ''
  try {
    // Bài ghim lấy bằng truy vấn riêng để luôn hiện ở đầu, kể cả khi bài đó nằm ngoài các trang đã tải.
    const [pinned, firstPage] = await Promise.all([getPinnedPosts(community.value.id), getPosts(community.value.id)])
    pinnedPosts.value = pinned
    posts.value = firstPage.posts
    nextCursor.value = firstPage.nextCursor
  } catch (error) {
    postsError.value = describeApiError(error).message
  } finally {
    postsLoading.value = false
  }
}

async function loadMore() {
  if (nextCursor.value === null || loadingMore.value || postsLoading.value) return

  const communityId = community.value.id
  loadingMore.value = true
  loadMoreError.value = ''
  try {
    const page = await getPosts(communityId, nextCursor.value)
    // Người dùng có thể đã chuyển sang cộng đồng khác trong lúc chờ.
    if (community.value?.id !== communityId) return
    posts.value = [...posts.value, ...page.posts]
    nextCursor.value = page.nextCursor
  } catch (error) {
    loadMoreError.value = describeApiError(error).message
  } finally {
    loadingMore.value = false
    observeFeedEnd()
  }
}

let feedEndObserver = null

// Quan sát lại để trình duyệt báo lần nữa nếu điểm cuối vẫn đang nằm trong tầm nhìn (trang vừa tải còn quá ngắn).
function observeFeedEnd() {
  feedEndObserver?.disconnect()
  if (feedEnd.value && !loadMoreError.value) feedEndObserver?.observe(feedEnd.value)
}

async function refreshMembers() {
  try {
    members.value = await fetchMembers(route.params.name)
  } catch (error) {
    actionError.value = describeApiError(error).message
  }
}

async function loadCommunity() {
  loadStatus.value = 'loading'
  try {
    // Hai yêu cầu độc lập nên gửi song song, không chờ nhau.
    const [loadedCommunity, loadedMembers] = await Promise.all([fetchCommunity(route.params.name), fetchMembers(route.params.name)])
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
  const currentIndex = TABS.findIndex((tab) => tab.id === activeTab.value)
  activeTab.value = TABS[(currentIndex + step + TABS.length) % TABS.length].id
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

function replacePost(list, updatedPost) {
  return list.map((post) => (post.id === updatedPost.id ? updatedPost : post))
}

function handleUpdated(updatedPost) {
  posts.value = replacePost(posts.value, updatedPost)
  pinnedPosts.value = replacePost(pinnedPosts.value, updatedPost)
  showToast('Post updated.')
}

async function togglePin(post) {
  actionError.value = ''
  try {
    const updatedPost = await setPostPinned(post.id, !post.isPinned)
    posts.value = posts.value.filter((existing) => existing.id !== updatedPost.id)
    pinnedPosts.value = pinnedPosts.value.filter((existing) => existing.id !== updatedPost.id)
    if (updatedPost.isPinned) {
      pinnedPosts.value = [...pinnedPosts.value, updatedPost].sort(byNewest)
    } else if (nextCursor.value === null || updatedPost.id > nextCursor.value) {
      // Bài cũ hơn trang đã tải sẽ tự quay lại bảng tin khi cuộn tới; chèn ngay sẽ làm hỏng con trỏ.
      posts.value = [...posts.value, updatedPost].sort(byNewest)
    }
    showToast(updatedPost.isPinned ? 'Post pinned.' : 'Post unpinned.')
  } catch (error) {
    actionError.value = describeApiError(error).message
  }
}

function requestDelete(post) {
  if (post?.author?.userId !== currentUserId.value && !canRemoveContent.value) return
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
    pinnedPosts.value = pinnedPosts.value.filter((post) => post.id !== postId)
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

// Điểm cuối chỉ tồn tại khi còn trang sau, nên theo dõi ref để gắn hoặc gỡ observer đúng lúc.
watch(feedEnd, observeFeedEnd)

onMounted(() => {
  authStore.initialize()
  feedEndObserver = new IntersectionObserver(
    (entries) => {
      if (entries.some((entry) => entry.isIntersecting)) loadMore()
    },
    { rootMargin: '400px' },
  )
  observeFeedEnd()
})

onBeforeUnmount(() => {
  if (toastTimer) window.clearTimeout(toastTimer)
  feedEndObserver?.disconnect()
})
</script>

<template>
  <UserNavbar />
  <div class="shell">
    <CommunitySidebar class="left" :current-community="community" />

    <main class="content">
      <div v-if="loadStatus === 'loading'" aria-busy="true" aria-label="Loading community">
        <div class="skeleton banner"></div>
        <div class="skeleton line"></div>
      </div>

      <div v-else-if="loadStatus !== 'ready'" class="state" role="alert">
        <h1>{{ loadStatus === 'missing' ? 'Community not found' : 'Could not load this community' }}</h1>
        <p>{{ loadStatus === 'missing' ? 'It may not exist, or it may be private.' : loadError }}</p>
        <RouterLink class="btn" to="/communities">Back to communities</RouterLink>
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
          <CommunityHeaderActions :community="community" :active-tab="activeTab" :busy="isMembershipBusy" @join="changeMembership(joinCommunity)" @leave="changeMembership(leaveCommunity)" @create="openComposer" />
        </header>
        <p v-if="actionError" class="action-error" role="alert">{{ actionError }}</p>

        <div class="layout">
          <section class="feed">
            <div class="tabs" role="tablist" aria-label="Community sections" @keydown="moveTabWithArrowKeys">
              <button v-for="tab in TABS" :id="`tab-${tab.id}`" :key="tab.id" type="button" role="tab" :aria-selected="activeTab === tab.id" :tabindex="activeTab === tab.id ? 0 : -1" @click="activeTab = tab.id">{{ tab.label }}</button>
            </div>

            <div v-if="activeTab === 'posts'" role="tabpanel" aria-labelledby="tab-posts" class="posts">
              <PinnedPosts :posts="pinnedPosts" :can-pin="canPinPosts" @unpin="togglePin" />
              <PostList
                :posts="posts"
                :loading="postsLoading"
                :error="postsError"
                :current-user-id="currentUserId"
                :can-remove-others="canRemoveContent"
                :can-pin="canPinPosts"
                @retry="loadFeed"
                @edit="handleEdit"
                @delete="requestDelete"
                @pin="togglePin"
              />
              <div v-if="nextCursor !== null && !postsLoading && !postsError" ref="feedEnd" class="feed-end" aria-hidden="true"></div>
              <p v-if="loadingMore" class="feed-status" role="status">Loading more posts…</p>
              <div v-if="loadMoreError" class="feed-status" role="alert">
                {{ loadMoreError }}
                <button type="button" class="btn" @click="loadMore">Try again</button>
              </div>
            </div>

            <div v-else role="tabpanel" :aria-labelledby="`tab-${activeTab}`" class="empty-tab">
              <p>{{ activeTab === 'puzzles' ? 'Puzzles' : 'Contests' }} for this community are coming soon.</p>
            </div>

          </section>

          <aside class="side" aria-label="About this community">
            <section class="card">
              <h2>About</h2>
              <p>{{ community.shoutout || 'This community has no description yet.' }}</p>
              <p class="created">Created {{ dateFormat.format(new Date(community.createdAt)) }}</p>
            </section>

            <section class="card">
              <h2>Moderators</h2>
              <ul class="mods">
                <li v-for="moderator in moderators" :key="moderator.userId">
                  <Avatar :src="moderator.imageUrl" :name="moderator.name" :size="28" :round="true" :show-name="false" />
                  {{ moderator.name }}
                  <svg v-if="moderator.isOwner" class="crown" viewBox="0 0 24 24" aria-label="Owner"><path d="M3 18 5 7l5 5 2-7 2 7 5-5 2 11z" /></svg>
                </li>
              </ul>
            </section>

            <RoleList :community-name="community.name" :members="members" :can-manage-members="canManageMembers" @members-changed="refreshMembers" />
            <CommunitySideCards />
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
