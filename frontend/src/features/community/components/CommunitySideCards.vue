<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import Avatar from '../../../components/common/Avatar.vue'
import { sampleContest, sampleSolvers, sampleTopics } from '../../../sample/sampleCommunityData'

const MINUTE_MS = 60_000

const now = ref(Date.now())
let countdownTimer = null

onMounted(() => (countdownTimer = setInterval(() => (now.value = Date.now()), MINUTE_MS / 2)))
onBeforeUnmount(() => clearInterval(countdownTimer))

const countdown = computed(() => {
  const remaining = Math.max(0, sampleContest.startsAt - now.value)
  const pad = (value) => String(value).padStart(2, '0')
  return [
    { label: 'days', value: pad(Math.floor(remaining / 86_400_000)) },
    { label: 'hours', value: pad(Math.floor(remaining / 3_600_000) % 24) },
    { label: 'min', value: pad(Math.floor(remaining / MINUTE_MS) % 60) },
  ]
})
</script>

<template>
  <section class="card">
    <h2>Upcoming contest</h2>
    <b>{{ sampleContest.title }}</b>
    <p>{{ sampleContest.description }}</p>
    <div class="countdown" role="timer" aria-label="Time until the contest starts">
      <div v-for="part in countdown" :key="part.label"><b>{{ part.value }}</b><small>{{ part.label }}</small></div>
    </div>
    <button type="button" class="btn p wide" disabled>Register</button>
  </section>

  <section class="card">
    <h2>Top solvers this week</h2>
    <ol class="solvers">
      <li v-for="(solver, index) in sampleSolvers" :key="solver.name">
        <span class="position">{{ index + 1 }}</span>
        <Avatar :name="solver.name" :size="26" :round="true" :show-name="false" />
        {{ solver.name }}<span class="solves">{{ solver.solves }}</span>
      </li>
    </ol>
  </section>

  <section class="card">
    <h2>Topics</h2>
    <ul class="topics">
      <li v-for="topic in sampleTopics" :key="topic">{{ topic }}</li>
    </ul>
  </section>
</template>

<style scoped>
.card {
  padding: var(--s-4);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
}

h2 {
  margin-bottom: var(--s-3);
  font: 400 11px var(--f-mono);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--tx2);
}

.card > b {
  display: block;
  font-weight: 500;
}

.card > p {
  font-size: var(--t-sm);
  color: var(--tx2);
}

.countdown {
  display: flex;
  gap: var(--s-2);
  margin: var(--s-3) 0;
}

.countdown div {
  display: grid;
  flex: 1;
  padding: var(--s-2);
  text-align: center;
  background: var(--s2);
  border-radius: var(--r-sm);
}

.countdown b {
  font: 500 var(--t-lg) var(--f-mono);
  font-variant-numeric: tabular-nums;
}

.countdown small {
  font: 400 10px var(--f-mono);
  text-transform: uppercase;
  color: var(--tx2);
}

.solvers {
  margin: 0;
  padding: 0;
  list-style: none;
}

.solvers li {
  display: flex;
  gap: var(--s-3);
  align-items: center;
  padding: 6px 0;
  font-size: var(--t-sm);
}

.position {
  width: 16px;
  font: 500 var(--t-xs) var(--f-mono);
  color: var(--tx2);
}

.solves {
  margin-left: auto;
  font: 500 var(--t-sm) var(--f-mono);
  font-variant-numeric: tabular-nums;
}

.topics {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s-2);
  margin: 0;
  padding: 0;
  list-style: none;
}

.topics li {
  padding: 4px var(--s-3);
  font-size: var(--t-xs);
  color: var(--tx2);
  border: 1px solid var(--line2);
  border-radius: 999px;
}
</style>
