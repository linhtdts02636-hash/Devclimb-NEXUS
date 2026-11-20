<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import PuzzleCard from '../../features/puzzle/components/PuzzleCard.vue'

const props = defineProps({
  items: { type: Array, default: null },
  puzzles: { type: Array, default: () => [] },
  query: { type: String, default: '' },
  difficulty: { type: String, default: '' },
  columns: { type: Number, default: 1 },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  placeholder: { type: String, default: 'Search for puzzles...' },
  sampleNote: { type: Boolean, default: false },
  sampleNoteText: { type: String, default: 'Puzzles are sample data for illustration' },
})

const emit = defineEmits(['update:query', 'update:difficulty', 'search', 'retry'])

const DIFFICULTY_OPTIONS = [
  { label: 'All', value: '' },
  { label: 'Easy', value: 'EASY', tone: 'easy' },
  { label: 'Medium', value: 'MEDIUM', tone: 'medium' },
  { label: 'Hard', value: 'HARD', tone: 'hard' },
]

// Danh sách các mẫu cú pháp tìm kiếm có thể bấm vào để điền nhanh vào ô tìm kiếm
const SYNTAX_OPTIONS = [
  { prefix: 'word', query: 'two', desc: 'Match title or content' },
  { prefix: '#tag', query: '#array', desc: 'Include tag' },
  { prefix: ':lang', query: ':java', desc: 'Include language' },
  { prefix: '-word', query: '-two', desc: 'Exclude title word' },
  { prefix: '-#tag', query: '-#dp', desc: 'Exclude tag' },
  { prefix: '-:lang', query: '-:python', desc: 'Exclude language' },
]

const searchInputRef = ref(null)
const searchHelpRef = ref(null)
const isHelpOpen = ref(false)
const localQuery = ref(props.query)
const localDifficulty = ref(props.difficulty)

// Bật/tắt menu hướng dẫn cú pháp khi bấm nút '?'
function toggleHelp() {
  isHelpOpen.value = !isHelpOpen.value
}

// Đóng menu hướng dẫn khi bấm ra ngoài
function closeHelpOnOutsideClick(event) {
  if (!searchHelpRef.value?.contains(event.target)) {
    isHelpOpen.value = false
  }
}

// Đóng menu hướng dẫn khi nhấn phím Escape
function handleHelpKeydown(event) {
  if (event.key === 'Escape' && isHelpOpen.value) {
    isHelpOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', closeHelpOnOutsideClick)
  document.addEventListener('keydown', handleHelpKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeHelpOnOutsideClick)
  document.removeEventListener('keydown', handleHelpKeydown)
})

// Tự động điền hoặc chèn đoạn cú pháp vào ô tìm kiếm và focus lại input
function applyQuerySnippet(snippet, { replace = false } = {}) {
  const current = localQuery.value.trim()
  if (replace || !current) {
    localQuery.value = snippet
  } else {
    localQuery.value = `${current} ${snippet}`
  }
  emit('update:query', localQuery.value)
  emit('search', localQuery.value)
  searchInputRef.value?.focus()
}

watch(
  () => props.query,
  (val) => {
    if (val !== localQuery.value) localQuery.value = val
  },
)

watch(
  () => props.difficulty,
  (val) => {
    if (val !== localDifficulty.value) localDifficulty.value = val
  },
)

function handleQueryInput(event) {
  const value = event.target.value
  localQuery.value = value
  emit('update:query', value)
}

function handleSearchSubmit() {
  emit('search', localQuery.value)
}

function clearQuery() {
  localQuery.value = ''
  emit('update:query', '')
  emit('search', '')
}

function selectDifficulty(value) {
  if (localDifficulty.value === value) return
  localDifficulty.value = value
  emit('update:difficulty', value)
}

function resetFilters() {
  localQuery.value = ''
  localDifficulty.value = ''
  emit('update:query', '')
  emit('update:difficulty', '')
  emit('search', '')
}

