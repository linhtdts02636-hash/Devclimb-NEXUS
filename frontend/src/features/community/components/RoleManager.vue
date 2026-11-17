<script setup>
import { computed, ref } from 'vue'

const PRESET_COLORS = ['#e7b44c', '#6f93ff', '#4ad08a', '#f08a6c', '#c58bf2', '#5ec9d6', '#e86fa0', '#9c9b93']
const PALETTE_KEY = 'dc-role-colors'
const HEX_COLOR = /^#[0-9a-f]{6}$/i
const PERMISSIONS = [
  { id: 'pin', label: 'Pin posts', hint: 'Pin and unpin posts at the top of the community.' },
  { id: 'remove', label: 'Remove posts and comments', hint: 'Take down content that breaks the rules.' },
  { id: 'puzzle', label: 'Create puzzles', hint: 'Add problems to the community library.' },
  { id: 'contest', label: 'Run contests', hint: 'Create, edit and judge contests.' },
  { id: 'members', label: 'Manage members', hint: 'Assign roles and remove members.' },
]

const props = defineProps({
  ownerCount: { type: Number, default: 1 },
})

// Roles have no backend yet, so this list lives only in this page and resets on reload.
const roles = ref([
  { id: 1, name: 'Owner', color: '#e7b44c', count: props.ownerCount, permissions: ['all'], isBuiltIn: true },
  { id: 2, name: 'Moderator', color: '#6f93ff', count: 3, permissions: ['pin', 'remove', 'members'] },
  { id: 3, name: 'Problem setter', color: '#4ad08a', count: 6, permissions: ['puzzle'] },
  { id: 4, name: 'Contest judge', color: '#c58bf2', count: 2, permissions: ['contest'] },
  { id: 5, name: 'Member', color: '#9c9b93', count: 143, permissions: [], isBuiltIn: true },
])
let nextRoleId = 6

function readSavedColors() {
  try {
    return JSON.parse(localStorage.getItem(PALETTE_KEY)) ?? []
  } catch {
    return []
  }
}

function writeSavedColors() {
  try {
    localStorage.setItem(PALETTE_KEY, JSON.stringify(savedColors.value))
  } catch {
    // Trình duyệt chặn lưu trữ (chế độ riêng tư): bảng màu vẫn đúng trong phiên này.
  }
}

const savedColors = ref(readSavedColors())
const allColors = computed(() => [...PRESET_COLORS, ...savedColors.value])

const roleDialog = ref(null)
const deleteDialog = ref(null)
const editingRoleId = ref(null)
const roleToDelete = ref(null)
const draftName = ref('')
const draftColor = ref(PRESET_COLORS[1])
const draftHex = ref(PRESET_COLORS[1])
const draftPermissions = ref([])
const nameError = ref('')

const isEditing = computed(() => editingRoleId.value !== null)

function describePermission(permissionId) {
  return permissionId === 'all' ? 'All permissions' : PERMISSIONS.find((permission) => permission.id === permissionId).label
}

function chooseColor(color) {
  draftColor.value = color.toLowerCase()
  draftHex.value = draftColor.value
}

function handleHexInput() {
  const typed = draftHex.value.startsWith('#') ? draftHex.value : `#${draftHex.value}`
  if (HEX_COLOR.test(typed)) draftColor.value = typed.toLowerCase()
}

function addColorToPalette() {
  if (allColors.value.includes(draftColor.value)) return
  savedColors.value = [...savedColors.value, draftColor.value]
  writeSavedColors()
}

function removeColorFromPalette(color) {
  savedColors.value = savedColors.value.filter((savedColor) => savedColor !== color)
  writeSavedColors()
}

function openRoleDialog(role = null) {
  editingRoleId.value = role?.id ?? null
  draftName.value = role?.name ?? ''
  draftPermissions.value = [...(role?.permissions ?? [])]
  chooseColor(role?.color ?? PRESET_COLORS[1])
  nameError.value = ''
  roleDialog.value.showModal()
}

