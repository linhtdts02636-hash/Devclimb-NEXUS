<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import Avatar from '../../../components/common/Avatar.vue'

const props = defineProps({
  id: { type: [Number, String], default: 1 },
  title: { type: String, default: 'Weekly Contest' },
  status: { type: String, default: 'Open' },
  statusTone: { type: String, default: '' },
  countdownLabel: { type: String, default: 'Starts In' },
  countdown: { type: String, default: '' },
  community: {
    type: Object,
    default: () => ({ src: '/favicon.svg', name: 'fpt-algo-club', type: 'community' }),
  },
  registeredLabel: { type: String, default: 'Registered:' },
  registeredCount: { type: String, default: '' },
  puzzleCount: { type: [Number, String], default: 0 },
  actionLabel: { type: String, default: '' },
  // Đường dẫn hoặc data URL ảnh bìa cho cuộc thi
  image: { type: String, default: '' },
})

// Ánh xạ trạng thái cuộc thi (OPEN, UPCOMING, CLOSED) sang bảng màu badge chuẩn của DevClimb
const resolvedStatusTone = computed(() => {
  if (props.statusTone) {
    const tone = props.statusTone.toLowerCase()
    if (['easy', 'medium', 'hard'].includes(tone)) return tone
  }
  const norm = (props.status || '').toUpperCase()
  if (norm === 'OPEN') return 'easy'
  if (norm === 'UPCOMING') return 'medium'
  if (norm === 'CLOSED') return 'hard'
  return 'easy'
})

// Nhãn nút mặc định dựa trên trạng thái cuộc thi
const resolvedActionLabel = computed(() => {
  if (props.actionLabel) return props.actionLabel
  const norm = (props.status || '').toUpperCase()
  if (norm === 'CLOSED') return 'View Results'
  return 'Register Now'
})
const router = useRouter()

// Điều hướng sang trang chi tiết cuộc thi khi bấm nút hành động
function handleAction() {
  router.push(`/contest/${props.id || 1}`)
}
</script>

<template>
  <article class="challenge-card" :class="{ 'has-image': Boolean(image) }">
    <!-- Khu vực phía trên chân thẻ: lấp đầy hoàn toàn từ đỉnh thẻ đến vạch kẻ card-foot -->
    <div class="card-hero">
      <!-- Ảnh bìa lấp đầy nền khi có prop image -->
      <img v-if="image" :src="image" :alt="title" class="card-cover-img" />
      <div v-if="image" class="card-cover-scrim" aria-hidden="true"></div>

      <!-- Nội dung dùng chung 1 cấu trúc layout duy nhất cho cả bản có ảnh và không có ảnh -->
      <div class="card-content">
        <div class="card-top-row">
          <Avatar :src="community.src" :name="community.name" :type="community.type" />
          
          <!-- Cụm trạng thái: Huy hiệu và Bộ đếm thời gian cùng chiều cao đặt ngay cạnh nhau -->
          <div class="card-status-group">
            <span class="badge" :class="resolvedStatusTone">{{ status }}</span>
            <div v-if="countdown" class="timer-chip">
              <svg class="ic timer-ic" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M10 2h4M12 14l3-3" />
                <circle cx="12" cy="14" r="8" />
              </svg>
              <span class="timer-text">{{ countdown }}</span>
            </div>
          </div>
        </div>

        <h2 class="card-title">{{ title }}</h2>
      </div>
    </div>

    <!-- Chân thẻ: Vạch kẻ hairline và thông tin số lượng đăng ký -->
    <div class="card-foot">
      <div class="meta-line">
        <span>{{ registeredLabel }} <strong class="meta-strong">{{ registeredCount }}</strong></span>
        <span class="dot">•</span>
        <span>{{ puzzleCount }} Puzzles</span>
      </div>
      <button class="register-btn" type="button" :disabled="status?.toUpperCase() === 'CLOSED' && !actionLabel" @click="handleAction">
        <span>{{ resolvedActionLabel }}</span>
        <svg class="ic accent" viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="9" cy="8" r="3" />
          <path d="M3 21a6 6 0 0 1 12 0M16 11l2 2 4-4" />
        </svg>
      </button>
    </div>
  </article>
