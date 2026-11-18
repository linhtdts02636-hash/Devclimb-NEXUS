<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import BaseModal from '../../components/common/BaseModal.vue'
import CreatePostButton from '../../components/post/CreatePostButton.vue'
import PostComposerModal from '../../components/post/PostComposerModal.vue'
import PostList from '../../components/post/PostList.vue'
import { getCommunityBySlug } from '../../features/community/api/communityApi'
import { deletePost, getPosts } from '../../features/post/api/postApi'
import { getApiErrorMessage } from '../../features/post/utils/apiError'
import { useAuthStore } from '../../stores/authStore'

const props = defineProps({
  slug: {
    type: String,
    required: true,
  },
})

const authStore = useAuthStore()
const community = ref(null)
const posts = ref([])
const loading = ref(false)
const error = ref('')
const composerOpen = ref(false)
const editingPost = ref(null)
const deleteOpen = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)
const deleteError = ref('')
const toast = ref('')
let toastTimer = null

const communityId = computed(() => community.value?.id ?? null)
const isAuthenticated = computed(() => authStore.status === 'authenticated')
const currentUserId = computed(() => authStore.profile?.id ?? null)
// Chỉ cần đã đăng nhập; không kiểm tra membership khi đăng bài.
const canPost = computed(
  () =>
    isAuthenticated.value &&
    Number.isInteger(communityId.value) &&
    communityId.value > 0,
)
async function loadCommunity() {
  const slug = props.slug.trim()
  composerOpen.value = false
  editingPost.value = null
  resetDeleteConfirm()

  if (!slug) {
    community.value = null
    posts.value = []
    loading.value = false
    error.value = 'Đường dẫn cộng đồng không hợp lệ.'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const loadedCommunity = await getCommunityBySlug(slug)
    community.value = loadedCommunity
    posts.value = await getPosts(loadedCommunity.id)
  } catch (requestError) {
    community.value = null
    posts.value = []
    error.value = getApiErrorMessage(requestError, 'Không thể tải cộng đồng.')
  } finally {
    loading.value = false
  }
}

function openComposer() {
  if (!canPost.value) {
    return
  }

  editingPost.value = null
  composerOpen.value = true
}

function handleEdit(post) {
  if (post?.author?.id !== currentUserId.value) {
    return
  }

  editingPost.value = post
  composerOpen.value = true
}

function handleCreated(post) {
  posts.value = [post, ...posts.value]
  showToast('Đã đăng bài viết.')
}

function handleUpdated(updatedPost) {
  posts.value = posts.value.map((post) =>
    post.id === updatedPost.id ? updatedPost : post,
  )
  showToast('Đã cập nhật bài viết.')
}

function requestDelete(post) {
  if (post?.author?.id !== currentUserId.value) {
    return
  }

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
  if (deleting.value) {
    return
  }

  resetDeleteConfirm()
}

function handleDeleteOpenChange(open) {
  if (!open) {
    closeDeleteConfirm()
  }
}

async function confirmDelete() {
  if (!deleteTarget.value || deleting.value) {
    return
  }

  deleting.value = true
  deleteError.value = ''

  try {
    const postId = deleteTarget.value.id
    await deletePost(postId)
    posts.value = posts.value.filter((post) => post.id !== postId)
    resetDeleteConfirm()
    showToast('Đã xoá bài viết.')
  } catch (requestError) {
    deleteError.value = getApiErrorMessage(requestError, 'Không thể xoá bài viết.')
  } finally {
    deleting.value = false
  }
}

function showToast(message) {
  toast.value = message

  if (toastTimer) {
    window.clearTimeout(toastTimer)
  }

  toastTimer = window.setTimeout(() => {
    toast.value = ''
  }, 5000)
}

watch(() => props.slug, loadCommunity, { immediate: true })

onMounted(() => {
  authStore.initialize()
})

onBeforeUnmount(() => {
  if (toastTimer) {
    window.clearTimeout(toastTimer)
  }
})
</script>

