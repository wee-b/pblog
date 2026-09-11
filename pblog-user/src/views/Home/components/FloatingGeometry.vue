<template>
  <span
    ref="elementRef"
    class="floating-geometry"
    :class="{ 'is-dragging': dragging }"
    :style="geometryStyle"
    @pointerdown="startDrag"
    @pointermove="moveDrag"
    @pointerup="finishDrag"
    @pointercancel="finishDrag"
  ></span>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'

const props = defineProps({
  sides: {
    type: Number,
    required: true,
    validator: (value) => Number.isInteger(value) && value >= 3 && value <= 12
  },
  color: { type: String, required: true },
  size: { type: Number, default: 100 },
  startX: { type: Number, default: 0.5 },
  startY: { type: Number, default: 0.5 },
  initialRotation: { type: Number, default: 0 },
  pointer: {
    type: Object,
    default: () => ({ active: false, x: 0, y: 0, vx: 0, vy: 0, updatedAt: 0 })
  },
  speedFactor: { type: Number, default: 1 }
})

const elementRef = ref(null)
const dragging = ref(false)

const polygonPoints = computed(() => {
  const points = []
  const startAngle = -Math.PI / 2
  for (let index = 0; index < props.sides; index += 1) {
    const angle = startAngle + (index * Math.PI * 2) / props.sides
    const x = 50 + Math.cos(angle) * 50
    const y = 50 + Math.sin(angle) * 50
    points.push(`${x.toFixed(3)}% ${y.toFixed(3)}%`)
  }
  return `polygon(${points.join(', ')})`
})

const geometryStyle = computed(() => ({
  '--geometry-color': props.color,
  '--geometry-size': `${props.size}px`,
  clipPath: polygonPoints.value
}))

// 随机范围使用 px/s、px/s²、deg/s、deg/s²，统一基于时间步长计算。
const LIMITS = Object.freeze({
  acceleration: [10, 28],
  accelerationChangeSeconds: [0.55, 1.5],
  maxSpeed: [38, 68],
  angularAcceleration: [2, 7],
  angularChangeSeconds: [0.65, 1.8],
  maxAngularSpeed: [12, 28],
  scale: [0.8, 1.2],
  scaleChangeSeconds: [1.4, 3],
  pointerRadius: 460,
  pointerGlobalForce: 0.02,
  pointerNearForce: 0.09,
  boundaryPadding: 30,
  boundaryBounce: 0.72,
  linearDrag: 0.995,
  throwDrag: 0.992,
  throwAngularDrag: 0.986,
  maxThrowSpeed: 1100,
  maxThrowAngularSpeed: 260,
  minThrowSpeed: 45
})

const randomBetween = ([min, max]) => min + Math.random() * (max - min)
const randomSigned = (range) => randomBetween(range) * (Math.random() < 0.5 ? -1 : 1)
const clamp = (value, min, max) => Math.min(max, Math.max(min, value))

const body = {
  x: 0,
  y: 0,
  vx: 0,
  vy: 0,
  ax: 0,
  ay: 0,
  rotation: 0,
  angularVelocity: 0,
  angularAcceleration: 0,
  maxSpeed: 0,
  maxAngularSpeed: 0,
  scale: 1,
  scaleTarget: 1,
  nextAccelerationChange: 0,
  nextAngularChange: 0,
  nextScaleChange: 0,
  throwing: false,
  spinningFromThrow: false
}

const dragState = {
  pointerId: null,
  offsetX: 0,
  offsetY: 0,
  lastX: 0,
  lastY: 0,
  lastTime: 0
}

let frameId = 0
let previousTime = 0
let observer = null
let isVisible = true
let motionIntensity = 1

const getContainer = () => elementRef.value?.parentElement

const getRenderedSize = (containerWidth) => {
  return props.size * (containerWidth <= 768 ? 0.72 : 1)
}

const chooseLinearAcceleration = (now) => {
  const angle = Math.random() * Math.PI * 2
  const magnitude = randomBetween(LIMITS.acceleration) * props.speedFactor * motionIntensity
  body.ax = Math.cos(angle) * magnitude
  body.ay = Math.sin(angle) * magnitude
  body.nextAccelerationChange = now + randomBetween(LIMITS.accelerationChangeSeconds)
}