</template>

<style scoped>
.challenge-card {
  position: relative;
  display: flex;
  flex: 0 0 420px;
  /* Tăng chiều cao thẻ lên theo yêu cầu của người dùng */
  min-height: 195px;
  flex-direction: column;
  justify-content: space-between;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
  overflow: hidden;
  transition: border-color 0.15s var(--ease);
}

.challenge-card:hover {
  border-color: var(--tx3);
}

/* Khu vực phía trên chân thẻ lấp đầy từ đỉnh thẻ đến vạch kẻ card-foot */
.card-hero {
  position: relative;
  flex: 1;
  min-height: 140px;
  width: 100%;
  display: flex;
  flex-direction: column;
  background: var(--s1);
}

/* Ảnh bìa lấp đầy hoàn toàn vùng hero */
.card-cover-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* Lớp phủ chuyển sắc nhẹ giúp chữ và avatar luôn sắc nét trên ảnh */
.card-cover-scrim {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(13, 13, 12, 0.45) 0%, rgba(13, 13, 12, 0.82) 100%);
  pointer-events: none;
}

/* Khối nội dung bố trí giống nhau cho cả 2 phiên bản */
.card-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex: 1;
  padding: var(--s-4);
  gap: var(--s-3);
}

.card-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-2);
}

.card-status-group {
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-title {
  font: 500 20px/1.25 var(--f-head);
  letter-spacing: -0.02em;
  color: var(--tx);
  margin: 0;
}

/* Phiên bản có ảnh tăng cường độ tương phản cho tiêu đề */
.challenge-card.has-image .card-title {
  color: #ffffff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.65);
}

/* Huy hiệu trạng thái và Bộ đếm thời gian cùng chiều cao 22px đặt cạnh nhau */
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

.timer-text {
  line-height: 1;
}

/* Khi có ảnh nền, timer chip và badge có hiệu ứng mờ kính nhẹ */
.challenge-card.has-image .timer-chip {
  background: rgba(28, 28, 26, 0.75);
  border-color: rgba(255, 255, 255, 0.16);
  color: #ecebe6;
  backdrop-filter: blur(8px);
}

.challenge-card.has-image .timer-ic {
  color: #c8c6c3;
}

.challenge-card.has-image .badge {
  backdrop-filter: blur(8px);
}

/* Chân thẻ cố định với đường kẻ hairline và thông tin số lượng đăng ký */
.card-foot {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
  padding: var(--s-3) var(--s-4);
  border-top: 1px solid var(--line);
  background: var(--s1);
}

.meta-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--s-2);
  font: 400 11px/1.5 var(--f-head);
  color: var(--tx2);
}

.meta-strong {
  font-weight: 500;
  color: var(--tx);
}

.dot {
  color: var(--tx3);
}

.register-btn {
  display: inline-flex;
  flex: none;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 32px;
  padding: 0 14px;
  background: var(--s2);
  border: 1px solid var(--line);
  border-radius: 8px;
  color: var(--tx);
  font: 500 12px/1 var(--f-head);
  letter-spacing: -0.01em;
  white-space: nowrap;
  transition: background 0.15s var(--ease), border-color 0.15s var(--ease), transform 0.12s var(--ease);
}

.register-btn:hover:not(:disabled) {
  background: var(--s3);
}

.register-btn:active:not(:disabled) {
  transform: translateY(1px);
}

.register-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
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

.accent {
  color: var(--ac);
}

@media (max-width: 640px) {
  .challenge-card {
    flex-basis: 320px;
    min-height: 180px;
  }

  .card-hero {
    min-height: 125px;
  }

  .card-title {
    font-size: 18px;
  }
}
</style>
