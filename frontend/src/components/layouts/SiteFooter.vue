<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import AppLogo from '../AppLogo.vue'

gsap.registerPlugin(ScrollTrigger)

const wordmarkCanvas = ref(null)
let stopWordmark = null

// Vẽ chữ "DEVCLIMB" thành lưới ô vuông, lấp đầy dần từ trái sang phải khi cuộn tới footer.
function startWordmark(canvas) {
  const context = canvas.getContext('2d')
  const animate = !matchMedia('(prefers-reduced-motion: reduce)').matches
  let litCells = []
  let columnCount = 0
  let cellSize = 8
  let progress = animate ? 0 : 1
  let scrollTrigger = null
  let isStopped = false

  const readColor = (name) => getComputedStyle(document.documentElement).getPropertyValue(name).trim()
  const isSmallScreen = () => innerWidth < 760

  function rasterize() {
    const availableWidth = canvas.parentNode.clientWidth - (isSmallScreen() ? 40 : 96)
    cellSize = isSmallScreen() ? 5 : 8
    columnCount = Math.max(20, Math.floor(availableWidth / cellSize))
    const rowCount = Math.round(columnCount * 0.16)

    // Vẽ chữ với mỗi ô là một pixel rồi đọc lại, để chuyển nét chữ thành lưới ô vuông.
    const offscreen = document.createElement('canvas')
    offscreen.width = columnCount
    offscreen.height = rowCount
    const offscreenContext = offscreen.getContext('2d')
    offscreenContext.fillStyle = '#000'
    offscreenContext.fillRect(0, 0, columnCount, rowCount)
    offscreenContext.fillStyle = '#fff'
    offscreenContext.textBaseline = 'middle'
    offscreenContext.textAlign = 'center'
    const fontSize = Math.round(rowCount * 1.05)
    offscreenContext.font = `600 ${fontSize}px "Inter Tight", sans-serif`
    const textWidth = offscreenContext.measureText('DEVCLIMB').width
    if (textWidth > columnCount * 0.98) {
      offscreenContext.font = `600 ${Math.round((fontSize * columnCount * 0.98) / textWidth)}px "Inter Tight", sans-serif`
    }
    offscreenContext.fillText('DEVCLIMB', columnCount / 2, rowCount * 0.55)

    const pixels = offscreenContext.getImageData(0, 0, columnCount, rowCount).data
    litCells = []
    for (let row = 0; row < rowCount; row++) {
      for (let column = 0; column < columnCount; column++) {
        if (pixels[(row * columnCount + column) * 4] > 120) litCells.push([column, row, Math.random()])
      }
    }

    const pixelRatio = Math.min(devicePixelRatio || 1, 2)
    canvas.width = columnCount * cellSize * pixelRatio
    canvas.height = rowCount * cellSize * pixelRatio
    canvas.style.aspectRatio = `${columnCount} / ${rowCount}`
    context.setTransform(pixelRatio, 0, 0, pixelRatio, 0, 0)
    draw()
  }

  function draw() {
    const mutedColor = readColor('--tx3')
    const accentColor = readColor('--ac')
    context.clearRect(0, 0, canvas.width, canvas.height)
    litCells.forEach(([column, row, jitter]) => {
      const threshold = (column / columnCount) * 0.85 + jitter * 0.15
      if (threshold > progress) return
      // Các ô vừa được lấp sáng bằng màu nhấn, giống như một con trỏ đang quét qua.
      context.fillStyle = progress - threshold < 0.04 && progress < 1 ? accentColor : mutedColor
      context.fillRect(column * cellSize + 1, row * cellSize + 1, cellSize - 2, cellSize - 2)
    })
  }

  document.fonts.ready.then(() => {
    if (isStopped) return
    rasterize()
    if (!animate) return
    scrollTrigger = ScrollTrigger.create({
      trigger: canvas,
      start: 'top 95%',
      end: 'max',
      scrub: 0.6,
      onUpdate: (trigger) => {
        progress = trigger.progress
        draw()
      },
    })
  })
  window.addEventListener('resize', rasterize)
  window.addEventListener('themechange', draw)

  return function stop() {
    isStopped = true
    scrollTrigger?.kill()
    window.removeEventListener('resize', rasterize)
    window.removeEventListener('themechange', draw)
  }
}

onMounted(() => (stopWordmark = startWordmark(wordmarkCanvas.value)))
onBeforeUnmount(() => stopWordmark?.())
</script>

<template>
  <footer class="footer">
    <span class="plus left" aria-hidden="true">+</span>
    <span class="plus right" aria-hidden="true">+</span>
    <div class="columns">
      <div>
        <AppLogo :show-tag="false" />
        <p>A competitive programming platform run by its communities.</p>
      </div>
      <div>
        <h3>Product</h3>
        <RouterLink :to="{ path: '/', hash: '#solve' }">Problems</RouterLink>
        <RouterLink :to="{ path: '/', hash: '#compete' }">Contests</RouterLink>
        <RouterLink :to="{ path: '/', hash: '#compete' }">Leaderboards</RouterLink>
        <RouterLink :to="{ path: '/', hash: '#communities' }">Communities</RouterLink>
      </div>
      <div>
        <h3>Help</h3>
        <RouterLink :to="{ path: '/', hash: '#faq' }">FAQ</RouterLink>
        <a href="#">Docs</a>
        <a href="#">Changelog</a>
      </div>
      <div>
        <h3>Project</h3>
        <a href="#">JAV102 · LKH Studio</a>
        <a href="#">Contact</a>
      </div>
    </div>
    <canvas ref="wordmarkCanvas" class="wordmark" aria-label="DEVCLIMB" role="img"></canvas>
    <div class="legal"><span>© 2026 DevClimb Nexus</span><span>build a41f9c2</span></div>
  </footer>
</template>

<style scoped>
.footer {
  position: relative;
  padding: var(--s-8) var(--gut) var(--s-6);
  border-top: 1px solid var(--line);
}

.plus {
  position: absolute;
  top: -8px;
  z-index: 2;
  padding: 1px 2px;
  background: var(--bg);
  font: 400 14px/1 var(--f-mono);
  color: var(--tx3);
}

.plus.left {
  left: -9px;
}

.plus.right {
  right: -9px;
}

.columns {
  display: grid;
  grid-template-columns: 2fr repeat(3, 1fr);
  gap: var(--s-6);
  font-size: var(--t-sm);
}

.columns h3 {
  margin-bottom: var(--s-3);
  font: 400 var(--t-xs) var(--f-mono);
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--tx2);
}

.columns a {
  display: block;
  padding: 3px 0;
  color: var(--tx2);
}

.columns a:hover {
  color: var(--tx);
}

.columns p {
  max-width: 32ch;
  margin-top: var(--s-3);
  color: var(--tx2);
}

/* Logo giữ nguyên màu và bố cục ngang của nó. */
.columns a.logo {
  display: flex;
  padding: 0;
  color: inherit;
}

.wordmark {
  display: block;
  width: 100%;
  margin-top: var(--s-8);
}

.legal {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: var(--s-3);
  margin-top: var(--s-5);
  font: 400 var(--t-xs) var(--f-mono);
  color: var(--tx2);
}

@media (max-width: 900px) {
  .footer {
    --gut: var(--s-5);
  }

  .columns {
    grid-template-columns: 1fr 1fr;
  }

  .columns > div:first-child {
    grid-column: 1 / -1;
  }
}
</style>
