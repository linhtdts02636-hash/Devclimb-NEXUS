<script setup>
import { computed, ref } from 'vue'
import Avatar from '../../../components/common/Avatar.vue'
import { sampleMemberNames, sampleRoles } from '../../../sample/sampleCommunityData'

const props = defineProps({
  owners: { type: Array, required: true },
})

const dateFormat = new Intl.DateTimeFormat('en', { year: 'numeric', month: 'short' })

const roles = computed(() => [
  {
    name: 'Owner',
    color: 'var(--warn)',
    count: props.owners.length,
    description: 'Created the community. Can edit it and delete it.',
    people: props.owners.map((owner) => ({ name: owner.name, imageUrl: owner.imageUrl, since: dateFormat.format(new Date(owner.joinedAt)) })),
  },
  ...sampleRoles.map((role) => ({
    ...role,
    people: Array.from({ length: Math.min(role.count, 18) }, (_, index) => ({
      name: sampleMemberNames[(index + role.name.length) % sampleMemberNames.length],
      imageUrl: '',
      since: 'Oct 2026',
    })),
  })),
])

const dialogElement = ref(null)
const selectedRole = ref(null)
const searchText = ref('')
let openerButton = null

const visiblePeople = computed(() => {
  const query = searchText.value.trim().toLowerCase()
  return selectedRole.value.people.filter((person) => person.name.includes(query))
})
const hiddenCount = computed(() => selectedRole.value.count - selectedRole.value.people.length)

function openRole(role, event) {
  openerButton = event.currentTarget
  selectedRole.value = role
  searchText.value = ''
  dialogElement.value.showModal()
}

function closeDialog() {
  dialogElement.value.close()
  openerButton?.focus()
}
</script>

<template>
  <section class="card">
    <h2>Roles</h2>
    <ul class="roles">
      <li v-for="role in roles" :key="role.name">
        <button type="button" class="role" :style="{ '--role-color': role.color }" @click="openRole(role, $event)">
          <span class="dot"></span>
          <span class="name">{{ role.name }}</span>
          <span class="count">{{ role.count }}</span>
        </button>
      </li>
    </ul>

    <dialog ref="dialogElement" class="dialog" aria-labelledby="role-dialog-title" @click.self="closeDialog" @close="openerButton?.focus()">
      <template v-if="selectedRole">
        <header>
          <div class="top">
            <h3 id="role-dialog-title">
              <i :style="{ background: selectedRole.color }"></i>{{ selectedRole.name }}
              <span class="badge">{{ selectedRole.count }}</span>
            </h3>
            <button type="button" class="close" aria-label="Close" @click="closeDialog">
              <svg viewBox="0 0 24 24"><path d="M6 6l12 12M18 6 6 18" /></svg>
            </button>
          </div>
          <p>{{ selectedRole.description }}</p>
        </header>
        <input v-model="searchText" type="search" placeholder="Search members" autocomplete="off" aria-label="Search members in this role" />
        <ul class="people">
          <li v-for="person in visiblePeople" :key="person.name">
            <Avatar :src="person.imageUrl" :name="person.name" :size="32" :round="true" :show-name="false" />
            {{ person.name }}<small>since {{ person.since }}</small>
          </li>
          <li v-if="!visiblePeople.length" class="empty">No one with this role matches that name.</li>
          <li v-else-if="hiddenCount > 0 && !searchText" class="empty">and {{ hiddenCount }} more</li>
        </ul>
      </template>
    </dialog>
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

.roles {
  position: relative;
  display: grid;
  gap: 2px;
  margin: 0;
  padding: 0;
  list-style: none;
}

/* Đường dọc nối các chấm tròn, tạo hình "thang" cho các bậc vai trò. */
.roles::before {
  content: '';
  position: absolute;
  top: 14px;
  bottom: 14px;
  left: 7px;
  width: 1px;
  background: var(--line2);
}

.role {
  display: grid;
  grid-template-columns: 16px 1fr auto;
  gap: var(--s-3);
  align-items: center;
  width: 100%;
  padding: 8px var(--s-2) 8px 0;
  text-align: left;
  border-radius: var(--r-sm);
}

.role:hover {
  background: var(--s2);
}

.dot {
  position: relative;
  z-index: 1;
  width: 15px;
  height: 15px;
  background: var(--bg);
  border: 2px solid var(--role-color);
  border-radius: 50%;
}

.name {
  font-size: var(--t-sm);
  font-weight: 500;
}

.count {
  min-width: 3ch;
  text-align: right;
  font: 500 var(--t-sm) var(--f-mono);
  font-variant-numeric: tabular-nums;
}

.dialog {
  width: min(440px, calc(100vw - 24px));
  padding: 0;
  margin: auto;
  color: var(--tx);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-lg);
}

.dialog::backdrop {
  background: rgba(0, 0, 0, 0.5);
}

header {
  display: grid;
  gap: 2px;
  padding: var(--s-4) var(--s-5);
  border-bottom: 1px solid var(--line);
}

.top {
  display: flex;
  gap: var(--s-3);
  align-items: center;
}

h3 {
  display: flex;
  flex: 1;
  gap: var(--s-2);
  align-items: center;
  font: 600 var(--t-lg) var(--f-head);
}

h3 i {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.badge {
  padding: 1px var(--s-2);
  font: 400 var(--t-xs) var(--f-mono);
  color: var(--tx2);
  background: var(--s2);
  border-radius: 999px;
}

header p {
  font-size: var(--t-sm);
  color: var(--tx2);
}

.close {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  color: var(--tx2);
  border-radius: var(--r-sm);
}

.close:hover {
  background: var(--s2);
  color: var(--tx);
}

svg {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
}

input {
  display: block;
  width: calc(100% - var(--s-5) * 2);
  height: 36px;
  margin: var(--s-3) var(--s-5) 0;
  padding: 0 var(--s-3);
  font: inherit;
  font-size: var(--t-sm);
  color: var(--tx);
  background: var(--bg);
  border: 1px solid var(--line2);
  border-radius: 10px;
}

input:focus-visible {
  border-color: var(--ac);
  box-shadow: 0 0 0 3px var(--ac-soft);
  outline: none;
}

.people {
  max-height: 340px;
  margin: var(--s-2) 0 0;
  padding: var(--s-2) var(--s-3) var(--s-3);
  overflow: auto;
  list-style: none;
}

.people li {
  display: flex;
  gap: var(--s-3);
  align-items: center;
  padding: 8px var(--s-2);
  font-size: var(--t-sm);
  border-radius: var(--r-sm);
}

.people li:hover {
  background: var(--s2);
}

.people small {
  margin-left: auto;
  font: 400 var(--t-xs) var(--f-mono);
  color: var(--tx2);
}

.people .empty {
  justify-content: center;
  color: var(--tx2);
}
</style>
