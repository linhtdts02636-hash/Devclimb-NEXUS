<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const ICON_PATHS = {
  problem: '<path d="m8 8-4 4 4 4M16 8l4 4-4 4"/>',
  community: '<circle cx="9" cy="8" r="3"/><path d="M3 20a6 6 0 0 1 12 0M16 4.5a3 3 0 0 1 0 6M21 20a6 6 0 0 0-4-5.6"/>',
  contest: '<path d="M8 21h8M12 17v4M7 4h10v5a5 5 0 0 1-10 0zM7 6H4v1a3 3 0 0 0 3 3M17 6h3v1a3 3 0 0 1-3 3"/>',
  person: '<circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/>',
  recent: '<path d="M3 12a9 9 0 1 0 2.6-6.4L3 8M3 3v5h5M12 7v5l3 2"/>',
}

const KINDS = {
  problem: { label: 'Problems', noun: 'problem', hash: '#solve' },
  community: { label: 'Communities', noun: 'community', hash: '#communities' },
  contest: { label: 'Contests', noun: 'contest', hash: '#compete' },
  person: { label: 'People', noun: 'person', hash: '#compete' },
}

// Dữ liệu mẫu để minh họa; thay bằng lời gọi API tìm kiếm khi backend có endpoint này.
const SAMPLE_INDEX = [
  { name: 'two-sum', kind: 'problem', meta: 'Medium' },
  { name: 'valid-parentheses', kind: 'problem', meta: 'Easy' },
  { name: 'merge-intervals', kind: 'problem', meta: 'Medium' },
  { name: 'lru-cache', kind: 'problem', meta: 'Hard' },
  { name: 'binary-search', kind: 'problem', meta: 'Easy' },
  { name: 'coin-change', kind: 'problem', meta: 'Medium' },
  { name: 'word-ladder', kind: 'problem', meta: 'Hard' },
  { name: 'top-k-frequent', kind: 'problem', meta: 'Medium' },
  { name: 'recursion: power set', kind: 'problem', meta: 'Easy' },
  { name: 'fpt-algo-club', kind: 'community', meta: '142 members' },
  { name: 'sd21304', kind: 'community', meta: '38 members' },
  { name: 'java-beginners', kind: 'community', meta: '310 members' },
  { name: 'night-owls-cp', kind: 'community', meta: '57 members' },
  { name: 'weekly round 12', kind: 'contest', meta: 'Sat 20:00' },
  { name: 'sd21304 mid-term', kind: 'contest', meta: 'Fri 19:00' },
  { name: 'minh.tran', kind: 'person', meta: '512 pts' },
  { name: 'linh_cp', kind: 'person', meta: '498 pts' },
  { name: 'hung.dev', kind: 'person', meta: '471 pts' },
  { name: 'khoa.nexus', kind: 'person', meta: '455 pts' },
]
const RECENT_SEARCHES = ['two pointers', 'fpt-algo-club', 'recursion']

const router = useRouter()
const query = ref('')
const isOpen = ref(false)
const activeIndex = ref(-1)
const inputElement = ref(null)
const listElement = ref(null)

function splitMatch(name, lowerQuery) {
  const start = name.indexOf(lowerQuery)
  if (!lowerQuery || start < 0) return { before: name, match: '', after: '' }
  return { before: name.slice(0, start), match: name.slice(start, start + lowerQuery.length), after: name.slice(start + lowerQuery.length) }
}

