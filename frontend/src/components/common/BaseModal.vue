<script setup>
import { nextTick, onBeforeUnmount, ref, useId, watch } from 'vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    required: true,
  },
  size: {
    type: String,
    default: 'md',
    validator: (value) => ['sm', 'md', 'lg'].includes(value),
  },
  closeOnBackdrop: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['update:open', 'close'])

const dialogRef = ref(null)
const titleId = `modal-title-${useId()}`
let previousOverflow = ''
let lastFocused = null

function focusFirst() {
  const dialog = dialogRef.value
  if (!dialog) {
    return
  }

  const target =
    dialog.querySelector('[data-autofocus]') ||
    dialog.querySelector(
      'button, [href], input, textarea, select, [tabindex]:not([tabindex="-1"])',
    )

  target?.focus()
}

function openDialog() {
  const dialog = dialogRef.value
  if (!dialog || dialog.open) {
    return
  }

  lastFocused = document.activeElement instanceof HTMLElement ? document.activeElement : null
  previousOverflow = document.body.style.overflow
  document.body.style.overflow = 'hidden'
  dialog.showModal()
  nextTick(focusFirst)
}

function closeDialog() {
  const dialog = dialogRef.value
  if (dialog?.open) {
    dialog.close()
  }
}

function close() {
  closeDialog()
}

function onClose() {
  document.body.style.overflow = previousOverflow
  emit('update:open', false)
  emit('close')

  if (lastFocused) {
    lastFocused.focus()
    lastFocused = null
  }
}

function onCancel(event) {
  event.preventDefault()
  close()
}

function onBackdrop(event) {
  if (props.closeOnBackdrop && event.target === dialogRef.value) {
    close()
  }
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      openDialog()
    } else {
      closeDialog()
    }
  },
  { immediate: true, flush: 'post' },
)

onBeforeUnmount(() => {
  document.body.style.overflow = previousOverflow
})
</script>

<template>
  <Teleport to="body">
    <dialog
      ref="dialogRef"
      class="base-modal"
      :class="`base-modal--${size}`"
      :aria-labelledby="titleId"
      aria-modal="true"
      @close="onClose"
      @cancel="onCancel"
      @click="onBackdrop"
    >
      <div class="base-modal__panel" @click.stop>
        <header class="base-modal__header">
          <h2 :id="titleId" class="base-modal__title">{{ title }}</h2>
          <button type="button" class="btn btn--ghost btn--icon" aria-label="Đóng" @click="close">
            ✕
          </button>
        </header>

        <div class="base-modal__body">
          <slot />
        </div>

        <footer v-if="$slots.footer" class="base-modal__footer">
          <slot name="footer" />
        </footer>
      </div>
    </dialog>
  </Teleport>
</template>

<style scoped>
.base-modal {
  position: fixed;
  inset: 0;
  width: 100%;
  height: 100%;
  max-width: none;
  max-height: none;
  margin: 0;
  padding: 16px;
  border: 0;
  background: transparent;
  color: var(--tx);
}

.base-modal[open] {
  display: flex;
  align-items: center;
  justify-content: center;
}

.base-modal::backdrop {
  background: rgba(13, 13, 12, 0.55);
  backdrop-filter: blur(2px);
}

.base-modal__panel {
  display: flex;
  width: min(560px, 100%);
  max-height: min(90vh, 760px);
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--line2);
  border-radius: var(--r-lg);
  background: var(--s1);
  box-shadow: var(--sh-pop);
}

.base-modal--sm .base-modal__panel {
  width: min(420px, 100%);
}

.base-modal--lg .base-modal__panel {
  width: min(720px, 100%);
}

.base-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px 14px 20px;
  border-bottom: 1px solid var(--line);
}

.base-modal__title {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.025em;
}

.base-modal__body {
  overflow-y: auto;
  padding: 20px;
}

.base-modal__footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 20px;
  border-top: 1px solid var(--line);
  background: var(--s1);
}

@media (max-width: 480px) {
  .base-modal {
    padding: 8px;
  }

  .base-modal__header {
    padding: 12px 12px 12px 16px;
  }

  .base-modal__body {
    padding: 16px;
  }

  .base-modal__footer {
    padding: 12px 16px;
  }
}
</style>
