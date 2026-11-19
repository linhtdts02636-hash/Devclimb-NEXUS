<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { EDITOR_LANGUAGES, buildStarter, createCodeEditor, getEditorLanguage } from '../../features/editor/codeEditor'
import { fetchPuzzle } from '../../features/puzzle/puzzleApi'
import { getBackendLanguageId } from '../../features/submission/submissionApi'
import { usePuzzleSubmission } from '../../features/submission/usePuzzleSubmission'

const editorHost = ref(null)
const selectedLanguage = ref('python')
const cursorLine = ref(1)
const cursorColumn = ref(1)
const runHint = 'Ctrl+Enter to submit'

let editor = null
let activeLanguage = 'python'
// Gi��_ code riA�ng cho t���ng ngA'n ng��_ khi �`��i qua l���i.
const languageDrafts = new Map()
const puzzle = ref(null)

// Only show languages that this puzzle actually supports.
const submissionLanguages = computed(() => {
  const allowedIds = new Set((puzzle.value?.languages ?? []).map((language) => language.languageId))
  return EDITOR_LANGUAGES.filter((language) => allowedIds.has(getBackendLanguageId(language.id)))
})
const selectedLanguageInfo = computed(() => getEditorLanguage(selectedLanguage.value))
const fileTag = computed(() => selectedLanguageInfo.value.fileName)
const puzzleLoading = ref(true)
const puzzleError = ref('')
const difficultyLabel = computed(() => {
  const difficulty = puzzle.value?.difficulty
  if (!difficulty) return ''
  return difficulty.charAt(0) + difficulty.slice(1).toLowerCase()
})
const difficultyTone = computed(() => puzzle.value?.difficulty?.toLowerCase() ?? '')
const functionHeaders = computed(() => {
  const headers = new Map()
  for (const language of puzzle.value?.languages ?? []) {
    if (language.languageName) {
      headers.set(language.languageName.toLowerCase(), language.functionHeader)
    }
  }
  return headers
})
const {
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
} = usePuzzleSubmission({
  getCode: () => editor?.getValue() ?? '',
  getLanguageId: () => getBackendLanguageId(selectedLanguage.value),
})

function handleUpdate({ line, column }) {
  cursorLine.value = line
  cursorColumn.value = column
}

function starterFor(languageId) {
  return buildStarter(languageId, functionHeaders.value.get(languageId))
}

function handleLanguageChange(event) {
  if (!editor) return

  const nextLanguage = event.target.value
  if (!submissionLanguages.value.some((language) => language.id === nextLanguage)) {
    event.target.value = selectedLanguage.value
    return
  }

  selectedLanguage.value = nextLanguage
  languageDrafts.set(activeLanguage, editor.getValue())
  activeLanguage = selectedLanguage.value
  editor.setLanguage(activeLanguage)
  editor.setValue(languageDrafts.get(activeLanguage) ?? starterFor(activeLanguage))
  nextTick(() => editor?.focus())
}

function resetCode() {
  if (!editor) return
  editor.setValue(starterFor(selectedLanguage.value))
  editor.focus()
}

// Bỏ qua phản hồi cũ nếu người dùng đổi puzzle trước khi request trước hoàn tất.
let puzzleRequest = 0
async function loadPuzzle() {
  if (!puzzleId.value) {
    puzzle.value = null
    puzzleError.value = 'This puzzle could not be identified.'
    puzzleLoading.value = false
    return
  }
  if (!(await authReady)) {
    puzzleLoading.value = false
    return
  }

  const token = ++puzzleRequest
  puzzleLoading.value = true
  puzzleError.value = ''
  try {
    const data = await fetchPuzzle(puzzleId.value)
    if (token !== puzzleRequest) return
    puzzle.value = data
    const nextLanguage = submissionLanguages.value.some((language) => language.id === selectedLanguage.value)
      ? selectedLanguage.value
      : submissionLanguages.value[0]?.id
    if (nextLanguage) {
      selectedLanguage.value = nextLanguage
      activeLanguage = nextLanguage
      editor?.setLanguage(nextLanguage)
    }
    languageDrafts.clear()
    editor?.setValue(starterFor(selectedLanguage.value))
  } catch (error) {
    if (token !== puzzleRequest) return
    puzzle.value = null
    puzzleError.value = error?.response?.data?.message || 'Could not load this puzzle.'
  } finally {
    if (token === puzzleRequest) puzzleLoading.value = false
  }
}

watch(puzzleId, loadPuzzle, { immediate: true })

