<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

const WORDMARK_TEXT = 'DEVCLIMB'
const CELL_SIZE = 5
const HORIZONTAL_PADDING = 96
const MINIMUM_COLUMNS = 20
const ROW_TO_COLUMN_RATIO = 0.16
const FONT_TO_ROW_RATIO = 1.05
const MAXIMUM_TEXT_WIDTH_RATIO = 0.98
const BRIGHTNESS_THRESHOLD = 120
const MAXIMUM_PIXEL_RATIO = 2

const CORNER_LABELS = [
  { text: '[ 200 OK ]', position: 'top-left' },
  { text: '[ ACCEPTED ]', position: 'top-right' },
  { text: '[ .PY ]', position: 'bottom-left' },
  { text: '[ .JAVA ]', position: 'bottom-right' },
]

const panelElement = ref(null)
const wordmarkCanvas = ref(null)

let litCells = []
let columnCount = 0
let rowCount = 0
let resizeObserver = null
let themeObserver = null

function setWordmarkFont(context, fontSize) {
  context.font = `600 ${fontSize}px "Inter Tight", sans-serif`
}

function rasterizeWordmark() {
  const availableWidth = panelElement.value.clientWidth - HORIZONTAL_PADDING
  columnCount = Math.max(MINIMUM_COLUMNS, Math.floor(availableWidth / CELL_SIZE))
  rowCount = Math.round(columnCount * ROW_TO_COLUMN_RATIO)

  // Vẽ chữ với mỗi ô là một pixel rồi đọc lại, để chuyển nét chữ thành lưới ô vuông.
  const offscreenCanvas = document.createElement('canvas')
  offscreenCanvas.width = columnCount
  offscreenCanvas.height = rowCount
  const context = offscreenCanvas.getContext('2d', { willReadFrequently: true })
  context.fillStyle = '#000'
  context.fillRect(0, 0, columnCount, rowCount)
  context.fillStyle = '#fff'
  context.textAlign = 'center'
  context.textBaseline = 'middle'

  const fontSize = Math.round(rowCount * FONT_TO_ROW_RATIO)
  setWordmarkFont(context, fontSize)
  const textWidth = context.measureText(WORDMARK_TEXT).width
  const maximumTextWidth = columnCount * MAXIMUM_TEXT_WIDTH_RATIO
  if (textWidth > maximumTextWidth) {
    setWordmarkFont(context, Math.round((fontSize * maximumTextWidth) / textWidth))
  }
  context.fillText(WORDMARK_TEXT, columnCount / 2, rowCount * 0.55)

  const pixels = context.getImageData(0, 0, columnCount, rowCount).data
  litCells = []
  for (let row = 0; row < rowCount; row++) {
    for (let column = 0; column < columnCount; column++) {
      if (pixels[(row * columnCount + column) * 4] > BRIGHTNESS_THRESHOLD) {
        litCells.push({ column, row })
      }
    }
  }
}

function drawWordmark() {
  const canvas = wordmarkCanvas.value
  if (!canvas || litCells.length === 0) return

  const pixelRatio = Math.min(window.devicePixelRatio || 1, MAXIMUM_PIXEL_RATIO)
  canvas.width = columnCount * CELL_SIZE * pixelRatio
  canvas.height = rowCount * CELL_SIZE * pixelRatio
  canvas.style.width = `${columnCount * CELL_SIZE}px`

  const context = canvas.getContext('2d')
  context.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0)
  context.fillStyle = getComputedStyle(document.documentElement).getPropertyValue('--tx3').trim()
  litCells.forEach(({ column, row }) => {
    context.fillRect(column * CELL_SIZE + 1, row * CELL_SIZE + 1, CELL_SIZE - 2, CELL_SIZE - 2)
  })
}

function renderWordmark() {
  if (!panelElement.value || panelElement.value.clientWidth === 0) return
  rasterizeWordmark()
  drawWordmark()
}

onMounted(() => {
  document.fonts.ready.then(renderWordmark)
  resizeObserver = new ResizeObserver(renderWordmark)
  resizeObserver.observe(panelElement.value)
  themeObserver = new MutationObserver(drawWordmark)
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  themeObserver?.disconnect()
})
</script>

<template>
  <aside ref="panelElement" class="visual-panel" aria-hidden="true">
    <span v-for="label in CORNER_LABELS" :key="label.text" class="corner-label" :class="label.position">
      {{ label.text }}
    </span>

    <div class="wordmark-block">
      <canvas ref="wordmarkCanvas" class="wordmark"></canvas>
      <p class="tagline">A competitive programming platform run by its communities.</p>
    </div>
  </aside>
</template>

<style scoped>
.visual-panel {
  position: relative;
  display: grid;
  place-items: center;
  padding: var(--s-8) var(--s-7);
  overflow: hidden;
  background-image:
    radial-gradient(circle, color-mix(in srgb, var(--tx3) 70%, transparent) 1.5px, transparent 2px),
    linear-gradient(to right, var(--line) 1px, transparent 1px),
    linear-gradient(to bottom, var(--line) 1px, transparent 1px);
  background-size: 104px 104px;
}

.corner-label {
  position: absolute;
  padding: 2px var(--s-1);
  background: var(--bg);
  font: 400 var(--t-xs) var(--f-mono);
  letter-spacing: 0.04em;
  color: var(--tx2);
}

.top-left {
  top: var(--s-4);
  left: var(--s-4);
}

.top-right {
  top: var(--s-4);
  right: var(--s-4);
}

.bottom-left {
  bottom: var(--s-4);
  left: var(--s-4);
}

.bottom-right {
  bottom: var(--s-4);
  right: var(--s-4);
}

.wordmark-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--s-4);
  max-width: 100%;
  padding: var(--s-5);
  background: var(--bg);
}

.wordmark {
  display: block;
  max-width: 100%;
  height: auto;
}

.tagline {
  font-size: var(--t-sm);
  color: var(--tx2);
  text-align: center;
}
</style>
