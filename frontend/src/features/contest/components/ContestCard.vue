<script setup>
import Avatar from './Avatar.vue'

defineProps({
  title: { type: String, default: 'Weekly Contest' },
  status: { type: String, default: 'Open' },
  statusTone: { type: String, default: 'easy' },
  countdownLabel: { type: String, default: 'Starts In' },
  countdown: { type: String, default: '' },
  community: {
    type: Object,
    default: () => ({ src: '/favicon.svg', name: 'fpt-algo-club', type: 'community' }),
  },
  registeredLabel: { type: String, default: 'Registered:' },
  registeredCount: { type: String, default: '' },
  puzzleCount: { type: [Number, String], default: 0 },
  actionLabel: { type: String, default: 'Register Now' },
})
</script>

<template>
  <article class="challenge-card">
    <div class="card-head">
      <div class="card-main">
        <Avatar :src="community.src" :name="community.name" :type="community.type" />
        <div class="card-title-row">
          <h2 class="card-title">{{ title }}</h2>
          <span class="badge" :class="statusTone">{{ status }}</span>
        </div>
      </div>
      <div class="countdown">
        <span class="label">{{ countdownLabel }}</span>
        <span class="value">
          <svg class="ic" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M10 2h4M12 14l3-3" />
            <circle cx="12" cy="14" r="8" />
          </svg>
          {{ countdown }}
        </span>
      </div>
    </div>
    <div class="card-foot">
      <div class="meta-line">
        <span>{{ registeredLabel }} <strong class="meta-strong">{{ registeredCount }}</strong></span>
        <span class="dot">•</span>
        <span>{{ puzzleCount }} Puzzles</span>
      </div>
      <button class="register-btn" type="button">
        <span>{{ actionLabel }}</span>
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
  display: flex;
  flex: 0 0 420px;
  flex-direction: column;
  justify-content: space-between;
  gap: var(--s-4);
  padding: var(--s-4);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--s-3);
}

.card-main {
  min-width: 0;
}

.card-title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--s-2);
  margin-top: var(--s-2);
}

.card-title {
  font: 500 20px/1.2 var(--f-head);
  letter-spacing: -0.02em;
  color: var(--tx);
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border: 1px solid transparent;
  border-radius: 6px;
  font: 500 11px/1.4 var(--f-head);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  white-space: nowrap;
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

.countdown {
  display: flex;
  flex: none;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  padding: 6px 10px;
  background: var(--s2);
  border: 1px solid var(--line);
  border-radius: 8px;
}

.countdown .label {
  font: 400 10px var(--f-head);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--tx2);
}

.countdown .value {
  display: flex;
  align-items: center;
  gap: 4px;
  font: 500 12px var(--f-head);
  font-variant-numeric: tabular-nums;
  color: var(--tx);
}

.countdown .ic {
  color: var(--tx2);
}

.card-foot {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
  padding-top: var(--s-3);
  border-top: 1px solid var(--line);
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
  transition: background 0.15s var(--ease), border-color 0.15s var(--ease);
}

.register-btn:hover {
  background: var(--s3);
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
    flex-basis: 310px;
  }

  .card-title {
    font-size: 18px;
  }
}
</style>
