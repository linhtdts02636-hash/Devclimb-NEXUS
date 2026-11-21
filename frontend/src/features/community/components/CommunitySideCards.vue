<script setup>
import Avatar from '../../../components/common/Avatar.vue'
import { sampleSolvers, sampleTopics } from '../../../sample/sampleCommunityData'
</script>

<template>
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
