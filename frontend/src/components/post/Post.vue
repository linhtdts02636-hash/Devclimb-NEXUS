<script setup>
import { computed } from 'vue'
import UserAvatar from '../common/UserAvatar.vue'
import PostImages from './PostImages.vue'
import { formatFullDate, formatRelativeTime } from '../../composables/useRelativeTime'

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
  // Ảnh mock phục vụ kiểm tra giao diện; bài viết thật không truyền prop này.
  images: {
    type: Array,
    default: () => [],
  },
})

defineEmits(['edit', 'delete'])

const authorName = computed(() => props.post.author?.name?.trim() || 'Người dùng')
const avatarUrl = computed(() => props.post.author?.avatarUrl || '')
const relativeTime = computed(() => formatRelativeTime(props.post.createdAt))
const fullDate = computed(() => formatFullDate(props.post.createdAt))
const editedAt = computed(() => props.post.updatedAt || null)
const editedLabel = computed(() => (editedAt.value ? formatFullDate(editedAt.value) : ''))

const canManage = computed(
  () =>
    Number.isInteger(props.currentUserId) &&
    props.currentUserId > 0 &&
    props.currentUserId === props.post.author?.id,
)
</script>

<template>
  <article class="post">
    <header class="post__header">
      <UserAvatar :src="avatarUrl" :name="authorName" :size="40" />

      <div class="post__author">
        <strong class="post__name">{{ authorName }}</strong>
        <div class="post__meta">
          <time class="post__time" :datetime="post.createdAt" :title="fullDate">
            {{ relativeTime }}
          </time>
          <span v-if="editedAt" class="post__edited" :title="editedLabel">
            Đã chỉnh sửa
          </span>
        </div>
      </div>

      <div v-if="canManage" class="post__actions">
        <button type="button" class="post__action" @click="$emit('edit', post)">
          Sửa
        </button>
        <button
          type="button"
          class="post__action post__action--danger"
          @click="$emit('delete', post)"
        >
          Xoá
        </button>
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
  padding: 18px 20px;
  border: 1px solid var(--line2);
  border-radius: var(--r);
  background: var(--s1);
  overflow-wrap: anywhere;
}

.post__header {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.post__author {
  display: grid;
  flex: 1;
  gap: 2px;
  min-width: 0;
}

.post__name {
  overflow: hidden;
  color: var(--tx);
  font-size: 15px;
  font-weight: 600;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post__meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  min-width: 0;
}

.post__time {
  color: var(--tx2);
  font-family: var(--mono);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.post__edited {
  color: var(--tx2);
  font-size: 12px;
  font-style: italic;
  line-height: 1.3;
}

.post__edited::before {
  content: '·';
  margin-right: 6px;
}

.post__actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 4px;
}

.post__action {
  height: 30px;
  padding: 0 10px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: var(--tx2);
  font: 500 12px / 1 var(--f-head);
  white-space: nowrap;
  transition:
    background 0.16s var(--ease),
    color 0.16s var(--ease);
}

.post__action:hover {
  background: var(--s2);
  color: var(--tx);
}

.post__action--danger:hover {
  background: color-mix(in srgb, var(--bad) 10%, transparent);
  color: var(--bad);
}

.post__title {
  margin-top: 14px;
  color: var(--tx);
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.01em;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.post__content {
  margin-top: 14px;
  color: var(--tx);
  font-size: 15px;
  line-height: 1.65;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

.post__title + .post__content {
  margin-top: 8px;
}

@media (max-width: 480px) {
  .post {
    padding: 16px;
    border-radius: var(--r-sm);
  }

  .post__action {
    padding: 0 8px;
  }
}
</style>
