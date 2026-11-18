<script setup>
import { useRouter } from 'vue-router'
import Avatar from './Avatar.vue'

const router = useRouter()

const props = defineProps({
  id: { type: Number, default: null },
  index: { type: String, default: '' },
  title: { type: String, required: true },
  content: { type: String, default: '' },
  difficulty: { type: String, default: 'Medium' },
  languages: { type: Array, default: () => [] },
  tags: { type: Array, default: () => [] },
  authorName: { type: String, default: '' },
  createdLabel: { type: String, default: '' },
  acceptance: { type: String, default: '' },
  community: {
    type: Object,
    default: () => ({ src: '/favicon.svg', name: 'fpt-algo-club', type: 'community' }),
  },
  showProof: { type: Boolean, default: true },
  proofAvatars: { type: Array, default: () => [] },
  proofText: { type: String, default: '' },
  solveLabel: { type: String, default: 'Solve' },
})

function openPuzzle() {
  // Thẻ chưa có id (dữ liệu cũ) thì không điều hướng.
  if (props.id == null) return
  router.push({ name: 'puzzle-solve', params: { id: props.id } })
}
</script>

<template>
  <article
    class="puzzle-card"
    :tabindex="content ? 0 : undefined"
    :aria-describedby="content && id != null ? `puzzle-preview-${id}` : undefined"
  >
    <div
      v-if="content"
      :id="id != null ? `puzzle-preview-${id}` : undefined"
      class="puzzle-preview"
      role="tooltip"
    >
      <span class="puzzle-preview-label">Puzzle</span>
      <p class="puzzle-preview-text">{{ content }}</p>
    </div>

    <div class="puzzle-card-head">
      <Avatar :src="community.src" :name="community.name" :type="community.type" />
      <span v-if="createdLabel" class="puzzle-date">{{ createdLabel }}</span>
    </div>

    <div>
      <div class="puzzle-title-row">
        <span v-if="index" class="puzzle-index">{{ index }}</span>
        <h3 class="puzzle-card-title">{{ title }}</h3>
      </div>

      <div class="puzzle-meta">
        <span class="badge" :class="difficulty.toLowerCase()">{{ difficulty }}</span>
        <span v-for="language in languages" :key="language" class="tag lang-tag">{{ language }}</span>
      </div>

      <div v-if="tags.length || acceptance || authorName" class="puzzle-tags">
        <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
        <span v-if="authorName" class="author-tag">by {{ authorName }}</span>
        <span v-if="acceptance" class="acceptance-tag">{{ acceptance }} acceptance</span>
      </div>
    </div>

    <div v-if="showProof && (proofText || proofAvatars.length)" class="puzzle-proof">
      <div v-if="proofAvatars.length" class="proof-avatars">
        <span
          v-for="(avatar, avatarIndex) in proofAvatars"
          :key="avatarIndex"
          class="proof-avatar"
          :class="avatar.tone"
        >
          {{ avatar.initials }}
        </span>
      </div>
      <span v-if="proofText" class="proof-text">{{ proofText }}</span>
    </div>

    <div class="puzzle-card-foot">
      <button class="solve-btn" type="button" :disabled="id == null" @click="openPuzzle">
        <span>{{ solveLabel }}</span>
        <svg class="solve-icon" viewBox="0 0 24 24" aria-hidden="true">
          <path d="M5 12h14M13 6l6 6-6 6" />
        </svg>
      </button>
    </div>
  </article>
</template>

<style scoped>
.puzzle-card {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 14px;
  padding: 20px;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-lg);
  outline: none;
  transition: border-color 0.15s var(--ease);
}

.puzzle-card:hover,
.puzzle-card:focus-visible {
  border-color: var(--tx3);
}

.puzzle-card:focus-visible {
  box-shadow: 0 0 0 3px var(--ac-soft);
}

.puzzle-preview {
  position: absolute;
  right: 0;
  bottom: calc(100% + 10px);
  left: 0;
  z-index: 30;
  padding: 12px 14px;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  box-shadow: var(--sh-card);
  opacity: 0;
  visibility: hidden;
  transform: translateY(4px);
  transition: opacity 0.15s var(--ease), transform 0.15s var(--ease), visibility 0.15s;
  pointer-events: none;
}

