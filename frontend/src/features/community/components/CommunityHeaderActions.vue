<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

defineProps({
  community: { type: Object, required: true },
  busy: { type: Boolean, default: false },
  activeTab: { type: String, default: 'posts' },
})

const emit = defineEmits(['join', 'leave', 'create'])

const openMenu = ref('')
const rootElement = ref(null)

function toggleMenu(menuName) {
  openMenu.value = openMenu.value === menuName ? '' : menuName
}

function closeMenuOnOutsideClick(event) {
  if (!rootElement.value?.contains(event.target)) openMenu.value = ''
}

function closeMenuOnEscape(event) {
  if (event.key === 'Escape') openMenu.value = ''
}

const currentAddress = () => window.location.href

async function copyToClipboard(text) {
  try {
    await navigator.clipboard.writeText(text)
  } catch {
    // Trình duyệt chặn clipboard (ví dụ trang không phải HTTPS) thì bỏ qua, người dùng tự copy từ thanh địa chỉ.
  }
  openMenu.value = ''
}

onMounted(() => {
  document.addEventListener('click', closeMenuOnOutsideClick)
  document.addEventListener('keydown', closeMenuOnEscape)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', closeMenuOnOutsideClick)
  document.removeEventListener('keydown', closeMenuOnEscape)
})
</script>

<template>
  <div ref="rootElement" class="actions">
    <RouterLink
      v-if="community.isMember && activeTab === 'puzzles'"
      class="btn p"
      :to="`/community/${community.name}/puzzles/new`"
    >
      <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" /></svg>Create puzzle
    </RouterLink>
    <button v-else-if="community.isMember && activeTab === 'posts'" type="button" class="btn p" @click="emit('create')">
      <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" /></svg>Create post
    </button>
    <button v-else-if="!community.isMember && community.isPublic" type="button" class="btn p" :disabled="busy" @click="emit('join')">Join</button>

    <span v-if="community.isOwner" class="pill static">Owner</span>
    <div v-else-if="community.isMember" class="anchor">
      <button type="button" class="pill joined" aria-haspopup="menu" :aria-expanded="openMenu === 'joined'" @click.stop="toggleMenu('joined')">
        <span class="on">Joined</span><span class="off">Leave</span>
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 9 6 6 6-6" /></svg>
      </button>
      <div v-show="openMenu === 'joined'" class="menu" role="menu">
        <button type="button" role="menuitem" class="danger" :disabled="busy" @click="emit('leave')">Leave community</button>
      </div>
    </div>

    <RouterLink v-if="community.isOwner" class="pill icon" :to="`/community/${community.name}/settings`" aria-label="Community settings">
      <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="3" /><path d="M19 12a7 7 0 0 0-.1-1.3l2-1.5-2-3.4-2.3.9a7 7 0 0 0-2.2-1.3L14 3h-4l-.4 2.4a7 7 0 0 0-2.2 1.3l-2.3-.9-2 3.4 2 1.5A7 7 0 0 0 5 12c0 .4 0 .9.1 1.3l-2 1.5 2 3.4 2.3-.9a7 7 0 0 0 2.2 1.3L10 21h4l.4-2.4a7 7 0 0 0 2.2-1.3l2.3.9 2-3.4-2-1.5c.1-.4.1-.9.1-1.3z" /></svg>
    </RouterLink>

    <div class="anchor">
      <button type="button" class="pill icon" aria-haspopup="menu" :aria-expanded="openMenu === 'more'" aria-label="More options" @click.stop="toggleMenu('more')">
        <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="5" cy="12" r="1.2" /><circle cx="12" cy="12" r="1.2" /><circle cx="19" cy="12" r="1.2" /></svg>
      </button>
      <div v-show="openMenu === 'more'" class="menu" role="menu">
        <button type="button" role="menuitem" @click="copyToClipboard(currentAddress())">Copy link</button>
        <button type="button" role="menuitem" @click="copyToClipboard(community.name)">Copy community name</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.actions {
  display: flex;
  gap: var(--s-2);
  align-items: center;
  padding-bottom: var(--s-2);
}

.btn {
  height: 34px;
  padding: 0 var(--s-4);
  font-size: var(--t-sm);
}

svg {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.pill {
  display: inline-flex;
  gap: var(--s-2);
  align-items: center;
  justify-content: center;
  height: 34px;
  padding: 0 var(--s-4);
  font-size: var(--t-sm);
  font-weight: 500;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: 999px;
}

.pill:hover:not(.static) {
  background: var(--s2);
}

.pill.static {
  color: var(--tx2);
}

.pill.icon {
  width: 34px;
  padding: 0;
}

.joined .off,
.joined:hover .on {
  display: none;
}

.joined:hover .off {
  display: inline;
}

.anchor {
  position: relative;
}

.menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  z-index: 20;
  min-width: 200px;
  padding: var(--s-1);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
  box-shadow: var(--sh-card);
}

.menu button {
  display: flex;
  width: 100%;
  height: 36px;
  padding: 0 var(--s-3);
  align-items: center;
  font-size: var(--t-sm);
  text-align: left;
  border-radius: var(--r-sm);
}

.menu button:hover {
  background: var(--s2);
}

.menu .danger {
  color: var(--bad);
}
</style>
