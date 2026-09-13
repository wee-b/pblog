const STORAGE_KEY = 'pblog_browser_id'

const getBrowserId = () => {
  try {
    let browserId = localStorage.getItem(STORAGE_KEY)
    if (!browserId) {
      browserId = globalThis.crypto?.randomUUID?.()
        || `${Date.now()}-${Math.random().toString(36).slice(2)}`
      localStorage.setItem(STORAGE_KEY, browserId)
    }
    return browserId
  } catch {
    return 'storage-unavailable'
  }
}

const getCanvasSignature = () => {
  try {
    const canvas = document.createElement('canvas')
    canvas.width = 240
    canvas.height = 60
    const context = canvas.getContext('2d')
    context.textBaseline = 'top'
    context.font = '16px Arial'
    context.fillStyle = '#f60'
    context.fillRect(8, 8, 120, 24)
    context.fillStyle = '#069'
    context.fillText('PBlog fingerprint 你好', 12, 12)
    return hashString(canvas.toDataURL())
  } catch {
    return 'canvas-unavailable'
  }
}

const hashString = (value) => {
  let first = 2166136261
  let second = 2166136261
  for (let index = 0; index < value.length; index++) {
    const code = value.charCodeAt(index)
    first = Math.imul(first ^ code, 16777619)
    second = Math.imul(second ^ (code + index), 16777619)
  }
  return `${(first >>> 0).toString(16)}${(second >>> 0).toString(16)}`
}

export const collectBrowserFingerprint = () => {
  const screenInfo = window.screen
    ? `${window.screen.width}x${window.screen.height}x${window.screen.colorDepth}`
    : 'screen-unavailable'

  return JSON.stringify({
    browserId: getBrowserId(),
    userAgent: navigator.userAgent,
    languages: navigator.languages,
    platform: navigator.platform,
    hardwareConcurrency: navigator.hardwareConcurrency,
    deviceMemory: navigator.deviceMemory,
    maxTouchPoints: navigator.maxTouchPoints,
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
    screen: screenInfo,
    canvas: getCanvasSignature()
  })
}