const displayedPuzzles = computed(() => {
  // Lọc phía client khi prop items được truyền vào
  if (props.items != null) {
    let list = props.items

    // Lọc theo độ khó
    if (localDifficulty.value) {
      const targetDiff = localDifficulty.value.toUpperCase()
      list = list.filter((p) => (p.difficulty ?? '').toUpperCase() === targetDiff)
    }

    // Lọc theo cú pháp tìm kiếm (tiêu đề, nội dung, tác giả, #tag, :ngôn ngữ và tiền tố loại trừ '-')
    const rawQuery = localQuery.value.trim()
    if (rawQuery) {
      const tokens = rawQuery.split(/\s+/)
      const posWords = []
      const negWords = []
      const posTags = []
      const negTags = []
      const posLangs = []
      const negLangs = []

      for (const token of tokens) {
        if (!token) continue
        if (token.startsWith('-:') && token.length > 2) {
          negLangs.push(token.slice(2).toLowerCase())
        } else if (token.startsWith('-#') && token.length > 2) {
          negTags.push(token.slice(2).toLowerCase())
        } else if (token.startsWith('-') && token.length > 1) {
          negWords.push(token.slice(1).toLowerCase())
        } else if (token.startsWith(':') && token.length > 1) {
          posLangs.push(token.slice(1).toLowerCase())
        } else if (token.startsWith('#') && token.length > 1) {
          posTags.push(token.slice(1).toLowerCase())
        } else {
          posWords.push(token.toLowerCase())
        }
      }

      list = list.filter((puzzle) => {
        const title = (puzzle.title ?? '').toLowerCase()
        const content = (puzzle.content ?? '').toLowerCase()
        const author = (puzzle.authorName ?? '').toLowerCase()
        const tags = (puzzle.tags ?? []).map((t) => t.toLowerCase())
        const langs = (puzzle.languages ?? []).map((l) => l.toLowerCase())

        // 1. Kiểm tra loại trừ: nếu thỏa mãn bất kỳ điều kiện loại trừ nào thì bỏ qua
        if (negLangs.some((nl) => langs.includes(nl))) return false
        if (negTags.some((nt) => tags.includes(nt))) return false
        if (negWords.some((nw) => title.includes(nw))) return false

        // 2. Kiểm tra điều kiện bắt buộc
        if (posLangs.length > 0 && !posLangs.every((pl) => langs.includes(pl))) return false
        if (posTags.length > 0 && !posTags.every((pt) => tags.includes(pt))) return false
        if (
          posWords.length > 0 &&
          !posWords.every((pw) => title.includes(pw) || content.includes(pw) || author.includes(pw))
        ) {
          return false
        }

        return true
      })
    }

    return list
  }

  // Chuyển tiếp dữ liệu trực tiếp từ server hoặc prop puzzles
  return props.puzzles
})

const hasActiveFilters = computed(() => Boolean(localQuery.value.trim() || localDifficulty.value))
</script>

