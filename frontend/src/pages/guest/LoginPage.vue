<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { LANDING_PAGE_PATH } from '../../config/applicationPaths'
import ThemeToggle from '../../components/ThemeToggle.vue'
import { describeAuthError, ErrorTarget } from '../../features/auth/authErrors'
import {
  resendVerificationEmail,
  sendPasswordReset,
  signInWithEmail,
  signInWithSocialProvider,
  signUpWithEmail,
} from '../../features/auth/authService'
import { loadRememberMePreference, saveRememberMePreference } from '../../features/auth/rememberMePreference'
import AuthShowcase from '../../features/auth/components/AuthShowcase.vue'
import CredentialsForm from '../../features/auth/components/CredentialsForm.vue'
import ResetPasswordForm from '../../features/auth/components/ResetPasswordForm.vue'
import SocialSignInButtons from '../../features/auth/components/SocialSignInButtons.vue'
import VerifyEmailPanel from '../../features/auth/components/VerifyEmailPanel.vue'
import { AuthStatus, useAuthStore } from '../../stores/authStore'

const Mode = Object.freeze({ LOGIN: 'login', SIGN_UP: 'signup', RESET_PASSWORD: 'reset' })
const TAB_MODES = [
  { mode: Mode.LOGIN, label: 'Log in' },
  { mode: Mode.SIGN_UP, label: 'Sign up' },
]

const authStore = useAuthStore()
const router = useRouter()

const mode = ref(Mode.LOGIN)
const rememberMe = ref(loadRememberMePreference())
const isBusy = ref(false)
const formError = ref('')
const notice = ref('')
const serverErrors = reactive({ email: '', password: '' })
const resetEmailSentTo = ref('')
const tabButtons = []

watch(rememberMe, saveRememberMePreference)

const needsEmailVerification = computed(() => authStore.status === AuthStatus.NEEDS_EMAIL_VERIFICATION)
const isResettingPassword = computed(() => !needsEmailVerification.value && mode.value === Mode.RESET_PASSWORD)

const heading = computed(() => {
  if (needsEmailVerification.value) return 'Verify your email'
  if (mode.value === Mode.RESET_PASSWORD) return 'Reset your password'
  return mode.value === Mode.SIGN_UP ? 'Create your account' : 'Log in to DevClimb'
})

const lead = computed(() => {
  if (needsEmailVerification.value) return ''
  if (mode.value === Mode.RESET_PASSWORD) return 'Enter your account email and we will send you a reset link.'
  return mode.value === Mode.SIGN_UP
    ? 'One account for every community you join.'
    : 'Use your email, Google or GitHub.'
})

function clearMessages() {
  formError.value = ''
  notice.value = ''
  serverErrors.email = ''
  serverErrors.password = ''
}

function showError(error) {
  const description = describeAuthError(error)
  if (!description) return
  if (description.target === ErrorTarget.FORM) {
    formError.value = description.message
    return
  }
  serverErrors[description.target] = description.message
}

async function runAuthAction(action) {
  isBusy.value = true
  clearMessages()
  try {
    await action()
  } catch (error) {
    showError(error)
  } finally {
    isBusy.value = false
  }
}

function selectMode(newMode) {
  clearMessages()
  resetEmailSentTo.value = ''
  mode.value = newMode
}

function findNextTabIndex(key, tabIndex) {
  if (key === 'Home') return 0
  if (key === 'End') return TAB_MODES.length - 1
  const step = { ArrowRight: 1, ArrowLeft: -1 }[key]
  return step ? (tabIndex + step + TAB_MODES.length) % TAB_MODES.length : null
}

function handleTabKeydown(event, tabIndex) {
  const nextIndex = findNextTabIndex(event.key, tabIndex)
  if (nextIndex === null) return
  event.preventDefault()
  selectMode(TAB_MODES[nextIndex].mode)
  tabButtons[nextIndex]?.focus()
}

async function finishSignIn() {
  await authStore.resolveSession()
  if (authStore.status === AuthStatus.AUTHENTICATED) {
    router.push(LANDING_PAGE_PATH)
  }
}

