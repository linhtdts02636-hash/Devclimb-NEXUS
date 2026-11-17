<script setup>
import { ref } from 'vue'
import { validateEmail } from '../validation'
import TextField from './TextField.vue'

defineProps({
  busy: { type: Boolean, default: false },
  formError: { type: String, default: '' },
  sentToEmail: { type: String, default: '' },
})

const emit = defineEmits(['submit', 'back', 'edit'])

const email = ref('')
const emailError = ref('')
const emailField = ref(null)

function handleSubmit() {
  emailError.value = validateEmail(email.value)
  if (emailError.value) {
    emailField.value?.focus()
    return
  }
  emit('submit', email.value.trim())
}
</script>

<template>
  <div class="reset-password">
    <div v-if="sentToEmail" class="sent-notice" role="status">
      <p>If an account exists for <strong>{{ sentToEmail }}</strong>, a reset link is on its way.</p>
      <p>Check your inbox and spam folder.</p>
    </div>

    <form v-else class="reset-form" novalidate @submit.prevent="handleSubmit">
      <TextField
        ref="emailField"
        v-model="email"
        label="Email"
        type="email"
        autocomplete="email"
        :error="emailError"
        @update:model-value="(emailError = ''), emit('edit')"
        @blur="emailError = validateEmail(email)"
      />
      <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>
      <button type="submit" class="btn p wide" :disabled="busy" :aria-busy="busy">
        <span v-if="busy" class="spinner" aria-hidden="true"></span>
        Send reset link
      </button>
    </form>

    <button type="button" class="btn wide" @click="emit('back')">Back to log in</button>
  </div>
</template>

<style scoped>
.reset-password,
.reset-form {
  display: flex;
  flex-direction: column;
  gap: var(--s-4);
}

.sent-notice {
  display: flex;
  flex-direction: column;
  gap: var(--s-2);
  padding: var(--s-4);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  background: var(--s2);
  font-size: var(--t-sm);
}

.form-error {
  padding: var(--s-3);
  border: 1px solid var(--bad);
  border-radius: var(--r-sm);
  font-size: var(--t-sm);
  color: var(--bad);
}
</style>