onMounted(() => {
  editor = createCodeEditor({
    parent: editorHost.value,
    language: selectedLanguage.value,
    doc: starterFor(selectedLanguage.value),
    onRun: submit,
    onUpdate: handleUpdate,
  })
})

onBeforeUnmount(() => {
  editor?.destroy()
  editor = null
})
</script>

<template>
  <div class="solve-page">
    <UserNavbar />

    <div class="solve-frame">
      <main class="workspace">
      <section class="panel problem-panel" aria-label="Problem description">
        <header class="panel-head problem-head">
          <nav class="tabs" aria-label="Problem sections">
            <button class="tab active" type="button">Description</button>
            <button class="tab" type="button">Discussions</button>
            <button class="tab" type="button">Submissions</button>
          </nav>
        </header>

        <div class="panel-scroll problem-scroll">
          <div v-if="puzzleLoading" class="problem-state">Loading puzzle...</div>
          <div v-else-if="puzzleError" class="problem-state problem-error">{{ puzzleError }}</div>

          <template v-else-if="puzzle">
            <div class="problem-title-row">
              <h1>{{ puzzle.id }}. {{ puzzle.title || 'Untitled puzzle' }}</h1>
              <span class="difficulty" :class="difficultyTone">{{ difficultyLabel }}</span>
            </div>

            <hr class="rule" />

            <div class="statement">
              <p>{{ puzzle.content }}</p>
            </div>
          </template>
        </div>
      </section>

      <section class="panel editor-panel" aria-label="Code editor">
        <header class="panel-head editor-head">
          <div class="editor-label">
            <span class="editor-title">Editor</span>
            <label class="language-picker">
              <select
                :value="selectedLanguage"
                aria-label="Editor language"
                @change="handleLanguageChange"
              >
                <option v-for="language in submissionLanguages" :key="language.id" :value="language.id">
                  {{ language.label }}
                </option>
              </select>
              <svg class="select-arrow" viewBox="0 0 24 24" aria-hidden="true">
                <path d="m7 10 5 5 5-5" />
              </svg>
            </label>
            <span class="file-tag">{{ fileTag }}</span>
          </div>
          <div class="editor-controls">
            <button class="icon-btn" type="button" aria-label="Reset code" title="Reset code" @click="resetCode">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
            </button>
            <span class="font-size">13px</span>
          </div>
        </header>

        <div ref="editorHost" class="editor-scroll" aria-label="Code editor"></div>

        <footer class="editor-status">
          <span>Ln {{ cursorLine }}, Col {{ cursorColumn }}</span>
          <div>
            <span class="hide-on-small">{{ runHint }}</span>
          </div>
        </footer>
      </section>

      <section class="panel tests-panel" aria-label="Test cases" :aria-busy="isSubmitting">
        <header class="panel-head tests-head">
          <span>Testcases</span>
          <span v-if="testRows.length" class="tests-count">{{ testRows.length }}</span>
        </header>

        <div class="panel-scroll tests-scroll">
          <div v-if="!testRows.length" class="tests-empty">
            <p>{{ isSubmitting ? 'Running testcases...' : 'Submit your solution to run the testcases.' }}</p>
          </div>

          <div v-else class="test-list">
            <div v-for="test in testRows" :key="test.index" class="test-row">
              <div class="test-name">
                <span class="dot" :class="test.passed ? 'easy' : 'bad'"></span>
                <span>{{ test.label }}</span>
              </div>
              <span class="test-time">{{ test.timeLabel }}</span>
            </div>
          </div>

          <div v-if="resultDetails" class="result-output">
            <span>Details</span>
            <pre>{{ resultDetails }}</pre>
          </div>

          <div class="tests-summary">
            <div
              v-if="testRows.length"
              class="test-progress"
              :style="progressStyle"
              aria-hidden="true"
            >
              <span v-for="test in testRows" :key="test.index" :class="test.passed ? 'pass' : 'fail'"></span>
            </div>
            <div class="tests-result">
              <span :class="verdictTone">{{ summaryLabel }}</span>
              <span>{{ runtimeLabel }}</span>
            </div>
          </div>
        </div>
      </section>
    </main>

    <footer class="verdict-panel" aria-label="Submission verdict">
      <div class="verdict-left">
        <div class="verdict-chip" :class="verdictTone">
          <span class="dot" :class="verdictTone === 'accepted' ? 'easy' : verdictTone === 'failed' ? 'bad' : 'idle'"></span>
          <span>{{ verdictLabel }}</span>
        </div>
        <div v-if="submission" class="verdict-metrics">
          <span>Runtime: <strong>{{ runtimeLabel }}</strong></span>
          <span class="metric-dot">•</span>
          <span>Memory: <strong>{{ memoryLabel }}</strong></span>
          <span class="metric-dot">•</span>
          <span>Tests: <strong>{{ submission.testPassed }} / {{ submission.testCount }} passed</strong></span>
        </div>
      </div>

      <div class="verdict-actions">
        <button
          class="primary-btn"
          type="button"
          :disabled="isSubmitting || !puzzle"
          :aria-busy="isSubmitting"
          @click="submit"
        >
          <span v-if="isSubmitting" class="spinner"></span>
          <span>{{ isSubmitting ? 'Submitting...' : 'Submit Solution' }}</span>
        </button>
      </div>
    </footer>
    </div>
  </div>
