<script setup>
import Post from './Post.vue'

defineProps({
  posts: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: '',
  },
  currentUserId: {
    type: Number,
    default: null,
  },
  images: {
    type: Array,
    default: () => [],
  },
})

defineEmits(['retry', 'edit', 'delete'])
</script>

<template>
  <div class="post-list" :aria-busy="loading">
    <template v-if="loading">
      <div v-for="index in 3" :key="`skeleton-${index}`" class="post-skeleton" aria-hidden="true">
        <div class="post-skeleton__header">
          <span class="post-skeleton__avatar"></span>
          <span class="post-skeleton__lines">
            <span class="post-skeleton__line post-skeleton__line--short"></span>
            <span class="post-skeleton__line post-skeleton__line--tiny"></span>
          </span>
        </div>
        <span class="post-skeleton__line"></span>
        <span class="post-skeleton__line post-skeleton__line--medium"></span>
      </div>
      <p class="sr-only">Đang tải bài viết…</p>
    </template>

    <div v-else-if="error" class="post-state post-state--error" role="alert">
      <h2 class="post-state__title">Không tải được bài viết</h2>
      <p class="post-state__text">{{ error }}</p>
      <button type="button" class="btn" @click="$emit('retry')">Thử lại</button>
    </div>

    <div v-else-if="posts.length === 0" class="post-state post-state--empty">
      <h2 class="post-state__title">Chưa có bài viết nào</h2>
      <p class="post-state__text">Hãy là người đầu tiên chia sẻ trong cộng đồng này.</p>
    </div>

    <template v-else>
      <Post
        v-for="post in posts"
        :key="post.id"
        :post="post"
        :images="post.images?.length ? post.images : images"
        :current-user-id="currentUserId"
        @edit="$emit('edit', $event)"
        @delete="$emit('delete', $event)"
      />
    </template>
  </div>
</template>

<style scoped>
.post-list {
  display: grid;
  gap: 12px;
}

.post-skeleton {
  display: grid;
  gap: 12px;
  padding: 18px 20px;
  border: 1px solid var(--line2);
  border-radius: var(--r);
  background: var(--s1);
}

.post-skeleton__header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-skeleton__avatar {
  width: 40px;
  height: 40px;
  flex: 0 0 auto;
  border-radius: 999px;
  background: var(--s2);
}

.post-skeleton__lines {
  display: grid;
  flex: 1;
  gap: 6px;
}

.post-skeleton__line {
  display: block;
  height: 10px;
  border-radius: 999px;
  background: var(--s2);
}

.post-skeleton__line--short {
  width: 32%;
}

.post-skeleton__line--tiny {
  width: 18%;
  height: 8px;
}

.post-skeleton__line--medium {
  width: 64%;
}

.post-state {
  display: grid;
  justify-items: start;
  gap: 8px;
  padding: 32px 24px;
  border: 1px solid var(--line2);
  border-radius: var(--r);
  background: var(--s1);
}

.post-state--error {
  border-color: color-mix(in srgb, var(--bad) 36%, var(--line2));
}

.post-state__title {
  font-size: 18px;
  font-weight: 600;
}

.post-state__text {
  max-width: 52ch;
  color: var(--tx2);
  font-size: 14px;
  line-height: 1.55;
}

.post-state .btn {
  margin-top: 6px;
}
</style>