function saveRole() {
  const name = draftName.value.trim()
  const isDuplicate = roles.value.some((role) => role.id !== editingRoleId.value && role.name.toLowerCase() === name.toLowerCase())
  nameError.value = !name ? 'Enter a role name.' : isDuplicate ? 'A role with this name already exists.' : ''
  if (!nameError.value && !HEX_COLOR.test(draftColor.value)) nameError.value = 'Enter a colour like #6f93ff.'
  if (nameError.value) return

  const details = { name, color: draftColor.value, permissions: [...draftPermissions.value] }
  if (isEditing.value) {
    Object.assign(roles.value.find((role) => role.id === editingRoleId.value), details)
  } else {
    // Insert before the last row so the built-in Member role stays at the bottom.
    roles.value.splice(roles.value.length - 1, 0, { id: nextRoleId++, count: 0, ...details })
  }
  roleDialog.value.close()
}

function askDeleteRole(role) {
  roleToDelete.value = role
  deleteDialog.value.showModal()
}

function confirmDeleteRole() {
  roles.value = roles.value.filter((role) => role.id !== roleToDelete.value.id)
  deleteDialog.value.close()
}

function closeOnBackdropClick(event) {
  if (event.target === event.currentTarget) event.currentTarget.close()
}
</script>

<template>
  <section class="panel" aria-labelledby="roles-heading">
    <header>
      <h2 id="roles-heading">Roles</h2>
      <p>Give members a title and decide what they can do. Roles are only for this community.</p>
    </header>

    <div class="toolbar">
      <span class="sample">sample data for illustration</span>
      <button type="button" class="btn p" @click="openRoleDialog()">
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" /></svg>Create role
      </button>
    </div>

    <ul class="roles">
      <li v-for="role in roles" :key="role.id" class="role" :style="{ '--role-color': role.color }">
        <span class="dot"></span>
        <div>
          <div class="name">{{ role.name }}<span v-if="role.isBuiltIn" class="chip">built-in</span></div>
          <div class="permissions">
            <span v-for="permissionId in role.permissions" :key="permissionId" class="chip">{{ describePermission(permissionId) }}</span>
            <span v-if="!role.permissions.length" class="chip">No extra permissions</span>
          </div>
        </div>
        <span class="count">{{ role.count }} {{ role.count === 1 ? 'member' : 'members' }}</span>
        <div class="operations">
          <button type="button" class="icon-button" :disabled="role.isBuiltIn" :aria-label="`Edit ${role.name}`" :title="role.isBuiltIn ? 'Built-in roles cannot be edited' : undefined" @click="openRoleDialog(role)">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 20h4L19 9l-4-4L4 16z" /></svg>
          </button>
          <button type="button" class="icon-button" :disabled="role.isBuiltIn" :aria-label="`Delete ${role.name}`" :title="role.isBuiltIn ? 'Built-in roles cannot be deleted' : undefined" @click="askDeleteRole(role)">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3" /></svg>
          </button>
        </div>
      </li>
    </ul>

    <dialog ref="roleDialog" class="dialog" aria-labelledby="role-dialog-title" @click="closeOnBackdropClick">
      <form novalidate @submit.prevent="saveRole">
        <div class="dialog-head">
          <h2 id="role-dialog-title">{{ isEditing ? 'Edit role' : 'Create role' }}</h2>
          <button type="button" class="icon-button" aria-label="Close" @click="roleDialog.close()">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6 6 18" /></svg>
          </button>
        </div>

        <div class="dialog-body">
          <div class="field">
            <label for="role-name">Role name</label>
            <input id="role-name" v-model="draftName" class="text-input" maxlength="30" autocomplete="off" placeholder="e.g. Teaching assistant" />
            <p v-if="nameError" class="error" role="alert">{{ nameError }}</p>
          </div>

          <div class="field">
            <span class="label">Colour</span>
            <div class="swatches">
              <span v-for="(color, index) in allColors" :key="color" class="swatch">
                <input :id="`role-color-${index}`" type="radio" name="role-color" :value="color" :checked="color === draftColor" @change="chooseColor(color)" />
                <label :for="`role-color-${index}`" :style="{ '--swatch-color': color }" :aria-label="`Colour ${color}`" :title="color"></label>
                <button v-if="index >= PRESET_COLORS.length" type="button" class="remove-swatch" :aria-label="`Remove ${color} from palette`" @click="removeColorFromPalette(color)">
                  <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6 6 18" /></svg>
                </button>
              </span>
            </div>
            <div class="custom-color">
              <input v-model="draftColor" type="color" aria-label="Pick any colour" @input="chooseColor($event.target.value)" />
              <input v-model="draftHex" class="text-input hex" maxlength="7" spellcheck="false" autocomplete="off" placeholder="#6f93ff" aria-label="Hex colour" @input="handleHexInput" />
              <button type="button" class="btn" @click="addColorToPalette">
                <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" /></svg>Add to palette
              </button>
              <span class="hint">Pick any colour or type a hex code. Added colours stay in the palette.</span>
            </div>
          </div>

          <div class="field">
            <span class="label">Preview</span>
            <div><span class="preview-chip" :style="{ '--role-color': draftColor }"><i></i>{{ draftName.trim() || 'New role' }}</span></div>
          </div>

          <div class="field">
            <span class="label">Permissions</span>
            <div class="permission-list">
              <label v-for="permission in PERMISSIONS" :key="permission.id" class="permission">
                <input v-model="draftPermissions" type="checkbox" :value="permission.id" />
                <span><b>{{ permission.label }}</b><small>{{ permission.hint }}</small></span>
              </label>
            </div>
          </div>
        </div>

        <div class="dialog-foot">
          <button type="button" class="btn" @click="roleDialog.close()">Cancel</button>
          <button type="submit" class="btn p">{{ isEditing ? 'Save role' : 'Create role' }}</button>
        </div>
      </form>
    </dialog>

    <dialog ref="deleteDialog" class="dialog small" aria-labelledby="delete-role-title" @click="closeOnBackdropClick">
      <div class="dialog-head"><h2 id="delete-role-title">Delete role</h2></div>
      <div class="dialog-body">
        <p v-if="roleToDelete">
          {{ roleToDelete.count
            ? `${roleToDelete.count} ${roleToDelete.count === 1 ? 'member' : 'members'} with "${roleToDelete.name}" will go back to the Member role.`
            : `Delete "${roleToDelete.name}"? No one has this role.` }}
        </p>
      </div>
      <div class="dialog-foot">
        <button type="button" class="btn" @click="deleteDialog.close()">Cancel</button>
        <button type="button" class="btn danger" @click="confirmDeleteRole">Delete role</button>
      </div>
    </dialog>
  </section>
