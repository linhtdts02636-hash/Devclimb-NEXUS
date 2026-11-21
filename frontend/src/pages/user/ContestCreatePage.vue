<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import Avatar from '../../components/common/Avatar.vue'
import PuzzleCatalog from '../../components/common/PuzzleCatalog.vue'
import SiteFooter from '../../components/layouts/SiteFooter.vue'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { describeApiError } from '../../config/apiClient'
import { fetchCommunity } from '../../features/community/communityApi'
import {
  createContest,
  fetchContest,
  fetchContestPuzzles,
  updateContest,
} from '../../features/contest/contestApi'
import { toContestPuzzleCard } from '../../features/contest/contestMapper'
import { useAuthStore } from '../../stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// Xác định chế độ tạo mới hay chỉnh sửa dựa vào tham số contestId trên URL
const contestId = computed(() => {
  const raw = Number(route.params.contestId)
  return Number.isInteger(raw) && raw > 0 ? raw : null
})
const isEditMode = computed(() => contestId.value !== null)

const community = ref(null)
const communityName = computed(() => community.value?.name || route.params.name || 'Community')

// Đường dẫn sang trang tạo bài toán thuộc cuộc thi hiện tại
const createPuzzleRoute = computed(() => {
  if (!contestId.value || !route.params.name) return '/puzzles/new'
  return {
    name: 'puzzle-create',
    params: { name: route.params.name },
    query: { contestId: contestId.value },
  }
})

// Dữ liệu biểu mẫu cuộc thi
const form = reactive({
  title: '',
  description: '',
  startDate: '',
  endDate: '',
  bannerUrl: '',
})

const bannerInputRef = ref(null)
const contestPuzzles = ref([])
const puzzlesLoading = ref(false)
const submitting = ref(false)
const submitError = ref('')
const submitSuccess = ref('')

