<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Avatar from '../../components/common/Avatar.vue'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { describeApiError } from '../../config/apiClient'
import { fetchCommunity } from '../../features/community/communityApi'
import { createCodeEditor, getEditorLanguage } from '../../features/editor/codeEditor'
import { DEFAULT_FUNCTION_HEADERS } from '../../features/editor/defaultHeaders'
import { createPuzzle, fetchTags, invalidateTags } from '../../features/puzzle/puzzleApi'
import { getBackendLanguageId } from '../../features/submission/submissionApi'

const CREATE_LANGUAGE_IDS = ['python', 'java', 'javascript']
const MAX_TAGS = 20
const DIFFICULTY_OPTIONS = [
  { value: 'EASY', label: 'Easy', tone: 'easy' },
  { value: 'MEDIUM', label: 'Medium', tone: 'medium' },
  { value: 'HARD', label: 'Hard', tone: 'hard' },
]
const route = useRoute()
const router = useRouter()
const contestId = computed(() => {
  const raw = Number(route.query.contestId)
  return Number.isInteger(raw) && raw > 0 ? raw : null
})
const contestScore = ref(100)
const community = ref(null)
const communityStatus = ref('loading') // loading | ready | blocked | missing | failed
const communityError = ref('')
const form = reactive({
  title: '',
  difficulty: 'MEDIUM',
  content: '',
})
const errors = reactive({
  title: '',
  content: '',
})
const selectedTags = ref([])
const availableTags = ref([])
const tagsLoading = ref(false)
const tagsError = ref('')
const tagQuery = ref('')
const tagError = ref('')
const selectedLanguages = ref(['python'])
const headers = reactive({ ...DEFAULT_FUNCTION_HEADERS })
const headerErrors = ref({})
const languageError = ref('')
const testcases = ref([{ id: 0, input: '', output: '' }])
const testErrors = ref({})

const openMenu = ref(null)
const tagMenu = ref(null)
const languageMenu = ref(null)
const difficultyMenu = ref(null)
const tagSearchInput = ref(null)
const testcaseList = ref(null)

const headerHosts = new Map()
const headerEditors = new Map()
const headerRefs = new Map()
let testcaseSequence = 1

const languageOptions = CREATE_LANGUAGE_IDS.map((languageId) => getEditorLanguage(languageId))
const difficultyTone = computed(() => form.difficulty.toLowerCase())
const difficultyLabel = computed(
  () => DIFFICULTY_OPTIONS.find((option) => option.value === form.difficulty)?.label ?? 'Medium',
)
const normalizedTagQuery = computed(() => normalizeTag(tagQuery.value))
const filteredTags = computed(() => {
  const query = tagQuery.value.trim().toLowerCase()
  if (!query) return availableTags.value
  return availableTags.value.filter((tag) => tag.name.toLowerCase().includes(query))
})
const canCreateTag = computed(() => {
  const name = normalizedTagQuery.value
  if (!name || name.length > 50 || selectedTags.value.length >= MAX_TAGS) return false
  return !availableTags.value.some((tag) => tag.name.toLowerCase() === name)
})
const createTone = ref('idle')
const createLabel = ref('Draft')
const createMessage = ref('Fill in the puzzle details, then create.')
const isCreating = ref(false)

// Tải catalog tag kèm số puzzle. Cache ở puzzleApi giúp mở lại trang không
// phải gọi API liên tục.
async function loadTags() {
  tagsLoading.value = true
  tagsError.value = ''
  try {
    availableTags.value = await fetchTags()
  } catch (error) {
    tagsError.value = describeApiError(error).message
    availableTags.value = []
  } finally {
    tagsLoading.value = false
  }
}

async function loadCommunity() {
  communityStatus.value = 'loading'
  communityError.value = ''
  try {
    const loaded = await fetchCommunity(route.params.name)
    community.value = loaded
    if (!loaded.isMember) {
      // Backend vẫn kiểm tra lại quyền này. Ẩn form ở client để người không
      // phải member không mất thời gian điền một draft không thể gửi.
      communityStatus.value = 'blocked'
      communityError.value = `Only members of c/${loaded.name} can create puzzles. Join the community first.`
      return
    }
    communityStatus.value = 'ready'
  } catch (error) {
    const { code, message } = describeApiError(error)
    communityStatus.value = code === 'COMMUNITY_NOT_FOUND' ? 'missing' : 'failed'
    communityError.value = message
    if (error?.response?.status === 401) {
      router.push('/login')
    }
  }
}

function languageInfo(languageId) {
  return getEditorLanguage(languageId)
}

function normalizeTag(value) {
  return value
    .trim()
    .toLowerCase()
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
}

function resetStatus() {
  if (isCreating.value || createTone.value === 'idle') return
  createTone.value = 'idle'
  createLabel.value = 'Draft'
  createMessage.value = 'Fill in the puzzle details, then create.'
}

function toggleTagMenu() {
  if (openMenu.value === 'tags') {
    closeTagMenu()
    return
  }
  openMenu.value = 'tags'
  tagError.value = ''
  nextTick(() => tagSearchInput.value?.focus())
}

function closeTagMenu() {
  openMenu.value = null
  tagQuery.value = ''
  tagError.value = ''
}

