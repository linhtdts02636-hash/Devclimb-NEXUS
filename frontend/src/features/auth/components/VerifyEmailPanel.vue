<script setup>
import { onBeforeUnmount, ref } from 'vue'

defineProps({
  email: { type: String, required: true },
  busy: { type: Boolean, default: false },
  formError: { type: String, default: '' },
  notice: { type: String, default: '' },
})

const emit = defineEmits(['confirm', 'resend', 'use-different-account'])

const RESEND_COOLDOWN_SECONDS = 30

const secondsUntilResend = ref(0)
let cooldownTimer = null

function handleResend() {
  emit('resend')
  secondsUntilResend.value = RESEND_COOLDOWN_SECONDS
  cooldownTimer = setInterval(() => {
    secondsUntilResend.value -= 1
    if (secondsUntilResend.value <= 0) {
      clearInterval(cooldownTimer)
    }
  }, 1000)
}

onBeforeUnmount(() => clearInterval(cooldownTimer))
</script>

<template>
  <div class="verify-panel">
    <p class="instructions">
      We sent a verification link to <strong>{{ email }}</strong>. Open it, then come back and continue.
    </p>

    <p v-if="notice" class="notice" role="status">{{ notice }}</p>
    <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>

    <button type="button" class="btn p wide" :disabled="busy" :aria-busy="busy" @click="emit('confirm')">
      <span v-if="busy" class="spinner" aria-hidden="true"></span>
      I have verified my email
    </button>
    <button type="button" class="btn wide" :disabled="busy || secondsUntilResend > 0" @click="handleResend">
      {{ secondsUntilResend > 0 ? `Resend email in ${secondsUntilResend}s` : 'Resend email' }}
    </button>
    <button type="button" class="text-button" @click="emit('use-different-account')">Use a different account</button>
  </div>
</template>

<style scoped src="./VerifyEmailPanel.css"></style>
