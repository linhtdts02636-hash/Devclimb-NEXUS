<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import BaseModal from '../common/BaseModal.vue'
import { createPost, updatePost } from '../../features/post/api/postApi'
import { getApiErrorMessage } from '../../features/post/utils/apiError'

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  communityId: {
    type: Number,
    required: true,
  },
  // Có post nghĩa là đang sửa; không có post nghĩa là tạo mới.
  post: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['update:open', 'created', 'updated'])

const title = ref('')
const content = ref('')
const error = ref('')
const submitting = ref(false)
const titleInputRef = ref(null)
const textareaRef = ref(null)

const isEditing = computed(() => Number.isInteger(props.post?.id))
const trimmedTitle = computed(() => title.value.trim())
const trimmedContent = computed(() => content.value.trim())
const canSubmit = computed(() => {
  if (submitting.value || trimmedContent.value.length === 0) {
    return false
  }

  if (isEditing.value) {
    return true
  }

  return Number.isInteger(props.communityId) && props.communityId > 0
})

function resetForm() {
  title.value = ''
  content.value = ''
  error.value = ''
  submitting.value = false
}

function syncFields() {
  resetForm()
  title.value = props.post?.title || ''
  content.value = props.post?.content || ''
}

watch(
  [() => props.open, () => props.post],
  ([open]) => {
    if (open) {
      syncFields()
      nextTick(() => titleInputRef.value?.focus())
      return
    }

    resetForm()
  },
)

async function submit() {
  if (trimmedContent.value.length === 0) {
    error.value = 'Nội dung bài viết không được để trống.'
    textareaRef.value?.focus()
    return
  }

  if (submitting.value) {
    return
  }

  if (isEditing.value) {
    submitting.value = true
    error.value = ''

    try {
      const updated = await updatePost(props.post.id, {
        title: trimmedTitle.value,
        content: trimmedContent.value,
      })
      emit('updated', updated)
      emit('update:open', false)
    } catch (requestError) {
      error.value = getApiErrorMessage(
        requestError,
        'Không thể lưu thay đổi. Vui lòng thử lại.',
      )
    } finally {
      submitting.value = false
    }
    return
  }

  if (!Number.isInteger(props.communityId) || props.communityId <= 0) {
    error.value = 'Cộng đồng không hợp lệ.'
    return
  }

  submitting.value = true
  error.value = ''

  try {
    const post = await createPost(props.communityId, {
      title: trimmedTitle.value,
      content: trimmedContent.value,
    })
    emit('created', post)
    emit('update:open', false)
  } catch (requestError) {
    error.value = getApiErrorMessage(
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
    :title="isEditing ? 'Chỉnh sửa bài viết' : 'Tạo bài viết'"
    size="md"
    :close-on-backdrop="!submitting"
    @update:open="$emit('update:open', $event)"
  >
    <form id="post-composer-form" novalidate @submit.prevent="submit">
      <p class="composer__context">
        <template v-if="isEditing">
          Chỉnh sửa tiêu đề và nội dung. Bài viết không có phần ảnh.
        </template>
        <template v-else>
          Đăng trong cộng đồng <span class="mono">#{{ communityId }}</span>
        </template>
      </p>

      <div class="composer__field">
        <label class="composer__label" for="post-title">Tiêu đề</label>
        <input
          id="post-title"
          ref="titleInputRef"
          v-model="title"
          class="composer__input"
          type="text"
          maxlength="255"
          placeholder="Tiêu đề bài viết (không bắt buộc)"
          :aria-invalid="Boolean(error)"
          @input="error = ''"
          @keydown.ctrl.enter.prevent="submit"
          @keydown.meta.enter.prevent="submit"
        />
        <span class="composer__hint">Không bắt buộc · {{ title.length }}/255 ký tự</span>
      </div>

      <div class="composer__field">
        <label class="composer__label" for="post-content">Nội dung bài viết</label>
        <textarea
          id="post-content"
          ref="textareaRef"
          v-model="content"
          class="composer__textarea"
          placeholder="Bạn đang nghĩ gì?"
          :aria-invalid="Boolean(error)"
          @input="error = ''"
          @keydown.ctrl.enter.prevent="submit"
          @keydown.meta.enter.prevent="submit"
        ></textarea>
      </div>

      <div class="composer__meta">
        <span class="mono">{{ content.length }} ký tự</span>
        <span class="composer__shortcut">Ctrl/⌘ + Enter để gửi</span>
      </div>

      <p v-if="error" class="composer__error" role="alert">
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
        <template v-if="isEditing">
          {{ submitting ? 'Đang lưu…' : 'Lưu thay đổi' }}
        </template>
        <template v-else>
          {{ submitting ? 'Đang đăng…' : 'Đăng bài' }}
        </template>
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

.composer__field {
  display: grid;
  gap: 6px;
}

.composer__field + .composer__field {
  margin-top: 14px;
}

.composer__label {
  color: var(--tx);
  font-size: 13px;
  font-weight: 600;
}

.composer__input,
.composer__textarea {
  display: block;
  width: 100%;
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  background: var(--bg);
  color: var(--tx);
  font-size: 15px;
  line-height: 1.6;
}

.composer__input {
  padding: 10px 12px;
}

.composer__textarea {
  min-height: 168px;
  padding: 12px 14px;
  resize: vertical;
}

.composer__input::placeholder,
.composer__textarea::placeholder {
  color: var(--tx2);
}

.composer__input:focus,
.composer__textarea:focus {
  border-color: var(--ac);
  outline: none;
  box-shadow: 0 0 0 3px var(--ac-soft);
}

.composer__input[aria-invalid='true'],
.composer__textarea[aria-invalid='true'] {
  border-color: var(--bad);
}

.composer__hint {
  color: var(--tx2);
  font-size: 12px;
  text-align: right;
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
