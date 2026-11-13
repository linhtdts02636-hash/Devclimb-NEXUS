<script setup>
import { computed, onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ThemeToggle from '../../components/ThemeToggle.vue'
import { LOGIN_PAGE_PATH } from '../../config/applicationPaths'
import { AuthStatus, useAuthStore } from '../../stores/authStore'
import { mountLandingAnimations } from './landingAnimations'

const authStore = useAuthStore()
const router = useRouter()
const isSignedIn = computed(() => authStore.status === AuthStatus.AUTHENTICATED)
let unmountLandingAnimations = null

onMounted(() => {
  authStore.initialize()
  unmountLandingAnimations = mountLandingAnimations()
})

onBeforeUnmount(() => unmountLandingAnimations?.())

async function handleLogout() {
  await authStore.logout()
  router.push(LOGIN_PAGE_PATH)
}
</script>

<template>
<div class="landing-page">
<a class="announce" href="#communities">DevClimb Nexus beta: every community gets its own problems, roles and contests. <u>See how it works</u></a>
<nav><div class="in">
  <a class="logo" href="#"><svg viewBox="0 0 24 24"><path d="M3 20 10 7l4 7 3-4 4 10z"/></svg>DevClimb <small>nexus</small></a>
  <div class="srch" id="srch">
    <button class="sbtn" id="sbtn" type="button" aria-label="Search" tabindex="-1"><svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg></button>
    <input id="q" type="search" placeholder="Search problems, communities, people" autocomplete="off" spellcheck="false" role="combobox" aria-expanded="false" aria-controls="sugg" aria-autocomplete="list" aria-label="Search DevClimb">
    <kbd aria-hidden="true">/</kbd>
    <div class="sugg" id="sugg" role="listbox" aria-label="Search suggestions" hidden></div>
  </div>
  <ul><li><a href="#solve">Solve</a></li><li><a href="#judge">Judge</a></li><li><a href="#compete">Compete</a></li><li><a href="#communities">Communities</a></li><li><a href="#faq">FAQ</a></li></ul>
  <div class="r"><ThemeToggle />
    <template v-if="isSignedIn"><span class="member-name">{{ authStore.profile?.name }}</span><button class="btn" type="button" @click="handleLogout">Log out</button></template>
    <template v-else><RouterLink class="btn" :to="LOGIN_PAGE_PATH">Log in</RouterLink><RouterLink class="btn p" :to="LOGIN_PAGE_PATH">Start climbing</RouterLink></template></div>
</div></nav>

<main class="frame">
<!-- =================== HERO =================== -->
<header class="hero" id="hero">
  <div class="cells" id="cells" aria-hidden="true"></div>
  <span class="corner-l" id="cl1">[ 200 OK ]</span><span class="corner-l" id="cl2">[ ACCEPTED ]</span>
  <span class="corner-l" id="cl3">[ .PY ]</span><span class="corner-l" id="cl4">[ .JAVA ]</span>
  <div class="hero-in" id="heroIn">
    <pre class="ascii l" id="asL" aria-hidden="true"></pre><pre class="ascii r" id="asR" aria-hidden="true"></pre>
    <h1 id="h1"><span class="l1">Climb with your</span><span class="l2">coding community</span></h1>
    <p class="sub">Solve problems, run contests and grow a shared problem library with the people you code with.</p>
    <div class="cta-row"><RouterLink class="btn p" :to="LOGIN_PAGE_PATH">Start climbing</RouterLink><a class="btn" href="#communities">Browse communities</a></div>
  </div>
</header>

<!-- =================== ANATOMY SCAN =================== -->
<section class="anat" id="anat" aria-label="Anatomy of the DevClimb solve page">
  <div class="browser" data-ui>
    <div class="chrome"><span class="d"><i></i><i></i><i></i></span><span class="url">devclimb.app/problems/two-sum</span></div>
    <div class="page" id="page">
      <div class="el top" data-tag="Navigation"><span class="tag"></span><span class="sk brand-bar"></span><span class="navs"><span class="sk"></span><span class="sk"></span><span class="sk"></span></span><span class="ava"></span></div>
      <div class="el prob" data-tag="Problem"><span class="tag"></span><span class="sk w60"></span><span class="sk w90"></span><span class="sk w75"></span><span class="sk w90"></span><span class="sk w45"></span><br><span class="sk w30"></span><span class="sk w75"></span></div>
      <div class="el edit" data-tag="Editor · python"><span class="tag"></span><span class="sk k w30"></span><span class="sk w60 indent-1"></span><span class="sk k w45 indent-1"></span><span class="sk w75 indent-2"></span><span class="sk w60 indent-3"></span><span class="sk w45 indent-2"></span><span class="sk k w30 indent-1"></span><span class="sk w60 indent-1"></span></div>
      <div class="el tests-el" data-tag="Test cases"><span class="tag"></span><div class="tests" id="tcs"><div><i></i><span class="sk w60"></span></div><div><i></i><span class="sk w75"></span></div><div><i></i><span class="sk w45"></span></div><div><i></i><span class="sk w60"></span></div><div><i></i><span class="sk w75"></span></div></div></div>
      <div class="el verdict" data-tag="Verdict"><span class="tag"></span><span class="pillok"></span><span class="sk w30 flush"></span><span class="sk verdict-meta"></span></div>
    </div>
  </div>
  <div class="anat-cap"><span>fig. 1: anatomy of a solve page</span><span id="anatStat">scanning…</span></div>
</section>

<!-- =================== 01 · SOLVE =================== -->
<section class="sec flush" id="solve"><span class="plus l" aria-hidden="true">+</span><span class="plus r" aria-hidden="true">+</span>
  <div class="sh">
    <div><div class="label"><b>[ 01 / 05 ]</b> solve</div><h2 class="rv">Write it, run it, <span class="ac">see every test</span></h2></div>
    <div class="r rv"><p class="lead">Pick a language, submit from the browser, and watch each test pass or fail.</p></div>
  </div>
  <div class="bleed" data-ui>
    <div class="pstrip"><h3>Two Sum</h3><span class="badge">Medium</span><p>Return the indices of the two numbers in <code>nums</code> that add up to <code>target</code>.</p></div>
    <div class="pg-body">
      <div class="rail" role="tablist" aria-label="Language" id="ptabs">
        <button class="ptab" role="tab" aria-selected="true" data-l="python"><i class="lg">py</i>Python</button>
        <button class="ptab" role="tab" aria-selected="false" data-l="javascript"><i class="lg">js</i>JavaScript</button>
        <button class="ptab" role="tab" aria-selected="false" data-l="java"><i class="lg">jv</i>Java</button>
        <button class="ptab" role="tab" aria-selected="false" data-l="csharp"><i class="lg">cs</i>C#</button>
        <button class="ptab" role="tab" aria-selected="false" data-l="php"><i class="lg">php</i>PHP</button>
        <button class="btn sm copy" id="copyBtn">Copy code</button>
      </div>
      <div class="pane src" id="src"></div>
      <div class="pane out"><div class="ohead"><span>Hidden tests</span><button class="btn p sm" id="runBtn">Run again</button></div><div class="tl" id="judgeT" aria-live="polite"></div><div class="vline" id="vline"></div></div>
    </div>
  </div>
</section>

<!-- =================== 02 · JUDGE =================== -->
<section class="sec flush" id="judge"><span class="plus l" aria-hidden="true">+</span><span class="plus r" aria-hidden="true">+</span>
  <div class="sh">
    <div><div class="label"><b>[ 02 / 05 ]</b> judge</div><h2 class="rv">One judge, <span class="ac">fair ranks</span></h2></div>
    <div class="r rv"><p class="lead">Practice, contest and community runs all go through Judge0, under the same time and memory limits.</p></div>
  </div>
  <div class="bleed jband">
    <div class="diag" id="diag">
      <div class="flow">
        <div class="node"><div class="box"><svg class="arc2" viewBox="0 0 92 92"><circle cx="46" cy="46" r="44" stroke-dasharray="60 220"/></svg><svg class="arc" viewBox="0 0 76 76"><circle cx="38" cy="38" r="36" stroke-dasharray="70 160"/></svg><span class="glyph">&lt;/&gt;</span></div><span class="cap">YOUR CODE</span></div>
        <div class="wire"><i></i></div>
        <div class="node mid"><div class="box"><svg class="arc2" viewBox="0 0 92 92"><circle cx="46" cy="46" r="44" stroke-dasharray="60 220"/></svg><svg class="arc" viewBox="0 0 76 76"><circle cx="38" cy="38" r="36" stroke-dasharray="70 160"/></svg><span class="glyph">J0</span></div><span class="cap">JUDGE0</span></div>
        <div class="wire"><i></i></div>
        <div class="node"><div class="box"><svg class="arc2" viewBox="0 0 92 92"><circle cx="46" cy="46" r="44" stroke-dasharray="60 220"/></svg><svg class="arc" viewBox="0 0 76 76"><circle cx="38" cy="38" r="36" stroke-dasharray="70 160"/></svg><span class="glyph">#1</span></div><span class="cap">RANK</span></div>
      </div>
    </div>
    <div class="feed"><div class="hd"><span>problem</span><span>verdict</span><span>time</span></div><div class="rows"><div id="subrows"></div></div></div>
  </div>
  <p class="note">sample data for illustration</p>
</section>

<!-- =================== 03 · COMPETE & DISCUSS =================== -->
<section class="sec" id="compete"><span class="plus l" aria-hidden="true">+</span><span class="plus r" aria-hidden="true">+</span>
  <div class="sh">
    <div><div class="label"><b>[ 03 / 05 ]</b> compete</div><h2 class="rv">Race the clock, <span class="ac">then compare notes</span></h2></div>
    <div class="r rv"><p class="lead">Timed contests with a live scoreboard, and a discussion thread for every problem.</p></div>
  </div>
  <div class="bento">
    <article class="card tall rv"><h3>Live leaderboard</h3><p>Ranks move on every accepted run, per contest or across a whole community.</p><div class="viz"><div class="cap2">fpt-algo-club · weekly round 12</div><div class="lb" id="lb"></div></div></article>
    <article class="card rv"><h3>Contests on a timer</h3><p>Set a window and a problem list. Entries are scored as they arrive.</p><div class="viz"><div class="clock" id="clock">01:24:37<small>left</small></div><div class="tl probs"><div><b>A</b>two-sum<small>38 solved</small></div><div><b>B</b>merge-intervals<small>21 solved</small></div><div><b>C</b>word-ladder<small>4 solved</small></div></div></div></article>
    <article class="card rv"><h3>Discussion after you pass</h3><p>A problem's thread opens once your code passes every test, so nobody spoils it.</p><div class="viz lockv"><svg viewBox="0 0 24 24" id="lock" aria-hidden="true"><rect x="5" y="11" width="14" height="10" rx="2"/><path id="shackle" d="M8 11V7a4 4 0 0 1 8 0v4"/></svg><div class="bub" id="bub"><span>one pass with a map is enough</span><span>watch the [3,3] case</span></div></div></article>
  </div>
  <p class="note">sample data for illustration</p>
</section>

<!-- =================== 04 · COMMUNITIES =================== -->
<section class="sec flush" id="communities"><span class="plus l" aria-hidden="true">+</span><span class="plus r" aria-hidden="true">+</span>
  <div class="sh">
    <div><div class="label"><b>[ 04 / 05 ]</b> communities</div><h2 class="rv">Every community runs <span class="ac">its own climb</span></h2></div>
    <div class="r rv"><p class="lead">Its own roles, problem library and contests. Members write problems, moderators review them.</p><a class="link" href="#">Explore communities ›</a></div>
  </div>
  <div class="cm-tabs" role="tablist" aria-label="Example communities" id="cmTabs"></div>
  <div class="bleed cm" aria-live="polite">
    <div class="cm-id"><span class="url" id="cmUrl"></span><h3 id="cmName"></h3><p id="cmDesc"></p><div class="roles" id="cmRoles"></div></div>
    <div class="cm-rows" id="cmRows"></div>
  </div>
  <p class="note">example communities for illustration</p>
</section>

<!-- =================== 05 · FAQ =================== -->
<section class="sec" id="faq"><span class="plus l" aria-hidden="true">+</span><span class="plus r" aria-hidden="true">+</span>
  <div class="faq">
    <div class="stick"><div class="label"><b>[ 05 / 05 ]</b> faq</div><h2 class="rv">Questions, <span class="ac">answered</span></h2></div>
    <div class="rv">
      <div class="qa open"><button aria-expanded="true">Which languages can I use?<i>+</i></button><div class="a"><div><p>Python, JavaScript, Java, C# and PHP.</p></div></div></div>
      <div class="qa"><button aria-expanded="false">Do failed submissions count?<i>+</i></button><div class="a"><div><p>Every submission is saved to your history. A problem counts as solved once all of its tests pass.</p></div></div></div>
      <div class="qa"><button aria-expanded="false">Who can add problems to a community?<i>+</i></button><div class="a"><div><p>Any member can submit a new problem or suggest an edit. A moderator reviews it before it joins the library.</p></div></div></div>
      <div class="qa"><button aria-expanded="false">How do I sign in?<i>+</i></button><div class="a"><div><p>With email and password, Google, or GitHub.</p></div></div></div>
    </div>
  </div>
</section>

<!-- =================== READY =================== -->
<section class="sec ready" id="ready"><span class="plus l" aria-hidden="true">+</span><span class="plus r" aria-hidden="true">+</span>
  <div>
    <h2 class="rv">Find your community and <span class="ac">start climbing</span></h2>
    <p class="rv">Create an account, join a community, and submit your first solution.</p>
    <div class="cta-row rv"><RouterLink class="btn p" :to="LOGIN_PAGE_PATH">Create account</RouterLink><a class="btn" href="#communities">Browse communities</a></div>
  </div>
  <div class="start rv" id="start" aria-label="Getting started checklist">
    <div class="h">// getting started</div>
    <div class="st"><i></i>create an account<small>pending</small></div>
    <div class="st"><i></i>join fpt-algo-club<small>pending</small></div>
    <div class="st"><i></i>submit two-sum<small>pending</small></div>
  </div>
</section>

<footer class="foot"><span class="plus l" aria-hidden="true">+</span><span class="plus r" aria-hidden="true">+</span>
  <div class="cols">
    <div><a class="logo" href="#"><svg viewBox="0 0 24 24"><path d="M3 20 10 7l4 7 3-4 4 10z"/></svg>DevClimb</a><p>A competitive programming platform run by its communities.</p></div>
    <div><h3>Product</h3><a href="#solve">Problems</a><a href="#compete">Contests</a><a href="#compete">Leaderboards</a><a href="#communities">Communities</a></div>
    <div><h3>Help</h3><a href="#faq">FAQ</a><a href="#">Docs</a><a href="#">Changelog</a></div>
    <div><h3>Project</h3><a href="#">JAV102 · LKH Studio</a><a href="#">Contact</a></div>
  </div>
  <canvas id="word" aria-label="DEVCLIMB" role="img"></canvas>
  <div class="legal"><span>© 2026 DevClimb Nexus</span><span>build a41f9c2</span></div>
</footer>
</main>
</div>
</template>

<style src="./LandingPage.css"></style>
