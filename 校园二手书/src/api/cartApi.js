import request from './request'

export const listCart = () => {
  return request({
    url: '/cart/list',
    method: 'get',
    headers: { token: localStorage.getItem('token') }
  })
}

export const addToCart = (bookId) => {
  return request({
    url: `/cart/add/${bookId}`,
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}

export const removeFromCart = (bookId, count) => {
  return request({
    url: `/cart/remove/${bookId}`,
    method: 'delete',
    params: count ? { count } : undefined,
    headers: { token: localStorage.getItem('token') }
  })
}

export const clearCart = () => {
  return request({
    url: '/cart/clear',
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}
