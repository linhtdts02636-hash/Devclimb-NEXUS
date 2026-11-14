<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import BaseModal from '../common/BaseModal.vue'
import { createPost } from '../../features/post/api/postApi'
import { getPostErrorMessage } from '../../features/post/utils/postError'

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  communityId: {
    type: Number,
    required: true,
  },
})

const emit = defineEmits(['update:open', 'created'])

const content = ref('')
const error = ref('')
const submitting = ref(false)
const textareaRef = ref(null)

const trimmedContent = computed(() => content.value.trim())
const canSubmit = computed(
  () =>
    Number.isInteger(props.communityId) &&
    props.communityId > 0 &&
    trimmedContent.value.length > 0 &&
    !submitting.value,
)

watch(
  () => props.open,
  (open) => {
    if (open) {
      error.value = ''
      nextTick(() => textareaRef.value?.focus())
      return
    }

    content.value = ''
    error.value = ''
    submitting.value = false
  },
)

async function submit() {
  if (!Number.isInteger(props.communityId) || props.communityId <= 0) {
    error.value = 'Cộng đồng không hợp lệ.'
    return
  }

  if (trimmedContent.value.length === 0) {
    error.value = 'Nội dung bài viết không được để trống.'
    textareaRef.value?.focus()
    return
  }

  if (submitting.value) {
    return
  }

  submitting.value = true
  error.value = ''

  try {
    const post = await createPost(props.communityId, trimmedContent.value)
    emit('created', post)
    emit('update:open', false)
  } catch (requestError) {
    error.value = getPostErrorMessage(
      requestError,
      'Không thể đăng bài viết. Vui lòng thử lại.',
    )
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <BaseModal
    :open="open"
    title="Tạo bài viết"
    size="md"
    :close-on-backdrop="!submitting"
    @update:open="$emit('update:open', $event)"
  >
    <form id="post-composer-form" novalidate @submit.prevent="submit">
      <p class="composer__context">
        Đăng trong cộng đồng <span class="mono">#{{ communityId }}</span>
      </p>

      <label class="sr-only" for="post-content">Nội dung bài viết</label>
      <textarea
        id="post-content"
        ref="textareaRef"
        v-model="content"
        class="composer__textarea"
        data-autofocus
        placeholder="Bạn đang nghĩ gì?"
        :aria-invalid="Boolean(error)"
        :aria-describedby="error ? 'post-content-error' : undefined"
        @input="error = ''"
        @keydown.ctrl.enter.prevent="submit"
        @keydown.meta.enter.prevent="submit"
      ></textarea>

      <div class="composer__meta">
        <span class="mono">{{ content.length }} ký tự</span>
        <span class="composer__shortcut">Ctrl/⌘ + Enter để gửi</span>
      </div>

      <p v-if="error" id="post-content-error" class="composer__error" role="alert">
        {{ error }}
      </p>
    </form>

    <template #footer>
      <button
        type="button"
        class="btn btn--ghost"
        :disabled="submitting"
        @click="$emit('update:open', false)"
      >
        Hủy
      </button>
      <button
        type="submit"
        form="post-composer-form"
        class="btn btn--primary"
        :disabled="!canSubmit"
      >
        <span v-if="submitting" class="spinner" aria-hidden="true"></span>
        {{ submitting ? 'Đang đăng…' : 'Đăng bài' }}
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
.composer__context {
  margin-bottom: 10px;
  color: var(--tx2);
  font-size: 14px;
}

.composer__textarea {
  display: block;
  width: 100%;
  min-height: 168px;
  padding: 12px 14px;
  resize: vertical;
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  background: var(--bg);
  color: var(--tx);
  font-size: 15px;
  line-height: 1.6;
}

.composer__textarea::placeholder {
  color: var(--tx2);
}

.composer__textarea:focus {
  border-color: var(--ac);
  outline: none;
  box-shadow: 0 0 0 3px var(--ac-soft);
}

.composer__textarea[aria-invalid='true'] {
  border-color: var(--bad);
}

.composer__meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
  color: var(--tx2);
  font-size: 12px;
}

.composer__shortcut {
  font-family: var(--mono);
}

.composer__error {
  margin-top: 12px;
  padding: 10px 12px;
  border: 1px solid color-mix(in srgb, var(--bad) 36%, transparent);
  border-radius: var(--r-sm);
  background: color-mix(in srgb, var(--bad) 8%, transparent);
  color: var(--bad);
  font-size: 13px;
  line-height: 1.45;
}

@media (max-width: 480px) {
  .composer__textarea {
    min-height: 150px;
  }

  .composer__shortcut {
    display: none;
  }
}
</style>
