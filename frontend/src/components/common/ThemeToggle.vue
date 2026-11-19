<script setup>
import { ref } from 'vue'

const THEME_STORAGE_KEY = 'dc-theme'

const currentTheme = ref(document.documentElement.dataset.theme)

function toggleTheme() {
  const nextTheme = currentTheme.value === 'dark' ? 'light' : 'dark'
  currentTheme.value = nextTheme
  document.documentElement.dataset.theme = nextTheme
  window.dispatchEvent(new CustomEvent('themechange', { detail: nextTheme }))
  try {
    localStorage.setItem(THEME_STORAGE_KEY, nextTheme)
  } catch {
    // Trình duyệt ẩn danh có thể chặn localStorage; giao diện vẫn được áp dụng trong lần truy cập này.
  }
}
</script>

<template>
  <button
    type="button"
    class="theme-toggle"
    :aria-label="currentTheme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme'"
    @click="toggleTheme"
  >
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path v-if="currentTheme === 'dark'" d="M12 4V2M12 22v-2M4 12H2M22 12h-2M5.6 5.6 4.2 4.2M19.8 19.8l-1.4-1.4M5.6 18.4l-1.4 1.4M19.8 4.2l-1.4 1.4M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10z" />
      <path v-else d="M20 14.5A8 8 0 0 1 9.5 4 8 8 0 1 0 20 14.5z" />
    </svg>
  </button>
</template>

<style scoped>
.theme-toggle {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: var(--r-sm);
  color: var(--tx2);
  transition: background 0.2s var(--ease), color 0.2s var(--ease);
}

.theme-toggle:hover {
  background: var(--s2);
  color: var(--tx);
}

svg {
  width: 20px;
  height: 20px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}
</style>
