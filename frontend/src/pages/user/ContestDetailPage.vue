<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import Avatar from '../../components/common/Avatar.vue'
import PuzzleCatalog from '../../components/common/PuzzleCatalog.vue'
import SiteFooter from '../../components/layouts/SiteFooter.vue'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { describeApiError } from '../../config/apiClient'
import {
  fetchContest,
  fetchContestPuzzles,
  fetchContestants,
  joinContest,
} from '../../features/contest/contestApi'
import {
  formatContestCountdown,
  toContestDetail,
  toContestPuzzleCard,
} from '../../features/contest/contestMapper'
import { useAuthStore } from '../../stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// Mốc thời gian thực tế hiện tại để đếm ngược từng giây
const now = ref(Date.now())
let countdownInterval = null

// Thông tin chi tiết cuộc thi, bài toán và thí sinh từ backend
const contest = ref(null)
const contestPuzzles = ref([])
const contestants = ref([])
const loadStatus = ref('loading') // 'loading' | 'ready' | 'missing' | 'failed'
const errorMessage = ref('')

// Trạng thái thao tác tham gia cuộc thi
const joining = ref(false)
const joinError = ref('')

// Trạng thái sao chép liên kết cuộc thi
const copied = ref(false)
let copyTimer = null

// Từ khóa tìm kiếm thí sinh trên bảng xếp hạng
const leaderboardQuery = ref('')

// Thông tin người dùng hiện tại
const currentUserId = computed(() => authStore.profile?.id ?? null)
const isAuthenticated = computed(() => authStore.status === 'authenticated')

// Trạng thái cuộc thi thực tế (UPCOMING, OPEN, CLOSED) cập nhật theo thời gian thực
const currentContestStatus = computed(() => {
  if (!contest.value) return 'OPEN'
  const startTime = contest.value.startTime ? new Date(contest.value.startTime).getTime() : NaN
  const endTime = contest.value.endTime ? new Date(contest.value.endTime).getTime() : NaN
  if (Number.isFinite(endTime) && now.value >= endTime) {
    return 'CLOSED'
  }
  if (Number.isFinite(startTime) && now.value < startTime) {
    return 'UPCOMING'
  }
  return (contest.value.status || 'OPEN').toUpperCase()
})

// Chuỗi đếm ngược thời gian cập nhật liên tục từng giây
const countdownDisplay = computed(() => {
  if (!contest.value) return ''
  return formatContestCountdown(contest.value, now.value)
})

// Cuộc thi đã kết thúc
const isEnded = computed(() => currentContestStatus.value === 'CLOSED')

// Cuộc thi sắp diễn ra (chưa đến giờ bắt đầu)
const isUpcoming = computed(() => currentContestStatus.value === 'UPCOMING')

// Danh sách bài tập hiển thị:
// Nếu cuộc thi chưa bắt đầu (UPCOMING), che giấu toàn bộ thông tin chi tiết (tiêu đề, mô tả, tag, điểm, nút giải)
// và render các thẻ dưới dạng bí ẩn "?" không có tooltip, chỉ giữ lại đúng số lượng bài tập
const displayedContestPuzzles = computed(() => {
  if (isUpcoming.value) {
    return contestPuzzles.value.map((puzzle, index) => ({
      id: puzzle.id ?? index,
      locked: true,
      title: '?',
      content: '',
      difficulty: '',
      languages: [],
      tags: [],
      authorName: '',
      createdLabel: '',
      scoreBadge: '',
      scoreTone: '',
      solveLabel: '',
      solved: false,
    }))
  }
  return contestPuzzles.value
})

// Kiểm tra xem người dùng hiện tại đã đăng ký cuộc thi hay chưa
const isJoined = computed(() => {
  if (!currentUserId.value) return false
  return contestants.value.some((c) => c.userId === currentUserId.value)
})

