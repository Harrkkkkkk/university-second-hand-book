import request from './request'

export const listMyNotifications = () => {
  return request({ url: '/notifications/my', method: 'get', headers: { token: localStorage.getItem('token') } })
}

export const announce = (data) => {
  return request({ url: '/admin/announce', method: 'post', data, headers: { token: localStorage.getItem('token') } })
}