</template>

<style scoped>
.solve-page {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  height: 100dvh;
  max-width: 100vw;
  max-height: 100vh;
  overflow: hidden;
  background: var(--bg);
  color: var(--tx);
  user-select: none;
}

.solve-frame {
  display: flex;
  flex: 1;
  flex-direction: column;
  width: 100%;
  max-width: var(--rail);
  min-height: 0;
  margin: 0 auto;
  overflow: hidden;
  background: var(--bg);
  border-inline: 1px solid var(--line);
}

.workspace {
  display: grid;
  flex: 1;
  grid-template-columns: 4fr 5fr 3fr;
  gap: var(--s-3);
  min-height: 0;
  padding: var(--s-5) var(--s-6) var(--s-4);
  overflow: hidden;
  background: var(--bg);
}

.panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.panel-head {
  flex: none;
  background: var(--s1);
  border-bottom: 1px solid var(--line2);
}

.panel-scroll,
.editor-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-color: var(--line2) transparent;
  scrollbar-width: thin;
  user-select: text;
}

.panel-scroll::-webkit-scrollbar,
.editor-scroll::-webkit-scrollbar {
  width: 5px;
  height: 5px;
}

.panel-scroll::-webkit-scrollbar-thumb,
.editor-scroll::-webkit-scrollbar-thumb {
  background: var(--line2);
  border-radius: 999px;
}

.problem-head {
  padding: var(--s-2) var(--s-3) 0;
}

.tabs {
  display: flex;
  gap: var(--s-4);
  overflow-x: auto;
  scrollbar-width: none;
}

.tabs::-webkit-scrollbar {
  display: none;
}

.tab {
  flex: none;
  padding: 0 0 var(--s-2);
  color: var(--tx2);
  border-bottom: 2px solid transparent;
  font: 500 var(--t-sm) var(--f-head);
  white-space: nowrap;
}

.tab:hover {
  color: var(--tx);
}

.tab.active {
  color: var(--tx);
  border-bottom-color: var(--ac);
  font-weight: 600;
}

.tab-count {
  color: var(--tx3);
  font: 400 10px var(--f-mono);
}

.problem-scroll {
  padding: var(--s-5);
  font-size: var(--t-sm);
  line-height: 1.65;
}

.problem-title-row {
  display: flex;
  align-items: center;
  gap: var(--s-2);
}

.problem-title-row h1 {
  font: 600 var(--t-lg)/1.3 var(--f-head);
  letter-spacing: -0.02em;
}

.difficulty {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border: 1px solid transparent;
  border-radius: 999px;
  font: 500 11px/1.4 var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.difficulty.easy {
  color: var(--ok);
  background: color-mix(in srgb, var(--ok) 12%, transparent);
  border-color: color-mix(in srgb, var(--ok) 25%, transparent);
}

.difficulty.medium {
  color: var(--ac);
  background: var(--ac-soft);
  border-color: color-mix(in srgb, var(--ac) 25%, transparent);
}

.difficulty.hard {
  color: var(--warn);
  background: color-mix(in srgb, var(--warn) 12%, transparent);
  border-color: color-mix(in srgb, var(--warn) 25%, transparent);
}

.problem-state {
  display: grid;
  place-items: center;
  min-height: 180px;
  padding: var(--s-4);
  color: var(--tx2);
  font: 400 11px/1.6 var(--f-mono);
  text-align: center;
}

.problem-error {
  color: var(--bad);
}

.problem-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: var(--s-3);
}

.problem-tags span {
  padding: 2px 6px;
  color: var(--tx2);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: 999px;
  font: 400 10px var(--f-mono);
}

.rule {
  margin: var(--s-4) 0;
  border: 0;
  border-top: 1px solid var(--line);
}

.statement {
  display: grid;
  gap: var(--s-2);
}

.statement p {
  white-space: pre-wrap;
}

