<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import PuzzleCard from '../../features/puzzle/components/PuzzleCard.vue'
import { fetchTags } from '../../features/puzzle/puzzleApi'

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

// Quản lý dữ liệu tag và menu autocomplete khi gõ '#' hoặc '-#'
const availableTags = ref([])
const tagsLoading = ref(false)
const tagsError = ref('')

const isTagDropdownOpen = ref(false)
const activeTagToken = ref(null)
const highlightedTagIndex = ref(0)
const tagDropdownRef = ref(null)
const tagMenuListRef = ref(null)

// Tải danh sách tag từ server (giống cơ chế cache trong PuzzleCreatePage)
async function loadTags() {
  tagsLoading.value = true
  tagsError.value = ''
  try {
    const data = await fetchTags()
    if (Array.isArray(data) && data.length > 0) {
      availableTags.value = data
    }
  } catch (err) {
    tagsError.value = err?.message || ''
  } finally {
    tagsLoading.value = false
  }
}

// Danh sách tag hiệu dụng: ưu tiên dữ liệu từ server (fetchTags), nếu rỗng thì trích xuất từ props.items
const effectiveTags = computed(() => {
  if (availableTags.value && availableTags.value.length > 0) {
    return availableTags.value
  }

  if (props.items && props.items.length > 0) {
    const tagCounts = {}
    for (const item of props.items) {
      if (Array.isArray(item.tags)) {
        for (const t of item.tags) {
          const norm = String(t).trim().toLowerCase()
          if (norm) {
            tagCounts[norm] = (tagCounts[norm] || 0) + 1
          }
        }
      }
    }
    return Object.entries(tagCounts)
      .map(([name, puzzleCount]) => ({ name, puzzleCount }))
      .sort((a, b) => b.puzzleCount - a.puzzleCount || a.name.localeCompare(b.name))
  }

  return []
})

// Lọc các tag khớp với từ khóa người dùng đang gõ sau dấu '#'
const filteredDropdownTags = computed(() => {
  const active = activeTagToken.value
  if (!active) return []
  const q = active.query.trim().toLowerCase()
  if (!q) return effectiveTags.value.slice(0, 30)
  return effectiveTags.value
    .filter((tag) => tag.name.toLowerCase().includes(q))
    .slice(0, 30)
})

// Bật/tắt menu hướng dẫn cú pháp khi bấm nút '?'
function toggleHelp() {
  isHelpOpen.value = !isHelpOpen.value
  if (isHelpOpen.value) {
    closeTagDropdown()
  }
}

// Đóng menu hướng dẫn hoặc menu tag khi bấm ra ngoài
function closeOnOutsideClick(event) {
  if (!searchHelpRef.value?.contains(event.target)) {
    isHelpOpen.value = false
  }
  if (
    !tagDropdownRef.value?.contains(event.target) &&
    !searchInputRef.value?.contains(event.target)
  ) {
    closeTagDropdown()
  }
}

// Đóng menu khi nhấn phím Escape
function handleGlobalKeydown(event) {
  if (event.key === 'Escape') {
    if (isTagDropdownOpen.value) {
      closeTagDropdown()
    } else if (isHelpOpen.value) {
      isHelpOpen.value = false
    }
  }
}

onMounted(() => {
  loadTags()
  document.addEventListener('click', closeOnOutsideClick)
  document.addEventListener('keydown', handleGlobalKeydown)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeOnOutsideClick)
  document.removeEventListener('keydown', handleGlobalKeydown)
})

