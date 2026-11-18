<script setup>
import { computed, onMounted, ref, useId } from 'vue'

const props = defineProps({
  communityName: { type: String, required: true },
  busy: { type: Boolean, default: false },
  errorMessage: { type: String, default: '' },
})

const emit = defineEmits(['confirm', 'cancel'])

const dialogElement = ref(null)
const hasAcknowledged = ref(false)
const typedName = ref('')
const titleId = useId()

const canDelete = computed(() => hasAcknowledged.value && typedName.value === props.communityName && !props.busy)

// showModal() chỉ gọi được sau khi phần tử đã gắn vào DOM; nó cũng tự khóa focus bên trong hộp thoại.
onMounted(() => dialogElement.value.showModal())
</script>

<template>
  <dialog ref="dialogElement" class="dialog" :aria-labelledby="titleId" @cancel.prevent="!busy && emit('cancel')">
    <h2 :id="titleId">Delete {{ communityName }}?</h2>
    <p class="warning">This cannot be undone. It permanently deletes:</p>
    <ul>
      <li>the community, its members and their roles</li>
      <li>all posts and replies</li>
      <li>all puzzles, their test cases and every submission</li>
      <li>all contests and their achievements</li>
      <li>all warnings and appeals</li>
    </ul>

    <label class="acknowledge">
      <input v-model="hasAcknowledged" type="checkbox" :disabled="busy" />
      I understand that all of this will be deleted.
    </label>

    <label class="confirm-name">
      <span>Type <b>{{ communityName }}</b> to confirm</span>
      <input v-model="typedName" type="text" autocomplete="off" spellcheck="false" :disabled="busy" />
    </label>

    <p v-if="errorMessage" class="error" role="alert">{{ errorMessage }}</p>

    <div class="buttons">
      <button type="button" class="btn" :disabled="busy" @click="emit('cancel')">Cancel</button>
      <button type="button" class="btn danger" :disabled="!canDelete" :aria-busy="busy" @click="emit('confirm', typedName)">
        <span v-if="busy" class="spinner" aria-hidden="true"></span>
        Delete this community
      </button>
    </div>
  </dialog>
</template>

<style scoped>
.dialog {
  width: min(480px, calc(100vw - var(--s-5)));
  padding: var(--s-5);
  color: var(--tx);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-lg);
}

.dialog::backdrop {
  background: rgba(0, 0, 0, 0.5);
}

h2 {
  font: 600 var(--t-lg) var(--f-head);
}

.warning {
  margin: var(--s-3) 0 var(--s-2);
  font-weight: 500;
  color: var(--bad);
}

ul {
  margin: 0 0 var(--s-4);
  padding-left: var(--s-5);
  color: var(--tx2);
  font-size: var(--t-sm);
}

.acknowledge {
  display: flex;
  gap: var(--s-2);
  align-items: center;
  margin-bottom: var(--s-4);
  font-size: var(--t-sm);
}

.confirm-name {
  display: grid;
  gap: var(--s-2);
  font-size: var(--t-sm);
}

.confirm-name input {
  height: 40px;
  padding: 0 var(--s-3);
  font: inherit;
  color: var(--tx);
  background: var(--bg);
  border: 1px solid var(--line2);
  border-radius: 10px;
}

.confirm-name input:focus-visible {
  border-color: var(--bad);
  outline: none;
}

.error {
  margin-top: var(--s-3);
  font-size: var(--t-sm);
  color: var(--bad);
}

.buttons {
  display: flex;
  gap: var(--s-2);
  justify-content: flex-end;
  margin-top: var(--s-5);
}

.btn.danger {
  background: var(--bad);
  color: var(--bg);
}
</style>