// Mỗi nhóm có dạng { title, items, emptyText }. Mỗi mục được đánh số liên tục để điều hướng bằng bàn phím.
const sections = computed(() => {
  const trimmedQuery = query.value.trim()
  const lowerQuery = trimmedQuery.toLowerCase()
  let built

  if (!trimmedQuery) {
    built = [
      {
        title: 'Recent',
        items: RECENT_SEARCHES.map((text) => ({ icon: 'recent', ...splitMatch(text, ''), meta: '', hash: '#solve', value: text })),
      },
      {
        title: 'Popular problems',
        items: SAMPLE_INDEX.filter((entry) => entry.kind === 'problem')
          .slice(0, 3)
          .map((entry) => ({ icon: 'problem', ...splitMatch(entry.name, ''), meta: entry.meta, hash: '#solve', value: entry.name })),
      },
    ]
  } else {
    const matches = SAMPLE_INDEX.filter((entry) => entry.name.includes(lowerQuery)).slice(0, 5)
    built = [
      {
        title: '',
        items: Object.entries(KINDS).map(([kind, details]) => ({
          icon: kind,
          ...splitMatch(trimmedQuery, ''),
          suffix: ` in ${details.label}`,
          meta: '',
          hash: details.hash,
          value: trimmedQuery,
        })),
      },
      {
        title: 'Top matches',
        emptyText: `No matches for “${trimmedQuery}”. Try a problem or community name.`,
        items: matches.map((entry) => ({
          icon: entry.kind,
          ...splitMatch(entry.name, lowerQuery),
          meta: `${KINDS[entry.kind].noun} · ${entry.meta}`,
          hash: KINDS[entry.kind].hash,
          value: entry.name,
        })),
      },
    ]
  }

  let index = 0
  built.forEach((section) => section.items.forEach((item) => (item.index = index++)))
  return built
})

const allItems = computed(() => sections.value.flatMap((section) => section.items))

watch(query, () => (activeIndex.value = -1))

function open() {
  activeIndex.value = -1
  isOpen.value = true
}

function close() {
  isOpen.value = false
  activeIndex.value = -1
}

function setActive(index) {
  const count = allItems.value.length
  if (!count) return
  activeIndex.value = (index + count) % count
  nextTick(() => listElement.value?.querySelector('[aria-selected="true"]')?.scrollIntoView({ block: 'nearest' }))
}

function choose(item) {
  if (!item) return
  query.value = item.value
  close()
  inputElement.value?.blur()
  router.push({ path: '/', hash: item.hash })
}

function handleKeydown(event) {
  if (event.key === 'ArrowDown') {
    event.preventDefault()
    setActive(activeIndex.value + 1)
  } else if (event.key === 'ArrowUp') {
    event.preventDefault()
    setActive(activeIndex.value - 1)
  } else if (event.key === 'Enter') {
    event.preventDefault()
    choose(allItems.value[activeIndex.value > -1 ? activeIndex.value : 0])
  } else if (event.key === 'Escape') {
    close()
    inputElement.value?.blur()
  }
}

function focusSearch() {
  isOpen.value = true
  // Trên điện thoại, ô nhập bị ẩn cho tới khi hộp tìm kiếm mở, nên phải focus sau khi DOM cập nhật.
  nextTick(() => inputElement.value?.focus())
}

// Nhấn "/" ở bất kỳ đâu (ngoài ô nhập liệu) để chuyển tới ô tìm kiếm.
function handleSlashShortcut(event) {
  const tagName = event.target.tagName
  if (event.key !== '/' || tagName === 'INPUT' || tagName === 'TEXTAREA' || event.target.isContentEditable) return
  event.preventDefault()
  focusSearch()
}

onMounted(() => window.addEventListener('keydown', handleSlashShortcut))
onBeforeUnmount(() => window.removeEventListener('keydown', handleSlashShortcut))
</script>

<template>
  <div class="srch" :class="{ open: isOpen }">
    <button class="sbtn" type="button" aria-label="Search" tabindex="-1" @click="focusSearch">
      <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></svg>
    </button>
    <input
      ref="inputElement"
      v-model="query"
      type="search"
      placeholder="Search problems, communities, people"
      autocomplete="off"
      spellcheck="false"
      role="combobox"
      aria-label="Search DevClimb"
      aria-controls="nav-search-options"
      aria-autocomplete="list"
      :aria-expanded="isOpen"
      :aria-activedescendant="activeIndex > -1 ? `nav-search-option-${activeIndex}` : undefined"
      @focus="open"
      @input="isOpen = true"
      @keydown="handleKeydown"
      @blur="close"
    />
    <kbd aria-hidden="true">/</kbd>

    <!-- Chặn mousedown để khi bấm vào một dòng, ô nhập không bị mất focus trước khi sự kiện click xảy ra. -->
    <div
      v-show="isOpen"
      id="nav-search-options"
      ref="listElement"
      class="sugg"
      role="listbox"
      aria-label="Search suggestions"
      @mousedown.prevent
    >
      <template v-for="section in sections" :key="section.title">
        <div v-if="section.title" class="grp">{{ section.title }}</div>
        <div
          v-for="item in section.items"
          :id="`nav-search-option-${item.index}`"
          :key="item.index"
          role="option"
          :aria-selected="item.index === activeIndex"
          @click="choose(item)"
          @mousemove="item.index !== activeIndex && setActive(item.index)"
        >
          <svg viewBox="0 0 24 24" aria-hidden="true" v-html="ICON_PATHS[item.icon]"></svg>
          <span class="nm"><b>{{ item.before }}<mark v-if="item.match">{{ item.match }}</mark>{{ item.after }}</b>{{ item.suffix }}</span>
          <span class="meta">{{ item.meta }}</span>
          <span class="ent">↵</span>
        </div>
        <div v-if="section.emptyText && !section.items.length" class="none">{{ section.emptyText }}</div>
      </template>
      <div class="ft" aria-hidden="true"><span>↑↓ navigate</span><span>↵ open</span><span>esc close</span></div>
    </div>
  </div>
