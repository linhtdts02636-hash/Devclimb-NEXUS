<script setup>
import { computed, ref, watch } from 'vue'
import Avatar from '../../../components/common/Avatar.vue'
import { describeApiError } from '../../../config/apiClient'
import { assignRole, fetchRoles, removeMember, unassignRole } from '../communityApi'
import { describePermission } from '../permissions'

const props = defineProps({
  communityName: { type: String, required: true },
  members: { type: Array, required: true },
  canManageMembers: { type: Boolean, default: false },
})
const emit = defineEmits(['members-changed'])

const dateFormat = new Intl.DateTimeFormat('en', { year: 'numeric', month: 'short' })

const roles = ref([])
const loadError = ref('')

async function loadRoles() {
  try {
    roles.value = await fetchRoles(props.communityName)
    loadError.value = ''
  } catch (error) {
    loadError.value = describeApiError(error).message
  }
}

// Số người giữ vai trò đổi khi gán hoặc xoá thành viên, nên tải lại mỗi khi danh sách thành viên cập nhật.
watch(() => [props.communityName, props.members], loadRoles, { immediate: true })

const dialogElement = ref(null)
const selectedRoleId = ref(null)
const searchText = ref('')
const memberToAdd = ref('')
const actionError = ref('')
const isBusy = ref(false)
let openerButton = null

const selectedRole = computed(() => roles.value.find((role) => role.id === selectedRoleId.value) ?? null)
const isCustomRole = computed(() => selectedRole.value?.type === 'CUSTOM')

function holdsRole(member, role) {
  return member.roles.some((memberRole) => memberRole.id === role.id) || (role.type === 'OWNER' && member.isOwner)
}

const holders = computed(() => (selectedRole.value ? props.members.filter((member) => holdsRole(member, selectedRole.value)) : []))
const visibleHolders = computed(() => {
  const query = searchText.value.trim().toLowerCase()
  return holders.value.filter((member) => member.name.toLowerCase().includes(query))
})
const addableMembers = computed(() => props.members.filter((member) => !member.isOwner && !holdsRole(member, selectedRole.value)))

function describeRole(role) {
  if (role.type === 'OWNER') return 'Created the community. Can do everything.'
  if (role.type === 'DEFAULT') return 'Given to everyone who joins. Always grey and cannot be changed.'
  return role.permissions.length ? role.permissions.map(describePermission).join(', ') : 'No extra permissions.'
}

function openRole(role, event) {
  openerButton = event.currentTarget
  selectedRoleId.value = role.id
  searchText.value = ''
  memberToAdd.value = ''
  actionError.value = ''
  dialogElement.value.showModal()
}

function closeDialog() {
  dialogElement.value.close()
  openerButton?.focus()
}

async function runAction(action) {
  isBusy.value = true
  actionError.value = ''
  try {
    await action()
    emit('members-changed')
  } catch (error) {
    actionError.value = describeApiError(error).message
  } finally {
    isBusy.value = false
  }
}

async function addMemberToRole() {
  if (!memberToAdd.value) return
  await runAction(() => assignRole(props.communityName, Number(memberToAdd.value), selectedRole.value.id))
  memberToAdd.value = ''
}

function removeFromRole(member) {
  return runAction(() => unassignRole(props.communityName, member.userId, selectedRole.value.id))
}

function removeFromCommunity(member) {
  if (!window.confirm(`Remove ${member.name} from this community?`)) return
  return runAction(() => removeMember(props.communityName, member.userId))
}
</script>