// Tự động điền hoặc chèn đoạn cú pháp vào ô tìm kiếm và focus lại input
function applyQuerySnippet(snippet, { replace = false } = {}) {
  closeTagDropdown()
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

// Phát hiện token tag đang được con trỏ chỉnh sửa (bắt đầu bằng # hoặc -#)
function detectActiveTagToken() {
  const input = searchInputRef.value
  if (!input) return null

  const val = localQuery.value || ''
  const selStart = input.selectionStart ?? val.length
  const selEnd = input.selectionEnd ?? val.length

  if (selStart !== selEnd) return null

  const textBefore = val.slice(0, selEnd)
  const match = textBefore.match(/(?:^|\s)(-?#[a-zA-Z0-9_-]*)$/)
  if (!match) return null

  const fullToken = match[1]
  const tokenStart = selEnd - fullToken.length
  const isNegative = fullToken.startsWith('-#')
  const prefix = isNegative ? '-#' : '#'
  const query = fullToken.slice(prefix.length)

  const textAfter = val.slice(selEnd)
  const restMatch = textAfter.match(/^[a-zA-Z0-9_-]*/)
  const restLength = restMatch ? restMatch[0].length : 0
  const tokenEnd = selEnd + restLength

  return {
    fullToken,
    prefix,
    query,
    tokenStart,
    tokenEnd,
  }
}

// Kiểm tra và cập nhật trạng thái mở dropdown tag autocomplete
function checkTagAutocomplete() {
  const token = detectActiveTagToken()
  if (token) {
    activeTagToken.value = token
    isTagDropdownOpen.value = true
    if (highlightedTagIndex.value >= filteredDropdownTags.value.length) {
      highlightedTagIndex.value = 0
    }
  } else {
    closeTagDropdown()
  }
}

// Đóng dropdown tag
function closeTagDropdown() {
  isTagDropdownOpen.value = false
  activeTagToken.value = null
  highlightedTagIndex.value = 0
}

// Chọn một tag từ dropdown và hoàn thiện vào ô tìm kiếm
function selectTag(tagName) {
  const active = activeTagToken.value
  const input = searchInputRef.value
  const val = localQuery.value || ''

  if (!active) {
    applyQuerySnippet(`#${tagName}`)
    closeTagDropdown()
    return
  }

  const before = val.slice(0, active.tokenStart)
  const after = val.slice(active.tokenEnd).replace(/^\s*/, '')
  const inserted = `${active.prefix}${tagName} `
  const newQuery = before + inserted + after

  localQuery.value = newQuery
  emit('update:query', newQuery)
  emit('search', newQuery)

  closeTagDropdown()

  nextTick(() => {
    if (input) {
      input.focus()
      const newCursorPos = (before + inserted).length
      input.setSelectionRange(newCursorPos, newCursorPos)
    }
  })
}

// Kiểm tra xem một tag đã xuất hiện trong chuỗi tìm kiếm hay chưa
function isTagInQuery(tagName) {
  const q = (localQuery.value || '').toLowerCase()
  const lowerTag = tagName.toLowerCase()
  return q.includes(`#${lowerTag}`) || q.includes(`-#${lowerTag}`)
}

// Cuộn item được chọn bằng phím mũi tên vào vùng nhìn thấy
function scrollHighlightedItemIntoView() {
  nextTick(() => {
    const list = tagMenuListRef.value
    if (!list) return
    const items = list.querySelectorAll('.tag-menu-item')
    const activeEl = items[highlightedTagIndex.value]
    if (activeEl && typeof activeEl.scrollIntoView === 'function') {
      activeEl.scrollIntoView({ block: 'nearest' })
    }
  })
}

// Xử lý các phím điều hướng khi đang mở dropdown tag
function handleSearchKeydown(event) {
  if (isTagDropdownOpen.value && filteredDropdownTags.value.length > 0) {
    if (event.key === 'ArrowDown') {
      event.preventDefault()
      highlightedTagIndex.value = (highlightedTagIndex.value + 1) % filteredDropdownTags.value.length
      scrollHighlightedItemIntoView()
      return
    }
    if (event.key === 'ArrowUp') {
      event.preventDefault()
      highlightedTagIndex.value =
        (highlightedTagIndex.value - 1 + filteredDropdownTags.value.length) %
        filteredDropdownTags.value.length
      scrollHighlightedItemIntoView()
      return
    }
    if (event.key === 'Enter' || event.key === 'Tab') {
      if (
        highlightedTagIndex.value >= 0 &&
        highlightedTagIndex.value < filteredDropdownTags.value.length
      ) {
        event.preventDefault()
        selectTag(filteredDropdownTags.value[highlightedTagIndex.value].name)
        return
      }
    }
    if (event.key === 'Escape') {
      event.preventDefault()
      closeTagDropdown()
      return
    }
  }

  if (event.key === 'Enter') {
    handleSearchSubmit()
  }
}

function handleSearchKeyup(event) {
  if (['ArrowDown', 'ArrowUp', 'Enter', 'Tab', 'Escape'].includes(event.key)) {
    return
  }
  checkTagAutocomplete()
}

function handleSearchClick() {
  checkTagAutocomplete()
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
  checkTagAutocomplete()
}

function handleSearchSubmit() {
  emit('search', localQuery.value)
}

function clearQuery() {
  localQuery.value = ''
  closeTagDropdown()
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
  closeTagDropdown()
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
            @keydown="handleSearchKeydown"
            @keyup="handleSearchKeyup"
            @click="handleSearchClick"
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

          <!-- Menu gợi ý Tag Autocomplete khi gõ '#' hoặc '-#' -->
          <div
            v-if="isTagDropdownOpen && activeTagToken"
            ref="tagDropdownRef"
            class="tag-autocomplete-menu"
            role="listbox"
            aria-label="Tag suggestions"
          >
            <div class="tag-menu-header">
              <span class="tag-menu-title">Tags ({{ activeTagToken.prefix }})</span>
              <span class="tag-menu-sub">↑↓ navigate · Enter to select</span>
            </div>
            <div ref="tagMenuListRef" class="tag-menu-list">
              <p v-if="tagsLoading" class="menu-empty">Loading tags...</p>
              <button
                v-for="(tag, index) in filteredDropdownTags"
                :key="tag.name"
                type="button"
                class="tag-menu-item"
                :class="{
                  'is-active': index === highlightedTagIndex,
                  'is-selected': isTagInQuery(tag.name),
                }"
                role="option"
                :aria-selected="index === highlightedTagIndex"
                @mousedown.prevent="selectTag(tag.name)"
                @mouseenter="highlightedTagIndex = index"
              >
                <span class="tag-hash">{{ activeTagToken.prefix }}</span>
                <span class="tag-name">{{ tag.name }}</span>
                <span class="tag-count">
                  {{ tag.puzzleCount != null ? tag.puzzleCount : 0 }}
                  {{ (tag.puzzleCount != null ? tag.puzzleCount : 0) === 1 ? 'puzzle' : 'puzzles' }}
                </span>
              </button>
              <p
                v-if="!tagsLoading && !filteredDropdownTags.length"
                class="menu-empty"
              >
                No matching tags{{ activeTagToken.query ? ` for "${activeTagToken.query}"` : '.' }}
              </p>
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

      <!-- Trạng thái Skeleton Shimmer (sao chép phong cách từ ContestCatalog theo DESIGN.md) -->
      <div
        v-else-if="loading && !displayedPuzzles.length"
        class="puzzle-grid"
        :class="`cols-${columns}`"
        aria-busy="true"
        aria-label="Loading puzzles"
      >
        <div v-for="n in (columns > 1 ? 6 : 3)" :key="n" class="skeleton-card">
          <div class="skeleton-hero"></div>
          <div class="skeleton-foot"></div>
        </div>
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

/* Tag Autocomplete Dropdown: Menu gợi ý thẻ khi gõ '#' hoặc '-#' */
.tag-autocomplete-menu {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  z-index: 70;
  padding: 6px;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.45);
}

.tag-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 8px 6px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 4px;
}

.tag-menu-title {
  font: 500 11px var(--f-mono);
  color: var(--tx);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.tag-menu-sub {
  font: 400 11px var(--f-mono);
  color: var(--tx3);
}

.tag-menu-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 220px;
  overflow-y: auto;
  scrollbar-color: var(--line2) transparent;
  scrollbar-width: thin;
}

.tag-menu-list::-webkit-scrollbar {
  width: 4px;
}

.tag-menu-list::-webkit-scrollbar-thumb {
  background: var(--line2);
  border-radius: 999px;
}

.tag-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 30px;
  padding: 0 8px;
  color: var(--tx);
  background: transparent;
  border: 1px solid transparent;
  border-radius: 5px;
  font: 400 12px var(--f-head);
  text-align: left;
  cursor: pointer;
  transition: background 0.12s var(--ease), border-color 0.12s var(--ease);
}

.tag-menu-item:hover,
.tag-menu-item.is-active {
  background: var(--s2);
  border-color: var(--line);
}

.tag-menu-item.is-selected {
  color: var(--ac);
  background: var(--ac-soft);
}

.tag-hash {
  font: 500 11px var(--f-mono);
  color: var(--ac);
}

.tag-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.tag-count {
  flex: none;
  margin-left: auto;
  color: var(--tx3);
  font: 500 11px var(--f-mono);
  font-variant-numeric: tabular-nums;
}

.tag-menu-item.is-selected .tag-count {
  color: var(--ac);
  opacity: 0.85;
}

.menu-empty {
  margin: 0;
  padding: 12px 8px;
  font: 400 12px var(--f-head);
  color: var(--tx2);
  text-align: center;
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
  font: 500 12px var(--f-head);
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

/* Thẻ Skeleton Shimmer khi đang tải (đồng bộ với ContestCatalog) */
.skeleton-card {
  height: 195px;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.skeleton-hero {
  flex: 1;
  background: linear-gradient(90deg, var(--s2) 25%, var(--s3) 50%, var(--s2) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-foot {
  height: 48px;
  border-top: 1px solid var(--line);
  background: var(--s1);
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
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