// Thông tin thứ hạng cá nhân của người dùng
const myStanding = computed(() => {
  const currentName = authStore.profile?.name || 'You'
  if (!currentUserId.value) {
    return { rank: '-', points: 0, handle: currentName, isJoined: false }
  }
  const idx = contestants.value.findIndex((c) => c.userId === currentUserId.value)
  if (idx === -1) {
    return { rank: '-', points: 0, handle: currentName, isJoined: false }
  }
  const c = contestants.value[idx]
  return {
    rank: idx + 1,
    points: c.points,
    handle: c.userName || currentName,
    isJoined: true,
  }
})

// Tính tổng điểm tối đa thực tế từ danh sách bài toán của cuộc thi
const maxScoreLabel = computed(() => {
  if (contestPuzzles.value.length > 0) {
    const total = contestPuzzles.value.reduce((sum, p) => sum + (Number(p.points ?? p.score) || 0), 0)
    return `${total.toLocaleString()} Max Score`
  }
  return contest.value?.maxScore || '0 Max Score'
})

// Tải thông tin chi tiết cuộc thi, bài tập và bảng xếp hạng từ backend
async function loadContestDetail(id) {
  if (!id) return
  loadStatus.value = 'loading'
  errorMessage.value = ''
  try {
    const [contestData, puzzlesData, contestantsData] = await Promise.all([
      fetchContest(id),
      fetchContestPuzzles(id).catch((err) => {
        console.warn('Failed to load contest puzzles:', err)
        return []
      }),
      fetchContestants(id).catch((err) => {
        console.warn('Failed to load contestants:', err)
        return []
      }),
    ])

    contest.value = toContestDetail(contestData)
    contestPuzzles.value = Array.isArray(puzzlesData) ? puzzlesData.map(toContestPuzzleCard) : []
    contestants.value = Array.isArray(contestantsData) ? contestantsData : []
    loadStatus.value = 'ready'
  } catch (err) {
    contest.value = null
    contestPuzzles.value = []
    contestants.value = []
    loadStatus.value = err?.response?.status === 404 ? 'missing' : 'failed'
    errorMessage.value = describeApiError(err).message || 'Failed to load contest details.'
  }
}

