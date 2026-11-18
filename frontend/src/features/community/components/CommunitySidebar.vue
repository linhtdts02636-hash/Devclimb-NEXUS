<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import AvatarImage from '../../../components/AvatarImage.vue'
import { fetchCommunitiesByIds, fetchMyCommunities } from '../communityApi'
import { RESOURCES } from '../resources'

const props = defineProps({
  currentCommunity: { type: Object, default: null },
})

// Lưu theo id (không đổi) kèm tên để hiển thị ngay; tên được làm mới từ server nên vẫn đúng sau khi đổi tên.
const RECENT_KEY = 'dc-recent-communities'
const FAVORITE_KEY = 'dc-favorite-communities'
// Lưu 6 để sau khi ẩn cộng đồng đang xem, Recent vẫn hiện đủ 5 mục.
const MAXIMUM_RECENT = 6

const SOON_ITEMS = [
  { label: 'Home', icon: '<path d="M4 11 12 4l8 7v8a1 1 0 0 1-1 1h-4v-6H9v6H5a1 1 0 0 1-1-1z" />' },
  { label: 'Explore', icon: '<circle cx="12" cy="12" r="9" /><path d="m15.5 8.5-2 5-5 2 2-5z" />' },
  { label: 'Puzzles', icon: '<path d="m8 8-4 4 4 4M16 8l4 4-4 4" />' },
  { label: 'Contests', icon: '<path d="M8 21h8M12 17v4M7 4h10v5a5 5 0 0 1-10 0zM7 6H4v1a3 3 0 0 0 3 3M17 6h3v1a3 3 0 0 1-3 3" />' },
]

function readStoredList(key) {
  try {
    return JSON.parse(localStorage.getItem(key)) ?? []
  } catch {
    return []
  }
}

function writeStoredList(key, list) {
  try {
    localStorage.setItem(key, JSON.stringify(list))
  } catch {
    // Trình duyệt chặn lưu trữ (chế độ riêng tư): danh sách vẫn đúng trong phiên này, chỉ không nhớ lần sau.
  }
}

const communities = ref([])
// Bản lưu cũ dùng tên (chuỗi) thay vì id; bỏ qua ở đây, riêng mục yêu thích được chuyển đổi trong loadJoinedCommunities.
const favoriteIds = ref(readStoredList(FAVORITE_KEY).filter(Number.isInteger))
const recentCommunities = ref(readStoredList(RECENT_KEY).filter((entry) => Number.isInteger(entry?.id)))
const openSections = ref({ communities: true, recent: true, resources: true })

const sortedCommunities = computed(() =>
  [...communities.value].sort((first, second) => favoriteIds.value.includes(second.id) - favoriteIds.value.includes(first.id)),
)
const otherRecentCommunities = computed(() => recentCommunities.value.filter((entry) => entry.id !== props.currentCommunity?.id))

function toggleFavorite(id) {
  favoriteIds.value = favoriteIds.value.includes(id)
    ? favoriteIds.value.filter((favoriteId) => favoriteId !== id)
    : [...favoriteIds.value, id]
  writeStoredList(FAVORITE_KEY, favoriteIds.value)
}

watch(
  () => props.currentCommunity,
  (community) => {
    if (!community) return
    const entry = { id: community.id, name: community.name, imageUrl: community.imageUrl }
    recentCommunities.value = [entry, ...recentCommunities.value.filter((recent) => recent.id !== entry.id)].slice(0, MAXIMUM_RECENT)
    writeStoredList(RECENT_KEY, recentCommunities.value)
  },
  { immediate: true },
)

async function loadJoinedCommunities() {
  try {
    communities.value = await fetchMyCommunities()
    const legacyNames = readStoredList(FAVORITE_KEY).filter((value) => typeof value === 'string')
    if (legacyNames.length) {
      const migratedIds = communities.value.filter((community) => legacyNames.includes(community.name)).map((community) => community.id)
      favoriteIds.value = [...new Set([...favoriteIds.value, ...migratedIds])]
      writeStoredList(FAVORITE_KEY, favoriteIds.value)
    }
  } catch {
    // Thanh bên chỉ là phần điều hướng phụ; lỗi mạng ở đây không nên chặn trang chính.
  }
}

// Làm mới tên trong Recent từ server: mục đổi tên được cập nhật, mục đã xóa hoặc thành riêng tư bị bỏ.
async function refreshRecentNames() {
  if (!recentCommunities.value.length) return
  try {
    const currentCommunities = await fetchCommunitiesByIds(recentCommunities.value.map((entry) => entry.id))
    const currentById = new Map(currentCommunities.map((community) => [community.id, community]))
    // Đọc lại danh sách sau khi chờ: trang hiện tại có thể vừa được thêm vào trong lúc chờ.
    recentCommunities.value = recentCommunities.value
      .filter((entry) => currentById.has(entry.id) || entry.id === props.currentCommunity?.id)
      .map((entry) => {
        const current = currentById.get(entry.id)
        return { id: entry.id, name: current?.name ?? entry.name, imageUrl: current?.imageUrl ?? entry.imageUrl }
      })
    writeStoredList(RECENT_KEY, recentCommunities.value)
  } catch {
    // Mất mạng thì giữ tên đã lưu; lần mở sau sẽ làm mới lại.
  }
}

