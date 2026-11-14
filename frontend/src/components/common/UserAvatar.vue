<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  src: {
    type: String,
    default: '',
  },
  name: {
    type: String,
    default: '',
  },
  size: {
    type: Number,
    default: 40,
  },
})

const failed = ref(false)

const initials = computed(() => {
  const parts = props.name.trim().split(/\s+/).filter(Boolean)

  if (parts.length === 0) {
    return '?'
  }

  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase()
  }

  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase()
})

const dimensions = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`,
  fontSize: `${Math.max(11, Math.round(props.size * 0.36))}px`,
}))

watch(
  () => props.src,
  () => {
    failed.value = false
  },
)
</script>

<template>
  <span class="user-avatar" :style="dimensions" aria-hidden="true">
    <img v-if="src && !failed" class="user-avatar__image" :src="src" alt="" @error="failed = true" />
    <span v-else class="user-avatar__fallback">{{ initials }}</span>
  </span>
</template>

<style scoped>
.user-avatar {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 999px;
  background: var(--s2);
  color: var(--tx2);
  font-family: var(--mono);
  font-weight: 500;
  letter-spacing: 0.02em;
  user-select: none;
}

.user-avatar__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar__fallback {
  line-height: 1;
}
</style>