const chooseAngularAcceleration = (now) => {
  body.angularAcceleration = randomSigned(LIMITS.angularAcceleration) * props.speedFactor * motionIntensity
  body.nextAngularChange = now + randomBetween(LIMITS.angularChangeSeconds)
}

const chooseScaleTarget = (now) => {
  body.scaleTarget = randomBetween(LIMITS.scale)
  body.nextScaleChange = now + randomBetween(LIMITS.scaleChangeSeconds)
}

const render = (renderedSize) => {
  const element = elementRef.value
  if (!element) return
  element.style.transform =
    `translate3d(${(body.x - renderedSize / 2).toFixed(2)}px, ` +
    `${(body.y - renderedSize / 2).toFixed(2)}px, 0) ` +
    `rotate(${body.rotation.toFixed(2)}deg) scale(${body.scale.toFixed(4)})`
}

const keepInsideBounds = (width, height, renderedSize) => {
  const radius = renderedSize * body.scale * 0.5
  const minX = Math.min(width / 2, LIMITS.boundaryPadding + radius)
  const maxX = Math.max(width / 2, width - LIMITS.boundaryPadding - radius)
  const minY = Math.min(height / 2, LIMITS.boundaryPadding + radius)
  const maxY = Math.max(height / 2, height - LIMITS.boundaryPadding - radius)

  if (body.x < minX) {
    body.x = minX
    body.vx = Math.abs(body.vx) * LIMITS.boundaryBounce
    body.ax = Math.abs(body.ax)
  } else if (body.x > maxX) {
    body.x = maxX
    body.vx = -Math.abs(body.vx) * LIMITS.boundaryBounce
    body.ax = -Math.abs(body.ax)
  }
  if (body.y < minY) {
    body.y = minY
    body.vy = Math.abs(body.vy) * LIMITS.boundaryBounce
    body.ay = Math.abs(body.ay)
  } else if (body.y > maxY) {
    body.y = maxY
    body.vy = -Math.abs(body.vy) * LIMITS.boundaryBounce
    body.ay = -Math.abs(body.ay)
  }
}

const clampDragPosition = (width, height, renderedSize) => {
  const radius = renderedSize * body.scale * 0.5
  body.x = clamp(
    body.x,
    Math.min(width / 2, LIMITS.boundaryPadding + radius),
    Math.max(width / 2, width - LIMITS.boundaryPadding - radius)
  )
  body.y = clamp(
    body.y,
    Math.min(height / 2, LIMITS.boundaryPadding + radius),
    Math.max(height / 2, height - LIMITS.boundaryPadding - radius)
  )
}

const startDrag = (event) => {
  if (event.button !== 0) return
  const element = elementRef.value
  const container = getContainer()
  if (!element || !container) return

  event.preventDefault()
  element.setPointerCapture(event.pointerId)
  const bounds = container.getBoundingClientRect()
  const pointerX = event.clientX - bounds.left
  const pointerY = event.clientY - bounds.top

  dragging.value = true
  body.throwing = false
  body.spinningFromThrow = false
  body.vx = 0
  body.vy = 0
  dragState.pointerId = event.pointerId
  dragState.offsetX = body.x - pointerX
  dragState.offsetY = body.y - pointerY
  dragState.lastX = pointerX
  dragState.lastY = pointerY
  dragState.lastTime = event.timeStamp
}

