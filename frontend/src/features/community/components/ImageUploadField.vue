<script setup>
import { ref, useId } from 'vue'
import { describeApiError } from '../../../config/apiClient'
import { uploadCommunityImage } from '../communityApi'
import ImageCropDialog from './ImageCropDialog.vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  label: { type: String, required: true },
  purpose: { type: String, required: true },
  disabled: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue'])

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAXIMUM_BYTES = { IMAGE: 2 * 1024 * 1024, BANNER: 5 * 1024 * 1024 }
// Ảnh chọn từ máy được cắt và thu về đúng kích thước này trong hộp thoại cắt trước khi tải lên, nên ảnh gốc có thể lớn hơn giới hạn của server.
const MAXIMUM_SOURCE_BYTES = 20 * 1024 * 1024
const TARGET_SIZES = { IMAGE: { width: 256, height: 256 }, BANNER: { width: 1200, height: 176 } }
const SIZE_HINT = `Any size works. You choose the visible part, and it is saved as ${TARGET_SIZES[props.purpose].width} × ${TARGET_SIZES[props.purpose].height}.`

const inputId = useId()
const isUploading = ref(false)
const errorMessage = ref('')
const pendingFile = ref(null)

function handleFileChosen(event) {
  const file = event.target.files[0]
  event.target.value = ''
  if (!file) return

  errorMessage.value = ''
  if (!ALLOWED_TYPES.includes(file.type) || file.size > MAXIMUM_SOURCE_BYTES) {
    errorMessage.value = 'Use a JPEG, PNG or WebP image up to 20 MB.'
    return
  }
  pendingFile.value = file
}

async function uploadCroppedImage(croppedImage) {
  pendingFile.value = null
  if (croppedImage.size > MAXIMUM_BYTES[props.purpose]) {
    errorMessage.value = 'This image is too detailed to upload. Try a simpler one.'
    return
  }
  isUploading.value = true
  try {
    emit('update:modelValue', await uploadCommunityImage(props.purpose, croppedImage))
  } catch (error) {
    errorMessage.value = describeApiError(error).message
  } finally {
    isUploading.value = false
  }
}
</script>

<template>
  <div class="upload-field">
    <span class="label">{{ label }}</span>
    <div class="row">
      <div class="preview" :class="purpose.toLowerCase()">
        <img v-if="modelValue" :src="modelValue" alt="" />
      </div>
      <div class="actions">
        <label :for="inputId" class="btn small" :class="{ disabled: disabled || isUploading }">
          {{ isUploading ? 'Uploading…' : modelValue ? 'Replace' : 'Upload' }}
        </label>
        <input
          :id="inputId"
          class="file-input"
          type="file"
          accept="image/jpeg,image/png,image/webp"
          :disabled="disabled || isUploading"
          @change="handleFileChosen"
        />
        <button v-if="modelValue" type="button" class="btn small" :disabled="disabled || isUploading" @click="emit('update:modelValue', '')">
          Remove
        </button>
      </div>
    </div>
    <p class="hint">{{ SIZE_HINT }}</p>
    <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>
    <ImageCropDialog
      v-if="pendingFile"
      :file="pendingFile"
      :title="`Crop ${label.toLowerCase()}`"
      :output-width="TARGET_SIZES[purpose].width"
      :output-height="TARGET_SIZES[purpose].height"
      @confirm="uploadCroppedImage"
      @cancel="pendingFile = null"
    />
  </div>
</template>

<style scoped>
.upload-field {
  display: grid;
  gap: var(--s-2);
}

.label {
  font-size: var(--t-sm);
  font-weight: 500;
}

.row {
  display: flex;
  align-items: center;
  gap: var(--s-4);
}

.preview {
  flex: none;
  overflow: hidden;
  background: var(--s2);
  border: 1px solid var(--line2);
}

.preview.image {
  width: 72px;
  height: 72px;
  border-radius: var(--r);
}

.preview.banner {
  width: 216px;
  height: 68px;
  border-radius: var(--r-sm);
}

.preview img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.actions {
  display: flex;
  gap: var(--s-2);
}

.small {
  height: 32px;
  padding: 0 var(--s-3);
  font-size: var(--t-sm);
}

.disabled {
  opacity: 0.5;
  pointer-events: none;
}

.file-input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.file-input:focus-visible + .btn,
.btn:has(+ .file-input:focus-visible) {
  outline: 2px solid var(--ac);
  outline-offset: 2px;
}

.hint {
  font-size: var(--t-xs);
  color: var(--tx2);
}

.error {
  font-size: var(--t-sm);
  color: var(--bad);
}
</style>
