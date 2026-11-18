<script setup>
import { computed, onBeforeUnmount, onMounted, ref, useId } from 'vue'

const props = defineProps({
  file: { type: File, required: true },
  title: { type: String, required: true },
  outputWidth: { type: Number, required: true },
  outputHeight: { type: Number, required: true },
})

const emit = defineEmits(['confirm', 'cancel'])

const MAXIMUM_ZOOM = 4
const KEYBOARD_STEP = 12
const IS_SQUARE = props.outputWidth === props.outputHeight

const dialogElement = ref(null)
const imageElement = ref(null)
const titleId = useId()
const sourceUrl = URL.createObjectURL(props.file)

const naturalWidth = ref(0)
const naturalHeight = ref(0)
const zoom = ref(1)
const offsetX = ref(0)
const offsetY = ref(0)
const isExporting = ref(false)
const errorMessage = ref('')

// Khung cắt có đúng tỷ lệ của ảnh kết quả; thu nhỏ khi màn hình hẹp.
const viewportWidth = Math.min(IS_SQUARE ? 320 : 640, window.innerWidth - 80)
const viewportHeight = Math.round((viewportWidth * props.outputHeight) / props.outputWidth)

const isLoaded = computed(() => naturalWidth.value > 0)
// Ở mức zoom 1 ảnh vừa đủ phủ kín khung, nên không bao giờ lộ khoảng trống.
const displayScale = computed(() => Math.max(viewportWidth / naturalWidth.value, viewportHeight / naturalHeight.value) * zoom.value)
const imageStyle = computed(() =>
  isLoaded.value
    ? {
        width: `${naturalWidth.value * displayScale.value}px`,
        height: `${naturalHeight.value * displayScale.value}px`,
        transform: `translate(${offsetX.value}px, ${offsetY.value}px)`,
      }
    : {},
)

function clampOffsets() {
  offsetX.value = Math.min(0, Math.max(viewportWidth - naturalWidth.value * displayScale.value, offsetX.value))
  offsetY.value = Math.min(0, Math.max(viewportHeight - naturalHeight.value * displayScale.value, offsetY.value))
}

function handleImageLoaded() {
  naturalWidth.value = imageElement.value.naturalWidth
  naturalHeight.value = imageElement.value.naturalHeight
  offsetX.value = (viewportWidth - naturalWidth.value * displayScale.value) / 2
  offsetY.value = (viewportHeight - naturalHeight.value * displayScale.value) / 2
  clampOffsets()
}

// Giữ nguyên điểm đang nằm giữa khung khi phóng to hoặc thu nhỏ.
function changeZoom(nextZoom) {
  const centerX = (viewportWidth / 2 - offsetX.value) / displayScale.value
  const centerY = (viewportHeight / 2 - offsetY.value) / displayScale.value
  zoom.value = Math.min(MAXIMUM_ZOOM, Math.max(1, nextZoom))
  offsetX.value = viewportWidth / 2 - centerX * displayScale.value
  offsetY.value = viewportHeight / 2 - centerY * displayScale.value
  clampOffsets()
}

let dragStart = null

function startDrag(event) {
  dragStart = { x: event.clientX - offsetX.value, y: event.clientY - offsetY.value }
  event.currentTarget.setPointerCapture(event.pointerId)
}

function drag(event) {
  if (!dragStart) return
  offsetX.value = event.clientX - dragStart.x
  offsetY.value = event.clientY - dragStart.y
  clampOffsets()
}

function endDrag() {
  dragStart = null
}

function moveWithArrowKeys(event) {
  const moves = { ArrowLeft: [KEYBOARD_STEP, 0], ArrowRight: [-KEYBOARD_STEP, 0], ArrowUp: [0, KEYBOARD_STEP], ArrowDown: [0, -KEYBOARD_STEP] }
  const move = moves[event.key]
  if (!move) return
  event.preventDefault()
  offsetX.value += move[0]
  offsetY.value += move[1]
  clampOffsets()
}

