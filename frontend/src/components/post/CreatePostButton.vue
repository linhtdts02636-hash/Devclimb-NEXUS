<script setup>
defineProps({
  label: {
    type: String,
    default: 'Đăng bài',
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['click'])
</script>

<template>
  <button type="button" class="btn btn--primary" :disabled="disabled" @click="$emit('click')">
    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
      <path
        d="M8 3v10M3 8h10"
        stroke="currentColor"
        stroke-width="1.8"
        stroke-linecap="round"
      />
    </svg>
    {{ label }}
  </button>
</template>
