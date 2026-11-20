<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import ContestCard from '../../features/contest/components/ContestCard.vue'
import PuzzleCatalog from '../../components/common/PuzzleCatalog.vue'
import SiteFooter from '../../components/layouts/SiteFooter.vue'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { fetchPuzzles } from '../../features/puzzle/puzzleApi'
import { toPuzzleCard } from '../../features/puzzle/puzzleMapper'
import { useAuthStore } from '../../stores/authStore'

const authStore = useAuthStore()
const router = useRouter()
const contestTrack = ref(null)

const community = ref({ src: '/favicon.svg', name: 'fpt-algo-club', type: 'community' })

const contests = [
  {
    title: 'Weekly Contest 389',
    status: 'Open',
    statusTone: 'easy',
    countdownLabel: 'Starts In',
    countdown: '01d 14:32:08',
    registeredCount: '12,480 devs',
    puzzleCount: 4,
    actionLabel: 'Register Now',
  },
  {
    title: 'Weekly Contest 389',
    status: 'Open',
    statusTone: 'easy',
    countdownLabel: 'Starts In',
    countdown: '01d 14:32:08',
    registeredCount: '12,480 devs',
    puzzleCount: 4,
    actionLabel: 'Register Now',
  },
  {
    title: 'Weekly Contest 389',
    status: 'Open',
    statusTone: 'easy',
    countdownLabel: 'Starts In',
    countdown: '01d 14:32:08',
    registeredCount: '12,480 devs',
    puzzleCount: 4,
    actionLabel: 'Register Now',
  },
  {
    title: 'Weekly Contest 389',
    status: 'Open',
    statusTone: 'easy',
    countdownLabel: 'Starts In',
    countdown: '01d 14:32:08',
    registeredCount: '12,480 devs',
    puzzleCount: 4,
    actionLabel: 'Register Now',
  },
]

const difficultyOptions = [
  { label: 'All', value: '' },
  { label: 'Easy', value: 'EASY', tone: 'easy' },
  { label: 'Medium', value: 'MEDIUM', tone: 'medium' },
  { label: 'Hard', value: 'HARD', tone: 'hard' },
]

const query = ref('')
const difficulty = ref('')
const page = ref(0)
const size = ref(10)
const puzzles = ref([])
const loading = ref(true)
const error = ref('')

let searchTimer
let requestToken = 0

const hasNext = computed(() => puzzles.value.length === size.value)
const rangeStart = computed(() => (puzzles.value.length ? page.value * size.value + 1 : 0))
const rangeEnd = computed(() => page.value * size.value + puzzles.value.length)

async function loadPuzzles() {
  const token = ++requestToken
  loading.value = true
  error.value = ''

  try {
    const data = await fetchPuzzles({
      q: query.value.trim(),
      difficulty: difficulty.value,
      page: page.value,
      size: size.value,
    })
    if (token !== requestToken) return
    puzzles.value = data.map(toPuzzleCard)
  } catch (loadError) {
    if (token !== requestToken) return
    error.value = 'Could not load puzzles. Try again.'
    puzzles.value = []
  } finally {
    if (token === requestToken) loading.value = false
  }
}

function applySearch() {
  clearTimeout(searchTimer)
  page.value = 0
  loadPuzzles()
}

watch(query, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(applySearch, 300)
})

watch(difficulty, () => {
  clearTimeout(searchTimer)
  page.value = 0
  loadPuzzles()
})

function onSizeChange() {
  clearTimeout(searchTimer)
  page.value = 0
  loadPuzzles()
}

function goToPage(target) {
  if (target < 0 || (target > page.value && !hasNext.value)) return
  clearTimeout(searchTimer)
  page.value = target
  loadPuzzles()
}

onMounted(async () => {
  await authStore.initialize()
  if (authStore.status !== 'authenticated') {
    router.push('/login')
    return
  }
  loadPuzzles()
})

onBeforeUnmount(() => clearTimeout(searchTimer))

function scrollContests(direction) {
  const track = contestTrack.value
  if (!track) return

  const card = track.querySelector('.challenge-card')
  const gap = Number.parseFloat(getComputedStyle(track).columnGap) || 0
  const distance = (card?.getBoundingClientRect().width ?? track.clientWidth) + gap

  track.scrollBy({ left: direction * distance, behavior: 'smooth' })
}
</script>

