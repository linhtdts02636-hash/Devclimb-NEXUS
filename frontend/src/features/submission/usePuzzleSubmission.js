import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/authStore'
import { submitSolution } from './submissionApi'

export function usePuzzleSubmission({ getCode, getLanguageId }) {
  const route = useRoute()
  const router = useRouter()
  const authStore = useAuthStore()
  const authReady = authStore.initialize().then(() => authStore.status === 'authenticated')
  const submission = ref(null)
  const submitError = ref('')
  const isSubmitting = ref(false)

  const puzzleId = computed(() => {
    const parsed = Number(route.params.id)
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null
  })
  // Backend trả lỗi riêng; đánh index để ghép lại với danh sách test đã chạy.
  const failedTests = computed(() => {
    const byIndex = new Map()
    for (const failed of submission.value?.failed ?? []) {
      byIndex.set(failed.index, failed)
    }
    return byIndex
  })
  const testRows = computed(() =>
    (submission.value?.tests ?? []).map((test) => {
      const failed = failedTests.value.get(test.index)
      return {
        ...test,
        label: failed ? `case ${test.index} · ${failed.input}` : `case ${test.index} · ${test.status}`,
        timeLabel: formatSeconds(test.time),
      }
    }),
  )
  const verdictTone = computed(() => {
    if (!submission.value) return 'idle'
    return submission.value.passed ? 'accepted' : 'failed'
  })
  const verdictLabel = computed(() => submission.value?.message ?? 'Ready to submit')
  const summaryLabel = computed(() => {
    if (!submission.value) return 'Submit to run testcases'
    return `${submission.value.message} · ${submission.value.testPassed}/${submission.value.testCount} tests`
  })
  const runtimeLabel = computed(() => formatSeconds(submission.value?.time ?? 0))
  const memoryLabel = computed(() => formatMemory(submission.value?.memory ?? 0))
  const progressStyle = computed(() => ({
    '--test-count': Math.max(testRows.value.length, 1),
  }))
  const resultDetails = computed(() => {
    const sections = []
    if (submitError.value) {
      sections.push(submitError.value)
    }
    const failed = submission.value?.failed?.[0]
    if (failed) {
      sections.push(`Input:\n${failed.input}\n\nExpected:\n${failed.expected}\n\nGot:\n${failed.got || '(no output)'}`)
    }
    if (submission.value?.compileOutput) {
      sections.push(`Compiler output:\n${submission.value.compileOutput}`)
    }
    if (submission.value?.stderr) {
      sections.push(`Stderr:\n${submission.value.stderr}`)
    }
    return sections.join('\n\n')
  })

  // Judge0 trả thời gian dạng chuỗi giây, giao diện hiển thị mili giây.
  function formatSeconds(value) {
    const seconds = Number(value)
    if (!Number.isFinite(seconds) || seconds <= 0) return '—'
    const milliseconds = seconds * 1_000
    return `${milliseconds < 10 ? milliseconds.toFixed(1) : Math.round(milliseconds)} ms`
  }

  function formatMemory(value) {
    const kilobytes = Number(value)
    if (!Number.isFinite(kilobytes) || kilobytes <= 0) return '—'
    return `${(kilobytes / 1_024).toFixed(1)} MB`
  }

  async function submit() {
    if (isSubmitting.value) return
    if (!puzzleId.value) {
      submitError.value = 'This puzzle could not be identified.'
      return
    }

    const languageId = getLanguageId()
    if (languageId == null) {
      submitError.value = 'This language is not supported for submissions yet.'
      return
    }

    isSubmitting.value = true
    submitError.value = ''
    submission.value = null

    try {
      submission.value = await submitSolution({
        puzzleId: puzzleId.value,
        languageId,
        code: getCode(),
      })
    } catch (error) {
      submitError.value = error?.response?.data?.message || 'Could not submit your solution. Try again.'
      if (error?.response?.status === 401) {
        router.push('/login')
      }
    } finally {
      isSubmitting.value = false
    }
  }

  onMounted(async () => {
    if (!(await authReady)) {
      router.push('/login')
    }
  })

  return {
    authReady,
    puzzleId,
    submission,
    isSubmitting,
    testRows,
    verdictTone,
    verdictLabel,
    summaryLabel,
    runtimeLabel,
    memoryLabel,
    progressStyle,
    resultDetails,
    submit,
  }
}
