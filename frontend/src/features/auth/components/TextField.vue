<script setup>
import { computed, ref, useId } from 'vue'

const props = defineProps({
  modelValue: { type: String, required: true },
  label: { type: String, required: true },
  type: { type: String, default: 'text' },
  autocomplete: { type: String, default: undefined },
  error: { type: String, default: '' },
  hint: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'blur'])

const inputId = useId()
const messageId = `${inputId}-message`
const inputElement = ref(null)
const isPasswordField = props.type === 'password'
const isPasswordVisible = ref(false)

const inputType = computed(() => (isPasswordField && isPasswordVisible.value ? 'text' : props.type))
const message = computed(() => props.error || props.hint)

defineExpose({ focus: () => inputElement.value?.focus() })
</script>

<template>
  <div class="field">
    <label :for="inputId">{{ label }}</label>
    <div class="control" :class="{ invalid: error }">
      <input
        :id="inputId"
        ref="inputElement"
        :type="inputType"
        :value="modelValue"
        :autocomplete="autocomplete"
        :disabled="disabled"
        :aria-invalid="error ? 'true' : undefined"
        :aria-describedby="message ? messageId : undefined"
        @input="emit('update:modelValue', $event.target.value)"
        @blur="emit('blur')"
      />
      <button
        v-if="isPasswordField"
        type="button"
        class="visibility-toggle"
        aria-label="Show password"
        :aria-pressed="isPasswordVisible"
        @click="isPasswordVisible = !isPasswordVisible"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
          <circle cx="12" cy="12" r="3" />
          <path v-if="isPasswordVisible" d="M3 3l18 18" />
        </svg>
      </button>
    </div>
    <p v-if="message" :id="messageId" class="message" :class="{ 'message-error': error }">{{ message }}</p>
  </div>
</template>

<style scoped src="./TextField.css"></style>