<template>
  <section class="card">
    <h2>Roles</h2>
    <p v-if="loadError" class="list-error" role="alert">{{ loadError }}</p>
    <ul class="roles">
      <li v-for="role in roles" :key="role.id">
        <button type="button" class="role" :style="{ '--role-color': role.color }" @click="openRole(role, $event)">
          <span class="dot"></span>
          <span class="name">{{ role.name }}</span>
          <span class="count">{{ role.memberCount }}</span>
        </button>
      </li>
    </ul>

    <dialog ref="dialogElement" class="dialog" aria-labelledby="role-dialog-title" @click.self="closeDialog" @close="openerButton?.focus()">
      <template v-if="selectedRole">
        <header>
          <div class="top">
            <h3 id="role-dialog-title">
              <i :style="{ background: selectedRole.color }"></i>{{ selectedRole.name }}
              <span class="badge">{{ selectedRole.memberCount }}</span>
            </h3>
            <button type="button" class="close" aria-label="Close" @click="closeDialog">
              <svg viewBox="0 0 24 24"><path d="M6 6l12 12M18 6 6 18" /></svg>
            </button>
          </div>
          <p>{{ describeRole(selectedRole) }}</p>
        </header>

        <div v-if="canManageMembers && isCustomRole" class="add">
          <select v-model="memberToAdd" aria-label="Member to add to this role" :disabled="isBusy || !addableMembers.length">
            <option value="" disabled>{{ addableMembers.length ? 'Add a member…' : 'Everyone already has this role' }}</option>
            <option v-for="member in addableMembers" :key="member.userId" :value="member.userId">{{ member.name }}</option>
          </select>
          <button type="button" class="action" :disabled="isBusy || !memberToAdd" @click="addMemberToRole">Add</button>
        </div>

        <input v-model="searchText" type="search" placeholder="Search members" autocomplete="off" aria-label="Search members in this role" />
        <p v-if="actionError" class="dialog-error" role="alert">{{ actionError }}</p>
        <ul class="people">
          <li v-for="member in visibleHolders" :key="member.userId">
            <Avatar :src="member.imageUrl" :name="member.name" :size="32" :round="true" :show-name="false" />
            <span class="person">{{ member.name }}</span>
            <small>since {{ dateFormat.format(new Date(member.joinedAt)) }}</small>
            <template v-if="canManageMembers && !member.isOwner">
              <button v-if="isCustomRole" type="button" class="action" :disabled="isBusy" @click="removeFromRole(member)">Remove role</button>
              <button type="button" class="action danger" :disabled="isBusy" @click="removeFromCommunity(member)">Kick</button>
            </template>
          </li>
          <li v-if="!visibleHolders.length" class="empty">{{ holders.length ? 'No one with this role matches that name.' : 'No one has this role yet.' }}</li>
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
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: var(--t-sm);
  font-weight: 500;
}

.count {
  min-width: 3ch;
  text-align: right;
  font: 500 var(--t-sm) var(--f-mono);
  font-variant-numeric: tabular-nums;
}

.list-error,
.dialog-error {
  font-size: var(--t-xs);
  color: var(--bad);
}

.list-error {
  margin-bottom: var(--s-2);
}

.dialog-error {
  margin: var(--s-2) var(--s-5) 0;
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

input:focus-visible,
select:focus-visible {
  border-color: var(--ac);
  box-shadow: 0 0 0 3px var(--ac-soft);
  outline: none;
}

.add {
  display: flex;
  gap: var(--s-2);
  margin: var(--s-3) var(--s-5) 0;
}

.add select {
  flex: 1;
  min-width: 0;
  height: 36px;
  padding: 0 var(--s-4);
  font: inherit;
  font-size: var(--t-sm);
  color: var(--tx);
  background: var(--bg);
  border: 1px solid var(--line2);
  border-radius: 10px;
  cursor: pointer;
}

.add select:hover:not(:disabled) {
  border-color: var(--tx2);
}

.add select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/*
 * Trình duyệt tự vẽ danh sách thả xuống thường (nền xanh, góc vuông) và bỏ qua CSS ở đó.
 * base-select cho phép tạo kiểu cho danh sách. Trình duyệt chưa hỗ trợ sẽ giữ danh sách gốc.
 */
@supports (appearance: base-select) {
  .add select,
  .add select::picker(select) {
    appearance: base-select;
  }

  .add select {
    align-items: center;
    justify-content: space-between;
    padding-block: 0;
    line-height: 1;
  }

  .add select::picker(select) {
    padding: var(--s-1);
    margin-top: 4px;
    color: var(--tx);
    background: var(--s1);
    border: 1px solid var(--line2);
    border-radius: 10px;
    box-shadow: var(--sh-card);
  }

  .add select::picker-icon {
    color: var(--tx2);
  }

  .add select option {
    padding: 8px var(--s-3);
    font-size: var(--t-sm);
    color: var(--tx);
    border-radius: var(--r-sm);
  }

  .add select option:hover,
  .add select option:focus-visible {
    background: var(--s2);
    outline: none;
  }

  .add select option:checked {
    background: var(--ac-soft);
  }

  .add select option:disabled {
    color: var(--tx2);
  }

  .add select option::checkmark {
    display: none;
  }
}

.action {
  flex: none;
  height: 28px;
  padding: 0 var(--s-3);
  font-size: var(--t-xs);
  color: var(--tx2);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
}

.add .action {
  height: 36px;
  color: var(--tx);
}

.action:hover:not(:disabled) {
  color: var(--tx);
  background: var(--s2);
}

.action.danger:hover:not(:disabled) {
  color: var(--bad);
  border-color: var(--bad);
  background: transparent;
}

.action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

.person {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.people small {
  margin-left: auto;
  font: 400 var(--t-xs) var(--f-mono);
  color: var(--tx2);
  white-space: nowrap;
}

.people .empty {
  justify-content: center;
  color: var(--tx2);
}
</style>
