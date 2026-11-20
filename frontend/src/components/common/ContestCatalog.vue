<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import ContestCard from '../../features/contest/components/ContestCard.vue'
import { formatContestCountdown, toContestCard } from '../../features/contest/contestMapper'

const props = defineProps({
  items: { type: Array, default: () => [] },
  columns: { type: Number, default: 1 },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  placeholder: { type: String, default: 'Search contests...' },
})

const emit = defineEmits(['retry'])

const searchQuery = ref('')
const now = ref(Date.now())
let timer = null

onMounted(() => {
  // Bộ đếm thời gian cập nhật mỗi giây để đồng hồ countdown luôn chính xác
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) window.clearInterval(timer)
})

// Chuyển đổi cuộc thi sang dạng thẻ hiển thị và tính toán countdown theo thời gian thực
function resolveCard(contest, currentTimestamp) {
  const card = toContestCard(contest)
  card.countdown = formatContestCountdown(contest, currentTimestamp)

  // Cập nhật trạng thái động nếu có mốc thời gian bắt đầu và kết thúc
  const startTime = contest.startTime ? new Date(contest.startTime).getTime() : NaN
  const endTime = contest.endTime ? new Date(contest.endTime).getTime() : NaN

  if (Number.isFinite(endTime) && currentTimestamp >= endTime) {
    card.status = 'CLOSED'
    card.actionLabel = 'View Standings'
  } else if (Number.isFinite(startTime) && currentTimestamp >= startTime) {
    card.status = 'OPEN'
    card.actionLabel = 'Join Contest'
  } else if (Number.isFinite(startTime) && currentTimestamp < startTime) {
    card.status = 'UPCOMING'
    card.actionLabel = 'Register Now'
  }

  return card
}

// Lọc các cuộc thi theo từ khóa tìm kiếm (tiêu đề hoặc mô tả)
const displayedContests = computed(() => {
  const list = Array.isArray(props.items) ? props.items : []
  const query = searchQuery.value.trim().toLowerCase()

  const filtered = query
    ? list.filter((contest) => {
        const title = (contest.title || '').toLowerCase()
        const content = (contest.content || contest.description || '').toLowerCase()
        return title.includes(query) || content.includes(query)
      })
    : list

  return filtered.map((contest) => resolveCard(contest, now.value))
})
</script>

<template>
  <div class="contest-catalog">
    <!-- Thanh công cụ tìm kiếm chuẩn theo DESIGN.md -->
    <section class="toolbar" aria-label="Filter contests">
      <div class="toolbar-row">
        <div class="search">
          <svg class="search-icon" viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="11" cy="11" r="7" />
            <path d="m20 20-3.5-3.5" />
          </svg>
          <input
            v-model="searchQuery"
            type="search"
            autocomplete="off"
            spellcheck="false"
            aria-label="Search contests"
            :placeholder="placeholder"
          />
          <button
            v-if="searchQuery"
            class="clear-btn"
            type="button"
            aria-label="Clear search"
            @click="searchQuery = ''"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="m6 6 12 12M18 6 6 18" />
            </svg>
          </button>
        </div>
      </div>
    </section>

    <!-- Trạng thái lỗi tải dữ liệu -->
    <div v-if="error" class="empty" role="alert">
      <p>{{ error }}</p>
      <button class="action-btn" type="button" @click="emit('retry')">Try again</button>
    </div>

    <!-- Trạng thái Skeleton Shimmer (tuân thủ DESIGN.md: dùng shimmer thay vì spinner) -->
    <div
      v-else-if="loading && (!items || items.length === 0)"
      class="contest-grid cols-1"
      aria-busy="true"
      aria-label="Loading contests"
    >
      <div v-for="n in 3" :key="n" class="skeleton-card">
        <div class="skeleton-hero"></div>
        <div class="skeleton-foot"></div>
      </div>
    </div>

    <!-- Trạng thái rỗng: cộng đồng chưa có cuộc thi nào -->
    <div v-else-if="!items || items.length === 0" class="empty">
      <p>No contests hosted by this community yet.</p>
    </div>

    <!-- Trạng thái rỗng: tìm kiếm không khớp kết quả nào -->
    <div v-else-if="displayedContests.length === 0" class="empty">
      <p>No contests match "{{ searchQuery }}".</p>
      <button class="action-btn" type="button" @click="searchQuery = ''">Clear search</button>
    </div>

    <!-- Lưới hiển thị danh sách thẻ cuộc thi (mặc định 1 cột) -->
    <div v-else class="contest-grid cols-1" :class="{ 'is-loading': loading }">
      <ContestCard
        v-for="contest in displayedContests"
        :key="contest.id"
        v-bind="contest"
      />
    </div>
  </div>
</template>

<style scoped>
.contest-catalog {
  display: flex;
  flex-direction: column;
  gap: var(--s-4);
  width: 100%;
}

/* Thanh công cụ tìm kiếm */
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
  justify-content: space-between;
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
  padding: 0 34px;
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

.toolbar-controls {
  display: flex;
  align-items: center;
  gap: var(--s-2);
}

.create-contest-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 14px;
  background: var(--ac);
  border: 1px solid transparent;
  border-radius: var(--r-sm);
  color: var(--ac-ink);
  font: 500 13px/1 var(--f-head);
  text-decoration: none;
  white-space: nowrap;
  transition: filter 0.15s var(--ease), transform 0.12s var(--ease);
}

.create-contest-btn:hover {
  filter: brightness(1.08);
}

.create-contest-btn:active {
  transform: translateY(1px);
}

.create-contest-btn svg {
  width: 14px;
  height: 14px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.clear-btn {
  position: absolute;
  right: 8px;
  display: grid;
  place-items: center;
  width: 20px;
  height: 20px;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: var(--tx2);
  cursor: pointer;
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

/* Bố cục lưới thẻ cuộc thi */
.contest-grid {
  display: grid;
  gap: var(--s-4);
  width: 100%;
}

.contest-grid.cols-1 {
  grid-template-columns: 1fr;
}

.contest-grid.is-loading {
  opacity: 0.55;
}

/* Thẻ Skeleton Shimmer khi đang tải */
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

/* Trạng thái rỗng / lỗi chuẩn theo DESIGN.md: điềm tĩnh, không emoji */
.empty {
  padding: var(--s-7) var(--s-4);
  text-align: center;
  color: var(--tx2);
  font: 400 14px var(--f-head);
  background: var(--s1);
  border: 1px dashed var(--line2);
  border-radius: var(--r);
}

.empty p {
  margin: 0 0 var(--s-3);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 30px;
  padding: 0 14px;
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  color: var(--tx);
  font: 500 12px var(--f-head);
  cursor: pointer;
  transition: background 0.15s var(--ease), border-color 0.15s var(--ease), transform 0.12s var(--ease);
}

.action-btn:hover {
  background: var(--s3);
}

.action-btn:active {
  transform: translateY(1px);
}
</style>
