<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import CreatePostButton from '../../components/post/CreatePostButton.vue'
import PostComposerModal from '../../components/post/PostComposerModal.vue'
import PostList from '../../components/post/PostList.vue'
import { getPosts } from '../../features/post/api/postApi'
import { getPostErrorMessage } from '../../features/post/utils/postError'

const props = defineProps({
  communityId: {
    type: [String, Number],
    required: true,
  },
})

const numericCommunityId = computed(() => Number(props.communityId))
const isValidCommunityId = computed(
  () => Number.isInteger(numericCommunityId.value) && numericCommunityId.value > 0,
)

const posts = ref([])
const loading = ref(false)
const error = ref('')
const composerOpen = ref(false)
const toast = ref('')
let toastTimer = null

async function loadPosts() {
  if (!isValidCommunityId.value) {
    loading.value = false
    error.value = 'Mã cộng đồng không hợp lệ.'
    return
  }

  loading.value = true
  error.value = ''

  try {
    posts.value = await getPosts(numericCommunityId.value)
  } catch (requestError) {
    error.value = getPostErrorMessage(requestError, 'Không thể tải bài viết.')
  } finally {
    loading.value = false
  }
}

function openComposer() {
  if (isValidCommunityId.value) {
    composerOpen.value = true
  }
}

function handleCreated(post) {
  posts.value = [post, ...posts.value]
  composerOpen.value = false
  showToast('Đã đăng bài viết.')
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

watch(() => props.communityId, loadPosts, { immediate: true })

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
          <p class="feed-context">
            Đang xem cộng đồng <span class="mono">#{{ communityId }}</span>
          </p>
        </div>

        <CreatePostButton :disabled="!isValidCommunityId" @click="openComposer" />
      </header>

      <PostList
        :posts="posts"
        :loading="loading"
        :error="error"
        @retry="loadPosts"
      />
    </div>

    <PostComposerModal
      v-model:open="composerOpen"
      :community-id="numericCommunityId"
      @created="handleCreated"
    />

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

  .feed-title {
    font-size: 22px;
  }
}
</style>