onMounted(() => {
  authStore.initialize()
  loadContestDetail(route.params.id)
  countdownInterval = setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onBeforeUnmount(() => {
  if (countdownInterval) clearInterval(countdownInterval)
  if (copyTimer) clearTimeout(copyTimer)
})

watch(
  () => route.params.id,
  (newId) => {
    if (newId) loadContestDetail(newId)
  }
)

// Ánh xạ trạng thái cuộc thi sang màu badge của DevClimb
const resolvedStatusTone = computed(() => {
  const norm = currentContestStatus.value
  if (norm === 'OPEN') return 'easy'
  if (norm === 'UPCOMING') return 'medium'
  if (norm === 'CLOSED') return 'hard'
  return 'easy'
})

// Sao chép liên kết cuộc thi vào bộ nhớ tạm
async function copyContestLink() {
  try {
    await navigator.clipboard.writeText(window.location.href)
    copied.value = true
    if (copyTimer) clearTimeout(copyTimer)
    copyTimer = setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch {
    copied.value = false
  }
}

// Xử lý tham gia cuộc thi
async function handleJoin() {
  if (isJoined.value || isEnded.value || joining.value || !contest.value?.id) return
  if (!isAuthenticated.value) {
    router.push('/login')
    return
  }

  joinError.value = ''
  joining.value = true
  try {
    await joinContest(contest.value.id)
    const data = await fetchContestants(contest.value.id)
    contestants.value = Array.isArray(data) ? data : []
    if (contest.value) {
      contest.value.registeredCount = `${contestants.value.length.toLocaleString()} devs`
    }
  } catch (err) {
    console.error('Failed to join contest:', err)
    joinError.value = describeApiError(err).message || 'Failed to join contest.'
  } finally {
    joining.value = false
  }
}

// Lọc danh sách thí sinh trên bảng xếp hạng theo username hoặc thứ hạng
const filteredLeaderboard = computed(() => {
  const q = leaderboardQuery.value.trim().toLowerCase()
  const list = contestants.value.map((c, index) => ({
    ...c,
    rank: index + 1,
    isUser: currentUserId.value != null && c.userId === currentUserId.value,
  }))
  if (!q) return list
  return list.filter((c) => {
    return (c.userName || '').toLowerCase().includes(q) || String(c.rank).includes(q)
  })
})
</script>

<template>
  <div class="page">
    <UserNavbar />

    <main class="contest-shell">
      <!-- Trạng thái đang tải dữ liệu cuộc thi -->
      <div v-if="loadStatus === 'loading'" class="contest-state-panel" role="status" aria-live="polite">
        <div class="state-icon-box">
          <svg class="state-ic spin" viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2" fill="none" stroke-dasharray="40 20" />
          </svg>
        </div>
        <h2 class="state-title">Loading contest details...</h2>
        <p class="state-desc">Fetching contest rules, leaderboard standings, and puzzles.</p>
      </div>

      <!-- Trạng thái không tìm thấy hoặc lỗi tải dữ liệu -->
      <div v-else-if="loadStatus !== 'ready'" class="contest-state-panel" role="alert">
        <div class="state-icon-box" :class="{ missing: loadStatus === 'missing', failed: loadStatus === 'failed' }">
          <svg v-if="loadStatus === 'missing'" class="state-ic" viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="12" cy="12" r="9" />
            <path d="M12 8v4M12 16h.01" />
          </svg>
          <svg v-else class="state-ic" viewBox="0 0 24 24" aria-hidden="true">
            <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z" />
            <path d="M12 9v4M12 17h.01" />
          </svg>
        </div>

        <h1 class="state-title">
          {{ loadStatus === 'missing' ? 'Contest not found' : 'Could not load this contest' }}
        </h1>

        <p class="state-desc">
          {{ loadStatus === 'missing'
            ? 'This contest may not exist, has expired, or is currently restricted.'
            : errorMessage }}
        </p>

        <div class="state-actions">
          <button
            v-if="loadStatus === 'failed'"
            class="btn p"
            type="button"
            @click="loadContestDetail(route.params.id)"
          >
            <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M3 12a9 9 0 0 1 15-6.7L21 8" />
              <path d="M21 3v5h-5" />
              <path d="M21 12a9 9 0 0 1-15 6.7L3 16" />
              <path d="M3 21v-5h5" />
            </svg>
            <span>Try Again</span>
          </button>

          <RouterLink class="btn" to="/puzzles">
            <span>Back to Contests & Puzzles</span>
          </RouterLink>
        </div>
      </div>

      <template v-else-if="contest">
        <!-- Thẻ Hero Banner Cuộc thi: Banner lấp đầy từ bên dưới/phía sau tiêu đề, viền hairline chuẩn DevClimb -->
        <section class="contest-hero-card" :class="{ 'has-image': Boolean(contest.image) }">
        <div class="hero-body">
          <img v-if="contest.image" :src="contest.image" :alt="contest.title" class="hero-bg-img" />
          <div v-if="contest.image" class="hero-bg-scrim" aria-hidden="true"></div>

          <div class="hero-content">
            <!-- Hàng đầu: Avatar bên trái; Huy hiệu và Bộ đếm thời gian bên phải cùng chiều cao -->
            <div class="hero-top-row">
              <Avatar :src="contest.community.src" :name="contest.community.name" :type="contest.community.type" />

              <div class="hero-status-group">
                <span class="badge" :class="resolvedStatusTone">{{ currentContestStatus }}</span>
                <div v-if="countdownDisplay" class="timer-chip">
                  <svg class="ic timer-ic" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M10 2h4M12 14l3-3" />
                    <circle cx="12" cy="14" r="8" />
                  </svg>
                  <span class="timer-text">{{ countdownDisplay }}</span>
                </div>
              </div>
            </div>

            <!-- Tiêu đề cuộc thi nổi bật ở phần thân banner -->
            <h1 class="hero-title">{{ contest.title }}</h1>
          </div>
        </div>

        <!-- Chân thẻ: Vạch kẻ hairline, metadata cuộc thi, các nút thao tác và mô tả chi tiết -->
        <div class="hero-foot">
          <div class="hero-foot-row">
            <div class="meta-line">
              <span>Registered: <strong class="meta-strong">{{ contest.registeredCount }}</strong></span>
              <span class="dot">•</span>
              <span>{{ contest.puzzleCount }} Puzzles</span>
              <span class="dot">•</span>
              <span>{{ maxScoreLabel }}</span>
            </div>

            <div class="hero-actions">
              <button
                v-if="isJoined"
                class="btn joined"
                type="button"
                disabled
              >
                <span>Joined</span>
              </button>
              <button
                v-else-if="isEnded"
                class="btn joined"
                type="button"
                disabled
                title="This contest has ended"
              >
                <span>Contest Ended</span>
              </button>
              <button
                v-else
                class="btn p"
                type="button"
                :disabled="joining"
                @click="handleJoin"
              >
                <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
                  <polygon points="6 3 20 12 6 21 6 3" />
                </svg>
                <span>{{ joining ? 'Joining...' : 'Join Contest' }}</span>
              </button>

              <button class="btn" type="button" @click="copyContestLink">
                <svg v-if="copied" class="ic ok" viewBox="0 0 24 24" aria-hidden="true">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
                <svg v-else class="ic" viewBox="0 0 24 24" aria-hidden="true">
                  <rect width="14" height="14" x="8" y="8" rx="2" />
                  <path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2" />
                </svg>
              </button>
            </div>
          </div>

          <p v-if="joinError" class="join-error-msg">{{ joinError }}</p>
          <p v-if="contest.description" class="contest-description">{{ contest.description }}</p>
        </div>
      </section>

      <!-- Bố cục 2 cột: Cột chính hiển thị PuzzleCatalog và Cột phụ sticky bảng xếp hạng -->
      <div class="contest-grid">
        <!-- Cột trái: Danh mục bài tập cuộc thi sử dụng PuzzleCatalog -->
        <div class="catalog-column">
          <div class="column-header">
            <h2 class="column-title">Contest Puzzles</h2>
            <span class="catalog-count">{{ contestPuzzles.length }} Puzzles</span>
          </div>
          <PuzzleCatalog :items="displayedContestPuzzles" :columns="2" />
        </div>

        <!-- Cột phải: Khung xếp hạng cố định khi cuộn (Sticky Ranking Rail) -->
        <aside class="ranking-rail" aria-label="Contest Ranking and Standings">
          <!-- Thẻ thứ hạng cá nhân của người dùng -->
          <section class="standing-card">
            <div class="standing-card-head">
              <span class="standing-label">My Standing</span>
            </div>

            <div class="standing-stats">
              <div>
                <div class="standing-rank">{{ myStanding.isJoined ? `Rank #${myStanding.rank}` : 'Unranked' }}</div>
                <div class="standing-handle">{{ myStanding.handle }}</div>
              </div>
              <div class="standing-score-box">
                <div class="standing-score">{{ myStanding.points }} pts</div>
              </div>
            </div>
          </section>

          <!-- Thẻ Bảng xếp hạng trực tiếp với bộ lọc tìm kiếm -->
          <section class="leaderboard-card">
            <div class="leaderboard-head">
              <div class="leaderboard-title-row">
                <svg class="ic accent" viewBox="0 0 24 24" aria-hidden="true">
                  <circle cx="9" cy="8" r="3" />
                  <path d="M3 20a6 6 0 0 1 12 0M16 4.5a3 3 0 0 1 0 6M21 20a6 6 0 0 0-4-5.6" />
                </svg>
                <h2 class="leaderboard-title">Leaderboard</h2>
              </div>
              <span class="live-pill">{{ contestants.length }} LIVE</span>
            </div>

            <!-- Ô lọc nhanh theo handle hoặc thứ hạng -->
            <div class="filter-box">
              <svg class="ic filter-ic" viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="11" cy="11" r="7" />
                <path d="m20 20-3.5-3.5" />
              </svg>
              <input
                v-model="leaderboardQuery"
                class="filter-input"
                type="text"
                placeholder="Filter username or rank..."
                aria-label="Filter leaderboard"
              />
              <button
                v-if="leaderboardQuery"
                class="filter-clear"
                type="button"
                aria-label="Clear filter"
                @click="leaderboardQuery = ''"
              >
                ×
              </button>
            </div>

            <!-- Danh sách hàng xếp hạng dạng hairline divided -->
            <div class="leaderboard-list">
              <div
                v-for="contender in filteredLeaderboard"
                :key="contender.userId || contender.rank"
                class="contender-row"
                :class="{ 'is-user': contender.isUser }"
              >
                <div class="contender-left">
                  <span class="rank-num">#{{ contender.rank }}</span>
                  <Avatar
                    :src="contender.userImage || ''"
                    :name="contender.userName"
                    type="user"
                    :round="true"
                    :size="24"
                    to="#"
                  />
                </div>
                <div class="contender-right">
                  <span class="score-val">{{ contender.points }} pts</span>
                </div>
              </div>

              <div v-if="!filteredLeaderboard.length" class="empty-leaderboard">
                {{ leaderboardQuery ? `No contenders match "${leaderboardQuery}"` : 'No contestants registered yet' }}
              </div>
            </div>
          </section>
        </aside>
      </div>
      </template>
    </main>

    <SiteFooter />
  </div>
</template>

<style scoped>
.contest-state-panel {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: clamp(380px, 60vh, 640px);
  padding: var(--s-12) var(--s-6);
  text-align: center;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-lg);
  animation: stateFade 0.2s ease-out;
}

@keyframes stateFade {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.state-icon-box {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--s2);
  border: 1px solid var(--line2);
  margin-bottom: var(--s-4);
  color: var(--tx-muted);
}

.state-icon-box.missing {
  color: var(--medium, #f59e0b);
  border-color: rgba(245, 158, 11, 0.25);
  background: rgba(245, 158, 11, 0.08);
}

.state-icon-box.failed {
  color: var(--bad, #f87171);
  border-color: rgba(248, 113, 113, 0.25);
  background: rgba(248, 113, 113, 0.08);
}

.state-ic {
  width: 22px;
  height: 22px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.state-ic.spin {
  animation: stateSpin 1s linear infinite;
}

@keyframes stateSpin {
  to {
    transform: rotate(360deg);
  }
}

.state-title {
  font: 500 22px/1.2 var(--f-head);
  letter-spacing: -0.02em;
  color: var(--tx);
  margin: 0 0 var(--s-2);
}

.state-desc {
  font: 400 14px/1.6 var(--f-mono);
  color: var(--tx2);
  max-width: 440px;
  margin: 0 0 var(--s-6);
}

.state-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s-3);
  justify-content: center;
  align-items: center;
}

.page {
  display: flex;
  flex-direction: column;
  min-height: 100svh;
  background: var(--bg);
}

.contest-shell {
  display: flex;
  flex-direction: column;
  flex: 1;
  width: 100%;
  max-width: var(--rail);
  margin: 0 auto;
  padding: var(--s-5) var(--s-6) var(--s-8);
  border-inline: 1px solid var(--line);
}

/* Thẻ Hero Banner Cuộc thi */
.contest-hero-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-lg);
  overflow: hidden;
  transition: border-color 0.15s var(--ease);
}

.hero-body {
  position: relative;
  min-height: 260px;
  width: 100%;
  display: flex;
  flex-direction: column;
  background: var(--s1);
}

.hero-bg-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.hero-bg-scrim {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(13, 13, 12, 0.45) 0%, rgba(13, 13, 12, 0.88) 100%);
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex: 1;
  padding: var(--s-6);
  gap: var(--s-4);
}