function toggleLanguageMenu() {
  openMenu.value = openMenu.value === 'languages' ? null : 'languages'
}

function toggleDifficultyMenu() {
  openMenu.value = openMenu.value === 'difficulty' ? null : 'difficulty'
}

function chooseDifficulty(value) {
  form.difficulty = value
  openMenu.value = null
}

function handleDocumentPointer(event) {
  if (openMenu.value === 'tags' && tagMenu.value && !tagMenu.value.contains(event.target)) {
    closeTagMenu()
    return
  }
  if (
    openMenu.value === 'difficulty' &&
    difficultyMenu.value &&
    !difficultyMenu.value.contains(event.target)
  ) {
    openMenu.value = null
    return
  }
  if (openMenu.value === 'languages' && languageMenu.value && !languageMenu.value.contains(event.target)) {
    openMenu.value = null
  }
}

function handleDocumentKeydown(event) {
  if (event.key !== 'Escape') return
  if (openMenu.value === 'tags') {
    closeTagMenu()
  } else {
    openMenu.value = null
  }
}

function clearTagError() {
  tagError.value = ''
}

function selectTag(tag) {
  if (selectedTags.value.includes(tag)) return
  if (selectedTags.value.length >= MAX_TAGS) {
    tagError.value = `You can select up to ${MAX_TAGS} tags.`
    return
  }
  selectedTags.value = [...selectedTags.value, tag]
  tagError.value = ''
}

function toggleTag(tag) {
  if (selectedTags.value.includes(tag)) {
    removeTag(tag)
    return
  }
  selectTag(tag)
}

function removeTag(tag) {
  selectedTags.value = selectedTags.value.filter((selected) => selected !== tag)
  tagError.value = ''
}

function submitTagQuery() {
  const name = normalizedTagQuery.value
  if (!name) return
  if (name.length > 50) {
    tagError.value = 'Tags can be at most 50 characters.'
    return
  }

  const existing = availableTags.value.find((tag) => tag.name.toLowerCase() === name)
  if (existing) {
    selectTag(existing.name)
    tagQuery.value = ''
    tagError.value = ''
    return
  }

  if (selectedTags.value.length >= MAX_TAGS) {
    tagError.value = `You can select up to ${MAX_TAGS} tags.`
    return
  }

  // Tag mới chưa có puzzle nào dùng, count tạm là 0 cho tới lần tải lại catalog.
  availableTags.value = [...availableTags.value, { name, puzzleCount: 0 }]
  selectedTags.value = [...selectedTags.value, name]
  tagQuery.value = ''
  tagError.value = ''
}

function toggleLanguage(languageId) {
  if (selectedLanguages.value.includes(languageId)) {
    removeLanguage(languageId)
    return
  }
  selectedLanguages.value = [...selectedLanguages.value, languageId]
  languageError.value = ''
}

function removeLanguage(languageId) {
  const editor = headerEditors.get(languageId)
  if (editor) {
    headers[languageId] = editor.getValue()
    editor.destroy()
    headerEditors.delete(languageId)
  }
  headerHosts.delete(languageId)
  selectedLanguages.value = selectedLanguages.value.filter((selected) => selected !== languageId)
  languageError.value = ''

  if (headerErrors.value[languageId]) {
    const nextErrors = { ...headerErrors.value }
    delete nextErrors[languageId]
    headerErrors.value = nextErrors
  }
}

function mountHeaderEditor(languageId) {
  if (headerEditors.has(languageId)) return
  const host = headerHosts.get(languageId)
  if (!host) return

  const editor = createCodeEditor({
    parent: host,
    language: languageId,
    doc: headers[languageId] ?? DEFAULT_FUNCTION_HEADERS[languageId] ?? '',
  })
  headerEditors.set(languageId, editor)
}

function getHeaderRef(languageId) {
  if (!headerRefs.has(languageId)) {
    headerRefs.set(languageId, (element) => {
      if (!element) {
        const editor = headerEditors.get(languageId)
        if (editor) {
          headers[languageId] = editor.getValue()
          editor.destroy()
          headerEditors.delete(languageId)
        }
        headerHosts.delete(languageId)
        return
      }

      headerHosts.set(languageId, element)
      nextTick(() => mountHeaderEditor(languageId))
    })
  }
  return headerRefs.get(languageId)
}

function syncHeaderEditors() {
  for (const [languageId, editor] of headerEditors) {
    headers[languageId] = editor.getValue()
  }
}

function clearHeaderError(languageId) {
  if (!headerErrors.value[languageId]) return
  const nextErrors = { ...headerErrors.value }
  delete nextErrors[languageId]
  headerErrors.value = nextErrors
}

function handleHeaderFocus(languageId) {
  clearHeaderError(languageId)
  resetStatus()
}

function addTestcase() {
  const id = testcaseSequence++
  testcases.value.push({ id, input: '', output: '' })
  nextTick(() => {
    testcaseList.value?.lastElementChild?.scrollIntoView({ block: 'nearest' })
    document.getElementById(`test-input-${id}`)?.focus()
  })
}

function removeTestcase(id) {
  if (testcases.value.length <= 1) return
  testcases.value = testcases.value.filter((test) => test.id !== id)
  const nextErrors = { ...testErrors.value }
  delete nextErrors[id]
  testErrors.value = nextErrors
}

