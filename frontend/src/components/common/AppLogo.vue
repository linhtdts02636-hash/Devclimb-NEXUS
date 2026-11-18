<script setup>
defineProps({
  // Nhãn nhỏ "nexus" cạnh tên; phần footer không hiển thị nhãn này.
  showTag: { type: Boolean, default: true },
})
</script>

<template>
  <RouterLink class="logo" to="/">
    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 20 10 7l4 7 3-4 4 10z" /></svg>
    DevClimb
    <small v-if="showTag">nexus</small>
  </RouterLink>
</template>

<style scoped>
.logo {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  font: 600 var(--t-lg) var(--f-head);
  letter-spacing: -0.02em;
}

.logo svg {
  width: 22px;
  height: 22px;
  fill: none;
  stroke: var(--ac);
  stroke-width: 2.2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.logo small {
  padding: 1px var(--s-2);
  border: 1px solid var(--line2);
  border-radius: 999px;
  font: 400 var(--t-xs) var(--f-mono);
  letter-spacing: 0;
  color: var(--tx2);
}

@media (max-width: 600px) {
  .logo small {
    display: none;
  }
}
</style>
