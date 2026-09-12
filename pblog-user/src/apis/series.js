import request from '@/utils/http.js'

function seriesRequest(config) {
  return request(config).then(response => {
    const payload = response.data
    if (payload && Object.prototype.hasOwnProperty.call(payload, 'code') && Number(payload.code) !== 200) {
      const error = new Error(payload.message || '合集请求失败')
      error.businessCode = Number(payload.code)
      error.response = response
      return Promise.reject(error)
    }
    return response
  })
}

export function pageQuerySeries(data) {
  return seriesRequest({ url: '/series/pageQuery', method: 'post', data })
}

export function getFeaturedSeries(limit = 4) {
  return seriesRequest({ url: '/series/featured', method: 'get', params: { limit } })
}

export function getSeriesDetail(id) {
  return seriesRequest({ url: `/series/queryById/${Number(id)}`, method: 'get' })
}

export function pageQueryMySeries(data) {
  return seriesRequest({ url: '/series/mine/pageQuery', method: 'post', data })
}

export function getMySeriesDetail(id) {
  return seriesRequest({ url: `/series/mine/queryById/${Number(id)}`, method: 'get' })
}

export function getSeriesCandidateArticles() {
  return seriesRequest({ url: '/series/candidateArticles', method: 'get' })
}

export function createSeries(data) {
  return seriesRequest({ url: '/series/insert', method: 'post', data })
}

export function updateSeries(id, data) {
  return seriesRequest({ url: `/series/update/${Number(id)}`, method: 'put', data })
}

export function deleteSeries(id) {
  return seriesRequest({ url: `/series/delete/${Number(id)}`, method: 'delete' })
}