<template>
  <main class="feed-page">
    <div class="feed-page__inner">
      <header class="feed-header">
        <div class="feed-header__copy">
          <p class="feed-eyebrow">CỘNG ĐỒNG</p>
          <h1 class="feed-title">Bảng tin cộng đồng</h1>
          <p v-if="community" class="feed-context">
            Đang xem <strong>{{ community.name }}</strong>
          </p>
        </div>

        <div class="feed-header__actions">
          <CreatePostButton
            label="Đăng bài"
            :disabled="!canPost"
            @click="openComposer"
          />
          <p v-if="authStore.status === 'signedOut'" class="feed-login-hint">
            Đăng nhập để đăng bài.
          </p>
          <p
            v-else-if="authStore.status === 'needsEmailVerification'"
            class="feed-login-hint"
          >
            Xác minh email để đăng bài.
          </p>
        </div>
      </header>

      <PostList
        :posts="posts"
        :loading="loading"
        :error="error"
        :current-user-id="currentUserId"
        @retry="loadCommunity"
        @edit="handleEdit"
        @delete="requestDelete"
      />
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
      title="Xoá bài viết"
      size="sm"
      :close-on-backdrop="!deleting"
      @update:open="handleDeleteOpenChange"
    >
      <p class="confirm-text">
        Bạn có chắc muốn xoá bài viết này? Hành động không thể hoàn tác.
      </p>
      <p v-if="deleteTarget?.title" class="confirm-title">“{{ deleteTarget.title }}”</p>
      <p v-if="deleteError" class="confirm-error" role="alert">{{ deleteError }}</p>

      <template #footer>
        <button type="button" class="btn" :disabled="deleting" @click="closeDeleteConfirm">
          Hủy
        </button>
        <button type="button" class="btn btn--danger" :disabled="deleting" @click="confirmDelete">
          <span v-if="deleting" class="spinner" aria-hidden="true"></span>
          {{ deleting ? 'Đang xoá…' : 'Xoá bài' }}
        </button>
      </template>
    </BaseModal>

    <Transition name="toast">
      <div v-if="toast" class="toast" role="status">{{ toast }}</div>
    </Transition>
  </main>
</template>

<style scoped>
.feed-page {
  min-height: 100vh;
  padding: 40px 20px 72px;
  background:
    radial-gradient(circle at 50% 0, var(--ac-soft), transparent 34rem),
    var(--bg);
}

.feed-page__inner {
  width: min(720px, 100%);
  margin: 0 auto;
}

.feed-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
}

.feed-header__copy {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.feed-header__actions {
  display: grid;
  flex: 0 0 auto;
  justify-items: end;
  gap: 8px;
}

.feed-eyebrow {
  color: var(--tx2);
  font-family: var(--mono);
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.06em;
  line-height: 1.2;
}

.feed-title {
  font-size: 26px;
  font-weight: 600;
  letter-spacing: -0.03em;
  line-height: 1.15;
}

.feed-context {
  color: var(--tx2);
  font-size: 14px;
}

.feed-login-hint {
  color: var(--tx2);
  font-size: 12px;
  line-height: 1.4;
  text-align: right;
}

.confirm-text {
  color: var(--tx);
  font-size: 14px;
  line-height: 1.55;
}

.confirm-title {
  margin-top: 8px;
  color: var(--tx2);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.confirm-error {
  margin-top: 12px;
  padding: 10px 12px;
  border: 1px solid color-mix(in srgb, var(--bad) 36%, transparent);
  border-radius: var(--r-sm);
  background: color-mix(in srgb, var(--bad) 8%, transparent);
  color: var(--bad);
  font-size: 13px;
  line-height: 1.45;
}

.btn--danger {
  background: var(--bad);
  color: #ffffff;
}

.btn--danger:hover:not(:disabled) {
  background: color-mix(in srgb, var(--bad) 88%, #000000);
}

.toast {
  position: fixed;
  bottom: 24px;
  left: 50%;
  z-index: 20;
  max-width: calc(100vw - 32px);
  padding: 10px 16px;
  border-radius: 999px;
  background: #1f1f1e;
  box-shadow: var(--sh-pop);
  color: #ffffff;
  font-size: 13px;
  line-height: 1.4;
  transform: translateX(-50%);
}

.toast-enter-active,
.toast-leave-active {
  transition:
    opacity 180ms var(--ease),
    transform 180ms var(--ease);
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, 6px);
}

@media (max-width: 560px) {
  .feed-page {
    padding: 24px 14px 56px;
  }

  .feed-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 16px;
  }

  .feed-header__actions {
    justify-items: start;
  }

  .feed-login-hint {
    text-align: left;
  }

  .feed-title {
    font-size: 22px;
  }
}
</style>
