<script setup>
import { computed } from 'vue'

const props = defineProps({
  src: { type: String, required: true },
  name: { type: String, default: '' },
  type: {
    type: String,
    default: 'community',
    validator: (value) => ['user', 'community'].includes(value),
  },
  size: { type: [Number, String], default: 28 },
  alt: { type: String, default: '' },
  showName: { type: Boolean, default: true },
})

const imageSize = computed(() => (typeof props.size === 'number' ? `${props.size}px` : props.size))
const isUser = computed(() => props.type === 'user')
</script>

<template>
  <div class="avatar">
    <img
      class="avatar-image"
      :class="isUser ? 'is-user' : 'is-community'"
      :src="src"
      :alt="alt"
      :style="{ width: imageSize, height: imageSize }"
    />
    <span v-if="showName && name" class="avatar-name">{{ name }}</span>
  </div>
</template>

<style scoped>
.avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  max-width: 100%;
}

.avatar-image {
  flex: none;
  object-fit: cover;
  background: var(--s2);
  border: 1px solid var(--line);
}

.avatar-image.is-user {
  border-radius: 50%;
}

.avatar-image.is-community {
  border-radius: 2px;
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
}
</style>