.hero-top-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
}

.hero-status-group {
  display: flex;
  align-items: center;
  gap: 6px;
}

.hero-title {
  font: 600 32px/1.2 var(--f-head);
  letter-spacing: -0.025em;
  color: #ffffff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.65);
  margin: 0;
}

.contest-hero-card:not(.has-image) .hero-title {
  color: var(--tx);
  text-shadow: none;
}

.hero-foot {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: var(--s-3);
  padding: var(--s-4) var(--s-5);
  border-top: 1px solid var(--line);
  background: var(--s1);
  font-size: 12px;
}

.hero-foot-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--s-2);
}

.btn.joined {
  background: var(--s3);
  color: var(--tx3);
  cursor: default;
  box-shadow: none;
  opacity: 0.85;
}

.btn.joined:hover {
  background: var(--s3);
  filter: none;
}

.contest-description {
  margin: 0;
  font: 400 var(--t-sm)/1.6 var(--f-head);
  color: var(--tx2);
}

.join-error-msg {
  margin: 0;
  font: 500 12px var(--f-head);
  color: var(--warn);
}

.meta-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--s-2);
  font: 400 12px/1.5 var(--f-head);
  color: var(--tx2);
}

.meta-strong {
  font-weight: 500;
  color: var(--tx);
}

.dot {
  color: var(--tx3);
}