.statement code {
  padding: 1px 4px;
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: 4px;
  font: 400 12px var(--f-mono);
}

.muted {
  color: var(--tx2);
  font-size: 12px;
}

.examples {
  display: grid;
  gap: var(--s-3);
  margin-top: var(--s-4);
  font: 400 11px/1.55 var(--f-mono);
}

.example {
  padding: 10px;
  background: color-mix(in srgb, var(--s2) 60%, transparent);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
}

.example-label {
  display: block;
  margin-bottom: 4px;
  color: var(--tx);
  font: 600 12px var(--f-head);
}

.example-key {
  color: var(--tx2);
}

.example-note {
  margin-top: 3px;
  color: var(--tx2);
  font-size: 10px;
}

.constraints {
  margin-top: var(--s-4);
  font-size: 12px;
}

.constraints-title {
  font-weight: 600;
}

.constraints ul {
  margin: var(--s-2) 0 0;
  padding-left: var(--s-4);
  color: var(--tx2);
  font: 400 11px/1.7 var(--f-mono);
}

.sample-note {
  margin-top: var(--s-5);
  padding-top: var(--s-3);
  border-top: 1px solid var(--line);
  color: var(--tx3);
  font: 400 10px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.editor-head,
.tests-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
  height: 38px;
  padding: 0 var(--s-3);
}

.editor-label {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  min-width: 0;
}

.editor-title,
.tests-head > span:first-child {
  color: var(--tx2);
  font: 600 10px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  white-space: nowrap;
}

