<script setup>
import { computed, reactive, ref, useId } from 'vue'
import TextField from '../../../components/common/TextField.vue'
import ImageUploadField from './ImageUploadField.vue'

const props = defineProps({
  initialValues: { type: Object, default: () => ({}) },
  isNameLocked: { type: Boolean, default: false },
  submitLabel: { type: String, required: true },
  formId: { type: String, default: undefined },
  hideSubmit: { type: Boolean, default: false },
  busy: { type: Boolean, default: false },
  nameError: { type: String, default: '' },
  formError: { type: String, default: '' },
})

const emit = defineEmits(['submit', 'edit'])

const MINIMUM_NAME_LENGTH = 3
const MAXIMUM_NAME_LENGTH = 50
const MAXIMUM_SHOUTOUT_LENGTH = 300
const VALID_NAME = /^[a-z0-9]+(-[a-z0-9]+)*$/

const values = reactive({
  name: props.initialValues.name ?? '',
  shoutout: props.initialValues.shoutout ?? '',
  isPublic: props.initialValues.isPublic ?? true,
  imageUrl: props.initialValues.imageUrl ?? '',
  bannerUrl: props.initialValues.bannerUrl ?? '',
})
const localNameError = ref('')
const nameField = ref(null)
const shoutoutId = useId()

const nameHint = computed(() => {
  if (props.isNameLocked) return 'The name of this community cannot be changed.'
  if (props.initialValues.name) return "Renaming changes the web address. Old links will stop working."
  return "Lowercase letters, numbers and hyphens, like fpt-algo-club. Do not start or end with a hyphen. This is the web address."
})
const normalizedName = computed(() => values.name.trim().toLowerCase())
const shoutoutLength = computed(() => [...values.shoutout].length)
const isShoutoutTooLong = computed(() => shoutoutLength.value > MAXIMUM_SHOUTOUT_LENGTH)

function validateName() {
  const name = normalizedName.value
  if (!name) return 'Enter a community name.'
  if (name.length < MINIMUM_NAME_LENGTH || name.length > MAXIMUM_NAME_LENGTH || !VALID_NAME.test(name)) {
    return 'Use 3 to 50 lowercase letters, digits or hyphens. Do not start or end with a hyphen.'
  }
  return ''
}

function handleNameInput(name) {
  values.name = name
  localNameError.value = ''
  emit('edit')
}

function handleSubmit() {
  localNameError.value = props.isNameLocked ? '' : validateName()
  if (localNameError.value) {
    nameField.value?.focus()
    return
  }
  if (isShoutoutTooLong.value) return
  emit('submit', {
    name: normalizedName.value,
    shoutout: values.shoutout.trim(),
    isPublic: values.isPublic,
    imageUrl: values.imageUrl,
    bannerUrl: values.bannerUrl,
  })
}
</script>

<template>
  <form :id="formId" class="community-form" novalidate @submit.prevent="handleSubmit">
    <TextField
      ref="nameField"
      :model-value="values.name"
      label="Community name"
      autocomplete="off"
      :disabled="isNameLocked || busy"
      :hint="nameHint"
      :error="localNameError || nameError"
      @update:model-value="handleNameInput"
      @blur="localNameError = isNameLocked ? '' : validateName()"
    />

    <div class="field">
      <label :for="shoutoutId">Description</label>
      <textarea
        :id="shoutoutId"
        v-model="values.shoutout"
        rows="4"
        :disabled="busy"
        :aria-invalid="isShoutoutTooLong ? 'true' : undefined"
        @input="emit('edit')"
      ></textarea>
      <p class="counter" :class="{ over: isShoutoutTooLong }">{{ shoutoutLength }} / {{ MAXIMUM_SHOUTOUT_LENGTH }}</p>
    </div>

    <fieldset class="visibility">
      <legend>Who can see it</legend>
      <label class="option" :class="{ selected: values.isPublic }">
        <input v-model="values.isPublic" type="radio" :value="true" :disabled="busy" @change="emit('edit')" />
        <span><b>Public</b><small>Anyone signed in can see it and join.</small></span>
      </label>
      <label class="option" :class="{ selected: !values.isPublic }">
        <input v-model="values.isPublic" type="radio" :value="false" :disabled="busy" @change="emit('edit')" />
        <span><b>Private</b><small>Only members can see it. Nobody else can join yet.</small></span>
      </label>
    </fieldset>

    <h3 class="section-label">Appearance</h3>
    <ImageUploadField v-model="values.imageUrl" label="Community image" purpose="IMAGE" :disabled="busy" @update:model-value="emit('edit')" />
    <ImageUploadField v-model="values.bannerUrl" label="Banner" purpose="BANNER" :disabled="busy" @update:model-value="emit('edit')" />

    <p v-if="formError" class="form-error" role="alert">{{ formError }}</p>
    <button v-if="!hideSubmit" type="submit" class="btn p" :disabled="busy || isShoutoutTooLong" :aria-busy="busy">
      <span v-if="busy" class="spinner" aria-hidden="true"></span>
      {{ submitLabel }}
    </button>
  </form>
</template>

<style scoped>
.community-form {
  display: grid;
  gap: var(--s-5);
}

.field {
  display: grid;
  gap: var(--s-2);
}

label,
legend {
  font-size: var(--t-sm);
  font-weight: 500;
}

textarea {
  min-height: 96px;
  padding: var(--s-3);
  font: inherit;
  color: var(--tx);
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: 10px;
  resize: vertical;
  transition: border-color 0.2s var(--ease), box-shadow 0.2s var(--ease);
}

textarea:focus-visible {
  border-color: var(--ac);
  box-shadow: 0 0 0 3px var(--ac-soft);
  outline: none;
}

.counter {
  font: 400 var(--t-xs) var(--f-mono);
  color: var(--tx2);
  text-align: right;
}

.counter.over {
  color: var(--bad);
}

.visibility {
  display: grid;
  gap: var(--s-2);
  margin: 0;
  padding: 0;
  border: 0;
}

.visibility legend {
  margin-bottom: var(--s-2);
  padding: 0;
}

.option {
  display: flex;
  gap: var(--s-3);
  align-items: flex-start;
  padding: var(--s-3) var(--s-4);
  font-weight: 400;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
  cursor: pointer;
}

.option.selected {
  border-color: var(--ac);
  background: var(--ac-soft);
}

.option input {
  margin-top: 3px;
  accent-color: var(--ac);
}

.option span {
  display: grid;
}

.option small {
  color: var(--tx2);
  font-size: var(--t-sm);
}

.section-label {
  margin-top: var(--s-2);
  padding-top: var(--s-5);
  font: 400 11px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--tx2);
  border-top: 1px solid var(--line);
}

.form-error {
  font-size: var(--t-sm);
  color: var(--bad);
}
</style>