<template>
  <div class="puzzle-catalog">
    <section class="toolbar" aria-label="Filter puzzles">
      <div class="toolbar-row">
        <div class="search">
          <svg class="search-icon" viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="11" cy="11" r="7" />
            <path d="m20 20-3.5-3.5" />
          </svg>
          <input
            ref="searchInputRef"
            :value="localQuery"
            type="search"
            autocomplete="off"
            spellcheck="false"
            aria-label="Search puzzles"
            :placeholder="placeholder"
            @input="handleQueryInput"
            @keyup.enter="handleSearchSubmit"
            @search="handleSearchSubmit"
          />
          <div class="search-actions">
            <button
              v-if="localQuery"
              class="clear-btn"
              type="button"
              aria-label="Clear search"
              @click="clearQuery"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="m6 6 12 12M18 6 6 18" />
              </svg>
            </button>
            <div ref="searchHelpRef" class="search-help">
              <button
                type="button"
                class="help-btn"
                :class="{ 'is-active': isHelpOpen }"
                :aria-expanded="isHelpOpen"
                aria-label="Search syntax help"
                @click.stop="toggleHelp"
              >
                ?
              </button>
              <div
                class="help-tooltip"
                :class="{ 'is-open': isHelpOpen }"
                role="tooltip"
                @mousedown.prevent
              >
                <div class="help-header">
                  <span class="help-title">Search syntax</span>
                  <span class="help-sub">Click to insert</span>
                </div>
                <ul class="help-list">
                  <li v-for="item in SYNTAX_OPTIONS" :key="item.prefix">
                    <button
                      type="button"
                      class="help-item"
                      :title="`Click to add ${item.query} to search`"
                      @click="applyQuerySnippet(item.query)"
                    >
                      <span class="help-item-left">
                        <code>{{ item.prefix }}</code>
                        <span class="help-desc">{{ item.desc }}</span>
                      </span>
                      <code class="help-sample">{{ item.query }}</code>
                    </button>
                  </li>
                </ul>
                <button
                  type="button"
                  class="help-hint-btn"
                  title="Click to apply example query"
                  @click="applyQuerySnippet('sum -two -:java -#array', { replace: true })"
                >
                  <span class="help-hint-label">Example query:</span>
                  <code>sum -two -:java -#array</code>
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="toolbar-controls">
          <div class="segmented" role="group" aria-label="Filter by difficulty">
            <button
              v-for="option in DIFFICULTY_OPTIONS"
              :key="option.value"
              type="button"
              class="seg"
              :class="[option.tone, { active: localDifficulty === option.value }]"
              :aria-pressed="localDifficulty === option.value"
              @click="selectDifficulty(option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="table-card" aria-label="Puzzle list" :aria-busy="loading">
      <div v-if="error" class="empty">
        <p>{{ error }}</p>
        <button class="retry-btn" type="button" @click="emit('retry')">Try again</button>
      </div>

      <div v-else-if="loading && !displayedPuzzles.length" class="empty">
        <p>Loading puzzles...</p>
      </div>

      <div v-else-if="!displayedPuzzles.length" class="empty">
        <p>No puzzles found.</p>
        <button v-if="hasActiveFilters" class="clear-filters-btn" type="button" @click="resetFilters">
          Clear filters
        </button>
      </div>

      <div
        v-else
        class="puzzle-grid"
        :class="[`cols-${columns}`, { 'is-loading': loading }]"
      >
        <PuzzleCard v-for="puzzle in displayedPuzzles" :key="puzzle.id ?? puzzle.index" v-bind="puzzle" />
      </div>

      <slot name="footer" />

      <p v-if="sampleNote" class="sample-note">{{ sampleNoteText }}</p>
    </section>
  </div>
</template>

<style scoped>
.puzzle-catalog {
  display: flex;
  flex-direction: column;
  gap: var(--s-4);
  width: 100%;
}

.toolbar {
  display: flex;
  flex-direction: column;
  gap: var(--s-3);
  padding: var(--s-3);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
}

.toolbar-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--s-3);
}

.search {
  position: relative;
  display: flex;
  flex: 1 1 280px;
  align-items: center;
  max-width: 560px;
}

.search-icon {
  position: absolute;
  left: 10px;
  width: 16px;
  height: 16px;
  fill: none;
  stroke: var(--tx2);
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
  pointer-events: none;
}

.search input {
  width: 100%;
  height: 34px;
  padding: 0 60px 0 34px;
  background: var(--s2);
  border: 1px solid var(--line);
  border-radius: 8px;
  outline: none;
  font: 400 13px var(--f-head);
  color: var(--tx);
  transition: border-color 0.15s var(--ease), box-shadow 0.15s var(--ease);
}

.search input::placeholder {
  color: var(--tx2);
}

.search input:focus {
  border-color: var(--ac);
  box-shadow: 0 0 0 3px var(--ac-soft);
}

.search input::-webkit-search-cancel-button {
  display: none;
}

.search-actions {
  position: absolute;
  right: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.clear-btn {
  display: grid;
  place-items: center;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  color: var(--tx2);
}

.clear-btn svg {
  width: 14px;
  height: 14px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
}

.clear-btn:hover {
  color: var(--tx);
}

.search-help {
  position: relative;
  display: flex;
  align-items: center;
}

.help-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: 4px;
  font: 500 11px/1 var(--f-mono);
  color: var(--tx2);
  user-select: none;
  transition: color 0.15s var(--ease), border-color 0.15s var(--ease), background 0.15s var(--ease);
}

.help-btn:hover,
.help-btn:focus-visible {
  color: var(--tx);
  border-color: var(--ac);
  background: var(--s2);
  outline: none;
}

.help-tooltip {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 60;
  width: 290px;
  padding: 10px;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  opacity: 0;
  visibility: hidden;
  pointer-events: none;
  transition: opacity 0.15s var(--ease), visibility 0.15s var(--ease);
}