function clearTestError(id, field) {
  const current = testErrors.value[id]
  if (!current?.[field]) return
  const nextErrors = {
    ...testErrors.value,
    [id]: { ...current, [field]: false },
  }
  if (!nextErrors[id].input && !nextErrors[id].output) {
    delete nextErrors[id]
  }
  testErrors.value = nextErrors
}

function validateTestcases() {
  const nextErrors = {}
  for (const test of testcases.value) {
    const inputMissing = !test.input.trim()
    const outputMissing = !test.output.trim()
    if (inputMissing || outputMissing) {
      nextErrors[test.id] = { input: inputMissing, output: outputMissing }
    }
  }
  testErrors.value = nextErrors
  return Object.keys(nextErrors).length === 0
}

async function handleCreate() {
  if (isCreating.value || communityStatus.value !== 'ready' || !community.value) return

  syncHeaderEditors()

  const titleMissing = !form.title.trim()
  const contentMissing = !form.content.trim()
  const languagesMissing = selectedLanguages.value.length === 0
  const nextHeaderErrors = {}
  for (const languageId of selectedLanguages.value) {
    if (!(headers[languageId] ?? '').trim()) {
      nextHeaderErrors[languageId] = true
    }
  }

  const testsValid = validateTestcases()
  errors.title = titleMissing ? 'Title is required.' : ''
  errors.content = contentMissing ? 'Problem statement is required.' : ''
  languageError.value = languagesMissing ? 'Select at least one language.' : ''
  headerErrors.value = nextHeaderErrors

  const valid =
    !titleMissing &&
    !contentMissing &&
    !languagesMissing &&
    Object.keys(nextHeaderErrors).length === 0 &&
    testsValid

  if (!valid) {
    createTone.value = 'failed'
    createLabel.value = 'Incomplete'
    createMessage.value = 'Complete the highlighted fields before creating.'
    return
  }

  isCreating.value = true
  createTone.value = 'idle'
  createLabel.value = 'Creating'
  createMessage.value = 'Checking function headers with Judge0 and saving the puzzle...'

  try {
    const created = await createPuzzle({
      title: form.title.trim(),
      difficulty: form.difficulty,
      content: form.content.trim(),
      communityId: community.value.id,
      tags: selectedTags.value,
      // Editor dùng ID dạng chuỗi; API cần numeric language row trong DB.
      languages: selectedLanguages.value.map((languageId) => ({
        languageId: getBackendLanguageId(languageId),
        functionHeader: (headers[languageId] ?? '').trim(),
      })),
      // Giữ nguyên khoảng trắng của testcase đúng như tác giả nhập.
      testcases: testcases.value.map((testcase) => ({
        input: testcase.input,
        output: testcase.output,
      })),
      ...(contestId.value
        ? {
            contestId: contestId.value,
            contestScore: Math.max(1, Math.trunc(Number(contestScore.value) || 100)),
          }
        : {}),
    })

    createTone.value = 'accepted'
    createLabel.value = 'Created'
    createMessage.value = contestId.value
      ? 'Puzzle created and added to contest. Returning...'
      : 'Puzzle created. Opening it now...'
    // Count của tag vừa dùng đã thay đổi, lần mở trang tạo tiếp theo phải tải mới.
    invalidateTags()
    if (contestId.value) {
      router.push({
        name: 'contest-edit',
        params: { name: route.params.name, contestId: contestId.value },
      })
    } else {
      router.push({ name: 'puzzle-solve', params: { id: created.id } })
    }
  } catch (error) {
    const { message } = describeApiError(error)
    createTone.value = 'failed'
    createLabel.value = 'Failed'
    createMessage.value = message
    if (error?.response?.status === 401) {
      router.push('/login')
    }
  } finally {
    isCreating.value = false
  }
}

watch(
  [() => form.title, () => form.content, () => form.difficulty, selectedTags, selectedLanguages, testcases],
  resetStatus,
  { deep: true },
)

onMounted(() => {
  document.addEventListener('pointerdown', handleDocumentPointer)
  document.addEventListener('keydown', handleDocumentKeydown)
  loadCommunity()
  loadTags()
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', handleDocumentPointer)
  document.removeEventListener('keydown', handleDocumentKeydown)
  for (const editor of headerEditors.values()) {
    editor.destroy()
  }
  headerEditors.clear()
  headerHosts.clear()
  headerRefs.clear()
})
</script>

