import request from './request'

export const listUnderReviewBooks = () => {
  return request({ url: '/admin/review/books', method: 'get', headers: { token: localStorage.getItem('token') } })
}

export const approveBook = (id) => {
  return request({ url: `/admin/review/books/${id}/approve`, method: 'post', headers: { token: localStorage.getItem('token') } })
}

export const rejectBook = (id) => {
  return request({ url: `/admin/review/books/${id}/reject`, method: 'post', headers: { token: localStorage.getItem('token') } })
}

export const listUsers = () => {
  return request({ url: '/admin/users', method: 'get', headers: { token: localStorage.getItem('token') } })
}

export const setUserRole = (username, role) => {
  return request({ url: `/admin/users/${username}/role`, method: 'post', params: { role }, headers: { token: localStorage.getItem('token') } })
}

export const deleteUser = (username) => {
  return request({ url: `/admin/users/${username}`, method: 'delete', headers: { token: localStorage.getItem('token') } })
}

export const listComplaints = () => {
  return request({ url: '/admin/complaints', method: 'get', headers: { token: localStorage.getItem('token') } })
}

export const approveComplaint = (id) => {
  return request({ url: `/admin/complaints/${id}/approve`, method: 'post', headers: { token: localStorage.getItem('token') } })
}

export const rejectComplaint = (id) => {
  return request({ url: `/admin/complaints/${id}/reject`, method: 'post', headers: { token: localStorage.getItem('token') } })
}
