<script setup>
import { nextTick, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { createPuzzle } from '../puzzleApi'

const props = defineProps({
  open: { type: Boolean, default: false },
})

const emit = defineEmits(['close', 'created'])

const difficultyOptions = [
  { value: 'EASY', label: 'Easy' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HARD', label: 'Hard' },
]

const form = reactive({
  title: '',
  functionName: '',
  difficulty: 'MEDIUM',
  content: '',
})

const errors = reactive({
  title: '',
  functionName: '',
  content: '',
})

const titleInput = ref(null)
const isSubmitting = ref(false)
const serverError = ref('')

function resetForm() {
  form.title = ''
  form.functionName = ''
  form.difficulty = 'MEDIUM'
  form.content = ''
  errors.title = ''
  errors.functionName = ''
  errors.content = ''
  serverError.value = ''
}

function validate() {
  errors.title = form.title.trim() ? '' : 'Title is required.'
  errors.functionName = form.functionName.trim() ? '' : 'Function name is required.'
  errors.content = form.content.trim() ? '' : 'Problem statement is required.'
  return !errors.title && !errors.functionName && !errors.content
}

function close() {
  if (isSubmitting.value) return
  emit('close')
}

async function submit() {
  if (isSubmitting.value || !validate()) return

  isSubmitting.value = true
  serverError.value = ''

  try {
    const puzzle = await createPuzzle({
      title: form.title.trim(),
      functionName: form.functionName.trim(),
      difficulty: form.difficulty,
      content: form.content.trim(),
    })
    emit('created', puzzle)
  } catch (error) {
    serverError.value = error?.response?.data?.message || 'Could not create the puzzle. Try again.'
  } finally {
    isSubmitting.value = false
  }
}

watch(
  () => props.open,
  (open) => {
    document.body.style.overflow = open ? 'hidden' : ''
    if (!open) return
    resetForm()
    nextTick(() => titleInput.value?.focus())
  },
)

onBeforeUnmount(() => {
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="modal-backdrop" @mousedown.self="close" @keydown.esc="close">
      <section
        class="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="create-puzzle-title"
        aria-describedby="create-puzzle-lead"
      >
        <header class="modal-head">
          <div>
            <h2 id="create-puzzle-title">Create your own puzzle</h2>
            <p id="create-puzzle-lead">Add a new challenge for the community to solve.</p>
          </div>
          <button class="icon-btn" type="button" aria-label="Close create puzzle dialog" @click="close">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="m6 6 12 12M18 6 6 18" />
            </svg>
          </button>
        </header>

        <form class="form" @submit.prevent="submit">
          <div class="field">
            <label for="puzzle-title">Title</label>
            <input
              id="puzzle-title"
              ref="titleInput"
              v-model="form.title"
              type="text"
              maxlength="200"
              autocomplete="off"
              :aria-invalid="errors.title ? 'true' : undefined"
              placeholder="Two Sum"
            />
            <p v-if="errors.title" class="field-error">{{ errors.title }}</p>
          </div>

          <div class="field">
            <label for="puzzle-function">Function name</label>
            <input
              id="puzzle-function"
              v-model="form.functionName"
              type="text"
              maxlength="50"
              autocomplete="off"
              spellcheck="false"
              :aria-invalid="errors.functionName ? 'true' : undefined"
              placeholder="twoSum"
            />
            <p v-if="errors.functionName" class="field-error">{{ errors.functionName }}</p>
          </div>

          <div class="field">
            <label for="puzzle-difficulty">Difficulty</label>
            <select id="puzzle-difficulty" v-model="form.difficulty">
              <option v-for="option in difficultyOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>

          <div class="field">
            <label for="puzzle-content">Problem statement</label>
            <textarea
              id="puzzle-content"
              v-model="form.content"
              rows="7"
              :aria-invalid="errors.content ? 'true' : undefined"
              placeholder="Describe the input, output, and constraints..."
            ></textarea>
            <p v-if="errors.content" class="field-error">{{ errors.content }}</p>
          </div>

          <p v-if="serverError" class="form-error">{{ serverError }}</p>

          <footer class="actions">
            <button type="button" class="btn ghost" :disabled="isSubmitting" @click="close">Cancel</button>
            <button type="submit" class="btn primary" :disabled="isSubmitting">
              {{ isSubmitting ? 'Creating...' : 'Create puzzle' }}
            </button>
          </footer>
        </form>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  padding: var(--s-4);
  background: rgba(0, 0, 0, 0.58);
  backdrop-filter: blur(3px);
}

.modal {
  width: min(100%, 580px);
  max-height: calc(100svh - 32px);
  overflow-y: auto;
  padding: var(--s-5);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-lg);
  box-shadow: var(--sh-card);
}

.modal-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--s-4);
  margin-bottom: var(--s-5);
}

.modal-head h2 {
  font: 600 20px/1.2 var(--f-head);
  letter-spacing: -0.02em;
  color: var(--tx);
}

.modal-head p {
  margin-top: 6px;
  color: var(--tx2);
  font: 400 13px/1.5 var(--f-head);
}

.icon-btn {
  display: grid;
  flex: none;
  place-items: center;
  width: 30px;
  height: 30px;
  color: var(--tx2);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: 50%;
}

.icon-btn:hover {
  color: var(--tx);
  background: var(--s3);
}

.icon-btn svg {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
}

.form {
  display: grid;
  gap: var(--s-4);
}

.field {
  display: grid;
  gap: var(--s-2);
}

.field label {
  color: var(--tx);
  font: 500 13px var(--f-head);
}

.field input,
.field select,
.field textarea {
  width: 100%;
  padding: 10px var(--s-3);
  color: var(--tx);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: 10px;
  font: 400 14px/1.4 var(--f-head);
  outline: none;
  transition: border-color 0.2s var(--ease), box-shadow 0.2s var(--ease);
}

.field textarea {
  min-height: 140px;
  resize: vertical;
}

.field input::placeholder,
.field textarea::placeholder {
  color: var(--tx3);
}

.field input:focus,
.field select:focus,
.field textarea:focus {
  border-color: var(--ac);
  box-shadow: 0 0 0 3px var(--ac-soft);
}

.field input[aria-invalid='true'],
.field textarea[aria-invalid='true'] {
  border-color: var(--bad);
}

.field-error,
.form-error {
  color: var(--bad);
  font: 400 12px/1.4 var(--f-head);
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--s-3);
  padding-top: var(--s-2);
}

.btn {
  height: 38px;
  padding: 0 var(--s-4);
  border: 1px solid transparent;
  border-radius: 999px;
  font: 600 13px var(--f-head);
}

.btn:disabled {
  opacity: 0.55;
}

.btn.ghost {
  color: var(--tx);
  background: transparent;
  border-color: var(--line2);
}

.btn.ghost:hover:not(:disabled) {
  background: var(--s2);
}

.btn.primary {
  color: var(--ac-ink);
  background: var(--ac);
}

.btn.primary:hover:not(:disabled) {
  filter: brightness(1.08);
}

@media (max-width: 520px) {
  .modal {
    padding: var(--s-4);
  }

  .actions {
    flex-direction: column-reverse;
  }

  .btn {
    width: 100%;
  }
}
</style>