.language-picker {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.language-picker select {
  height: 26px;
  padding: 0 26px 0 var(--s-2);
  color: var(--tx);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  font: 500 var(--t-xs) var(--f-head);
  appearance: none;
  cursor: pointer;
}

.language-picker select:hover {
  background: var(--s3);
}

.select-arrow {
  position: absolute;
  right: 6px;
  width: 14px;
  height: 14px;
  fill: none;
  stroke: var(--tx2);
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
  pointer-events: none;
}

.file-tag {
  padding: 2px 6px;
  color: var(--tx);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: 4px;
  font: 400 10px var(--f-mono);
  white-space: nowrap;
}

.editor-controls {
  display: flex;
  align-items: center;
  gap: var(--s-1);
  color: var(--tx2);
}

.icon-btn {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border-radius: 5px;
}

.icon-btn:hover {
  color: var(--tx);
  background: var(--s2);
}

.icon-btn svg,
.secondary-btn svg {
  width: 14px;
  height: 14px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.font-size {
  padding: 0 4px;
  font: 400 10px var(--f-mono);
}

.editor-scroll {
  overflow: hidden;
  background: var(--code-bg);
  color: var(--code-tx);
}

.editor-scroll :deep(.cm-editor) {
  height: 100%;
}

.editor-status {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
  height: 28px;
  padding: 0 var(--s-3);
  color: var(--tx2);
  background: var(--s1);
  border-top: 1px solid var(--line2);
  font: 400 10px var(--f-mono);
}

.editor-status > div {
  display: flex;
  gap: var(--s-3);
}

.tests-head {
  height: 40px;
}

.tests-head > span:first-child {
  color: var(--tx2);
}

.run-again-btn {
  height: 30px;
  padding: 0 var(--s-3);
  color: var(--ac-ink);
  background: var(--ac);
  border-radius: var(--r-sm);
  font: 600 var(--t-xs) var(--f-head);
}

.run-again-btn:hover {
  filter: brightness(1.08);
}

.tests-count {
  color: var(--tx3);
  font: 400 10px var(--f-mono);
}

.tests-scroll {
  display: flex;
  flex-direction: column;
  padding: var(--s-3);
}

.tests-empty {
  display: grid;
  place-items: center;
  min-height: 120px;
  padding: var(--s-4);
  color: var(--tx2);
  border: 1px dashed var(--line2);
  border-radius: var(--r-sm);
  font: 400 11px/1.6 var(--f-mono);
  text-align: center;
}

.result-output {
  display: grid;
  gap: 6px;
  margin-top: var(--s-3);
}

.result-output > span {
  color: var(--tx2);
  font: 600 10px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.result-output pre {
  max-height: 180px;
  margin: 0;
  padding: 10px;
  overflow: auto;
  color: var(--tx);
  background: var(--code-bg);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  font: 400 10px/1.6 var(--f-mono);
  white-space: pre-wrap;
  word-break: break-word;
}

.test-list {
  display: grid;
  width:100%;
  gap: var(--s-2);
  grid-template-columns: minmax(0, 1fr);
}

.test-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width:100%;
  gap: var(--s-2);
  padding: 10px;
  background: color-mix(in srgb, var(--s2) 70%, transparent);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  font: 400 11px var(--f-mono);
}

.test-row:hover {
  background: var(--s2);
}

.test-name {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  min-width: 0;
}

.test-name > span:first-child {
  flex-shrink: 0;
}

.test-name > span:last-child {
    flex: 1; 
    min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}




.dot {
  display: inline-block;
  flex: none;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot.easy {
  background: var(--ok);
}

.dot.bad {
  background: var(--bad);
}

.dot.idle {
  background: var(--tx3);
}

.test-time {
  flex: none;
  color: var(--tx2);
  font-size: 10px;
}

.tests-summary {
  margin-top: auto;
  padding-top: var(--s-4);
  border-top: 1px solid var(--line);
}

.test-progress {
  display: grid;
  grid-template-columns: repeat(var(--test-count, 5), 1fr);
  gap: 4px;
  margin-top: var(--s-4);
}

.test-progress span {
  height: 4px;
  background: var(--line2);
  border-radius: 999px;
}

.test-progress span.pass {
  background: var(--ok);
}

.test-progress span.fail {
  background: var(--bad);
}

.tests-result {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  color: var(--tx2);
  font: 400 11px var(--f-mono);
}

.tests-result > span:first-child {
  color: var(--tx2);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.tests-result > span:first-child.accepted {
  color: var(--ok);
}

.tests-result > span:first-child.failed {
  color: var(--bad);
}

.tests-result > span:first-child.idle {
  font-weight: 500;
  letter-spacing: 0;
  text-transform: none;
}

.verdict-panel {
  position: sticky;
  bottom: 0;
  z-index: 20;
  display: flex;
  flex: none;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-4);
  height: 56px;
  padding: 0 var(--s-4);
  background: var(--s1);
  border-top: 1px solid var(--line2);
}

.verdict-left {
  display: flex;
  align-items: center;
  gap: var(--s-4);
  min-width: 0;
}

.verdict-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--s-2);
  padding: 4px 12px;
  color: var(--tx2);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: 999px;
  font: 600 11px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  white-space: nowrap;
}

.verdict-chip.accepted {
  color: var(--ok);
  background: color-mix(in srgb, var(--ok) 12%, transparent);
  border-color: color-mix(in srgb, var(--ok) 30%, transparent);
}

.verdict-chip.failed {
  color: var(--bad);
  background: color-mix(in srgb, var(--bad) 12%, transparent);
  border-color: color-mix(in srgb, var(--bad) 30%, transparent);
}

.verdict-chip.idle {
  text-transform: none;
  letter-spacing: 0;
}

.verdict-metrics {
  display: flex;
  align-items: center;
  gap: var(--s-3);
  min-width: 0;
  color: var(--tx2);
  font: 400 11px var(--f-mono);
  white-space: nowrap;
}

.verdict-metrics strong {
  color: var(--tx);
  font-weight: 600;
}

.verdict-metrics em {
  color: var(--ok);
  font-style: normal;
  font-size: 10px;
}

.metric-dot {
  color: var(--tx3);
}

.verdict-actions {
  display: flex;
  flex: none;
  align-items: center;
  gap: var(--s-2);
}

.secondary-btn,
.primary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--s-2);
  height: 36px;
  padding: 0 var(--s-4);
  border: 1px solid transparent;
  border-radius: var(--r-sm);
  font: 500 var(--t-sm) var(--f-head);
  white-space: nowrap;
}

.secondary-btn {
  color: var(--tx);
  background: var(--s1);
  border-color: var(--line2);
}

.secondary-btn:hover {
  background: var(--s2);
}

.primary-btn {
  color: var(--ac-ink);
  background: var(--ac);
  font-weight: 600;
}

.primary-btn:hover:not(:disabled) {
  filter: brightness(1.08);
}

.primary-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.next-arrow {
  font-size: 14px;
  line-height: 1;
}

@media (max-width: 980px) {
  .verdict-metrics {
    display: none;
  }

  .hide-on-small {
    display: none;
  }
}

@media (max-width: 760px) {
  .workspace {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 1fr 1fr;
  }

  .problem-panel {
    grid-row: span 2;
  }

  .editor-panel,
  .tests-panel {
    min-height: 0;
  }

  .verdict-panel {
    height: auto;
    min-height: 56px;
    padding: var(--s-2) var(--s-3);
  }

  .verdict-actions {
    gap: 4px;
  }

  .secondary-btn,
  .primary-btn {
    padding: 0 10px;
  }
}
</style>