</template>

<style scoped>
.srch {
  position: relative;
  flex: 1;
  max-width: 420px;
  display: flex;
  align-items: center;
  gap: var(--s-1);
  height: 40px;
  padding: 0 var(--s-2) 0 var(--s-1);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: 10px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.srch:focus-within {
  border-color: var(--ac);
  box-shadow: 0 0 0 3px var(--ac-soft);
}

.srch svg,
.sugg svg {
  flex: none;
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.sbtn {
  flex: none;
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  color: var(--tx2);
}

.srch input {
  flex: 1;
  min-width: 0;
  height: 100%;
  border: 0;
  outline: none;
  background: none;
  font: 400 var(--t-sm) var(--f-head);
  color: var(--tx);
}

.srch input::placeholder {
  color: var(--tx2);
}

.srch input::-webkit-search-cancel-button {
  display: none;
}

.srch kbd {
  padding: 1px 6px;
  border: 1px solid var(--line2);
  border-radius: 5px;
  font: 400 11px var(--f-mono);
  color: var(--tx2);
}

.srch:focus-within kbd {
  display: none;
}

.sugg {
  position: absolute;
  top: calc(100% + 8px);
  left: -1px;
  right: -1px;
  z-index: 50;
  max-height: min(70vh, 480px);
  overflow: auto;
  padding: var(--s-2);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-lg);
  box-shadow: var(--sh-card);
}

.grp {
  padding: var(--s-3) var(--s-3) var(--s-1);
  font: 400 11px var(--f-mono);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--tx2);
}

.grp:first-child {
  padding-top: var(--s-1);
}

[role='option'] {
  display: flex;
  align-items: center;
  gap: var(--s-3);
  padding: 9px var(--s-3);
  border-radius: var(--r-sm);
  font-size: var(--t-sm);
  color: var(--tx2);
  cursor: pointer;
}

[role='option'] b {
  font-weight: 500;
  color: var(--tx);
}

[role='option'] mark {
  background: none;
  color: var(--ac);
}

[aria-selected='true'] {
  background: var(--s2);
}

.nm {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta {
  margin-left: auto;
  font: 400 var(--t-xs) var(--f-mono);
  white-space: nowrap;
  color: var(--tx2);
}

.ent {
  visibility: hidden;
  font: 400 11px var(--f-mono);
  color: var(--tx2);
}

[aria-selected='true'] .ent {
  visibility: visible;
}

.none {
  padding: var(--s-3);
  font-size: var(--t-sm);
  color: var(--tx2);
}

.ft {
  display: flex;
  gap: var(--s-4);
  margin-top: var(--s-2);
  padding: var(--s-3) var(--s-3) var(--s-1);
  border-top: 1px solid var(--line);
  font: 400 11px var(--f-mono);
  color: var(--tx2);
}

/* Trên điện thoại, hộp tìm kiếm thu gọn thành biểu tượng và mở rộng phủ lên navbar khi được mở. */
@media (max-width: 600px) {
  .srch {
    flex: 0 0 40px;
    justify-content: center;
    margin-left: auto;
    padding: 0;
  }

  .srch input,
  .srch kbd,
  .ft {
    display: none;
  }

  .srch.open {
    position: absolute;
    top: 14px;
    left: var(--s-4);
    right: var(--s-4);
    z-index: 5;
    max-width: none;
    justify-content: flex-start;
    padding: 0 var(--s-2) 0 var(--s-1);
  }

  .srch.open input {
    display: block;
  }
}
</style>
