<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { EDITOR_LANGUAGES, createCodeEditor, getEditorLanguage } from '../../features/editor/codeEditor'

const editorHost = ref(null)
const selectedLanguage = ref('python')
const cursorLine = ref(1)
const cursorColumn = ref(1)
const runHint = ref('Ctrl+Enter to run')

let editor = null
let runTimer = 0
let activeLanguage = 'python'
const languageDrafts = new Map()

const selectedLanguageInfo = computed(() => getEditorLanguage(selectedLanguage.value))
const fileTag = computed(() => selectedLanguageInfo.value.fileName)

function handleUpdate({ line, column }) {
  cursorLine.value = line
  cursorColumn.value = column
}

function handleRun() {
  runHint.value = 'Run requested'
  clearTimeout(runTimer)
  runTimer = window.setTimeout(() => {
    runHint.value = 'Ctrl+Enter to run'
  }, 1400)
}

function handleLanguageChange(event) {
  if (!editor) return

  selectedLanguage.value = event.target.value
  languageDrafts.set(activeLanguage, editor.getValue())
  activeLanguage = selectedLanguage.value
  editor.setLanguage(activeLanguage)
  editor.setValue(languageDrafts.get(activeLanguage) ?? selectedLanguageInfo.value.starter)
  nextTick(() => editor?.focus())
}

function resetCode() {
  if (!editor) return
  editor.setValue(selectedLanguageInfo.value.starter)
  editor.focus()
}

onMounted(() => {
  editor = createCodeEditor({
    parent: editorHost.value,
    language: selectedLanguage.value,
    doc: selectedLanguageInfo.value.starter,
    onRun: handleRun,
    onUpdate: handleUpdate,
  })
})

onBeforeUnmount(() => {
  clearTimeout(runTimer)
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
          <div class="problem-title-row">
            <h1>1. Two Sum</h1>
            <span class="difficulty easy">Easy</span>
          </div>

          <div class="problem-tags">
            <span>Array</span>
            <span>Hash Table</span>
            <span>Time O(N)</span>
            <span>Space O(N)</span>
          </div>

          <hr class="rule" />

          <div class="statement">
            <p>
              Given an array of integers <code>nums</code> and an integer <code>target</code>, return
              <em>indices of the two numbers such that they add up to <code>target</code></em>.
            </p>
            <p class="muted">
              You may assume that each input would have <strong>exactly one solution</strong>, and you may not use
              the same element twice. You can return the answer in any order.
            </p>
          </div>

          <div class="examples">
            <div class="example">
              <span class="example-label">Example 1:</span>
              <p><span class="example-key">Input:</span> nums = [2,7,11,15], target = 9</p>
              <p><span class="example-key">Output:</span> [0,1]</p>
              <p class="example-note">Explanation: Because nums[0] + nums[1] == 9, we return [0, 1].</p>
            </div>
            <div class="example">
              <span class="example-label">Example 2:</span>
              <p><span class="example-key">Input:</span> nums = [3,2,4], target = 6</p>
              <p><span class="example-key">Output:</span> [1,2]</p>
            </div>
            <div class="example">
              <span class="example-label">Example 3:</span>
              <p><span class="example-key">Input:</span> nums = [3,3], target = 6</p>
              <p><span class="example-key">Output:</span> [0,1]</p>
            </div>
          </div>

          <div class="constraints">
            <p class="constraints-title">Constraints:</p>
            <ul>
              <li>2 ≤ nums.length ≤ 10<sup>4</sup></li>
              <li>-10<sup>9</sup> ≤ nums[i] ≤ 10<sup>9</sup></li>
              <li>-10<sup>9</sup> ≤ target ≤ 10<sup>9</sup></li>
              <li>Only one valid answer exists.</li>
            </ul>
          </div>

          <p class="sample-note">Sample data for illustration</p>
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
                <option v-for="language in EDITOR_LANGUAGES" :key="language.id" :value="language.id">
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
            <button class="icon-btn" type="button" aria-label="Format code" title="Format code">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
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
            <span>Tab = 4 spaces</span>
          </div>
        </footer>
      </section>

      <section class="panel tests-panel" aria-label="Test cases">
        <header class="panel-head tests-head">
          <span>Testcases</span>
        </header>

        <div class="panel-scroll tests-scroll">
          <div class="test-list">
            <div class="test-row">
              <div class="test-name"><span class="dot easy"></span><span>case 1 · [2,7,11,15] → [0,1]</span></div>
              <span class="test-time">8 ms</span>
            </div>
            <div class="test-row">
              <div class="test-name"><span class="dot easy"></span><span>case 2 · [3,2,4] → [1,2]</span></div>
              <span class="test-time">9 ms</span>
            </div>
            <div class="test-row">
              <div class="test-name"><span class="dot easy"></span><span>case 3 · [3,3] → [0,1]</span></div>
              <span class="test-time">10 ms</span>
            </div>
            <div class="test-row">
              <div class="test-name"><span class="dot easy"></span><span>case 4 · negatives</span></div>
              <span class="test-time">11 ms</span>
            </div>
            <div class="test-row">
              <div class="test-name"><span class="dot easy"></span><span>case 5 · n = 10,000</span></div>
              <span class="test-time">12 ms</span>
            </div>
          </div>

          <div class="tests-summary">
            <div class="test-progress" aria-hidden="true">
              <span></span>
              <span></span>
              <span></span>
              <span></span>
              <span></span>
            </div>
            <div class="tests-result">
              <span>Accepted 5/5 tests</span>
              <span>12 ms</span>
            </div>
          </div>
        </div>
      </section>
    </main>

    <footer class="verdict-panel" aria-label="Submission verdict">
      <div class="verdict-left">
        <div class="verdict-chip">
          <span class="dot easy"></span>
          <span>Accepted</span>
        </div>
        <div class="verdict-metrics">
          <span>Runtime: <strong>12 ms</strong> </span>
          <span class="metric-dot">•</span>
          <span>Memory: <strong>17.4 MB</strong> </span>
          <span class="metric-dot">•</span>
          <span>Tests: <strong>5 / 5 passed</strong></span>
        </div>
      </div>

      <div class="verdict-actions">
        <button class="primary-btn" type="button">Submit Solution</button>
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

.tests-scroll {
  display: flex;
  flex-direction: column;
  padding: var(--s-3);
}

.test-list {
  display: grid;
  gap: var(--s-2);
}

.test-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.test-name > span:last-child {
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
  grid-template-columns: repeat(5, 1fr);
  gap: 4px;
  margin-top: var(--s-4);
}

.test-progress span {
  height: 4px;
  background: var(--ok);
  border-radius: 999px;
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
  color: var(--ok);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
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
  color: var(--ok);
  background: color-mix(in srgb, var(--ok) 12%, transparent);
  border: 1px solid color-mix(in srgb, var(--ok) 30%, transparent);
  border-radius: 999px;
  font: 600 11px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  white-space: nowrap;
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

.primary-btn:hover {
  filter: brightness(1.08);
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
