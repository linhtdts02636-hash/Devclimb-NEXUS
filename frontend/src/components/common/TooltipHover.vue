<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  offsetX: { type: Number, default: 14 },
  offsetY: { type: Number, default: 14 },
  disabled: { type: Boolean, default: false },
})

const rootAnchor = ref(null)
const tooltipElement = ref(null)
const isVisible = ref(false)
const hasPositioned = ref(false)
const coords = ref({ x: 0, y: 0 })
let parentElement = null
let rafId = null

// Cập nhật tọa độ tooltip theo vị trí con trỏ chuột, tự động đảo hướng khi chạm mép màn hình
function updatePosition(clientX, clientY) {
  let posX = clientX + props.offsetX
  let posY = clientY + props.offsetY

  if (tooltipElement.value) {
    const width = tooltipElement.value.offsetWidth || 300
    const height = tooltipElement.value.offsetHeight || 120
    const pad = 12

    // Đảo sang bên trái con trỏ chuột nếu tràn màn hình bên phải
    if (posX + width > window.innerWidth - pad) {
      posX = clientX - width - props.offsetX
    }
    // Đảo lên phía trên con trỏ chuột nếu tràn màn hình bên dưới
    if (posY + height > window.innerHeight - pad) {
      posY = clientY - height - props.offsetY
    }

    posX = Math.max(pad, posX)
    posY = Math.max(pad, posY)
  }

  coords.value = { x: posX, y: posY }
}

function handleMouseEnter(event) {
  if (props.disabled) return
  if (rafId) cancelAnimationFrame(rafId)
  updatePosition(event.clientX, event.clientY)
  hasPositioned.value = false
  isVisible.value = true
  // Kích hoạt transition mượt sau frame đầu tiên để tránh bị trượt từ tọa độ (0, 0)
  rafId = requestAnimationFrame(() => {
    hasPositioned.value = true
  })
}

function handleMouseMove(event) {
  if (props.disabled) return
  if (!isVisible.value) {
    if (rafId) cancelAnimationFrame(rafId)
    updatePosition(event.clientX, event.clientY)
    hasPositioned.value = false
    isVisible.value = true
    rafId = requestAnimationFrame(() => {
      hasPositioned.value = true
    })
    return
  }
  updatePosition(event.clientX, event.clientY)
}

function handleMouseLeave() {
  if (rafId) cancelAnimationFrame(rafId)
  isVisible.value = false
  hasPositioned.value = false
}

// Hiển thị tooltip khi người dùng điều hướng bằng bàn phím
function handleFocusIn() {
  if (props.disabled || !parentElement) return
  if (rafId) cancelAnimationFrame(rafId)
  const rect = parentElement.getBoundingClientRect()
  updatePosition(rect.left + 20, rect.top + 20)
  hasPositioned.value = false
  isVisible.value = true
  rafId = requestAnimationFrame(() => {
    hasPositioned.value = true
  })
}

function handleFocusOut(event) {
  if (parentElement?.contains(event.relatedTarget)) return
  if (rafId) cancelAnimationFrame(rafId)
  isVisible.value = false
  hasPositioned.value = false
}

onMounted(() => {
  parentElement = rootAnchor.value?.parentElement
  if (!parentElement) return

  parentElement.addEventListener('mouseenter', handleMouseEnter)
  parentElement.addEventListener('mousemove', handleMouseMove)
  parentElement.addEventListener('mouseleave', handleMouseLeave)
  parentElement.addEventListener('focusin', handleFocusIn)
  parentElement.addEventListener('focusout', handleFocusOut)
})

onBeforeUnmount(() => {
  if (rafId) cancelAnimationFrame(rafId)
  if (!parentElement) return
  parentElement.removeEventListener('mouseenter', handleMouseEnter)
  parentElement.removeEventListener('mousemove', handleMouseMove)
  parentElement.removeEventListener('mouseleave', handleMouseLeave)
  parentElement.removeEventListener('focusin', handleFocusIn)
  parentElement.removeEventListener('focusout', handleFocusOut)
})

const tooltipStyle = computed(() => ({
  transform: `translate3d(${coords.value.x}px, ${coords.value.y}px, 0)`,
}))
</script>

<template>
  <span ref="rootAnchor" class="tooltip-hover-anchor" aria-hidden="true" />
  <Teleport to="body">
    <div
      ref="tooltipElement"
      class="tooltip-hover"
      :class="{
        'is-visible': isVisible && !disabled,
        'is-smooth': hasPositioned && isVisible
      }"
      :style="tooltipStyle"
      role="tooltip"
      aria-hidden="true"
    >
      <div class="tooltip-hover-content">
        <slot />
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.tooltip-hover-anchor {
  display: none;
}

.tooltip-hover {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 9999;
  pointer-events: none;
  visibility: hidden;
  will-change: transform;
}

.tooltip-hover.is-visible {
  visibility: visible;
}

/* Lướt mượt mà khi con trỏ di chuyển hoặc khi tooltip đổi hướng/vị trí */
.tooltip-hover.is-smooth {
  transition: transform 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

.tooltip-hover-content {
  opacity: 0;
  transform: translateY(4px) scale(0.97);
  transition: opacity 0.2s cubic-bezier(0.16, 1, 0.3, 1),
              transform 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: opacity, transform;
}

.tooltip-hover.is-visible .tooltip-hover-content {
  opacity: 1;
  transform: translateY(0) scale(1);
}
</style>
