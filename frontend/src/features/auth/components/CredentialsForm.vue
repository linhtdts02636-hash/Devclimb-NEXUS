<script setup>
import { computed, reactive, watch } from 'vue'
import {
  validateDisplayName,
  validateEmail,
  validateExistingPassword,
  validateNewPassword,
  validatePasswordConfirmation,
} from '../validation'
import TextField from './TextField.vue'

const props = defineProps({
  mode: { type: String, required: true },
  rememberMe: { type: Boolean, required: true },
  busy: { type: Boolean, default: false },
  formError: { type: String, default: '' },
  serverErrors: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['submit', 'forgot-password', 'update:rememberMe', 'edit'])

const FIELD_ORDER = ['displayName', 'email', 'password', 'passwordConfirmation']

// Each tab keeps its own values, so a stray click on the other tab neither loses nor leaks what was typed.
const fieldsByMode = {
  login: reactive({ displayName: '', email: '', password: '', passwordConfirmation: '' }),
  signup: reactive({ displayName: '', email: '', password: '', passwordConfirmation: '' }),
}
const validationErrors = reactive({ displayName: '', email: '', password: '', passwordConfirmation: '' })
const fieldComponents = {}

const isSignUp = computed(() => props.mode === 'signup')
const fields = computed(() => fieldsByMode[props.mode])

const validators = {
  displayName: () => (isSignUp.value ? validateDisplayName(fields.value.displayName) : ''),
  email: () => validateEmail(fields.value.email),
  password: () => (isSignUp.value ? validateNewPassword(fields.value.password) : validateExistingPassword(fields.value.password)),
  passwordConfirmation: () =>
    isSignUp.value ? validatePasswordConfirmation(fields.value.password, fields.value.passwordConfirmation) : '',
}

function errorFor(fieldName) {
  return validationErrors[fieldName] || props.serverErrors[fieldName] || ''
}

function validateField(fieldName) {
  validationErrors[fieldName] = validators[fieldName]()
}

function updateField(fieldName, value) {
  fields.value[fieldName] = value
  validationErrors[fieldName] = ''
  emit('edit')
}

function handleSubmit() {
  FIELD_ORDER.forEach(validateField)
  const firstInvalidField = FIELD_ORDER.find((fieldName) => validationErrors[fieldName])
  if (firstInvalidField) {
    fieldComponents[firstInvalidField]?.focus()
    return
  }
  emit('submit', {
    displayName: fields.value.displayName.trim(),
    email: fields.value.email.trim(),
    password: fields.value.password,
  })
}

watch(
  () => props.mode,
  () => {
    FIELD_ORDER.forEach((fieldName) => (validationErrors[fieldName] = ''))
  },
)
</script>

<template>
  <form class="credentials-form" novalidate @submit.prevent="handleSubmit">
    <TextField
      v-if="isSignUp"
      :ref="(component) => (fieldComponents.displayName = component)"
      :model-value="fields.displayName"
      label="Display name"
      autocomplete="nickname"
      :error="errorFor('displayName')"
      @update:model-value="updateField('displayName', $event)"
      @blur="validateField('displayName')"
    />
    <TextField
      :ref="(component) => (fieldComponents.email = component)"
      :model-value="fields.email"
      label="Email"
      type="email"
      :autocomplete="isSignUp ? 'off' : 'email'"
      :error="errorFor('email')"
      @update:model-value="updateField('email', $event)"
      @blur="validateField('email')"
    />
    <TextField
      :ref="(component) => (fieldComponents.password = component)"
      :model-value="fields.password"
      label="Password"
      type="password"
      :autocomplete="isSignUp ? 'new-password' : 'current-password'"
      :hint="isSignUp ? 'Use at least 8 characters.' : ''"
      :error="errorFor('password')"
      @update:model-value="updateField('password', $event)"
      @blur="validateField('password')"
    />
    <TextField
      v-if="isSignUp"
      :ref="(component) => (fieldComponents.passwordConfirmation = component)"
      :model-value="fields.passwordConfirmation"
      label="Confirm password"
      type="password"
      autocomplete="new-password"
      :error="errorFor('passwordConfirmation')"
      @update:model-value="updateField('passwordConfirmation', $event)"
      @blur="validateField('passwordConfirmation')"
    />

    <div v-if="!isSignUp" class="options">
      <label class="remember-me">
        <input
          type="checkbox"
          :checked="rememberMe"
          @change="emit('update:rememberMe', $event.target.checked)"
        />
        Remember me
      </label>
      <button type="button" class="text-button" @click="emit('forgot-password')">
        Forgot password?
      </button>
    </div>

    <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>

    <button type="submit" class="btn p wide" :disabled="busy" :aria-busy="busy">
      <span v-if="busy" class="spinner" aria-hidden="true"></span>
      {{ isSignUp ? 'Create account' : 'Log in' }}
    </button>
  </form>
</template>

<style scoped src="./CredentialsForm.css"></style>
