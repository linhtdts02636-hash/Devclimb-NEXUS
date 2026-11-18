<script setup>
import { computed } from 'vue'

const props = defineProps({
  // Hỗ trợ cả mảng URL string và mảng object { src, alt }.
  images: {
    type: Array,
    default: () => [],
  },
})

const normalizedImages = computed(() =>
  (props.images || [])
    .map((image) => {
      if (typeof image === 'string') {
        return { src: image, alt: '' }
      }

      return {
        src: image?.src || image?.url || '',
        alt: image?.alt || '',
      }
    })
    .filter((image) => image.src),
)

const visibleImages = computed(() => normalizedImages.value.slice(0, 4))
const hiddenCount = computed(() => Math.max(0, normalizedImages.value.length - 4))

const layoutClass = computed(() => {
  const count = normalizedImages.value.length

  if (count === 1) {
    return 'post-images--single'
  }

  if (count === 2) {
    return 'post-images--two'
  }

  if (count === 3) {
    return 'post-images--three'
  }

  return 'post-images--many'
})
</script>

<template>
  <div
    v-if="normalizedImages.length"
    class="post-images"
    :class="layoutClass"
    role="group"
    aria-label="Ảnh trong bài viết"
  >
    <template v-if="normalizedImages.length === 1">
      <img
        class="post-images__single"
        :src="visibleImages[0].src"
        :alt="visibleImages[0].alt"
        loading="lazy"
        decoding="async"
      />
    </template>

    <template v-else>
      <div
        v-for="(image, index) in visibleImages"
        :key="`${image.src}-${index}`"
        class="post-images__cell"
        :class="{ 'post-images__cell--more': index === 3 && hiddenCount > 0 }"
      >
        <img
          class="post-images__image"
          :src="image.src"
          :alt="image.alt"
          loading="lazy"
          decoding="async"
        />
        <span
          v-if="index === 3 && hiddenCount > 0"
          class="post-images__more"
          :aria-label="`Còn ${hiddenCount} ảnh khác`"
        >
          +{{ hiddenCount }}
        </span>
      </div>
    </template>
  </div>
</template>

<style scoped>
.post-images {
  margin-top: 14px;
  max-width: 100%;
  min-width: 0;
}

.post-images--single {
  display: flex;
  justify-content: center;
  overflow: hidden;
  border-radius: var(--r-sm);
  background: var(--s2);
}

.post-images__single {
  display: block;
  width: auto;
  max-width: 100%;
  height: auto;
  max-height: 560px;
  object-fit: contain;
}

.post-images--two,
.post-images--three,
.post-images--many {
  display: grid;
  gap: 6px;
  overflow: hidden;
  border-radius: var(--r-sm);
}

.post-images--two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.post-images--three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.post-images--many {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.post-images__cell {
  position: relative;
  overflow: hidden;
  min-width: 0;
  aspect-ratio: 1 / 1;
  background: var(--s2);
}

.post-images__image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-images__cell--more .post-images__image {
  filter: brightness(0.32) saturate(0.85);
}

.post-images__more {
  position: absolute;
  inset: 0;
  display: grid;
  background: rgb(0 0 0 / 0.7);
  color: #ffffff;
  font-family: var(--mono);
  font-size: 26px;
  font-weight: 600;
  text-shadow: 0 1px 8px rgb(0 0 0 / 0.7);
  place-items: center;
}

/* Trên màn hình hẹp, 3 ảnh chuyển thành 2 cột để không bị quá nhỏ. */
@media (max-width: 480px) {
  .post-images--three {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .post-images--three .post-images__cell:last-child {
    grid-column: span 2;
    aspect-ratio: 2 / 1;
  }

  .post-images__single {
    max-height: 420px;
  }
}
</style>