onMounted(() => {
  loadJoinedCommunities()
  refreshRecentNames()
})
</script>

<template>
  <nav class="sidebar" aria-label="Community navigation">
    <ul class="list">
      <li v-for="item in SOON_ITEMS" :key="item.label" class="item soon">
        <svg viewBox="0 0 24 24" aria-hidden="true" v-html="item.icon"></svg>
        <span class="name">{{ item.label }}</span><small>soon</small>
      </li>
      <li>
        <RouterLink class="item" to="/communities/new">
          <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" /></svg>
          <span class="name">Create community</span>
        </RouterLink>
      </li>
    </ul>

    <section class="group">
      <button type="button" class="head" :aria-expanded="openSections.communities" aria-controls="sidebar-communities" @click="openSections.communities = !openSections.communities">
        Communities<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 9 6 6 6-6" /></svg>
      </button>
      <ul v-show="openSections.communities" id="sidebar-communities" class="list">
        <li v-for="community in sortedCommunities" :key="community.id" class="row">
          <RouterLink class="item" :to="`/community/${community.name}`">
            <AvatarImage :image-url="community.imageUrl" :name="community.name" :size="26" />
            <span class="name">{{ community.name }}</span>
          </RouterLink>
          <button type="button" class="star" :aria-pressed="favoriteIds.includes(community.id)" :aria-label="`Favorite ${community.name}`" @click="toggleFavorite(community.id)">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m12 3 2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1-4.4-4.3 6.1-.9z" /></svg>
          </button>
        </li>
      </ul>
    </section>

    <section v-if="otherRecentCommunities.length" class="group">
      <button type="button" class="head" :aria-expanded="openSections.recent" aria-controls="sidebar-recent" @click="openSections.recent = !openSections.recent">
        Recent<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 9 6 6 6-6" /></svg>
      </button>
      <ul v-show="openSections.recent" id="sidebar-recent" class="list">
        <li v-for="entry in otherRecentCommunities" :key="entry.id">
          <RouterLink class="item" :to="`/community/${entry.name}`">
            <AvatarImage :image-url="entry.imageUrl" :name="entry.name" :size="26" /><span class="name">{{ entry.name }}</span>
          </RouterLink>
        </li>
      </ul>
      <p class="note">Kept in this browser only</p>
    </section>

    <section class="group">
      <button type="button" class="head" :aria-expanded="openSections.resources" aria-controls="sidebar-resources" @click="openSections.resources = !openSections.resources">
        Resources<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 9 6 6 6-6" /></svg>
      </button>
      <ul v-show="openSections.resources" id="sidebar-resources" class="list">
        <li v-for="resource in RESOURCES" :key="resource.slug">
          <RouterLink class="item" :to="`/resources/${resource.slug}`"><span class="name">{{ resource.label }}</span></RouterLink>
        </li>
      </ul>
    </section>
  </nav>
</template>

<style scoped>
.sidebar {
  display: grid;
  gap: var(--s-3);
}

.list {
  display: grid;
  gap: 1px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.group {
  padding-top: var(--s-3);
  border-top: 1px solid var(--line);
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: var(--s-1) var(--s-2) var(--s-2);
  font: 400 11px var(--f-mono);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--tx2);
}

.head:hover {
  color: var(--tx);
}

.head svg {
  transition: transform 0.2s var(--ease);
}

.head[aria-expanded='false'] svg {
  transform: rotate(-90deg);
}

svg {
  width: 16px;
  height: 16px;
  flex: none;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.item {
  display: flex;
  gap: var(--s-3);
  align-items: center;
  height: 38px;
  padding: 0 var(--s-2);
  font-size: var(--t-sm);
  color: var(--tx2);
  border-radius: var(--r-sm);
}

a.item:hover {
  background: var(--s2);
  color: var(--tx);
}

a.item.router-link-exact-active {
  background: var(--ac-soft);
  color: var(--tx);
  font-weight: 500;
}

.soon {
  opacity: 0.55;
}

.item small {
  font: 400 10px var(--f-mono);
  text-transform: uppercase;
}

.name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row {
  display: flex;
  align-items: center;
}

.row .item {
  flex: 1;
  min-width: 0;
}

.star {
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  color: var(--tx2);
  border-radius: var(--r-sm);
  opacity: 0;
}

.row:hover .star,
.star:focus-visible,
.star[aria-pressed='true'] {
  opacity: 1;
}

.star[aria-pressed='true'] {
  color: var(--warn);
}

.star[aria-pressed='true'] svg {
  fill: currentColor;
}

.star:hover {
  background: var(--s2);
}

.note {
  padding: var(--s-2);
  font: 400 11px var(--f-mono);
  color: var(--tx2);
}
</style>