</template>

<style scoped>
.panel {
  display: grid;
  gap: var(--s-4);
}

.panel > header h2 {
  font: 600 20px / 1.3 var(--f-head);
  letter-spacing: -0.02em;
}

.panel > header p {
  margin-top: 2px;
  font-size: var(--t-sm);
  color: var(--tx2);
}

svg {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.btn {
  height: 34px;
  padding: 0 var(--s-4);
  font-size: var(--t-sm);
}

.btn.danger {
  color: var(--bad);
  background: transparent;
  border: 1px solid var(--bad);
}

.btn.danger:hover {
  color: var(--bg);
  background: var(--bad);
}

.toolbar {
  display: flex;
  gap: var(--s-3);
  align-items: center;
  justify-content: space-between;
}

.sample {
  font: 400 var(--t-xs) var(--f-mono);
  color: var(--tx2);
}

.chip {
  padding: 1px var(--s-2);
  font: 400 var(--t-xs) var(--f-mono);
  color: var(--tx2);
  white-space: nowrap;
  background: var(--s2);
  border-radius: 999px;
}

.roles {
  display: grid;
  gap: var(--s-2);
  margin: 0;
  padding: 0;
  list-style: none;
}

.role {
  display: grid;
  grid-template-columns: 16px minmax(0, 1fr) auto auto;
  gap: var(--s-4);
  align-items: center;
  padding: var(--s-3) var(--s-4);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
}

.role:hover {
  background: var(--s2);
}

.dot {
  width: 14px;
  height: 14px;
  border: 2px solid var(--role-color);
  border-radius: 50%;
}

.name {
  display: flex;
  gap: var(--s-2);
  align-items: center;
  font-size: var(--t-sm);
  font-weight: 500;
}

.permissions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s-1);
  margin-top: 4px;
}

.count {
  font: 500 var(--t-sm) var(--f-mono);
  font-variant-numeric: tabular-nums;
  color: var(--tx2);
}

.operations {
  display: flex;
  gap: 2px;
}

.icon-button {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  color: var(--tx2);
  border-radius: var(--r-sm);
}

.icon-button:hover:not(:disabled) {
  color: var(--tx);
  background: var(--s3);
}

