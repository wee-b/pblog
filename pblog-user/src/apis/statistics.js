import http from '@/utils/http.js'
import { collectBrowserFingerprint } from '@/utils/browserFingerprint.js'

let visitTrackingPromise

export const recordVisit = (fingerprint) => http({
  url: '/statistics/visit',
  method: 'POST',
  data: { fingerprint },
  silent: true
})

export const trackCurrentVisit = () => {
  if (!visitTrackingPromise) {
    visitTrackingPromise = recordVisit(collectBrowserFingerprint()).catch((error) => {
      visitTrackingPromise = undefined
      throw error
    })
  }
  return visitTrackingPromise
}

export const getStatisticsOverview = (days = 7) => http({
  url: '/statistics/overview',
  method: 'GET',
  params: { days }
})
