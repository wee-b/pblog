<template>
  <section ref="heroRef" class="hero-section">
    <div class="hero-grid" aria-hidden="true"></div>
    <div class="shape-field" aria-hidden="true">
      <FloatingGeometry
        v-for="shape in shapes"
        :key="shape.id"
        :sides="shape.sides"
        :color="shape.color"
        :size="shape.size"
        :start-x="shape.x"
        :start-y="shape.y"
        :initial-rotation="shape.rotation"
        :speed-factor="shape.speed"
        :pointer="pointerFlow"
      />
    </div>

    <div class="hero-content">
      <p class="hero-kicker"><span class="kicker-mark"></span>PERSONAL BLOG · CODE & LIFE</p>
      <h1 class="hero-title">欢迎来到<br><span>我的博客</span></h1>
      <p class="hero-subtitle">
        <span>{{ displayText }}</span><span class="typing-cursor" aria-hidden="true"></span>
      </p>
      <div class="hero-actions">
        <RouterLink class="primary-action" to="/allBlogs">浏览文章</RouterLink>
        <button class="secondary-action" type="button" @click="scrollToCollections">查看合集</button>
      </div>
    </div>

    <button class="scroll-indicator" type="button" aria-label="滚动到热门文章" @click="scrollToNext">
      <span>SCROLL</span><i></i>
    </button>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import FloatingGeometry from './FloatingGeometry.vue'

const heroRef = ref(null)

// Hero 只声明“需要哪些图形”；颜色、边数、尺寸与运动强度都是组件参数。
const shapes = [
  { id: 'coral-triangle', sides: 3, color: '#ff5c8a', size: 168, x: 0.78, y: 0.17, rotation: 8, speed: 0.92 },
  { id: 'blue-square', sides: 4, color: '#42bff5', size: 126, x: 0.88, y: 0.64, rotation: 16, speed: 1.05 },
  { id: 'gold-hexagon', sides: 6, color: '#ffd84d', size: 98, x: 0.68, y: 0.58, rotation: 0, speed: 1.12 },
  { id: 'navy-diamond', sides: 4, color: '#151525', size: 116, x: 0.16, y: 0.22, rotation: 45, speed: 0.82 },
  { id: 'pink-pentagon', sides: 5, color: '#ffb3c7', size: 138, x: 0.09, y: 0.66, rotation: -12, speed: 0.96 },
  { id: 'yellow-triangle', sides: 3, color: '#ffd84d', size: 92, x: 0.26, y: 0.78, rotation: -20, speed: 1.16 },
  { id: 'sky-octagon', sides: 8, color: '#42bff5', size: 64, x: 0.92, y: 0.30, rotation: 10, speed: 1.22 },
  { id: 'navy-hexagon', sides: 6, color: '#151525', size: 104, x: 0.74, y: 0.84, rotation: 28, speed: 0.88 }
]

// Hero 负责把鼠标划动转换成“水流”，各图形组件自行决定如何响应。
const pointerFlow = reactive({
  active: false,
  x: 0,
  y: 0,
  vx: 0,
  vy: 0,
  updatedAt: 0
})

let pointerLastX = 0
let pointerLastY = 0
let pointerLastTime = 0
let removePointerListeners = null

const updatePointerFlow = (event) => {
  const hero = heroRef.value
  if (!hero) return
  const bounds = hero.getBoundingClientRect()
  const x = event.clientX - bounds.left
  const y = event.clientY - bounds.top
  const elapsed = Math.max((event.timeStamp - pointerLastTime) / 1000, 1 / 120)

  if (pointerLastTime) {
    pointerFlow.vx = Math.min(1300, Math.max(-1300, (x - pointerLastX) / elapsed))
    pointerFlow.vy = Math.min(1300, Math.max(-1300, (y - pointerLastY) / elapsed))
  }
  pointerFlow.x = x
  pointerFlow.y = y
  pointerFlow.updatedAt = performance.now()
  pointerFlow.active = true
  pointerLastX = x
  pointerLastY = y
  pointerLastTime = event.timeStamp
}

const stopPointerFlow = () => {
  pointerFlow.active = false
  pointerFlow.vx = 0
  pointerFlow.vy = 0
  pointerLastTime = 0
}

const phrases = ['分享技术与生活', '记录成长的每一步', '保持好奇，持续创造']
const displayText = ref('')
let phraseIndex = 0
let characterIndex = 0
let deleting = false
let typingTimer = 0

const runTyping = () => {
  const phrase = phrases[phraseIndex]
  if (!deleting) {
    characterIndex += 1
    displayText.value = phrase.slice(0, characterIndex)
    if (characterIndex >= phrase.length) {
      deleting = true
      typingTimer = window.setTimeout(runTyping, 2200)
      return
    }
    typingTimer = window.setTimeout(runTyping, 105 + Math.random() * 55)
    return
  }

  characterIndex -= 1
  displayText.value = phrase.slice(0, Math.max(0, characterIndex))
  if (characterIndex <= 0) {
    deleting = false
    phraseIndex = (phraseIndex + 1) % phrases.length
    typingTimer = window.setTimeout(runTyping, 450)
    return
  }
  typingTimer = window.setTimeout(runTyping, 48 + Math.random() * 32)
}

const scrollToNext = () => {
  document.querySelector('.popular-articles')?.scrollIntoView({ behavior: 'smooth' })
}

const scrollToCollections = () => {
  document.querySelector('.featured-collections')?.scrollIntoView({ behavior: 'smooth' })
}

