<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AvatarImage from '../../components/AvatarImage.vue'
import UserNavbar from '../../components/layouts/user/UserNavbar.vue'
import { describeApiError } from '../../config/apiClient'
import { fetchCommunity, fetchMembers, joinCommunity, leaveCommunity } from '../../features/community/communityApi'
import CommunityHeaderActions from '../../features/community/components/CommunityHeaderActions.vue'
import CommunitySideCards from '../../features/community/components/CommunitySideCards.vue'
import CommunitySidebar from '../../features/community/components/CommunitySidebar.vue'
import PinnedPosts from '../../features/community/components/PinnedPosts.vue'
import PostCard from '../../features/community/components/PostCard.vue'
import RoleList from '../../features/community/components/RoleList.vue'
import { samplePosts } from '../../features/community/sampleCommunityData'

const route = useRoute()
const dateFormat = new Intl.DateTimeFormat('en', { year: 'numeric', month: 'short', day: 'numeric' })
const pinnedPosts = samplePosts.filter((post) => post.isPinned)
const feedPosts = samplePosts.filter((post) => !post.isPinned)

const TABS = [
  { id: 'posts', label: 'Posts' },
  { id: 'puzzles', label: 'Puzzles' },
  { id: 'contests', label: 'Contests' },
  { id: 'members', label: 'Members' },
]

const community = ref(null)
const members = ref([])
const loadStatus = ref('loading') // loading | ready | missing | failed
const loadError = ref('')
const activeTab = ref('posts')
const isMembershipBusy = ref(false)
const actionError = ref('')

const owners = computed(() => members.value.filter((member) => member.isOwner))

async function loadCommunity() {
  loadStatus.value = 'loading'
  try {
    // Hai yêu cầu độc lập nên gửi song song, không chờ nhau.
    const [loadedCommunity, loadedMembers] = await Promise.all([fetchCommunity(route.params.name), fetchMembers(route.params.name)])
    community.value = loadedCommunity
    members.value = loadedMembers
    loadStatus.value = 'ready'
  } catch (error) {
    loadStatus.value = error.response?.status === 404 ? 'missing' : 'failed'
    loadError.value = describeApiError(error).message
  }
}

function moveTabWithArrowKeys(event) {
  if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') return
  event.preventDefault()
  const step = event.key === 'ArrowRight' ? 1 : -1
  const currentIndex = TABS.findIndex((tab) => tab.id === activeTab.value)
  activeTab.value = TABS[(currentIndex + step + TABS.length) % TABS.length].id
  document.getElementById(`tab-${activeTab.value}`)?.focus()
}

async function changeMembership(action) {
  isMembershipBusy.value = true
  actionError.value = ''
  try {
    await action(route.params.name)
    await loadCommunity()
  } catch (error) {
    actionError.value = describeApiError(error).message
  } finally {
    isMembershipBusy.value = false
  }
}

watch(
  () => route.params.name,
  () => {
    activeTab.value = 'posts'
    actionError.value = ''
    loadCommunity()
  },
  { immediate: true },
)
</script>

