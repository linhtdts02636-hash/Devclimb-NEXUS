<script setup>
import AvatarImage from '../../../components/AvatarImage.vue'

defineProps({
  post: { type: Object, required: true },
})

const ROLE_COLORS = {
  Owner: 'var(--warn)',
  Moderator: 'var(--ac)',
  'Problem setter': 'var(--ok)',
  Member: 'var(--tx2)',
}
</script>

<template>
  <article class="post">
    <header class="post-head">
      <AvatarImage :name="post.author" :size="26" is-round />
      <b>{{ post.author }}</b>
      <span class="role-tag" :style="{ color: ROLE_COLORS[post.role] }">{{ post.role }}</span>
      <span class="time">· {{ post.time }}</span>
    </header>

    <h3>{{ post.title }}</h3>
    <p v-if="post.text" class="text">{{ post.text }}</p>

    <div v-if="post.images.length" class="media" :class="post.images.length === 1 ? 'single' : 'grid'">
      <img v-for="image in post.images" :key="image" :src="image" alt="" loading="lazy" />
    </div>

    <ul v-if="post.files.length" class="files">
      <li v-for="file in post.files" :key="file.name" class="file">
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8zM14 3v5h5" /></svg>
        {{ file.name }}<small>{{ file.size }}</small>
      </li>
    </ul>

    <!-- Chưa có backend nên các nút tương tác bị khóa, tránh để người dùng tưởng đã lưu được. -->
    <footer class="actions">
      <span class="pill votes">
        <button type="button" disabled aria-label="Upvote"><svg viewBox="0 0 24 24"><path d="m12 5 7 8h-4v6H9v-6H5z" /></svg></button>
        <b>{{ post.upvotes }}</b>
        <button type="button" disabled aria-label="Downvote"><svg viewBox="0 0 24 24"><path d="m12 19-7-8h4V5h6v6h4z" /></svg></button>
      </span>
      <button type="button" class="pill" disabled>
        <svg viewBox="0 0 24 24"><path d="M21 12a8 8 0 0 1-11.6 7.1L4 20l1-4.6A8 8 0 1 1 21 12z" /></svg>{{ post.replies }}
      </button>
      <button type="button" class="pill" disabled>
        <svg viewBox="0 0 24 24"><path d="M4 12v7h16v-7M12 3v12m0-12L8 7m4-4 4 4" /></svg>Share
      </button>
    </footer>
  </article>
</template>

<style scoped>
.post {
  padding: var(--s-4) var(--s-4) var(--s-3);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
}

.post-head {
  display: flex;
  gap: var(--s-2);
  align-items: center;
  flex-wrap: wrap;
  font-size: var(--t-sm);
}

.post-head b {
  font-weight: 500;
}

.time {
  color: var(--tx2);
}

.role-tag {
  padding: 0 var(--s-2);
  font: 400 11px var(--f-mono);
  border: 1px solid currentColor;
  border-radius: 999px;
}

h3 {
  margin: var(--s-2) 0 var(--s-1);
  font: 500 var(--t-lg) / 1.35 var(--f-head);
}

.text {
  display: -webkit-box;
  overflow: hidden;
  color: var(--tx2);
  font-size: var(--t-sm);
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.media {
  display: grid;
  gap: var(--s-2);
  margin-top: var(--s-3);
}

.media.grid {
  grid-template-columns: 1fr 1fr;
}

.media img {
  display: block;
  width: 100%;
  background: var(--s2);
  border: 1px solid var(--line);
  border-radius: var(--r);
  object-fit: cover;
}

.media.single img {
  max-height: 420px;
}

.media.grid img {
  aspect-ratio: 4 / 3;
}

.files {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s-2);
  margin: var(--s-3) 0 0;
  padding: 0;
  list-style: none;
}

.file {
  display: inline-flex;
  gap: var(--s-2);
  align-items: center;
  height: 40px;
  padding: 0 var(--s-3);
  font-size: var(--t-sm);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
}

.file small {
  font: 400 var(--t-xs) var(--f-mono);
  color: var(--tx2);
}

svg {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.actions {
  display: flex;
  gap: var(--s-2);
  margin-top: var(--s-3);
}

.pill {
  display: inline-flex;
  gap: var(--s-2);
  align-items: center;
  height: 34px;
  padding: 0 var(--s-3);
  font: 500 var(--t-sm) var(--f-mono);
  color: var(--tx2);
  background: var(--s2);
  border-radius: 999px;
}

.pill:disabled {
  opacity: 0.7;
}

.votes {
  padding: 0;
  gap: 0;
}

.votes button {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
}

.votes b {
  min-width: 2ch;
  text-align: center;
  font-weight: 500;
  color: var(--tx);
  font-variant-numeric: tabular-nums;
}

@media (max-width: 560px) {
  .media.grid {
    grid-template-columns: 1fr;
  }
}
</style>