<template>
  <div class="page">
    <svg class="icon-sprite" aria-hidden="true" focusable="false">
      <symbol id="ic-calendar" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="17" rx="2" /><path d="M8 2v4M16 2v4M3 9h18" /></symbol>
      <symbol id="ic-clock" viewBox="0 0 24 24"><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></symbol>
      <symbol id="ic-arrow" viewBox="0 0 24 24"><path d="M5 12h14M13 6l6 6-6 6" /></symbol>
      <symbol id="ic-trophy" viewBox="0 0 24 24"><path d="M8 21h8M12 17v4M7 4h10v5a5 5 0 0 1-10 0zM7 6H4v1a3 3 0 0 0 3 3M17 6h3v1a3 3 0 0 1-3 3" /></symbol>
      <symbol id="ic-timer" viewBox="0 0 24 24"><path d="M10 2h4M12 14l3-3" /><circle cx="12" cy="14" r="8" /></symbol>
      <symbol id="ic-user-check" viewBox="0 0 24 24"><circle cx="9" cy="8" r="3" /><path d="M3 21a6 6 0 0 1 12 0M16 11l2 2 4-4" /></symbol>
      <symbol id="ic-medal" viewBox="0 0 24 24"><circle cx="12" cy="15" r="5" /><path d="M9 11 7 3h4l1 4 1-4h4l-2 8" /></symbol>
      <symbol id="ic-search" viewBox="0 0 24 24"><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></symbol>
      <symbol id="ic-checkbox" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="16" rx="2" /><path d="m7 12 3 3 7-7" /></symbol>
      <symbol id="ic-chevron-down" viewBox="0 0 24 24"><path d="m6 9 6 6 6-6" /></symbol>
      <symbol id="ic-chevron-left" viewBox="0 0 24 24"><path d="m15 6-6 6 6 6" /></symbol>
      <symbol id="ic-chevron-right" viewBox="0 0 24 24"><path d="m9 6 6 6-6 6" /></symbol>
      <symbol id="ic-shuffle" viewBox="0 0 24 24"><path d="M16 3h5v5M21 16v5h-5M4 20 21 3M4 4l5 5M15 15l6 6" /></symbol>
      <symbol id="ic-code" viewBox="0 0 24 24"><path d="m8 8-4 4 4 4M16 8l4 4-4 4" /></symbol>
      <symbol id="ic-verified" viewBox="0 0 24 24"><path d="m12 2 2.4 2.1 3.1-.4 1.1 3 2.8 1.5-1 3 1 3-2.8 1.5-1.1 3-3.1-.4L12 22l-2.4-2.1-3.1.4-1.1-3L2.6 15.8l1-3-1-3 2.8-1.5 1.1-3 3.1.4z" /><path d="m8.5 12 2.4 2.4 4.6-4.8" /></symbol>
      <symbol id="ic-bolt" viewBox="0 0 24 24"><path d="M13 2 4 14h7l-1 8 9-12h-7z" /></symbol>
      <symbol id="ic-bookmark" viewBox="0 0 24 24"><path d="M6 3h12v18l-6-4-6 4z" /></symbol>
      <symbol id="ic-close" viewBox="0 0 24 24"><path d="m6 6 12 12M18 6 6 18" /></symbol>
    </svg>

    <UserNavbar />

    <main class="puzzles">
      <div class="contest-carousel">
        <button class="carousel-arrow prev" type="button" aria-label="Previous contests" @click="scrollContests(-1)">
          <svg class="ic"><use href="#ic-chevron-left" /></svg>
        </button>
        <section ref="contestTrack" class="top-cards" aria-label="Contests">
          <ContestCard
            v-for="(contest, index) in contests"
            :key="index"
            v-bind="contest"
            :community="community"
          />
        </section>
        <button class="carousel-arrow next" type="button" aria-label="Next contests" @click="scrollContests(1)">
          <svg class="ic"><use href="#ic-chevron-right" /></svg>
        </button>
      </div>
      <PuzzleCatalog
        v-model:query="query"
        v-model:difficulty="difficulty"
        :puzzles="puzzles"
        :columns="2"
        :loading="loading"
        :error="error"
        @retry="loadPuzzles"
        @search="applySearch"
      >
        <template #footer>
          <div class="table-foot">
            <div class="foot-left">
              <span v-if="puzzles.length">
                Showing <strong>{{ rangeStart }}–{{ rangeEnd }}</strong> puzzles
              </span>
              <span v-else>No puzzles</span>
              <span class="dot">•</span>
              <label class="rows-control">
                Rows:
                <select v-model.number="size" aria-label="Puzzles per page" @change="onSizeChange">
                  <option :value="10">10 / page</option>
                  <option :value="30">30 / page</option>
                  <option :value="50">50 / page</option>
                </select>
              </label>
            </div>

            <nav class="pager" aria-label="Page navigation">
              <button
                class="page-btn"
                type="button"
                aria-label="Previous page"
                :disabled="page === 0 || loading"
                @click="goToPage(page - 1)"
              >
                <svg class="ic"><use href="#ic-chevron-left" /></svg>
              </button>
              <span class="page-status">Page {{ page + 1 }}</span>
              <button
                class="page-btn"
                type="button"
                aria-label="Next page"
                :disabled="!hasNext || loading"
                @click="goToPage(page + 1)"
              >
                <svg class="ic"><use href="#ic-chevron-right" /></svg>
              </button>
            </nav>
          </div>
        </template>
      </PuzzleCatalog>
    </main>

    <SiteFooter />
  </div>
</template>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100svh;
  background: var(--bg);
}

.icon-sprite {
  position: absolute;
  width: 0;
  height: 0;
  overflow: hidden;
}