<template>
  <UserNavbar />
  <div class="shell">
    <CommunitySidebar class="left" :current-community="community" />

    <main class="content">
      <div v-if="loadStatus === 'loading'" aria-busy="true" aria-label="Loading community">
        <div class="skeleton banner"></div>
        <div class="skeleton line"></div>
      </div>

      <div v-else-if="loadStatus !== 'ready'" class="state" role="alert">
        <h1>{{ loadStatus === 'missing' ? 'Community not found' : 'Could not load this community' }}</h1>
        <p>{{ loadStatus === 'missing' ? 'It may not exist, or it may be private.' : loadError }}</p>
        <RouterLink class="btn" to="/communities">Back to communities</RouterLink>
      </div>

      <template v-else>
        <div class="banner">
          <img v-if="community.bannerUrl" :src="community.bannerUrl" alt="" />
          <svg v-else viewBox="0 0 1200 176" preserveAspectRatio="none" aria-hidden="true">
            <path d="M0 176V124l160-52 140 42 180-72 200 84 150-42 190 62 180-32v62z" fill="var(--s3)" opacity=".55" />
            <path d="M0 176V146l200-42 160 32 220-62 180 72 170-32 270 52v10z" fill="var(--ac)" opacity=".16" />
          </svg>
        </div>

        <header class="header">
          <AvatarImage class="community-avatar" :image-url="community.imageUrl" :name="community.name" :size="96" />
          <div class="identity">
            <h1><span class="prefix">c/</span>{{ community.name }}</h1>
            <p class="meta">
              <span class="badge">{{ community.isPublic ? 'public' : 'private' }}</span>
              <span><b>{{ community.memberCount }}</b> {{ community.memberCount === 1 ? 'member' : 'members' }}</span>
            </p>
          </div>
          <CommunityHeaderActions :community="community" :active-tab="activeTab" :busy="isMembershipBusy" @join="changeMembership(joinCommunity)" @leave="changeMembership(leaveCommunity)" />
        </header>
        <p v-if="actionError" class="action-error" role="alert">{{ actionError }}</p>

        <div class="layout">
          <section class="feed">
            <div class="tabs" role="tablist" aria-label="Community sections" @keydown="moveTabWithArrowKeys">
              <button v-for="tab in TABS" :id="`tab-${tab.id}`" :key="tab.id" type="button" role="tab" :aria-selected="activeTab === tab.id" :tabindex="activeTab === tab.id ? 0 : -1" @click="activeTab = tab.id">{{ tab.label }}</button>
            </div>

            <div v-if="activeTab === 'posts'" role="tabpanel" aria-labelledby="tab-posts" class="posts">
              <PinnedPosts :posts="pinnedPosts" />
              <div class="composer">
                <AvatarImage :name="community.name" :size="32" is-round />
                <button type="button" disabled>Posting is coming soon</button>
              </div>
              <PostCard v-for="post in feedPosts" :key="post.id" :post="post" />
              <p class="sample-note">sample data for illustration</p>
            </div>

            <div v-else-if="activeTab === 'puzzles' || activeTab === 'contests'" role="tabpanel" :aria-labelledby="`tab-${activeTab}`" class="empty-tab">
              <p>{{ activeTab === 'puzzles' ? 'Puzzles' : 'Contests' }} for this community are coming soon.</p>
            </div>

            <ul v-else class="members" role="tabpanel" aria-labelledby="tab-members">
              <li v-for="member in members" :key="member.userId">
                <AvatarImage :image-url="member.imageUrl" :name="member.name" :size="36" is-round />
                <b>{{ member.name }}</b>
                <span v-if="member.isOwner" class="badge">owner</span>
                <small>Joined {{ dateFormat.format(new Date(member.joinedAt)) }}</small>
              </li>
            </ul>
          </section>

          <aside class="side" aria-label="About this community">
            <section class="card">
              <h2>About</h2>
              <p>{{ community.shoutout || 'This community has no description yet.' }}</p>
              <p class="created">Created {{ dateFormat.format(new Date(community.createdAt)) }}</p>
            </section>

            <section class="card">
              <h2>Moderators</h2>
              <ul class="mods">
                <li v-for="owner in owners" :key="owner.userId">
                  <AvatarImage :image-url="owner.imageUrl" :name="owner.name" :size="28" is-round />
                  {{ owner.name }}
                  <svg class="crown" viewBox="0 0 24 24" aria-label="Owner"><path d="M3 18 5 7l5 5 2-7 2 7 5-5 2 11z" /></svg>
                </li>
              </ul>
            </section>

            <RoleList :owners="owners" />
            <CommunitySideCards />
            <p class="sample-note">Roles, contest, solvers and topics are sample data for illustration</p>
          </aside>
        </div>
      </template>
    </main>
  </div>
</template>

<style scoped src="./CommunityPage.css"></style>
