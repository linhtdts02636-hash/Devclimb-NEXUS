<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import Avatar from '../../../components/common/Avatar.vue'
import PostImages from './PostImages.vue'
import { formatFullDate, formatRelativeTime } from '../../../composables/useRelativeTime'

const props = defineProps({
  post: {
    type: Object,
    required: true,
  },
  // Chỉ truyền khi đã đăng nhập; dùng để hiện nút Sửa/Xoá cho đúng tác giả.
  currentUserId: {
    type: Number,
    default: null,
  },
  // True khi người xem có quyền PIN_POSTS: được ghim/bỏ ghim bài của bất kỳ ai.
  canPin: {
    type: Boolean,
    default: false,
  },
  // True khi người xem có quyền REMOVE_CONTENT trong cộng đồng: được xoá bài của người khác nhưng không được sửa.
  canRemoveOthers: {
    type: Boolean,
    default: false,
  },
  // Ảnh của bài viết, lấy từ PostDTO.images.
  images: {
    type: Array,
    default: () => [],
  },
})

defineEmits(['edit', 'delete', 'pin'])

const menuOpen = ref(false)
const rootElement = ref(null)

function closeMenuOnOutsideClick(event) {
  if (!rootElement.value?.contains(event.target)) menuOpen.value = false
}

function closeMenuOnEscape(event) {
  if (event.key === 'Escape') menuOpen.value = false
}

onMounted(() => {
  document.addEventListener('click', closeMenuOnOutsideClick)
  document.addEventListener('keydown', closeMenuOnEscape)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', closeMenuOnOutsideClick)
  document.removeEventListener('keydown', closeMenuOnEscape)
})

const authorName = computed(() => props.post.author?.name?.trim() || 'User')
const avatarUrl = computed(() => props.post.author?.imageUrl || '')
const relativeTime = computed(() => formatRelativeTime(props.post.createdAt))
const fullDate = computed(() => formatFullDate(props.post.createdAt))
const editedAt = computed(() => props.post.updatedAt || null)
const editedLabel = computed(() => (editedAt.value ? formatFullDate(editedAt.value) : ''))

const isAuthor = computed(
  () =>
    Number.isInteger(props.currentUserId) &&
    props.currentUserId > 0 &&
    props.currentUserId === props.post.author?.userId,
)
const canDelete = computed(() => isAuthor.value || props.canRemoveOthers)
const hasActions = computed(() => canDelete.value || props.canPin)
const authorRole = computed(() => props.post.author?.role ?? null)
</script>

<template>
  <article class="post">
    <header class="post__header">
      <Avatar :src="avatarUrl" :name="authorName" :size="26" type="user" :show-name="false" />
      <strong class="post__name">{{ authorName }}</strong>
      <span v-if="authorRole" class="post__role" :style="{ '--role-color': authorRole.color }"><i></i>{{ authorRole.name }}</span>
      <time class="post__time" :datetime="post.createdAt" :title="fullDate">· {{ relativeTime }}</time>
      <span v-if="editedAt" class="post__edited" :title="editedLabel">edited</span>

      <div v-if="hasActions" ref="rootElement" class="post__menu-anchor">
        <button
          type="button"
          class="post__menu-button"
          aria-haspopup="menu"
          :aria-expanded="menuOpen"
          aria-label="Post actions"
          @click="menuOpen = !menuOpen"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="5" cy="12" r="1.2" /><circle cx="12" cy="12" r="1.2" /><circle cx="19" cy="12" r="1.2" /></svg>
        </button>
        <div v-show="menuOpen" class="post__menu" role="menu">
          <button v-if="canPin" type="button" role="menuitem" @click="menuOpen = false; $emit('pin', post)">{{ post.isPinned ? 'Unpin' : 'Pin' }}</button>
          <button v-if="isAuthor" type="button" role="menuitem" @click="menuOpen = false; $emit('edit', post)">Edit</button>
          <button v-if="canDelete" type="button" role="menuitem" class="post__menu-danger" @click="menuOpen = false; $emit('delete', post)">Delete</button>
        </div>
      </div>
    </header>

    <h2 v-if="post.title" class="post__title">{{ post.title }}</h2>
    <p v-if="post.content" class="post__content">{{ post.content }}</p>

    <PostImages v-if="images?.length" :images="images" />
  </article>
</template>

<style scoped>
.post {
  min-width: 0;
  max-width: 100%;
  padding: var(--s-4) var(--s-4) var(--s-3);
  border: 1px solid var(--line2);
  border-radius: var(--r);
  background: var(--s1);
  overflow-wrap: anywhere;
}

.post__header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--s-2);
  min-width: 0;
  font-size: var(--t-sm);
}

.post__name {
  min-width: 0;
  overflow: hidden;
  color: var(--tx);
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post__role {
  display: inline-flex;
  gap: 5px;
  align-items: center;
  padding: 1px 8px 1px 6px;
  font: 500 var(--t-xs) var(--f-mono);
  color: var(--role-color);
  white-space: nowrap;
  border: 1px solid var(--role-color);
  border-radius: 999px;
}

.post__role i {
  width: 6px;
  height: 6px;
  background: var(--role-color);
  border-radius: 50%;
}

.post__time,
.post__edited {
  color: var(--tx2);
}

.post__edited {
  font-style: italic;
}

.post__menu-anchor {
  position: relative;
  margin-left: auto;
}

.post__menu-button {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: var(--r-sm);
  color: var(--tx2);
}

.post__menu-button:hover {
  background: var(--s2);
  color: var(--tx);
}

.post__menu-button svg {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.post__menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  z-index: 20;
  min-width: 140px;
  padding: var(--s-1);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
  box-shadow: var(--sh-card);
}

.post__menu button {
  display: flex;
  width: 100%;
  height: 36px;
  padding: 0 var(--s-3);
  align-items: center;
  font-size: var(--t-sm);
  text-align: left;
  border-radius: var(--r-sm);
}

.post__menu button:hover {
  background: var(--s2);
}

.post__menu .post__menu-danger {
  color: var(--bad);
}

.post__title {
  margin: var(--s-2) 0 var(--s-1);
  font: 500 var(--t-lg) / 1.35 var(--f-head);
  overflow-wrap: anywhere;
}

.post__content {
  color: var(--tx2);
  font-size: var(--t-sm);
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

@media (max-width: 480px) {
  .post {
    padding: var(--s-3);
    border-radius: var(--r-sm);
  }
}
</style>