.puzzles {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: var(--s-5);
  width: 100%;
  max-width: var(--rail);
  margin: 0 auto;
  padding: var(--s-5) var(--s-6) var(--s-8);
}

.ic {
  flex: none;
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.dot {
  color: var(--tx3);
}

.muted {
  color: var(--tx2);
}

.accent {
  color: var(--ac);
}

.contest-carousel {
  position: relative;
}

.carousel-arrow {
  position: absolute;
  top: 50%;
  z-index: 2;
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  transform: translateY(-50%);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: 50%;
  color: var(--tx2);
  transition: background 0.15s var(--ease), color 0.15s var(--ease);
}

.carousel-arrow:hover {
  background: var(--s3);
  color: var(--tx);
}

.carousel-arrow.prev {
  left: -12px;
}

.carousel-arrow.next {
  right: -12px;
}

.top-cards {
  display: flex;
  gap: var(--s-4);
  overflow-x: auto;
  padding-bottom: 2px;
  scrollbar-width: none;
}

.top-cards::-webkit-scrollbar {
  display: none;
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
  flex: 1 1 320px;
  align-items: center;
  max-width: 560px;
}

.search-icon {
  position: absolute;
  left: 10px;
  color: var(--tx2);
  pointer-events: none;
}

.search input {
  width: 100%;
  height: 34px;
  padding: 0 84px 0 34px;
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
  gap: 4px;
}

.clear-btn {
  display: grid;
  place-items: center;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  color: var(--tx2);
}

.clear-btn:hover {
  color: var(--tx);
}

kbd {
  padding: 1px 5px;
  background: var(--s1);
  border: 1px solid var(--line);
  border-radius: 4px;
  font: 400 10px var(--f-head);
  color: var(--tx2);
}

.toolbar-controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--s-2);
  margin-left: auto;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: var(--s-2);
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

.seg .count {
  font-size: 10px;
  color: var(--tx2);
}

.dropdown {
  position: relative;
}

.dd-menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  z-index: 20;
  min-width: 180px;
  padding: var(--s-1);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  box-shadow: var(--sh-card);
}

.dd-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
  width: 100%;
  padding: 7px 10px;
  border-radius: 6px;
  font: 400 12px var(--f-head);
  color: var(--tx2);
  text-align: left;
}

.dd-item:hover {
  background: var(--s2);
  color: var(--tx);
}

.dd-item[aria-selected='true'] {
  background: var(--ac-soft);
  color: var(--ac);
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
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--s-4);
  padding: var(--s-4);
  background: var(--bg);
}

.empty {
  padding: var(--s-7) var(--s-4);
  text-align: center;
  color: var(--tx2);
}

.empty p {
  margin-bottom: var(--s-3);
  font-size: 14px;
}

.retry-btn {
  height: 30px;
  padding: 0 14px;
  background: var(--ac);
  border: 1px solid transparent;
  border-radius: 999px;
  color: var(--ac-ink);
  font: 500 12px/1 var(--f-head);
}

.retry-btn:hover {
  filter: brightness(1.07);
}

.puzzle-grid.is-loading {
  opacity: 0.55;
}

.table-foot {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
  padding: var(--s-3) var(--s-4);
  background: color-mix(in srgb, var(--s2) 70%, transparent);
  border-top: 1px solid var(--line2);
  font: 400 12px var(--f-head);
  color: var(--tx2);
}

.foot-left {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--s-2);
}

.foot-left strong {
  font-weight: 500;
  color: var(--tx);
}

.rows-control {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
}

.rows-control select {
  padding: 2px 6px;
  background: var(--s1);
  border: 1px solid var(--line);
  border-radius: 5px;
  font: 400 11px var(--f-head);
  color: var(--tx);
  outline: none;
}

.rows-control select:focus {
  border-color: var(--ac);
}

.sample-note {
  color: var(--tx2);
}

.pager {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-status {
  padding: 0 6px;
  color: var(--tx);
  font: 500 12px var(--f-head);
}

.page-btn {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  background: var(--s1);
  border: 1px solid var(--line);
  border-radius: 7px;
  font: 500 12px var(--f-head);
  color: var(--tx2);
  transition: background 0.15s var(--ease), color 0.15s var(--ease);
}

.page-btn:hover:not(:disabled) {
  background: var(--s3);
  color: var(--tx);
}

.page-btn.active {
  background: var(--ac);
  border-color: transparent;
  color: var(--ac-ink);
}

.page-btn:disabled {
  opacity: 0.4;
}

.ellipsis {
  padding: 0 4px;
  color: var(--tx2);
}

@media (max-width: 900px) {
  .puzzles {
    padding: var(--s-4) var(--s-4) var(--s-7);
  }

  .toolbar-controls {
    width: 100%;
    margin-left: 0;
    justify-content: space-between;
  }

  .search {
    max-width: none;
  }
}

@media (max-width: 760px) {
  .puzzle-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .segmented {
    overflow-x: auto;
  }

  .hide-phone {
    display: none;
  }

  .sample-note {
    display: none;
  }
}
</style>