<template>
  <div class="create-page">
    <UserNavbar />

    <div class="create-frame">
      <div v-if="communityStatus === 'loading'" class="create-state" aria-busy="true">
        <p>Loading community...</p>
      </div>

      <div v-else-if="communityStatus !== 'ready'" class="create-state" role="alert">
        <h1>
          {{
            communityStatus === 'missing'
              ? 'Community not found'
              : communityStatus === 'blocked'
                ? 'Members only'
                : 'Could not load this community'
          }}
        </h1>
        <p>{{ communityError }}</p>
        <RouterLink
          class="secondary-btn"
          :to="community ? `/community/${community.name}` : '/communities'"
        >
          {{ community ? 'Back to community' : 'Back to communities' }}
        </RouterLink>
      </div>

      <template v-else>
      <main class="workspace">
        <section class="panel details-panel" aria-label="Puzzle details">
          <header class="panel-head panel-headline">
            <div class="draft-context">
              <span class="draft-label">Drafting a puzzle in</span>
              <Avatar
                :src="community.imageUrl"
                :name="community.name"
                type="community"
                :size="20"
              />
            </div>

            <label v-if="contestId" class="score-control">
              <span class="score-control-label">Score</span>
              <input
                v-model.number="contestScore"
                type="number"
                min="1"
                step="10"
                class="score-control-input"
                aria-label="Contest puzzle score"
              />
            </label>
          </header>

          <div class="panel-scroll form-scroll">
            <div class="field">
              <div class="title-row">
                <input
                  id="puzzle-title"
                  v-model="form.title"
                  class="title-input"
                  type="text"
                  maxlength="200"
                  autocomplete="off"
                  placeholder="Edit Distance"
                  aria-label="Puzzle title"
                  :aria-invalid="errors.title ? 'true' : undefined"
                  @input="errors.title = ''"
                />
                <span ref="difficultyMenu" class="difficulty" :class="difficultyTone">
                  <button
                    class="difficulty-trigger"
                    type="button"
                    aria-haspopup="listbox"
                    :aria-expanded="openMenu === 'difficulty'"
                    @click="toggleDifficultyMenu"
                  >
                    {{ difficultyLabel }}
                    <svg class="difficulty-arrow" viewBox="0 0 24 24" aria-hidden="true">
                      <path d="m7 10 5 5 5-5" />
                    </svg>
                  </button>

                  <div
                    v-if="openMenu === 'difficulty'"
                    class="menu difficulty-menu"
                    role="listbox"
                    aria-label="Puzzle difficulty"
                  >
                    <button
                      v-for="option in DIFFICULTY_OPTIONS"
                      :key="option.value"
                      class="difficulty-option"
                      :class="{ selected: form.difficulty === option.value }"
                      type="button"
                      role="option"
                      :aria-selected="form.difficulty === option.value"
                      @click="chooseDifficulty(option.value)"
                    >
                      <span class="difficulty-dot" :class="option.tone"></span>
                      <span>{{ option.label }}</span>
                      <svg
                        v-if="form.difficulty === option.value"
                        class="difficulty-check"
                        viewBox="0 0 24 24"
                        aria-hidden="true"
                      >
                        <path d="m5 12 4 4L19 6" />
                      </svg>
                    </button>
                  </div>
                </span>
              </div>
              <p v-if="errors.title" class="field-error">{{ errors.title }}</p>
            </div>

            <div class="field">
              <div class="field-head">
                <span class="field-label">Tags</span>
                <span class="field-meta">{{ selectedTags.length }} / {{ MAX_TAGS }}</span>
              </div>

              <div ref="tagMenu" class="tag-control">
                <div v-if="selectedTags.length" class="tag-selected">
                  <span v-for="tag in selectedTags" :key="tag" class="tag-chip">
                    <span>{{ tag }}</span>
                    <button type="button" :aria-label="`Remove ${tag} tag`" @click="removeTag(tag)">
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="m6 6 12 12M18 6 6 18" />
                      </svg>
                    </button>
                  </span>
                </div>
                <p v-else class="field-hint">No tags selected.</p>

                <button
                  class="add-control"
                  type="button"
                  :aria-expanded="openMenu === 'tags'"
                  @click="toggleTagMenu"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M12 5v14M5 12h14" />
                  </svg>
                  <span>Add tags</span>
                </button>

                <div v-if="openMenu === 'tags'" class="menu tag-menu">
                  <input
                    ref="tagSearchInput"
                    v-model="tagQuery"
                    type="text"
                    autocomplete="off"
                    spellcheck="false"
                    placeholder="Search or create a tag..."
                    aria-label="Search or create a tag"
                    @input="clearTagError"
                    @keydown.enter.prevent="submitTagQuery"
                  />

                  <div class="menu-list">
                    <p v-if="tagsLoading" class="menu-empty">Loading tags...</p>

                    <button
                      v-for="tag in filteredTags"
                      :key="tag.name"
                      class="menu-item"
                      :class="{ selected: selectedTags.includes(tag.name) }"
                      type="button"
                      @click="toggleTag(tag.name)"
                    >
                      <span class="check">
                        <svg v-if="selectedTags.includes(tag.name)" viewBox="0 0 24 24" aria-hidden="true">
                          <path d="m5 12 4 4L19 6" />
                        </svg>
                      </span>
                      <span class="tag-name">{{ tag.name }}</span>
                      <span class="tag-count">
                        {{ tag.puzzleCount }} {{ tag.puzzleCount === 1 ? 'puzzle' : 'puzzles' }}
                      </span>
                    </button>

                    <button
                      v-if="canCreateTag"
                      class="menu-item create-tag"
                      type="button"
                      @click="submitTagQuery"
                    >
                      <span class="check">
                        <svg viewBox="0 0 24 24" aria-hidden="true">
                          <path d="M12 5v14M5 12h14" />
                        </svg>
                      </span>
                      <span>Create "{{ normalizedTagQuery }}"</span>
                    </button>

                    <p v-if="tagsError" class="menu-empty">{{ tagsError }}</p>
                    <p
                      v-else-if="!tagsLoading && !filteredTags.length && !canCreateTag"
                      class="menu-empty"
                    >
                      No matching tags.
                    </p>
                  </div>

                  <p v-if="tagError" class="field-error menu-error">{{ tagError }}</p>
                </div>
              </div>
            </div>

            <div class="field">
              <label for="puzzle-content">Problem statement</label>
              <textarea
                id="puzzle-content"
                v-model="form.content"
                class="statement-input"
                rows="12"
                placeholder="Describe the input, output, and constraints..."
                :aria-invalid="errors.content ? 'true' : undefined"
                @input="errors.content = ''"
              ></textarea>
              <p v-if="errors.content" class="field-error">{{ errors.content }}</p>
            </div>
          </div>
        </section>

        <section class="panel headers-panel" aria-label="Function headers">
          <header class="panel-head editor-head">
            <span class="panel-kicker">Function headers</span>

            <div ref="languageMenu" class="language-control">
              <button
                class="add-control compact"
                type="button"
                :aria-expanded="openMenu === 'languages'"
                @click="toggleLanguageMenu"
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M12 5v14M5 12h14" />
                </svg>
                <span>Languages</span>
                <span class="count-chip">{{ selectedLanguages.length }}</span>
              </button>

              <div v-if="openMenu === 'languages'" class="menu language-menu">
                <button
                  v-for="language in languageOptions"
                  :key="language.id"
                  class="menu-item"
                  :class="{ selected: selectedLanguages.includes(language.id) }"
                  type="button"
                  @click="toggleLanguage(language.id)"
                >
                  <span class="check">
                    <svg v-if="selectedLanguages.includes(language.id)" viewBox="0 0 24 24" aria-hidden="true">
                      <path d="m5 12 4 4L19 6" />
                    </svg>
                  </span>
                  <span>{{ language.label }}</span>
                </button>
              </div>
            </div>
          </header>

          <div class="panel-scroll header-scroll">
            <p v-if="languageError" class="field-error panel-error">{{ languageError }}</p>

            <div v-if="!selectedLanguages.length" class="empty-state">
              No languages selected. Use the plus button above to add Python, Java, or JavaScript.
            </div>

            <div v-else class="header-list">
              <article
                v-for="languageId in selectedLanguages"
                :key="languageId"
                class="header-card"
                :class="{ invalid: headerErrors[languageId] }"
                @focusin="handleHeaderFocus(languageId)"
              >
                <header class="header-card-head">
                  <span class="header-lang">{{ languageInfo(languageId).label }}</span>
                  <span class="file-tag">{{ languageInfo(languageId).fileName }}</span>
                  <button
                    class="icon-btn"
                    type="button"
                    :aria-label="`Remove ${languageInfo(languageId).label}`"
                    @click="removeLanguage(languageId)"
                  >
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                      <path d="m6 6 12 12M18 6 6 18" />
                    </svg>
                  </button>
                </header>

                <div
                  :ref="getHeaderRef(languageId)"
                  class="header-editor"
                  :aria-label="`${languageInfo(languageId).label} function header`"
                  :aria-invalid="headerErrors[languageId] ? 'true' : undefined"
                ></div>

                <p v-if="headerErrors[languageId]" class="field-error header-error">
                  Function header is required.
                </p>
              </article>
            </div>
          </div>
        </section>

        <section class="panel tests-panel" aria-label="Testcases">
          <header class="panel-head tests-head">
            <div class="tests-heading">
              <span class="panel-kicker">Testcases</span>
              <span class="tests-count">{{ testcases.length }}</span>
            </div>
            <button class="add-control compact" type="button" @click="addTestcase">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M12 5v14M5 12h14" />
              </svg>
              <span>Add testcase</span>
            </button>
          </header>

          <div class="panel-scroll tests-scroll">
            <div ref="testcaseList" class="test-list">
              <article v-for="(test, index) in testcases" :key="test.id" class="testcard">
                <header class="testcard-head">
                  <span class="test-index">Test {{ index + 1 }}</span>
                  <button
                    class="icon-btn"
                    type="button"
                    :disabled="testcases.length === 1"
                    :aria-label="`Remove test ${index + 1}`"
                    @click="removeTestcase(test.id)"
                  >
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                      <path d="m6 6 12 12M18 6 6 18" />
                    </svg>
                  </button>
                </header>

                <label class="test-label" :for="`test-input-${test.id}`">Input</label>
                <textarea
                  :id="`test-input-${test.id}`"
                  v-model="test.input"
                  class="test-field"
                  :class="{ invalid: testErrors[test.id]?.input }"
                  rows="3"
                  spellcheck="false"
                  placeholder="Test input"
                  :aria-invalid="testErrors[test.id]?.input ? 'true' : undefined"
                  @input="clearTestError(test.id, 'input')"
                ></textarea>
                <p v-if="testErrors[test.id]?.input" class="field-error">Input is required.</p>

                <label class="test-label" :for="`test-output-${test.id}`">Output</label>
                <textarea
                  :id="`test-output-${test.id}`"
                  v-model="test.output"
                  class="test-field"
                  :class="{ invalid: testErrors[test.id]?.output }"
                  rows="2"
                  spellcheck="false"
                  placeholder="Expected output"
                  :aria-invalid="testErrors[test.id]?.output ? 'true' : undefined"
                  @input="clearTestError(test.id, 'output')"
                ></textarea>
                <p v-if="testErrors[test.id]?.output" class="field-error">Output is required.</p>
              </article>
            </div>
          </div>
        </section>
      </main>

      <footer class="create-panel" aria-live="polite">
        <div class="create-status">
          <div class="status-chip" :class="createTone">
            <span
              class="dot"
              :class="createTone === 'accepted' ? 'easy' : createTone === 'failed' ? 'bad' : 'idle'"
            ></span>
            <span>{{ createLabel }}</span>
          </div>
          <div class="status-copy">
            <span>{{ createMessage }}</span>
            <span class="status-metrics">
              Languages {{ selectedLanguages.length }} · Tags {{ selectedTags.length }} · Tests
              {{ testcases.length }}
            </span>
          </div>
        </div>

        <div class="create-actions">
          <RouterLink class="secondary-btn" :to="`/community/${community.name}`">Cancel</RouterLink>
          <button class="primary-btn" type="button" :disabled="isCreating" @click="handleCreate">
            {{ isCreating ? 'Creating...' : 'Create Puzzle' }}
          </button>
        </div>
      </footer>
      </template>
    </div>
  </div>
