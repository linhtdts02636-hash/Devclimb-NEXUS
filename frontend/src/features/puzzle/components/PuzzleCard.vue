<script setup>
import { useRouter } from 'vue-router'
import Avatar from '../../../components/common/Avatar.vue'
import TooltipHover from '../../../components/common/TooltipHover.vue'

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
  acceptance: { type: String, default: '' }, //this exists! dont forget
  community: {
    type: Object,
    default: () => ({ src: '/favicon.svg', name: 'fpt-algo-club', type: 'community' }),
  },
  showProof: { type: Boolean, default: true },
  proofAvatars: { type: Array, default: () => [] },
  proofText: { type: String, default: '' },
  solveLabel: { type: String, default: 'Solve' },
  solved: { type: Boolean, default: false },
  scoreBadge: { type: String, default: '' },
  scoreTone: { type: String, default: '' },
  locked: { type: Boolean, default: false },
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
    :class="{ 'is-locked': locked }"
    :tabindex="content && !locked ? 0 : undefined"
    :aria-describedby="content && !locked && id != null ? `puzzle-preview-${id}` : undefined"
  >
    <TooltipHover v-if="content && !locked">
      <div
        :id="id != null ? `puzzle-preview-${id}` : undefined"
        class="puzzle-preview"
      >
        <span class="puzzle-preview-label">Puzzle</span>
        <p class="puzzle-preview-text">{{ content }}</p>
      </div>
    </TooltipHover>

    <template v-if="locked">
      <div class="locked-card-body">
        <div class="locked-question-circle">
          <span class="locked-question-mark">?</span>
        </div>
        <span class="locked-hint">Hidden until contest begins</span>
      </div>
    </template>

    <template v-else>
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

      <div class="puzzle-card-foot" :class="{ 'has-score-badge': Boolean(scoreBadge) }">
        <span v-if="scoreBadge" class="badge" :class="scoreTone || 'easy'">{{ scoreBadge }}</span>
        <button
          class="solve-btn"
          :class="{ 'is-solved': solved }"
          type="button"
          :disabled="id == null"
          @click="openPuzzle"
        >
          <span>{{ solved ? 'Solved' : solveLabel }}</span>
          <svg v-if="!solved" class="solve-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M5 12h14M13 6l6 6-6 6" />
          </svg>
          <svg v-else class="solve-icon" viewBox="0 0 24 24" aria-hidden="true">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </button>
      </div>
    </template>
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
  max-width: 320px;
  padding: 12px 14px;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.22);
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
  gap: 10px;
  padding-top: 2px;
}

.puzzle-card-foot.has-score-badge {
  justify-content: space-between;
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

.solve-btn.is-solved {
  background: var(--s3);
  color: var(--tx2);
  box-shadow: none;
  cursor: pointer;
  opacity: 0.92;
}

.solve-btn.is-solved:hover:not(:disabled) {
  background: color-mix(in srgb, var(--s3) 80%, var(--tx));
  color: var(--tx);
  filter: none;
}

.puzzle-card.is-locked {
  min-height: 200px;
  align-items: center;
  justify-content: center;
  cursor: default;
}

.locked-card-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px 16px;
  text-align: center;
  width: 100%;
}

.locked-question-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--s2);
  border: 1px dashed var(--line2);
  box-shadow: inset 0 0 12px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s var(--ease), border-color 0.2s var(--ease);
}

.puzzle-card.is-locked:hover .locked-question-circle {
  border-color: var(--tx3);
  transform: scale(1.05);
}

.locked-question-mark {
  font: 700 28px/1 var(--f-mono);
  color: var(--tx3);
  user-select: none;
}

.locked-hint {
  font: 500 11px/1.4 var(--f-mono);
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--tx3);
  max-width: 220px;
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