function handleCredentialsSubmit({ displayName, email, password }) {
  runAuthAction(async () => {
    if (mode.value === Mode.SIGN_UP) {
      await signUpWithEmail({ displayName, email, password, rememberMe: rememberMe.value })
      await authStore.resolveSession()
      return
    }
    await signInWithEmail({ email, password, rememberMe: rememberMe.value })
    await finishSignIn()
  })
}

function handleSocialSignIn(socialProvider) {
  runAuthAction(async () => {
    await signInWithSocialProvider({ socialProvider, rememberMe: rememberMe.value })
    await finishSignIn()
  })
}

function handleResetSubmit(email) {
  runAuthAction(async () => {
    await sendPasswordReset(email)
    resetEmailSentTo.value = email
  })
}

function handleVerificationConfirm() {
  runAuthAction(async () => {
    const isVerified = await authStore.confirmEmailVerified()
    if (!isVerified) {
      formError.value = 'We cannot see a verified email yet. Open the link in the email, then try again.'
      return
    }
    router.push(LANDING_PAGE_PATH)
  })
}

function handleVerificationResend() {
  runAuthAction(async () => {
    await resendVerificationEmail()
    notice.value = 'Verification email sent again. It can take a minute to arrive.'
  })
}

async function handleUseDifferentAccount() {
  clearMessages()
  await authStore.logout()
  selectMode(Mode.LOGIN)
}
</script>

<template>
  <div class="auth-page">
    <nav class="top-bar">
      <div class="top-bar-inner">
        <RouterLink class="logo" :to="LANDING_PAGE_PATH">
          <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 20 10 7l4 7 3-4 4 10z" /></svg>
          DevClimb
          <small>nexus</small>
        </RouterLink>
        <div class="top-bar-actions">
          <RouterLink class="btn back-link" :to="LANDING_PAGE_PATH">Back to home</RouterLink>
          <ThemeToggle />
        </div>
      </div>
    </nav>

    <main class="frame">
      <span class="plus left" aria-hidden="true">+</span>
      <span class="plus right" aria-hidden="true">+</span>

      <section class="form-side" aria-labelledby="auth-heading">
        <div class="form-column">
          <div class="intro">
            <h1 id="auth-heading">{{ heading }}</h1>
            <p v-if="lead" class="lead">{{ lead }}</p>
          </div>

          <VerifyEmailPanel
            v-if="needsEmailVerification"
            :email="authStore.pendingVerificationEmail"
            :busy="isBusy"
            :form-error="formError"
            :notice="notice"
            @confirm="handleVerificationConfirm"
            @resend="handleVerificationResend"
            @use-different-account="handleUseDifferentAccount"
          />

          <ResetPasswordForm
            v-else-if="isResettingPassword"
            :busy="isBusy"
            :form-error="formError"
            :sent-to-email="resetEmailSentTo"
            @submit="handleResetSubmit"
            @edit="clearMessages"
            @back="selectMode(Mode.LOGIN)"
          />

          <template v-else>
            <div class="tabs" role="tablist" aria-label="Log in or sign up">
              <button
                v-for="(tab, tabIndex) in TAB_MODES"
                :id="`tab-${tab.mode}`"
                :key="tab.mode"
                :ref="(element) => (tabButtons[tabIndex] = element)"
                type="button"
                role="tab"
                class="tab"
                :aria-selected="mode === tab.mode"
                aria-controls="auth-panel"
                :tabindex="mode === tab.mode ? 0 : -1"
                @click="selectMode(tab.mode)"
                @keydown="handleTabKeydown($event, tabIndex)"
              >
                {{ tab.label }}
              </button>
            </div>

            <div id="auth-panel" class="panel" role="tabpanel" :aria-labelledby="`tab-${mode}`">
              <CredentialsForm
                v-model:remember-me="rememberMe"
                :mode="mode"
                :busy="isBusy"
                :form-error="formError"
                :server-errors="serverErrors"
                @submit="handleCredentialsSubmit"
                @forgot-password="selectMode(Mode.RESET_PASSWORD)"
                @edit="clearMessages"
              />

              <div class="divider" role="separator"><span>or continue with</span></div>

              <SocialSignInButtons :disabled="isBusy" @select="handleSocialSignIn" />
            </div>
          </template>
        </div>
      </section>

      <AuthShowcase class="visual-side" />
    </main>
  </div>
</template>

<style scoped src="./LoginPage.css"></style>