</template>

<style scoped>
.create-page {
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

.create-state {
  display: grid;
  flex: 1;
  place-content: center;
  gap: var(--s-3);
  padding: var(--s-6);
  text-align: center;
}

.create-state h1 {
  margin: 0;
  font: 600 20px/1.25 var(--f-head);
}

.create-state p {
  max-width: 420px;
  margin: 0;
  color: var(--tx2);
}

.create-state .secondary-btn {
  justify-self: center;
}

.create-frame {
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
  grid-template-columns: minmax(0, 4fr) minmax(0, 5fr) minmax(0, 3fr);
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

.panel-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-color: var(--line2) transparent;
  scrollbar-width: thin;
  user-select: text;
}

.panel-scroll::-webkit-scrollbar {
  width: 5px;
  height: 5px;
}

.panel-scroll::-webkit-scrollbar-thumb {
  background: var(--line2);
  border-radius: 999px;
}

.panel-headline,
.editor-head,
.tests-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-3);
  height: 38px;
  padding: 0 var(--s-3);
}

.panel-kicker {
  color: var(--tx2);
  font: 500 10px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  white-space: nowrap;
}

.draft-context {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  min-width: 0;
}

.draft-label {
  flex: none;
  color: var(--tx2);
  font: 500 12px var(--f-head);
  white-space: nowrap;
}

