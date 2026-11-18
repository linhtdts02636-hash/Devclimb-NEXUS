<script setup>
import { computed } from 'vue'

const props = defineProps({
  imageUrl: { type: String, default: '' },
  name: { type: String, required: true },
  size: { type: Number, default: 40 },
  isRound: { type: Boolean, default: false },
})

const initials = computed(() => props.name.slice(0, 2).toUpperCase())
const sizeStyle = computed(() => ({ width: `${props.size}px`, height: `${props.size}px`, fontSize: `${props.size * 0.36}px` }))
</script>

<template>
  <!-- Trang trí: tên luôn nằm ngay cạnh avatar nên trình đọc màn hình không cần đọc thêm. -->
  <span class="avatar" :class="{ round: isRound }" :style="sizeStyle" aria-hidden="true">
    <img v-if="imageUrl" :src="imageUrl" alt="" />
    <template v-else>{{ initials }}</template>
  </span>
</template>

<style scoped>
.avatar {
  display: inline-grid;
  flex: none;
  place-items: center;
  overflow: hidden;
  font-family: var(--f-mono);
  font-weight: 500;
  color: var(--tx2);
  background: var(--s3);
  border-radius: var(--r-sm);
}

.avatar.round {
  border-radius: 50%;
}

img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