function confirmCrop() {
  isExporting.value = true
  const canvas = document.createElement('canvas')
  canvas.width = props.outputWidth
  canvas.height = props.outputHeight
  const context = canvas.getContext('2d')
  context.imageSmoothingQuality = 'high'
  const scale = displayScale.value
  context.drawImage(
    imageElement.value,
    -offsetX.value / scale,
    -offsetY.value / scale,
    viewportWidth / scale,
    viewportHeight / scale,
    0,
    0,
    props.outputWidth,
    props.outputHeight,
  )
  canvas.toBlob(
    (blob) => {
      if (blob) {
        emit('confirm', blob)
      } else {
        errorMessage.value = 'Could not crop this image. Try another one.'
        isExporting.value = false
      }
    },
    'image/webp',
    0.85,
  )
}

// showModal() chỉ gọi được sau khi phần tử đã gắn vào DOM; nó cũng tự khóa focus bên trong hộp thoại.
onMounted(() => dialogElement.value.showModal())
onBeforeUnmount(() => URL.revokeObjectURL(sourceUrl))
</script>

<template>
  <dialog ref="dialogElement" class="dialog" :aria-labelledby="titleId" @cancel.prevent="emit('cancel')">
    <h2 :id="titleId">{{ title }}</h2>
    <p class="hint">Drag the image to choose the visible part. The result is {{ outputWidth }} × {{ outputHeight }}.</p>

    <div
      class="viewport"
      :style="{ width: `${viewportWidth}px`, height: `${viewportHeight}px` }"
      tabindex="0"
      role="group"
      aria-label="Image position. Drag, or use the arrow keys, to move the image."
      @pointerdown="startDrag"
      @pointermove="drag"
      @pointerup="endDrag"
      @pointercancel="endDrag"
      @keydown="moveWithArrowKeys"
      @wheel.prevent="changeZoom(zoom - $event.deltaY * 0.002)"
    >
      <img ref="imageElement" class="source" :src="sourceUrl" alt="" draggable="false" :style="imageStyle" @load="handleImageLoaded" @error="errorMessage = 'This image could not be opened.'" />
    </div>

    <label class="zoom">
      Zoom
      <input type="range" min="1" :max="MAXIMUM_ZOOM" step="0.01" :value="zoom" :disabled="!isLoaded" @input="changeZoom(Number($event.target.value))" />
    </label>

    <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>

    <div class="buttons">
      <button type="button" class="btn" :disabled="isExporting" @click="emit('cancel')">Cancel</button>
      <button type="button" class="btn p" :disabled="!isLoaded || isExporting" :aria-busy="isExporting" @click="confirmCrop">Use this crop</button>
    </div>
  </dialog>
</template>

<style scoped>
.dialog {
  width: fit-content;
  max-width: calc(100vw - var(--s-5));
  padding: var(--s-5);
  color: var(--tx);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-lg);
}

.dialog::backdrop {
  background: rgba(0, 0, 0, 0.5);
}

h2 {
  font: 600 var(--t-lg) var(--f-head);
}

.hint {
  margin: var(--s-1) 0 var(--s-4);
  font-size: var(--t-sm);
  color: var(--tx2);
}

.viewport {
  position: relative;
  overflow: hidden;
  cursor: grab;
  touch-action: none;
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
}

.viewport:active {
  cursor: grabbing;
}

.viewport:focus-visible {
  outline: 2px solid var(--ac);
  outline-offset: 2px;
}

.source {
  position: absolute;
  top: 0;
  left: 0;
  max-width: none;
  user-select: none;
  pointer-events: none;
}

.zoom {
  display: flex;
  gap: var(--s-3);
  align-items: center;
  margin-top: var(--s-4);
  font-size: var(--t-sm);
}

.zoom input {
  flex: 1;
  accent-color: var(--ac);
}

.error {
  margin-top: var(--s-3);
  font-size: var(--t-sm);
  color: var(--bad);
}

.buttons {
  display: flex;
  gap: var(--s-2);
  justify-content: flex-end;
  margin-top: var(--s-5);
}
</style>