// Chuyển đổi ISO timestamp sang chuỗi YYYY-MM-DDTHH:mm theo giờ địa phương cho input datetime-local
function toLocalDateTimeInput(isoString) {
  if (!isoString) return ''
  const date = new Date(isoString)
  if (Number.isNaN(date.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

// Tính tổng điểm tối đa từ danh sách bài toán của cuộc thi
const maxScoreLabel = computed(() => {
  const total = contestPuzzles.value.reduce((sum, p) => sum + (Number(p.points ?? p.score) || 0), 0)
  return `${total.toLocaleString()} pts max`
})

// Mở hộp thoại chọn ảnh bìa (chỉ xem trước cục bộ, chưa nối upload backend)
function triggerBannerUpload() {
  bannerInputRef.value?.click()
}

function handleBannerChange(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (form.bannerUrl && form.bannerUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.bannerUrl)
  }
  form.bannerUrl = URL.createObjectURL(file)
}

// Tải thông tin cộng đồng và dữ liệu cuộc thi (nếu đang ở chế độ chỉnh sửa)
async function loadPageData() {
  submitError.value = ''
  submitSuccess.value = ''

  if (route.params.name) {
    try {
      community.value = await fetchCommunity(route.params.name)
    } catch (error) {
      submitError.value = describeApiError(error).message
    }
  }

  if (!contestId.value) {
    contestPuzzles.value = []
    return
  }

  puzzlesLoading.value = true
  try {
    const [contestData, puzzlesData] = await Promise.all([
      fetchContest(contestId.value),
      fetchContestPuzzles(contestId.value).catch(() => []),
    ])
    form.title = contestData.title || ''
    form.description = contestData.content || ''
    form.startDate = toLocalDateTimeInput(contestData.startTime)
    form.endDate = toLocalDateTimeInput(contestData.endTime)
    if (contestData.banner) {
      form.bannerUrl = contestData.banner
    }
    contestPuzzles.value = Array.isArray(puzzlesData) ? puzzlesData.map(toContestPuzzleCard) : []
  } catch (error) {
    submitError.value = describeApiError(error).message || 'Failed to load contest.'
  } finally {
    puzzlesLoading.value = false
  }
}

// Tạo mới hoặc cập nhật thông tin cuộc thi
async function handleSubmit() {
  if (submitting.value) return
  submitError.value = ''
  submitSuccess.value = ''

  const title = form.title.trim()
  const content = form.description.trim()
  if (!title) {
    submitError.value = 'Please enter a contest title.'
    return
  }
  if (!content) {
    submitError.value = 'Please enter a contest description.'
    return
  }
  if (!form.startDate || !form.endDate) {
    submitError.value = 'Please select both Start and End dates.'
    return
  }

  const startMs = new Date(form.startDate).getTime()
  const endMs = new Date(form.endDate).getTime()
  if (!Number.isFinite(startMs) || !Number.isFinite(endMs) || endMs <= startMs) {
    submitError.value = 'End date must be after Start date.'
    return
  }

  // Không gửi blob URL cục bộ lên backend khi chưa nối upload
  const bannerPayload =
    form.bannerUrl && !form.bannerUrl.startsWith('blob:') ? form.bannerUrl : null

  submitting.value = true
  try {
    if (isEditMode.value) {
      await updateContest(contestId.value, {
        title,
        content,
        banner: bannerPayload,
        startTime: new Date(startMs).toISOString(),
        endTime: new Date(endMs).toISOString(),
      })
      submitSuccess.value = 'Changes applied.'
    } else {
      if (!community.value?.id) {
        submitError.value = 'Community could not be resolved.'
        return
      }
      const created = await createContest({
        title,
        content,
        banner: bannerPayload,
        startTime: new Date(startMs).toISOString(),
        endTime: new Date(endMs).toISOString(),
        communityId: community.value.id,
      })
      router.replace({
        name: 'contest-edit',
        params: { name: route.params.name || community.value.name, contestId: created.id },
      })
    }
  } catch (error) {
    submitError.value = describeApiError(error).message || 'Failed to save contest.'
  } finally {
    submitting.value = false
  }
}

watch([() => route.params.name, () => route.params.contestId], () => {
  loadPageData()
})

onMounted(() => {
  authStore.initialize()
  loadPageData()
})

onBeforeUnmount(() => {
  if (form.bannerUrl && form.bannerUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.bannerUrl)
  }
})
</script>

<template>
  <div class="page">
    <UserNavbar />

    <main class="contest-shell">
      <!-- Thẻ Hero Banner Cuộc thi -->
      <section class="contest-hero-card" :class="{ 'has-image': Boolean(form.bannerUrl) }">
        <div class="hero-body">
          <img v-if="form.bannerUrl" :src="form.bannerUrl" alt="Contest banner preview" class="hero-bg-img" />
          <div v-if="form.bannerUrl" class="hero-bg-scrim" aria-hidden="true"></div>

          <input
            ref="bannerInputRef"
            type="file"
            accept="image/*"
            hidden
            @change="handleBannerChange"
          />

          <div class="hero-content">
            <!-- Hàng đầu: Avatar cộng đồng bên trái; 2 ô chọn ngày Start và End date bên phải -->
            <div class="hero-top-row">
              <Avatar :src="community?.imageUrl || ''" :name="communityName" type="community" />

              <div class="hero-date-group">
                <label class="date-field">
                  <span class="date-label">Start</span>
                  <input
                    v-model="form.startDate"
                    type="datetime-local"
                    class="date-input"
                    aria-label="Start date"
                  />
                </label>

                <label class="date-field">
                  <span class="date-label">End date</span>
                  <input
                    v-model="form.endDate"
                    type="datetime-local"
                    class="date-input"
                    aria-label="End date"
                  />
                </label>
              </div>
            </div>

            <!-- Hàng tiêu đề và nút tải ảnh bìa -->
            <div class="hero-title-row">
              <input
                v-model="form.title"
                type="text"
                class="hero-title-input"
                maxlength="250"
                autocomplete="off"
                placeholder="Contest title"
                aria-label="Contest title"
              />

              <button
                type="button"
                class="banner-upload-btn"
                aria-label="Upload contest banner"
                @click="triggerBannerUpload"
              >
                <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="17 8 12 3 7 8" />
                  <line x1="12" y1="3" x2="12" y2="15" />
                </svg>
                <span>{{ form.bannerUrl ? 'Change banner' : 'Upload banner' }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Chân thẻ: Metadata cuộc thi, nút Create/Apply và ô nhập mô tả -->
        <div class="hero-foot">
          <div class="hero-foot-row">
            <div class="meta-line">
              <span>{{ contestPuzzles.length }} Puzzles</span>
              <span class="dot">•</span>
              <span>{{ maxScoreLabel }}</span>
            </div>

            <div class="hero-actions">
              <span v-if="submitSuccess" class="submit-success" role="status">{{ submitSuccess }}</span>
              <button class="btn p" type="button" :disabled="submitting" @click="handleSubmit">
                <span>
                  {{ submitting
                    ? (isEditMode ? 'Applying...' : 'Creating...')
                    : (isEditMode ? 'Apply' : 'Create') }}
                </span>
              </button>
            </div>
          </div>

          <p v-if="submitError" class="submit-error" role="alert">{{ submitError }}</p>

          <textarea
            v-model="form.description"
            class="contest-description-input"
            rows="2"
            placeholder="Describe the contest rules, schedule, and scoring..."
            aria-label="Contest description"
          ></textarea>
        </div>
      </section>

      <!-- Chỉ hiển thị PuzzleCatalog sau khi cuộc thi đã được tạo (chế độ chỉnh sửa) -->
      <div v-if="isEditMode" class="contest-grid">
        <div class="catalog-column">
          <div class="column-header">
            <div class="column-heading">
              <h2 class="column-title">Contest Puzzles</h2>
              <span class="catalog-count">{{ contestPuzzles.length }} Puzzles</span>
            </div>

            <RouterLink class="btn p" :to="createPuzzleRoute">
              <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M12 5v14M5 12h14" />
              </svg>
              <span>Create puzzle</span>
            </RouterLink>
          </div>

          <PuzzleCatalog :items="contestPuzzles" :columns="3" :loading="puzzlesLoading" />
        </div>
      </div>
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

/* Nhóm 2 ô chọn ngày Start và End date */
.hero-date-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--s-2);
}