onMounted(() => {
  runTyping()
  const hero = heroRef.value
  if (!hero) return
  hero.addEventListener('pointermove', updatePointerFlow, { passive: true })
  hero.addEventListener('pointerleave', stopPointerFlow)
  removePointerListeners = () => {
    hero.removeEventListener('pointermove', updatePointerFlow)
    hero.removeEventListener('pointerleave', stopPointerFlow)
  }
})

onUnmounted(() => {
  if (typingTimer) window.clearTimeout(typingTimer)
  removePointerListeners?.()
})
</script>

<style scoped>
.hero-section {
  --hero-ink: #151525;
  --hero-coral: #ff5c8a;
  --hero-yellow: #ffd84d;
  --hero-blue: #42bff5;
  position: relative;
  z-index: 0;
  display: grid;
  min-height: 100vh;
  min-height: 100svh;
  margin-top: calc(-1 * var(--nav-height, 64px));
  padding: calc(var(--nav-height, 64px) + 68px) clamp(24px, 7vw, 112px) 84px;
  overflow: hidden;
  isolation: isolate;
  background: #fff;
  color: var(--hero-ink);
}

.hero-grid {
  position: absolute;
  inset: 0;
  z-index: -3;
  opacity: 0.38;
  background-image:
    linear-gradient(rgba(21, 21, 37, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(21, 21, 37, 0.045) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: linear-gradient(to bottom, transparent 2%, #000 32%, transparent 96%);
}

.shape-field {
  position: absolute;
  inset: 0;
  z-index: -2;
  overflow: hidden;
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 2;
  align-self: center;
  width: min(690px, 100%);
  padding: clamp(26px, 4vw, 52px);
  border: 1px solid rgba(21, 21, 37, 0.08);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 24px 80px rgba(21, 21, 37, 0.08);
  backdrop-filter: blur(7px);
}

.hero-content::before,
.hero-content::after {
  content: '';
  position: absolute;
  width: 22px;
  height: 22px;
  pointer-events: none;
}
.hero-content::before { top: -1px; left: -1px; border-top: 4px solid var(--hero-coral); border-left: 4px solid var(--hero-coral); }
.hero-content::after { right: -1px; bottom: -1px; border-right: 4px solid var(--hero-blue); border-bottom: 4px solid var(--hero-blue); }

.hero-kicker {
  display: flex;
  align-items: center;
  gap: 11px;
  margin: 0 0 20px;
  color: #626275;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.18em;
}
.kicker-mark { width: 28px; height: 5px; background: linear-gradient(90deg, var(--hero-coral) 0 50%, var(--hero-yellow) 50%); }

.hero-title {
  margin: 0;
  color: var(--hero-ink);
  font-size: clamp(3rem, 6.5vw, 6.7rem);
  font-weight: 900;
  letter-spacing: -0.075em;
  line-height: 0.94;
}
.hero-title span { color: transparent; -webkit-text-stroke: 2px var(--hero-ink); }

.hero-subtitle {
  display: flex;
  align-items: center;
  min-height: 32px;
  margin: 28px 0 32px;
  color: #57576a;
  font-size: clamp(1rem, 1.7vw, 1.25rem);
  letter-spacing: 0.08em;
}
.typing-cursor { width: 2px; height: 1.15em; margin-left: 6px; background: var(--hero-coral); animation: cursor-blink 0.9s steps(1) infinite; }

.hero-actions { display: flex; flex-wrap: wrap; gap: 12px; }
.hero-actions a,
.hero-actions button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 126px;
  min-height: 46px;
  padding: 0 24px;
  border: 2px solid var(--hero-ink);
  color: var(--hero-ink);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, background 0.25s ease;
}
.hero-actions a:hover,
.hero-actions button:hover { transform: translateY(-3px); }
.primary-action { background: var(--hero-yellow); box-shadow: 5px 5px 0 var(--hero-ink); }
.primary-action:hover { box-shadow: 8px 8px 0 var(--hero-ink); }
.secondary-action { background: #fff; }
.secondary-action:hover { background: #f7f7fa; box-shadow: 5px 5px 0 var(--hero-coral); }

.scroll-indicator {
  position: absolute;
  right: clamp(24px, 4vw, 58px);
  bottom: 34px;
  z-index: 3;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border: 0;
  background: transparent;
  color: var(--hero-ink);
  cursor: pointer;
}
.scroll-indicator span { font-size: 0.67rem; font-weight: 800; letter-spacing: 0.24em; }
.scroll-indicator i { position: relative; width: 48px; height: 1px; overflow: hidden; background: rgba(21, 21, 37, 0.22); }
.scroll-indicator i::after { content: ''; position: absolute; inset: 0; background: var(--hero-ink); transform: translateX(-100%); animation: scroll-line 2s ease-in-out infinite; }

@keyframes cursor-blink { 50% { opacity: 0; } }
@keyframes scroll-line { 0% { transform: translateX(-100%); } 55%, 100% { transform: translateX(100%); } }

@media (max-width: 768px) {
  .hero-section { padding: calc(var(--nav-height, 56px) + 54px) 20px 78px; }
  .hero-content { align-self: end; margin-bottom: 46px; padding: 28px 24px 30px; }
  .hero-title { font-size: clamp(3rem, 15vw, 4.4rem); }
  .scroll-indicator { right: 16px; bottom: 18px; }
}

@media (max-width: 480px) {
  .hero-kicker { font-size: 0.62rem; letter-spacing: 0.1em; }
  .hero-actions a,
  .hero-actions button { flex: 1; min-width: 0; padding: 0 14px; }
  .scroll-indicator span { display: none; }
}

@media (prefers-reduced-motion: reduce) {
  .typing-cursor,
  .scroll-indicator i::after { animation: none; }
}
</style>
