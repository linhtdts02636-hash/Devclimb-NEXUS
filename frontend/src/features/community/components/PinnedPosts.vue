<script setup>
import { ref, useId } from 'vue'

defineProps({
  posts: { type: Array, required: true },
  canPin: { type: Boolean, default: false },
})
defineEmits(['unpin'])

const idPrefix = useId()
const openPostIds = ref([])

// Bài viết có thể không có tiêu đề nên lấy phần đầu nội dung thay thế.
function headline(post) {
  return post.title || post.content.slice(0, 80)
}

function toggle(postId) {
  openPostIds.value = openPostIds.value.includes(postId)
    ? openPostIds.value.filter((openId) => openId !== postId)
    : [...openPostIds.value, postId]
}
</script>

<template>
  <section v-if="posts.length" class="pinned" aria-label="Pinned posts">
    <h2>
      <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 17v5M9 3h6l-1 7 3 3v2H7v-2l3-3z" /></svg>
      Pinned
    </h2>
    <div v-for="post in posts" :key="post.id" class="row">
      <button
        type="button"
        class="head"
        :aria-expanded="openPostIds.includes(post.id)"
        :aria-controls="`${idPrefix}-${post.id}`"
        @click="toggle(post.id)"
      >
        <span class="title">{{ headline(post) }}</span>
        <span v-if="post.author.role" class="role" :style="{ '--role-color': post.author.role.color }"><i></i>{{ post.author.role.name }}</span>
        <small>{{ post.author.name }}</small>
        <svg class="chevron" viewBox="0 0 24 24" aria-hidden="true"><path d="m6 9 6 6 6-6" /></svg>
      </button>
      <div v-show="openPostIds.includes(post.id)" :id="`${idPrefix}-${post.id}`" class="body">
        <p class="text">{{ post.content }}</p>
        <button v-if="canPin" type="button" class="unpin" @click="$emit('unpin', post)">Unpin</button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.pinned {
  margin-bottom: var(--s-4);
  overflow: hidden;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
}

h2 {
  display: flex;
  gap: var(--s-2);
  align-items: center;
  padding: var(--s-3) var(--s-4);
  font: 400 11px var(--f-mono);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--tx2);
  border-bottom: 1px solid var(--line);
}

.row + .row {
  border-top: 1px solid var(--line);
}

.head {
  display: flex;
  gap: var(--s-3);
  align-items: center;
  width: 100%;
  padding: var(--s-3) var(--s-4);
  text-align: left;
}

.head:hover {
  background: var(--s2);
}

.title {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role {
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

.role i {
  width: 6px;
  height: 6px;
  background: var(--role-color);
  border-radius: 50%;
}

small {
  font: 400 var(--t-xs) var(--f-mono);
  color: var(--tx2);
}

svg {
  width: 16px;
  height: 16px;
  flex: none;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.chevron {
  color: var(--tx2);
  transition: transform 0.2s var(--ease);
}

.head[aria-expanded='true'] .chevron {
  transform: rotate(180deg);
}

.body {
  display: grid;
  gap: var(--s-3);
  padding: 0 var(--s-4) var(--s-4);
  font-size: var(--t-sm);
  color: var(--tx2);
}

.text {
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

.unpin {
  justify-self: start;
  height: 28px;
  padding: 0 var(--s-3);
  font-size: var(--t-xs);
  color: var(--tx2);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
}

.unpin:hover {
  color: var(--tx);
  background: var(--s2);
}
</style>