/* Huy hiệu và Bộ đếm thời gian */
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 22px;
  padding: 0 8px;
  border: 1px solid transparent;
  border-radius: 999px;
  font: 500 11px/1 var(--f-head);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  white-space: nowrap;
  box-sizing: border-box;
}

.badge.easy {
  color: var(--ok);
  background: color-mix(in srgb, var(--ok) 12%, transparent);
  border-color: color-mix(in srgb, var(--ok) 25%, transparent);
}

.badge.medium {
  color: var(--ac);
  background: var(--ac-soft);
  border-color: color-mix(in srgb, var(--ac) 25%, transparent);
}

.badge.hard {
  color: var(--warn);
  background: color-mix(in srgb, var(--warn) 12%, transparent);
  border-color: color-mix(in srgb, var(--warn) 25%, transparent);
}

.timer-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 22px;
  padding: 0 8px;
  background: var(--s2);
  border: 1px solid var(--line);
  border-radius: 999px;
  font: 500 11px/1 var(--f-mono);
  font-variant-numeric: tabular-nums;
  color: var(--tx);
  white-space: nowrap;
  box-sizing: border-box;
}

.timer-ic {
  width: 13px;
  height: 13px;
  color: var(--tx2);
}

.contest-hero-card.has-image .timer-chip {
  background: rgba(28, 28, 26, 0.75);
  border-color: rgba(255, 255, 255, 0.16);
  color: #ecebe6;
  backdrop-filter: blur(8px);
}

