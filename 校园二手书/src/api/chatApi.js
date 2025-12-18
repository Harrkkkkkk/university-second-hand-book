import request from './request'

export const sendMessage = (data) => {
  return request({ url: '/chat/send', method: 'post', data, headers: { token: localStorage.getItem('token') } })
}

export const getHistory = (params) => {
  return request({ url: '/chat/history', method: 'get', params, headers: { token: localStorage.getItem('token') } })
}

export const getConversations = () => {
  return request({ url: '/chat/conversations', method: 'get', headers: { token: localStorage.getItem('token') } })
}

export const markRead = (peer) => {
  return request({ url: `/chat/read/${peer}`, method: 'post', headers: { token: localStorage.getItem('token') } })
}