.puzzle-card:hover .puzzle-preview,
.puzzle-card:focus-visible .puzzle-preview {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.puzzle-preview-label {
  font: 500 10px var(--f-head);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--tx3);
}

.puzzle-preview-text {
  display: -webkit-box;
  margin: 6px 0 0;
  overflow: hidden;
  font: 400 12px/1.55 var(--f-head);
  color: var(--tx2);
  white-space: pre-line;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 5;
}

.puzzle-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-2);
}

.puzzle-date {
  flex: none;
  font: 400 11px var(--f-head);
  color: var(--tx3);
}

.puzzle-title-row {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  margin-bottom: 2px;
}

.puzzle-index {
  flex: none;
  font: 500 11px var(--f-head);
  color: var(--tx2);
}

.puzzle-card-title {
  min-width: 0;
  font: 600 16px/1.25 var(--f-head);
  letter-spacing: -0.02em;
  color: var(--tx);
}

.puzzle-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border: 1px solid transparent;
  border-radius: 999px;
  font: 500 11px/1.4 var(--f-head);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  white-space: nowrap;
}

.badge.easy {
  color: var(--ok);
  background: color-mix(in srgb, var(--ok) 12%, transparent);
  border-color: color-mix(in srgb, var(--ok) 25%, transparent);
}

.badge.medium {
  color: var(--ac);
  background: var(--ac-soft);
  border-color: color-mix(in srgb, var(--ac) 25%, transparent);
}

.badge.hard {
  color: var(--warn);
  background: color-mix(in srgb, var(--warn) 12%, transparent);
  border-color: color-mix(in srgb, var(--warn) 25%, transparent);
}

.tag {
  display: inline-flex;
  padding: 1px 6px;
  background: var(--s2);
  border: 1px solid var(--line);
  border-radius: 999px;
  font: 400 11px/1.5 var(--f-head);
  color: var(--tx2);
  white-space: nowrap;
}

.lang-tag {
  border-color: var(--line2);
  color: var(--tx);
}

.puzzle-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
}

.author-tag {
  padding: 1px 8px;
  background: var(--ac-soft);
  border: 1px solid color-mix(in srgb, var(--ac) 25%, transparent);
  border-radius: 999px;
  font: 400 11px/1.5 var(--f-head);
  color: var(--ac);
  white-space: nowrap;
}

.acceptance-tag {
  padding: 1px 8px;
  background: color-mix(in srgb, var(--s2) 60%, transparent);
  border: 1px solid var(--line);
  border-radius: 999px;
  font: 400 11px/1.5 var(--f-head);
  color: var(--tx2);
  white-space: nowrap;
}

.puzzle-proof {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  padding-top: var(--s-2);
  border-top: 1px solid var(--line);
}

.proof-avatars {
  display: flex;
}

.proof-avatar {
  display: grid;
  place-items: center;
  width: 20px;
  height: 20px;
  margin-left: -6px;
  border: 1px solid var(--s1);
  border-radius: 50%;
  font: 600 9px var(--f-head);
}

.proof-avatar:first-child {
  margin-left: 0;
}

.av-accent {
  background: color-mix(in srgb, var(--ac) 30%, transparent);
  color: var(--ac);
}

.av-easy {
  background: color-mix(in srgb, var(--ok) 30%, transparent);
  color: var(--ok);
}

.av-warn {
  background: color-mix(in srgb, var(--warn) 30%, transparent);
  color: var(--warn);
}

.proof-text {
  font: 400 11px var(--f-head);
  color: var(--tx2);
}

.puzzle-card-foot {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-top: 2px;
}

.solve-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 32px;
  padding: 0 20px;
  background: var(--ac);
  border: 1px solid transparent;
  border-radius: 999px;
  color: var(--ac-ink);
  font: 500 12px/1 var(--f-head);
  letter-spacing: -0.01em;
  white-space: nowrap;
  box-shadow: inset 0 -4px 8px var(--sh-in);
  transition: filter 0.15s var(--ease);
}

.solve-btn:hover:not(:disabled) {
  filter: brightness(1.07);
}

.solve-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.solve-icon {
  flex: none;
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}
</style>