.contest-hero-card.has-image .timer-ic {
  color: #c8c6c3;
}

.timer-text {
  line-height: 1;
}

/* Bố cục 2 cột */
.contest-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: var(--s-5);
  align-items: start;
  margin-top: var(--s-5);
}

.catalog-column {
  display: flex;
  flex-direction: column;
  gap: var(--s-3);
}

.column-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: var(--s-2);
}

.column-title {
  font: 500 18px var(--f-head);
  letter-spacing: -0.02em;
  color: var(--tx);
  margin: 0;
}

.catalog-count {
  font: 500 12px var(--f-mono);
  color: var(--tx2);
}

/* Cột phải: Sticky Ranking Rail */
.ranking-rail {
  position: sticky;
  top: 88px;
  display: flex;
  flex-direction: column;
  gap: var(--s-4);
}

.standing-card,
.leaderboard-card {
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
  padding: var(--s-4);
}

.standing-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: var(--s-3);
  margin-bottom: var(--s-3);
  border-bottom: 1px solid var(--line);
}

.standing-label {
  font: 500 11px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--tx2);
}

.standing-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.standing-rank {
  font: 500 22px var(--f-head);
  letter-spacing: -0.02em;
  color: var(--tx);
}

.standing-handle {
  font: 500 12px var(--f-mono);
  color: var(--ac);
  margin-top: 2px;
}