.score-control {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 8px;
  color: var(--tx);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  font: 500 11px var(--f-head);
}

.score-control:focus-within {
  border-color: var(--ac);
}

.score-control-label {
  color: var(--tx2);
  font: 500 11px/1 var(--f-head);
  white-space: nowrap;
}

.score-control-input {
  width: 56px;
  padding: 0;
  border: 0;
  outline: none;
  background: transparent;
  color: var(--tx);
  font: 500 12px/1 var(--f-mono);
  font-variant-numeric: tabular-nums;
  text-align: right;
}

.form-scroll {
  display: grid;
  gap: var(--s-4);
  align-content: start;
  padding: var(--s-5);
  font-size: var(--t-sm);
}

.field {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.field label,
.field-label {
  color: var(--tx);
  font: 500 12px var(--f-head);
}

.field-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-2);
}

.field-meta {
  color: var(--tx3);
  font: 400 10px var(--f-mono);
}

.field input:not(.title-input),
.field textarea {
  width: 100%;
  padding: 10px var(--s-3);
  color: var(--tx);
  background: var(--s2);
  border: 0;
  border-radius: 8px;
  font: 400 13px/1.5 var(--f-head);
  outline: none;
  transition: background 0.2s var(--ease);
}

.field textarea {
  resize: vertical;
}

.field input:not(.title-input)::placeholder,
.field textarea::placeholder {
  color: var(--tx3);
}

.field input:not(.title-input):focus,
.field textarea:focus {
  background: var(--s3);
}

.field input[aria-invalid='true'],
.field textarea[aria-invalid='true'] {
  background: color-mix(in srgb, var(--bad) 10%, var(--s2));
}

.title-row {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  min-width: 0;
}

.field input.title-input {
  flex: 1;
  width: auto;
  min-width: 0;
  height: 30px;
  padding: 0;
  color: var(--tx);
  background: transparent;
  border: 0;
  border-radius: 0;
  font: 600 var(--t-lg)/1.3 var(--f-head);
  letter-spacing: -0.02em;
  outline: none;
}

.field input.title-input::placeholder {
  color: var(--tx3);
}

.field input.title-input:focus {
  background: transparent;
}

.field input.title-input[aria-invalid='true'] {
  background: transparent;
}

.statement-input {
  min-height: 220px;
  line-height: 1.65;
}

.field-error {
  margin: 0;
  color: var(--bad);
  font: 400 11px/1.4 var(--f-head);
}

.field-hint {
  margin: 0;
  color: var(--tx3);
  font: 400 11px var(--f-head);
}

