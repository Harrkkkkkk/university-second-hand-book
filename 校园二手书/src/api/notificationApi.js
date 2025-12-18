import request from '@/api/request'

export const getUnreadCount = async () => {
  const res = await request({
    url: '/notifications/unread-count',
    method: 'get',
    headers: { token: localStorage.getItem('token') }
  })
  return res ? res.count : 0
}

export const listNotifications = async () => {
  return await request({
    url: '/notifications/list',
    method: 'get',
    headers: { token: localStorage.getItem('token') }
  })
}

export const markRead = async (id) => {
  return await request({
    url: `/notifications/read/${id}`,
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}

export const markAllRead = async () => {
  return await request({
    url: '/notifications/mark-all-read',
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}

export const announce = async (data) => {
  return await request({
    url: '/notifications/announce',
    method: 'post',
    data,
    headers: { token: localStorage.getItem('token') }
  })
}