.standing-score-box {
  text-align: right;
}

.standing-score {
  font: 500 22px var(--f-head);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
  color: var(--tx);
}

.standing-penalty {
  font: 400 11px var(--f-mono);
  color: var(--tx2);
  margin-top: 2px;
}

/* Leaderboard Card */
.leaderboard-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--s-3);
}

.leaderboard-title-row {
  display: flex;
  align-items: center;
  gap: var(--s-2);
}

.leaderboard-title {
  font: 500 14px var(--f-head);
  color: var(--tx);
  margin: 0;
}

.live-pill {
  font: 500 11px var(--f-mono);
  color: var(--tx2);
}

.filter-box {
  position: relative;
  display: flex;
  align-items: center;
  margin-bottom: var(--s-3);
}

.filter-ic {
  position: absolute;
  left: 10px;
  width: 14px;
  height: 14px;
  color: var(--tx2);
  pointer-events: none;
}

.filter-input {
  width: 100%;
  height: 34px;
  padding: 0 28px 0 32px;
  background: var(--s2);
  border: 1px solid var(--line);
  border-radius: var(--r-sm);
  outline: none;
  font: 400 12px var(--f-head);
  color: var(--tx);
  transition: border-color 0.15s var(--ease), box-shadow 0.15s var(--ease);
}

.filter-input:focus {
  border-color: var(--ac);
  box-shadow: 0 0 0 3px var(--ac-soft);
}

.filter-clear {
  position: absolute;
  right: 8px;
  font-size: 16px;
  color: var(--tx2);
  cursor: pointer;
  background: none;
  border: none;
  padding: 2px 6px;
}

.filter-clear:hover {
  color: var(--tx);
}

/* Danh sách Leaderboard */
.leaderboard-list {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--line);
  border-radius: var(--r-sm);
  overflow: hidden;
  background: var(--s1);
}

.contender-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  font: 500 12px var(--f-mono);
  transition: background 0.15s var(--ease);
}

.contender-row + .contender-row {
  border-top: 1px solid var(--line);
}

.contender-row:hover {
  background: var(--s2);
}

.contender-row.is-user {
  background: var(--ac-soft);
  color: var(--tx);
}

.contender-left {
  display: flex;
  align-items: center;
  gap: var(--s-2);
}

.rank-num {
  width: 26px;
  color: var(--tx);
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

.handle-name {
  color: var(--tx);
  font-weight: 500;
}

.tag-pill {
  font-size: 10px;
  color: var(--tx2);
}

.tag-pill.you-tag {
  padding: 1px 4px;
  background: var(--s1);
  border: 1px solid color-mix(in srgb, var(--ac) 40%, transparent);
  border-radius: 4px;
  color: var(--ac);
  font-weight: 500;
}

.contender-right {
  text-align: right;
}

.score-val {
  display: block;
  font-weight: 500;
  color: var(--tx);
  font-variant-numeric: tabular-nums;
}

.solved-val {
  display: block;
  font-size: 10px;
  color: var(--ok);
  line-height: 1;
}

.empty-leaderboard {
  padding: var(--s-4);
  text-align: center;
  font: 400 12px var(--f-head);
  color: var(--tx2);
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

.ic.ok {
  stroke: var(--ok);
}

.accent {
  color: var(--ac);
}

@media (max-width: 900px) {
  .contest-grid {
    grid-template-columns: 1fr;
  }

  .ranking-rail {
    position: static;
  }
}

@media (max-width: 600px) {
  .hero-title {
    font-size: 22px;
  }

  .contest-shell {
    padding: var(--s-4);
  }
}
</style>