.date-field {
  display: inline-flex;
  align-items: center;
  gap: var(--s-2);
  height: 32px;
  padding: 0 10px;
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  transition: border-color 0.15s var(--ease), box-shadow 0.15s var(--ease);
}

.date-field:focus-within {
  border-color: var(--ac);
  box-shadow: 0 0 0 3px var(--ac-soft);
}

.date-label {
  font: 500 11px/1 var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--tx2);
  white-space: nowrap;
}

.date-input {
  background: transparent;
  border: 0;
  outline: none;
  padding: 0;
  font: 400 12px/1 var(--f-mono);
  font-variant-numeric: tabular-nums;
  color: var(--tx);
}

/* Hàng tiêu đề không nền không viền và nút upload ảnh */
.hero-title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--s-4);
}

.hero-title-input {
  flex: 1 1 300px;
  min-width: 0;
  padding: 0;
  background: transparent;
  border: 0;
  border-radius: 0;
  outline: none;
  font: 600 32px/1.2 var(--f-head);
  letter-spacing: -0.025em;
  color: var(--tx);
}

.hero-title-input::placeholder {
  color: var(--tx3);
}

.contest-hero-card.has-image .hero-title-input {
  color: #ffffff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.65);
}

.contest-hero-card.has-image .hero-title-input::placeholder {
  color: rgba(255, 255, 255, 0.55);
}

.banner-upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 12px;
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  color: var(--tx);
  font: 500 12px/1 var(--f-head);
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s var(--ease), transform 0.12s var(--ease);
}

.banner-upload-btn:hover {
  background: var(--s3);
}

.banner-upload-btn:active {
  transform: translateY(1px);
}

/* Chân thẻ Hero */
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
  gap: var(--s-3);
}

.submit-error {
  margin: 0;
  font: 500 12px var(--f-head);
  color: var(--bad);
}

.submit-success {
  font: 500 12px var(--f-head);
  color: var(--ok);
}

/* Ô nhập mô tả không nền không viền */
.contest-description-input {
  width: 100%;
  padding: 0;
  background: transparent;
  border: 0;
  border-radius: 0;
  outline: none;
  resize: vertical;
  min-height: 44px;
  font: 400 var(--t-sm)/1.6 var(--f-head);
  color: var(--tx2);
}

.contest-description-input::placeholder {
  color: var(--tx3);
}

.meta-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--s-2);
  font: 400 12px/1.5 var(--f-head);
  color: var(--tx2);
}

.dot {
  color: var(--tx3);
}

/* Nút bấm chuẩn DESIGN.md */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 32px;
  padding: 0 var(--s-4);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  color: var(--tx);
  font: 500 13px/1 var(--f-head);
  text-decoration: none;
  cursor: pointer;
  transition: background 0.15s var(--ease), filter 0.15s var(--ease), transform 0.12s var(--ease);
}

.btn:hover:not(:disabled) {
  background: var(--s3);
}

.btn:active:not(:disabled) {
  transform: translateY(1px);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn.p {
  background: var(--ac);
  border-color: transparent;
  color: var(--ac-ink);
}

.btn.p:hover:not(:disabled) {
  background: var(--ac);
  filter: brightness(1.08);
}

/* Bố cục 1 cột toàn chiều rộng cho PuzzleCatalog 3 cột */
.contest-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--s-6);
  margin-top: var(--s-6);
}

.catalog-column {
  display: flex;
  flex-direction: column;
  gap: var(--s-4);
  min-width: 0;
}

.column-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
}

.column-heading {
  display: flex;
  align-items: center;
  gap: var(--s-3);
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

/* Hỗ trợ lưới 3 cột cho PuzzleCatalog mà không cần sửa PuzzleCatalog.vue */
:deep(.puzzle-grid.cols-3) {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

@media (max-width: 1080px) {
  :deep(.puzzle-grid.cols-3) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  :deep(.puzzle-grid.cols-3) {
    grid-template-columns: 1fr;
  }
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

@media (max-width: 600px) {
  .hero-title-input {
    font-size: 22px;
  }

  .contest-shell {
    padding: var(--s-4);
  }
}
</style>
