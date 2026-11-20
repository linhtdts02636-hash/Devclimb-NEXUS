<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  src: { type: String, default: '' },
  name: { type: String, default: '' },
  type: {
    type: String,
    default: 'community',
    validator: (value) => ['user', 'community'].includes(value),
  },
  size: { type: [Number, String], default: 28 },
  alt: { type: String, default: '' },
  showName: { type: Boolean, default: true },
  // Mặc định avatar người dùng tròn, avatar cộng đồng vuông; prop này cho phép ghi đè.
  round: { default: undefined },
  // Bật/tắt tính năng bấm điều hướng (mặc định bật khi có tên).
  clickable: { type: Boolean, default: true },
  // Đường dẫn tùy chỉnh nếu cần ghi đè đích đến.
  to: { type: [String, Object], default: null },
})

const failed = ref(false)

const imageSize = computed(() => (typeof props.size === 'number' ? `${props.size}px` : props.size))
const isRound = computed(() => (props.round === undefined ? props.type === 'user' : props.round))
const fontSize = computed(() => {
  const pixels = typeof props.size === 'number' ? props.size : Number.parseFloat(props.size)
  return Number.isFinite(pixels) ? `${Math.max(11, Math.round(pixels * 0.36))}px` : '11px'
})
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
const showImage = computed(() => Boolean(props.src) && !failed.value)

// Đường dẫn điều hướng của avatar: chuyển đến trang cộng đồng hoặc trang người dùng
const routeTarget = computed(() => {
  if (!props.clickable || !props.name?.trim()) return null
  if (props.to) return props.to
  if (props.type === 'community') {
    return `/community/${encodeURIComponent(props.name.trim())}`
  }
  if (props.type === 'user') {
    return `/user/${encodeURIComponent(props.name.trim())}`
  }
  return null
})

watch(
  () => props.src,
  () => {
    failed.value = false
  },
)
</script>

<template>
  <component
    :is="routeTarget ? 'RouterLink' : 'div'"
    :to="routeTarget || undefined"
    class="avatar"
    :class="{ 'is-clickable': Boolean(routeTarget) }"
    :title="routeTarget ? (type === 'community' ? `View community ${name}` : `View user ${name}`) : undefined"
  >
    <span
      class="avatar-box"
      :class="{ 'is-round': isRound }"
      :style="{ width: imageSize, height: imageSize, fontSize }"
      aria-hidden="true"
    >
      <img v-if="showImage" class="avatar-image" :src="src" :alt="alt" @error="failed = true" />
      <span v-else class="avatar-fallback">{{ initials }}</span>
    </span>
    <span v-if="showName && name" class="avatar-name">{{ name }}</span>
  </component>
</template>

<style scoped>
.avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  max-width: 100%;
  text-decoration: none;
  color: inherit;
}

.avatar.is-clickable {
  cursor: pointer;
  transition: opacity 0.15s var(--ease);
}

.avatar.is-clickable:hover {
  opacity: 0.88;
}

.avatar.is-clickable:hover .avatar-name {
  color: var(--tx);
  text-decoration: underline;
  text-underline-offset: 2px;
}

.avatar.is-clickable:hover .avatar-box {
  border-color: var(--tx3);
}

.avatar-box {
  display: inline-grid;
  flex: none;
  place-items: center;
  overflow: hidden;
  color: var(--tx2);
  background: var(--s2);
  border: 1px solid var(--line);
  border-radius: 2px;
  font-family: var(--f-mono);
  font-weight: 500;
  line-height: 1;
  user-select: none;
  transition: border-color 0.15s var(--ease);
}

.avatar-box.is-round {
  border-radius: 50%;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  text-transform: uppercase;
}

.avatar-name {
  flex: 0 1 auto;
  min-width: 0;
  max-width: 100%;
  overflow: hidden;
  font: 500 12px var(--f-head);
  color: var(--tx2);
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.15s var(--ease);
}
</style>
