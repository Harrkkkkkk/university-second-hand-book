import request from './request'

export const listReceivedReviews = () => {
  return request({ url: '/reviews/received', method: 'get', headers: { token: localStorage.getItem('token') } })
}

export const listSellerReviews = (sellerName) => {
  return request({ url: `/reviews/seller/${sellerName}`, method: 'get', headers: { token: localStorage.getItem('token') } })
}

export const getGoodRate = () => {
  return request({ url: '/reviews/stats/good-rate', method: 'get', headers: { token: localStorage.getItem('token') } })
}