const moveDrag = (event) => {
  if (!dragging.value || event.pointerId !== dragState.pointerId) return
  const container = getContainer()
  if (!container) return
  event.preventDefault()

  const bounds = container.getBoundingClientRect()
  const pointerX = event.clientX - bounds.left
  const pointerY = event.clientY - bounds.top
  const elapsed = Math.max((event.timeStamp - dragState.lastTime) / 1000, 1 / 120)
  const velocityX = (pointerX - dragState.lastX) / elapsed
  const velocityY = (pointerY - dragState.lastY) / elapsed
  const speed = Math.hypot(velocityX, velocityY)

  body.x = pointerX + dragState.offsetX
  body.y = pointerY + dragState.offsetY
  body.vx = clamp(velocityX, -LIMITS.maxThrowSpeed, LIMITS.maxThrowSpeed)
  body.vy = clamp(velocityY, -LIMITS.maxThrowSpeed, LIMITS.maxThrowSpeed)

  // 拖得越快，旋转越快；主移动轴决定旋转方向，避免对角移动时相互抵消。
  const spinDirection = Math.abs(velocityX) >= Math.abs(velocityY)
    ? Math.sign(velocityX || 1)
    : -Math.sign(velocityY || 1)
  body.angularVelocity = spinDirection * Math.min(LIMITS.maxThrowAngularSpeed, speed * 0.24)

  const renderedSize = getRenderedSize(container.clientWidth)
  clampDragPosition(container.clientWidth, container.clientHeight, renderedSize)
  render(renderedSize)
  dragState.lastX = pointerX
  dragState.lastY = pointerY
  dragState.lastTime = event.timeStamp
}

const finishDrag = (event) => {
  if (!dragging.value || event.pointerId !== dragState.pointerId) return
  const element = elementRef.value
  if (element?.hasPointerCapture(event.pointerId)) element.releasePointerCapture(event.pointerId)

  // 停顿后再松手不算投掷；快速拖动后立即松手才保留惯性。
  const idleMilliseconds = event.timeStamp - dragState.lastTime
  if (idleMilliseconds > 120) {
    body.vx *= 0.2
    body.vy *= 0.2
    body.angularVelocity *= 0.35
  }
  body.throwing = Math.hypot(body.vx, body.vy) >= LIMITS.minThrowSpeed
  body.spinningFromThrow = Math.abs(body.angularVelocity) > body.maxAngularSpeed * 1.1
  dragging.value = false
  dragState.pointerId = null
}

const applyPointerCurrent = (deltaTime, nowMs) => {
  const current = props.pointer
  if (!current.active || nowMs - current.updatedAt > 140) return
  const pointerSpeed = Math.hypot(current.vx, current.vy)
  if (pointerSpeed < 8) return

  const distance = Math.hypot(body.x - current.x, body.y - current.y)
  const proximity = clamp(1 - distance / LIMITS.pointerRadius, 0, 1)
  const force = LIMITS.pointerGlobalForce + proximity * proximity * LIMITS.pointerNearForce
  body.vx += current.vx * force * deltaTime
  body.vy += current.vy * force * deltaTime
  body.angularVelocity += (current.vx - current.vy) * proximity * 0.002 * deltaTime
}

const animate = (timestamp) => {
  const container = getContainer()
  if (!container || !isVisible) {
    frameId = 0
    return
  }

  if (!previousTime) previousTime = timestamp
  const deltaTime = Math.min((timestamp - previousTime) / 1000, 0.034)
  const now = timestamp / 1000
  previousTime = timestamp

  const renderedSize = getRenderedSize(container.clientWidth)
  if (dragging.value) {
    body.rotation += body.angularVelocity * deltaTime
    body.scale += (body.scaleTarget - body.scale) * (1 - Math.exp(-1.1 * deltaTime))
    clampDragPosition(container.clientWidth, container.clientHeight, renderedSize)
    render(renderedSize)
    frameId = requestAnimationFrame(animate)
    return
  }

  if (now >= body.nextScaleChange) chooseScaleTarget(now)

  // 投掷阶段只保留惯性与阻尼，避免随机加速度持续给物体补充能量。
  if (!body.throwing) {
    if (now >= body.nextAccelerationChange) chooseLinearAcceleration(now)
    body.vx += body.ax * deltaTime
    body.vy += body.ay * deltaTime
    applyPointerCurrent(deltaTime, timestamp)
  }
  if (!body.spinningFromThrow) {
    if (now >= body.nextAngularChange) chooseAngularAcceleration(now)
    body.angularVelocity += body.angularAcceleration * deltaTime
  }

  const speed = Math.hypot(body.vx, body.vy)
  const activeSpeedLimit = body.throwing ? LIMITS.maxThrowSpeed : body.maxSpeed
  if (speed > activeSpeedLimit) {
    body.vx = (body.vx / speed) * activeSpeedLimit
    body.vy = (body.vy / speed) * activeSpeedLimit
  }
  const drag = body.throwing ? LIMITS.throwDrag : LIMITS.linearDrag
  body.vx *= Math.pow(drag, deltaTime * 60)
  body.vy *= Math.pow(drag, deltaTime * 60)
  body.x += body.vx * deltaTime
  body.y += body.vy * deltaTime

  const activeAngularLimit = body.spinningFromThrow
    ? LIMITS.maxThrowAngularSpeed
    : body.maxAngularSpeed
  body.angularVelocity = clamp(body.angularVelocity, -activeAngularLimit, activeAngularLimit)
  if (body.spinningFromThrow) {
    body.angularVelocity *= Math.pow(LIMITS.throwAngularDrag, deltaTime * 60)
  }
  body.rotation += body.angularVelocity * deltaTime
  body.scale += (body.scaleTarget - body.scale) * (1 - Math.exp(-1.1 * deltaTime))

  keepInsideBounds(container.clientWidth, container.clientHeight, renderedSize)
  if (body.throwing && Math.hypot(body.vx, body.vy) < body.maxSpeed * 1.1) body.throwing = false
  if (body.spinningFromThrow && Math.abs(body.angularVelocity) < body.maxAngularSpeed * 1.1) {
    body.spinningFromThrow = false
  }
  render(renderedSize)
  frameId = requestAnimationFrame(animate)
}

