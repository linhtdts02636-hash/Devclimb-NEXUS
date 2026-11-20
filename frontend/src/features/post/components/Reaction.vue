<script setup>
import { computed, ref, watch } from 'vue'
import { describeApiError } from '../../../config/apiClient'
import { removePostReaction, setPostReaction } from '../postApi'

const REACTION_ERROR = 'Your reaction could not be saved. Try again.'

const props = defineProps({
  postId: {
    type: Number,
    required: true,
  },
  reaction: {
    type: Object,
    default: () => ({ upvotes: 0, downvotes: 0, myReaction: null }),
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

const upvotes = ref(0)
const downvotes = ref(0)
const myReaction = ref(null)
const pending = ref(false)
const error = ref('')
const errorDetail = ref('')

function syncFromReaction(value) {
  upvotes.value = Number.isFinite(value?.upvotes) ? value.upvotes : 0
  downvotes.value = Number.isFinite(value?.downvotes) ? value.downvotes : 0
  myReaction.value = value?.myReaction === 'LIKE' || value?.myReaction === 'DISLIKE'
    ? value.myReaction
    : null
}

watch(
  () => props.reaction,
  syncFromReaction,
  { immediate: true, deep: true },
)

const upvoteActive = computed(() => myReaction.value === 'LIKE')
const downvoteActive = computed(() => myReaction.value === 'DISLIKE')
const buttonsDisabled = computed(() => props.disabled || pending.value)
// Khách chỉ thấy icon và số; câu nhắc đăng nhập chỉ dành cho trình đọc màn hình.
const signInHint = computed(() => (props.disabled ? '. Sign in to vote.' : ''))

// Cập nhật tức thời trước khi server phản hồi.
function applyOptimisticReaction(nextReaction) {
  let nextUpvotes = upvotes.value
  let nextDownvotes = downvotes.value

  if (myReaction.value === 'LIKE') nextUpvotes -= 1
  if (myReaction.value === 'DISLIKE') nextDownvotes -= 1
  if (nextReaction === 'LIKE') nextUpvotes += 1
  if (nextReaction === 'DISLIKE') nextDownvotes += 1

  upvotes.value = Math.max(0, nextUpvotes)
  downvotes.value = Math.max(0, nextDownvotes)
  myReaction.value = nextReaction
}

async function chooseReaction(type) {
  if (buttonsDisabled.value) return

  const previous = {
    upvotes: upvotes.value,
    downvotes: downvotes.value,
    myReaction: myReaction.value,
  }
  // Bấm lại đúng nút đang chọn nghĩa là bỏ reaction hiện tại.
  const nextReaction = myReaction.value === type ? null : type

  applyOptimisticReaction(nextReaction)
  pending.value = true
  error.value = ''
  errorDetail.value = ''

  try {
    const summary = nextReaction === null
      ? await removePostReaction(props.postId)
      : await setPostReaction(props.postId, nextReaction)
    syncFromReaction(summary)
  } catch (requestError) {
    // Khôi phục snapshot cũ rồi hiện lỗi cố định kèm chi tiết từ backend.
    upvotes.value = previous.upvotes
    downvotes.value = previous.downvotes
    myReaction.value = previous.myReaction
    error.value = REACTION_ERROR
    const detail = describeApiError(requestError).message
    errorDetail.value = detail && detail !== REACTION_ERROR ? detail : ''
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <div class="reaction" :aria-busy="pending">
    <div class="reaction__buttons" role="group" aria-label="Post reaction">
      <button
        type="button"
        class="reaction__button reaction__button--up"
        :class="{ 'is-active': upvoteActive }"
        :disabled="buttonsDisabled"
        :aria-pressed="upvoteActive"
        :aria-label="`Upvote, ${upvotes} ${upvotes === 1 ? 'vote' : 'votes'}${signInHint}`"
        @click="chooseReaction('LIKE')"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M7 10v10H4a1 1 0 0 1-1-1v-8a1 1 0 0 1 1-1z" />
          <path d="m7 10 4-7a2 2 0 0 1 2.8 1.9V9h4.4a2 2 0 0 1 2 2.4l-1.3 7A2 2 0 0 1 17 20H7z" />
        </svg>
        <span class="reaction__count">{{ upvotes }}</span>
      </button>

      <button
        type="button"
        class="reaction__button reaction__button--down"
        :class="{ 'is-active': downvoteActive }"
        :disabled="buttonsDisabled"
        :aria-pressed="downvoteActive"
        :aria-label="`Downvote, ${downvotes} ${downvotes === 1 ? 'vote' : 'votes'}${signInHint}`"
        @click="chooseReaction('DISLIKE')"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M7 14V4H4a1 1 0 0 0-1 1v8a1 1 0 0 0 1 1z" />
          <path d="m7 14 4 7a2 2 0 0 0 2.8-1.9V15h4.4a2 2 0 0 0 2-2.4l-1.3-7A2 2 0 0 0 17 4H7z" />
        </svg>
        <span class="reaction__count">{{ downvotes }}</span>
      </button>
    </div>

    <p v-if="error" class="reaction__error" role="alert">
      {{ error }}
      <span v-if="errorDetail" class="reaction__error-detail">{{ errorDetail }}</span>
    </p>
  </div>
</template>

<style scoped>
/* Chỉ icon và số hiển thị; nhãn chữ nằm trong aria-label cho trình đọc màn hình. */
.reaction {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--s-2);
  min-width: 0;
  margin-top: var(--s-3);
}

.reaction__buttons {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s-2);
  min-width: 0;
}

.reaction__button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  min-height: 34px;
  padding: 5px 10px;
  color: var(--tx2);
  font-size: var(--t-sm);
  font-weight: 500;
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: 999px;
}

.reaction__button:hover:not(:disabled) {
  color: var(--tx);
  background: var(--s3);
}

.reaction__button:focus-visible {
  outline: 2px solid var(--ac);
  outline-offset: 2px;
}

.reaction__button:disabled {
  cursor: not-allowed;
  opacity: 0.58;
}

.reaction__button--up.is-active:not(:disabled) {
  color: var(--ac);
  background: var(--ac-soft);
  border-color: color-mix(in srgb, var(--ac) 40%, var(--line2));
}

.reaction__button--down.is-active:not(:disabled) {
  color: var(--bad);
  background: color-mix(in srgb, var(--bad) 10%, transparent);
  border-color: color-mix(in srgb, var(--bad) 40%, var(--line2));
}

.reaction__button svg {
  width: 15px;
  height: 15px;
  flex: 0 0 auto;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.reaction__count {
  min-width: 1ch;
  font-variant-numeric: tabular-nums;
}

.reaction__error {
  flex-basis: 100%;
  margin: 0;
  color: var(--bad);
  font-size: var(--t-xs);
  line-height: 1.45;
}

.reaction__error-detail {
  display: block;
  margin-top: 2px;
  color: color-mix(in srgb, var(--bad) 78%, var(--tx2));
}

@media (max-width: 360px) {
  .reaction__button {
    padding-inline: 8px;
  }
}
</style>