.difficulty {
  position: relative;
  display: inline-flex;
  flex: none;
  align-items: center;
  padding: 2px 3px;
  border: 1px solid transparent;
  border-radius: 999px;
  font: 500 11px/1.4 var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.difficulty:focus-within {
  box-shadow: 0 0 0 2px var(--ac-soft);
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

.difficulty-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 82px;
  height: 20px;
  padding: 0 18px;
  color: inherit;
  font: inherit;
  text-align: center;
  text-transform: inherit;
  letter-spacing: inherit;
  outline: none;
}

.difficulty-arrow {
  position: absolute;
  top: 50%;
  right: 7px;
  width: 12px;
  height: 12px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
  transform: translateY(-50%);
  pointer-events: none;
}

.difficulty-menu {
  top: calc(100% + 8px);
  right: 0;
  left: auto;
  width: 150px;
  padding: 4px;
  font: 400 12px var(--f-head);
  text-transform: none;
  letter-spacing: normal;
}

.difficulty-option {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  height: 30px;
  padding: 0 8px;
  color: var(--tx);
  border-radius: 5px;
  font: 400 12px var(--f-head);
  text-align: left;
  text-transform: none;
  letter-spacing: normal;
}

.difficulty-option:hover {
  background: var(--s2);
}

.difficulty-option.selected {
  color: var(--ac);
  background: var(--ac-soft);
}

.difficulty-dot {
  flex: none;
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.difficulty-dot.easy {
  background: var(--ok);
}

.difficulty-dot.medium {
  background: var(--ac);
}

.difficulty-dot.hard {
  background: var(--warn);
}

.difficulty-check {
  width: 13px;
  height: 13px;
  margin-left: auto;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.tag-control,
.language-control {
  position: relative;
  display: grid;
  gap: var(--s-2);
  min-width: 0;
}

.tag-selected {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 4px 3px 8px;
  color: var(--tx2);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: 999px;
  font: 400 11px var(--f-head);
  white-space: nowrap;
}

.tag-chip > button {
  display: grid;
  place-items: center;
  width: 16px;
  height: 16px;
  color: var(--tx2);
  border-radius: 50%;
}

.tag-chip > button:hover {
  color: var(--bad);
  background: color-mix(in srgb, var(--bad) 10%, transparent);
}

.tag-chip svg {
  width: 10px;
  height: 10px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
}

.add-control {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: max-content;
  height: 30px;
  padding: 0 10px;
  color: var(--tx);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  font: 500 12px var(--f-head);
  white-space: nowrap;
  transition: background 0.15s var(--ease);
}

.add-control:hover {
  background: var(--s3);
}

.add-control.compact {
  height: 28px;
  padding: 0 8px;
  font-size: 11px;
}

.add-control svg {
  width: 14px;
  height: 14px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.count-chip {
  display: inline-grid;
  place-items: center;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  color: var(--ac);
  background: var(--ac-soft);
  border-radius: 999px;
  font: 500 10px var(--f-mono);
}

.menu {
  position: absolute;
  top: calc(100% + 6px);
  z-index: 40;
  padding: 6px;
  background: var(--s1);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
  box-shadow: var(--sh-card);
}

.tag-menu {
  right: 0;
  left: 0;
}

.language-menu {
  right: 0;
  width: 200px;
}

.menu input {
  width: 100%;
  height: 30px;
  margin-bottom: 4px;
  padding: 0 8px;
  color: var(--tx);
  background: var(--s2);
  border: 0;
  border-radius: 5px;
  font: 400 12px var(--f-head);
  outline: none;
  transition: background 0.2s var(--ease);
}

.menu input::placeholder {
  color: var(--tx3);
}

.menu input:focus {
  background: var(--s3);
}

.menu-list {
  display: grid;
  max-height: 220px;
  overflow-y: auto;
  scrollbar-color: var(--line2) transparent;
  scrollbar-width: thin;
}

.menu-list::-webkit-scrollbar {
  width: 4px;
}

.menu-list::-webkit-scrollbar-thumb {
  background: var(--line2);
  border-radius: 999px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 30px;
  padding: 0 6px;
  color: var(--tx);
  border-radius: 5px;
  font: 400 12px var(--f-head);
  text-align: left;
}

.menu-item:hover {
  background: var(--s2);
}

.menu-item.selected {
  color: var(--ac);
  background: var(--ac-soft);
}

.tag-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-count {
  flex: none;
  margin-left: auto;
  color: var(--tx3);
  font-family: var(--f-mono);
  font-size: 11px;
  font-variant-numeric: tabular-nums;
}

.menu-item.selected .tag-count {
  color: var(--ac);
  opacity: 0.8;
}

.check {
  display: inline-grid;
  flex: none;
  place-items: center;
  width: 14px;
  height: 14px;
  color: var(--ac);
}

.check svg {
  width: 13px;
  height: 13px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.create-tag {
  color: var(--ac);
}

.menu-empty {
  margin: 0;
  padding: 8px 6px;
  color: var(--tx3);
  font: 400 11px var(--f-head);
}

.menu-error {
  padding: 6px 6px 2px;
}

.language-control {
  display: block;
}

.header-scroll {
  display: flex;
  flex-direction: column;
  gap: var(--s-2);
  padding: var(--s-3);
}

.panel-error {
  padding: 0 0 var(--s-2);
}

.empty-state {
  display: grid;
  place-items: center;
  min-height: 150px;
  padding: var(--s-4);
  color: var(--tx2);
  border: 1px dashed var(--line2);
  border-radius: var(--r-sm);
  font: 400 11px/1.6 var(--f-mono);
  text-align: center;
}

.header-list {
  display: grid;
  gap: var(--s-3);
}

.header-card {
  overflow: hidden;
  background: color-mix(in srgb, var(--s2) 45%, transparent);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
}

.header-card.invalid {
  border-color: var(--bad);
}

.header-card-head {
  display: flex;
  align-items: center;
  gap: var(--s-2);
  height: 34px;
  padding: 0 var(--s-2) 0 var(--s-3);
}

.header-lang {
  color: var(--tx);
  font: 500 12px var(--f-head);
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

.header-card-head .icon-btn {
  margin-left: auto;
}

.header-editor {
  height: 148px;
  overflow: hidden;
  background: var(--code-bg);
  border-top: 1px solid var(--line2);
}

.header-editor :deep(.cm-editor) {
  height: 100%;
}

.header-error {
  padding: 6px var(--s-3) 8px;
}

.icon-btn {
  display: grid;
  flex: none;
  place-items: center;
  width: 24px;
  height: 24px;
  color: var(--tx2);
  border-radius: 5px;
}

.icon-btn:hover:not(:disabled) {
  color: var(--tx);
  background: var(--s2);
}

.icon-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.icon-btn svg {
  width: 14px;
  height: 14px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.tests-heading {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tests-count {
  display: inline-grid;
  place-items: center;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  color: var(--tx3);
  background: var(--s2);
  border-radius: 999px;
  font: 400 10px var(--f-mono);
}

.tests-scroll {
  padding: var(--s-3);
}

.test-list {
  display: grid;
  gap: var(--s-2);
}

.testcard {
  display: grid;
  gap: 6px;
  padding: 10px;
  background: color-mix(in srgb, var(--s2) 70%, transparent);
  border: 1px solid var(--line2);
  border-radius: var(--r-sm);
}

.testcard-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-2);
}

.test-index {
  color: var(--tx2);
  font: 500 10px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.test-label {
  color: var(--tx2);
  font: 500 10px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.test-field {
  width: 100%;
  min-height: 56px;
  padding: 8px;
  color: var(--code-tx);
  background: var(--code-bg);
  border: 0;
  border-radius: 6px;
  font: 400 11px/1.55 var(--f-mono);
  outline: none;
  resize: vertical;
  transition: background 0.2s var(--ease);
}

.test-field::placeholder {
  color: var(--syn-c);
}

.test-field:focus {
  background: color-mix(in srgb, var(--code-bg) 88%, var(--s3));
}

.test-field.invalid {
  background: color-mix(in srgb, var(--bad) 12%, var(--code-bg));
}

.create-panel {
  position: sticky;
  bottom: 0;
  z-index: 20;
  display: flex;
  flex: none;
  align-items: center;
  justify-content: space-between;
  gap: var(--s-4);
  min-height: 56px;
  padding: 0 var(--s-4);
  background: var(--s1);
  border-top: 1px solid var(--line2);
}

.create-status {
  display: flex;
  align-items: center;
  gap: var(--s-3);
  min-width: 0;
}

.status-chip {
  display: inline-flex;
  flex: none;
  align-items: center;
  gap: var(--s-2);
  padding: 4px 12px;
  color: var(--tx2);
  background: var(--s2);
  border: 1px solid var(--line2);
  border-radius: 999px;
  font: 500 11px var(--f-mono);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  white-space: nowrap;
}

.status-chip.accepted {
  color: var(--ok);
  background: color-mix(in srgb, var(--ok) 12%, transparent);
  border-color: color-mix(in srgb, var(--ok) 30%, transparent);
}

.status-chip.failed {
  color: var(--bad);
  background: color-mix(in srgb, var(--bad) 12%, transparent);
  border-color: color-mix(in srgb, var(--bad) 30%, transparent);
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

.status-copy {
  display: grid;
  gap: 2px;
  min-width: 0;
  color: var(--tx2);
  font: 400 11px/1.4 var(--f-mono);
}

.status-metrics {
  overflow: hidden;
  color: var(--tx3);
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.create-actions {
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
  text-decoration: none;
}

.secondary-btn:hover {
  background: var(--s2);
}

.primary-btn {
  color: var(--ac-ink);
  background: var(--ac);
}

.primary-btn:hover:not(:disabled) {
  filter: brightness(1.08);
}

.primary-btn:disabled {
  cursor: progress;
  opacity: 0.65;
}

@media (max-width: 980px) {
  .status-metrics {
    display: none;
  }
}

@media (max-width: 760px) {
  .workspace {
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 1fr 1fr;
  }

  .details-panel {
    grid-row: span 2;
  }

  .headers-panel,
  .tests-panel {
    min-height: 0;
  }

  .form-scroll {
    padding: var(--s-4);
  }

  .create-panel {
    height: auto;
    min-height: 56px;
    padding: var(--s-2) var(--s-3);
  }

  .create-actions {
    gap: 4px;
  }

  .secondary-btn,
  .primary-btn {
    padding: 0 10px;
  }
}
</style>