.icon-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.dialog {
  width: min(520px, calc(100vw - 24px));
  padding: 0;
  margin: auto;
  color: var(--tx);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-lg);
  box-shadow: var(--sh-card);
}

.dialog::backdrop {
  background: rgba(0, 0, 0, 0.5);
}

.dialog-head {
  display: flex;
  gap: var(--s-3);
  align-items: center;
  padding: var(--s-4) var(--s-5);
  border-bottom: 1px solid var(--line);
}

.dialog-head h2 {
  flex: 1;
  font: 600 var(--t-md) var(--f-head);
}

.dialog-body {
  display: grid;
  gap: var(--s-4);
  padding: var(--s-5);
}

.dialog-foot {
  display: flex;
  gap: var(--s-2);
  justify-content: flex-end;
  padding: var(--s-3) var(--s-5) var(--s-4);
}

.field {
  display: grid;
  gap: var(--s-2);
}

.field > label {
  font-size: var(--t-sm);
  font-weight: 500;
}

.label {
  font: 400 11px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--tx2);
}

.text-input {
  width: 100%;
  height: 38px;
  padding: 0 var(--s-3);
  font: inherit;
  font-size: var(--t-sm);
  color: var(--tx);
  background: var(--bg);
  border: 1px solid var(--line2);
  border-radius: 10px;
  outline: none;
}

.text-input:focus-visible {
  border-color: var(--ac);
  box-shadow: 0 0 0 3px var(--ac-soft);
}

.error {
  font-size: var(--t-xs);
  color: var(--bad);
}

.hint {
  font-size: var(--t-xs);
  color: var(--tx2);
}

.swatches {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s-2);
}

.swatch {
  position: relative;
  flex: none;
  width: 28px;
  height: 28px;
}

.swatch input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.swatch label {
  display: block;
  width: 28px;
  height: 28px;
  background: var(--swatch-color);
  border-radius: 50%;
  outline: 2px solid transparent;
  outline-offset: 2px;
  cursor: pointer;
}

.swatch input:checked + label {
  outline-color: var(--tx);
}

.swatch input:focus-visible + label {
  outline-color: var(--ac);
}

.remove-swatch {
  position: absolute;
  top: -6px;
  right: -6px;
  display: none;
  place-items: center;
  width: 16px;
  height: 16px;
  color: var(--tx);
  background: var(--s3);
  border: 1px solid var(--line2);
  border-radius: 50%;
}

.swatch:hover .remove-swatch,
.swatch:focus-within .remove-swatch {
  display: grid;
}

.remove-swatch svg {
  width: 10px;
  height: 10px;
  stroke-width: 2.4;
}

.custom-color {
  display: flex;
  flex-wrap: wrap;
  gap: var(--s-3);
  align-items: center;
  margin-top: var(--s-1);
}

.custom-color input[type='color'] {
  width: 38px;
  height: 38px;
  padding: 3px;
  background: var(--bg);
  border: 1px solid var(--line2);
  border-radius: 10px;
  cursor: pointer;
}

.hex {
  width: 110px;
  font-family: var(--f-mono);
}

.preview-chip {
  display: inline-flex;
  gap: var(--s-2);
  align-items: center;
  padding: 2px var(--s-3) 2px var(--s-2);
  font-size: var(--t-xs);
  font-weight: 500;
  color: var(--role-color);
  border: 1px solid var(--role-color);
  border-radius: 999px;
}

.preview-chip i {
  width: 8px;
  height: 8px;
  background: var(--role-color);
  border-radius: 50%;
}

.permission-list {
  display: grid;
  overflow: hidden;
  border: 1px solid var(--line2);
  border-radius: var(--r);
}

.permission {
  display: flex;
  gap: var(--s-3);
  align-items: center;
  padding: var(--s-3) var(--s-4);
  background: var(--bg);
  cursor: pointer;
}

.permission + .permission {
  border-top: 1px solid var(--line);
}

.permission:hover {
  background: var(--s2);
}

.permission input {
  width: 16px;
  height: 16px;
  accent-color: var(--ac);
}

.permission b {
  display: block;
  font-size: var(--t-sm);
  font-weight: 500;
}

.permission small {
  font-size: var(--t-xs);
  color: var(--tx2);
}

@media (max-width: 820px) {
  .role {
    grid-template-columns: 16px minmax(0, 1fr) auto;
  }

  .count {
    display: none;
  }
}
</style>