const startAnimation = () => {
  if (frameId || !isVisible) return
  previousTime = 0
  frameId = requestAnimationFrame(animate)
}

const stopAnimation = () => {
  if (frameId) cancelAnimationFrame(frameId)
  frameId = 0
}

onMounted(() => {
  const container = getContainer()
  if (!container) return
  const now = performance.now() / 1000
  body.x = container.clientWidth * clamp(props.startX, 0, 1)
  body.y = container.clientHeight * clamp(props.startY, 0, 1)
  motionIntensity = window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 0.7 : 1
  body.vx = randomSigned([14, 26]) * props.speedFactor * motionIntensity
  body.vy = randomSigned([14, 26]) * props.speedFactor * motionIntensity
  body.rotation = props.initialRotation
  body.angularVelocity = randomSigned([4, 10]) * props.speedFactor * motionIntensity
  body.maxSpeed = randomBetween(LIMITS.maxSpeed) * props.speedFactor * motionIntensity
  body.maxAngularSpeed = randomBetween(LIMITS.maxAngularSpeed) * props.speedFactor * motionIntensity
  body.scale = randomBetween([0.92, 1.08])
  chooseLinearAcceleration(now)
  chooseAngularAcceleration(now)
  chooseScaleTarget(now)

  const renderedSize = getRenderedSize(container.clientWidth)
  keepInsideBounds(container.clientWidth, container.clientHeight, renderedSize)
  render(renderedSize)

  observer = new IntersectionObserver(([entry]) => {
    isVisible = entry.isIntersecting
    if (isVisible) startAnimation()
    else stopAnimation()
  }, { threshold: 0.01 })
  observer.observe(elementRef.value)
  startAnimation()
})

onUnmounted(() => {
  stopAnimation()
  observer?.disconnect()
})
</script>

<style scoped>
.floating-geometry {
  position: absolute;
  top: 0;
  left: 0;
  display: block;
  width: var(--geometry-size);
  height: var(--geometry-size);
  background: var(--geometry-color);
  transform-origin: 50% 50%;
  will-change: transform;
  filter: drop-shadow(0 14px 18px rgba(21, 21, 37, 0.13));
  pointer-events: auto;
  touch-action: none;
  user-select: none;
  cursor: grab;
}

.floating-geometry.is-dragging {
  z-index: 10;
  cursor: grabbing;
  filter: drop-shadow(0 22px 26px rgba(21, 21, 37, 0.24));
}

@media (max-width: 768px) {
  .floating-geometry {
    width: calc(var(--geometry-size) * 0.72);
    height: calc(var(--geometry-size) * 0.72);
  }
}

@media (prefers-reduced-motion: reduce) {
  .floating-geometry {
    will-change: auto;
  }
}
</style>