.help-tooltip.is-open {
  opacity: 1;
  visibility: visible;
  pointer-events: auto;
}

.help-btn.is-active {
  color: var(--tx);
  border-color: var(--ac);
  background: var(--s2);
}

.help-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
  padding: 0 4px;
}

.help-title {
  font: 600 12px var(--f-head);
  color: var(--tx);
}

.help-sub {
  font: 400 10.5px var(--f-head);
  color: var(--tx3);
}

.help-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.help-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  padding: 5px 6px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 6px;
  text-align: left;
  cursor: pointer;
  transition: background 0.12s var(--ease), border-color 0.12s var(--ease);
}

.help-item:hover,
.help-item:focus-visible {
  background: var(--s2);
  border-color: var(--line);
  outline: none;
}

.help-item-left {
  display: flex;
  align-items: baseline;
  gap: 6px;
  min-width: 0;
}

.help-item code,
.help-hint-btn code {
  padding: 1px 5px;
  background: var(--s2);
  border: 1px solid var(--line);
  border-radius: 4px;
  font: 500 11px var(--f-mono);
  color: var(--ac);
  white-space: nowrap;
}

.help-item:hover code,
.help-hint-btn:hover code {
  background: var(--s3);
  border-color: var(--ac);
}

.help-desc {
  font: 400 11.5px var(--f-head);
  color: var(--tx2);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.help-sample {
  font: 400 10.5px var(--f-mono);
  color: var(--tx3);
  white-space: nowrap;
}

.help-hint-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  width: 100%;
  margin-top: 6px;
  padding: 6px 8px;
  background: var(--s2);
  border: 1px dashed var(--line2);
  border-radius: 6px;
  text-align: left;
  cursor: pointer;
  transition: background 0.12s var(--ease), border-color 0.12s var(--ease);
}

.help-hint-btn:hover,
.help-hint-btn:focus-visible {
  background: var(--s3);
  border-color: var(--ac);
  outline: none;
}

.help-hint-label {
  font: 400 11px var(--f-head);
  color: var(--tx2);
  white-space: nowrap;
}

.clear-btn:hover {
  color: var(--tx);
}

.toolbar-controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--s-2);
  margin-left: auto;
}

.segmented {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 2px;
  background: var(--s2);
  border: 1px solid var(--line);
  border-radius: 8px;
  font: 500 12px/1 var(--f-head);
}

.seg {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 9px;
  border-radius: 6px;
  color: var(--tx2);
  transition: background 0.15s var(--ease), color 0.15s var(--ease);
}

.seg:hover {
  background: var(--s3);
}

.seg.active {
  background: var(--s3);
  color: var(--tx);
}

.seg.easy {
  color: var(--ok);
}

.seg.medium {
  color: var(--ac);
}

.seg.hard {
  color: var(--warn);
}

.table-card {
  overflow: visible;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.puzzle-grid {
  display: grid;
  gap: var(--s-4);
  padding: var(--s-4);
  background: var(--bg);
}

.puzzle-grid.cols-1 {
  grid-template-columns: 1fr;
}

.puzzle-grid.cols-2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

@media (max-width: 768px) {
  .puzzle-grid.cols-2 {
    grid-template-columns: 1fr;
  }
}

.puzzle-grid.is-loading {
  opacity: 0.55;
}

.empty {
  padding: var(--s-7) var(--s-4);
  text-align: center;
  color: var(--tx2);
  font: 400 14px var(--f-head);
}

.empty p {
  margin-bottom: var(--s-3);
}

.retry-btn,
.clear-filters-btn {
  height: 30px;
  padding: 0 14px;
  background: var(--ac);
  border: 1px solid transparent;
  border-radius: 999px;
  color: var(--ac-ink);
  font: 500 12px/1 var(--f-head);
  cursor: pointer;
}

.retry-btn:hover,
.clear-filters-btn:hover {
  filter: brightness(1.07);
}

.clear-filters-btn {
  background: var(--s2);
  border-color: var(--line2);
  color: var(--tx);
}

.sample-note {
  padding: var(--s-2) var(--s-4);
  text-align: center;
  font: 400 11px var(--f-head);
  color: var(--tx3);
  font-style: italic;
  border-top: 1px solid var(--line);
}
</style>
