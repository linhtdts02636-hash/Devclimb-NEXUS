<script setup>
import { computed } from 'vue'

const props = defineProps({
  attachments: {
    type: Array,
    default: () => [],
  },
})

function safeUrl(value) {
  if (typeof value !== 'string' || !value.trim()) {
    return null
  }

  try {
    const url = new URL(value, window.location.origin)
    return ['http:', 'https:'].includes(url.protocol) ? url.href : null
  } catch {
    return null
  }
}

function normalize(type) {
  return props.attachments
    .filter((attachment) => attachment.type === type)
    .map((attachment) => ({ ...attachment, safeUrl: safeUrl(attachment.url) }))
    .filter((attachment) => attachment.safeUrl)
}

const images = computed(() => [...normalize('IMAGE'), ...normalize('GIF')])
const videos = computed(() => normalize('VIDEO'))
const files = computed(() => normalize('FILE'))

const imageGridClass = computed(() => {
  if (images.value.length === 1) {
    return 'post-media__grid--single'
  }

  if (images.value.length === 2) {
    return 'post-media__grid--two'
  }

  return 'post-media__grid--many'
})

function fileLabel(url) {
  try {
    const path = new URL(url).pathname
    const name = decodeURIComponent(path.split('/').filter(Boolean).pop() || '')
    return name || 'Mở tệp đính kèm'
  } catch {
    return 'Mở tệp đính kèm'
  }
}
</script>

<template>
  <div v-if="attachments.length" class="post-media">
    <div v-if="images.length" class="post-media__grid" :class="imageGridClass">
      <img
        v-for="image in images"
        :key="`image-${image.id}`"
        class="post-media__image"
        :src="image.safeUrl"
        :alt="image.type === 'GIF' ? 'Ảnh động đính kèm' : 'Ảnh đính kèm'"
        loading="lazy"
      />
    </div>

    <div v-for="video in videos" :key="`video-${video.id}`" class="post-media__video-wrap">
      <video
        class="post-media__video"
        :src="video.safeUrl"
        controls
        playsinline
        preload="metadata"
        aria-label="Video đính kèm"
      ></video>
    </div>

    <ul v-if="files.length" class="post-media__files">
      <li v-for="file in files" :key="`file-${file.id}`">
        <a class="post-media__file" :href="file.safeUrl" target="_blank" rel="noopener noreferrer">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" aria-hidden="true">
            <path
              d="M6.5 9.5 9.8 6.2a2.1 2.1 0 0 1 3 3l-4.4 4.4a3.3 3.3 0 0 1-4.7-4.7l4.2-4.2"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
            />
          </svg>
          {{ fileLabel(file.safeUrl) }}
        </a>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.post-media {
  display: grid;
  gap: 8px;
  margin-top: 14px;
}

.post-media__grid {
  display: grid;
  gap: 4px;
  overflow: hidden;
  border-radius: var(--r-sm);
}

.post-media__grid--single {
  grid-template-columns: 1fr;
}

.post-media__grid--two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.post-media__grid--many {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.post-media__image {
  width: 100%;
  height: 100%;
  max-height: 480px;
  object-fit: cover;
  background: var(--s2);
}

.post-media__grid--single .post-media__image {
  max-height: 520px;
}

.post-media__grid--two .post-media__image,
.post-media__grid--many .post-media__image {
  aspect-ratio: 1 / 1;
}

.post-media__video {
  width: 100%;
  border-radius: var(--r-sm);
  background: #000;
}

.post-media__files {
  display: grid;
  gap: 6px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.post-media__file {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  background: var(--s2);
  color: var(--tx);
  font-size: 13px;
  text-decoration: none;
  overflow-wrap: anywhere;
}

.post-media__file:hover {
  background: var(--s3);
}
</style>
