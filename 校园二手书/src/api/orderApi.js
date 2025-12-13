import request from './request'

export const listOrders = () => {
  return request({
    url: '/orders/list',
    method: 'get',
    headers: { token: localStorage.getItem('token') }
  })
}

export const createOrder = (bookId) => {
  return request({
    url: '/orders/create',
    method: 'post',
    params: { bookId },
    headers: { token: localStorage.getItem('token') }
  })
}

export const payOrder = (id) => {
  return request({
    url: `/orders/pay/${id}`,
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}

export const cancelOrder = (id) => {
  return request({
    url: `/orders/cancel/${id}`,
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}

export const receiveOrder = (id) => {
  return request({
    url: `/orders/receive/${id}`,
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}
