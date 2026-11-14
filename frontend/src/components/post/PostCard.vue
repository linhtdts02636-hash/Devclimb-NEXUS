<script setup>
import { computed } from 'vue'
import UserAvatar from '../common/UserAvatar.vue'
import PostMedia from './PostMedia.vue'
import { formatFullDate, formatRelativeTime } from '../../composables/useRelativeTime'

const props = defineProps({
  post: {
    type: Object,
    required: true,
  },
})

const authorName = computed(() => props.post.author?.name?.trim() || 'Người dùng')
const avatarUrl = computed(() => props.post.author?.avatarUrl || '')
const relativeTime = computed(() => formatRelativeTime(props.post.createdAt))
const fullDate = computed(() => formatFullDate(props.post.createdAt))
</script>

<template>
  <article class="post-card">
    <header class="post-card__header">
      <UserAvatar :src="avatarUrl" :name="authorName" :size="40" />

      <div class="post-card__author">
        <strong class="post-card__name">{{ authorName }}</strong>
        <time class="post-card__time" :datetime="post.createdAt" :title="fullDate">
          {{ relativeTime }}
        </time>
      </div>
    </header>

    <p v-if="post.content" class="post-card__content">{{ post.content }}</p>

    <PostMedia :attachments="post.attachments || []" />
  </article>
</template>

<style scoped>
.post-card {
  padding: 18px 20px;
  border: 1px solid var(--line2);
  border-radius: var(--r);
  background: var(--s1);
}

.post-card__header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-card__author {
  display: grid;
  gap: 1px;
  min-width: 0;
}

.post-card__name {
  overflow: hidden;
  color: var(--tx);
  font-size: 15px;
  font-weight: 600;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-card__time {
  color: var(--tx2);
  font-family: var(--mono);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
}

.post-card__content {
  margin-top: 14px;
  color: var(--tx);
  font-size: 15px;
  line-height: 1.65;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

@media (max-width: 480px) {
  .post-card {
    padding: 16px;
    border-radius: var(--r-sm);
  }
}
</style>
