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

export const listUsers = (keyword) => {
  return request({ url: '/admin/users', method: 'get', params: { keyword }, headers: { token: localStorage.getItem('token') } })
}

export const setUserRole = (username, role) => {
  return request({ url: `/admin/users/${username}/role`, method: 'post', params: { role }, headers: { token: localStorage.getItem('token') } })
}

export const deleteUser = (username) => {
  return request({ url: `/admin/users/${username}`, method: 'delete', headers: { token: localStorage.getItem('token') } })
}

export const getUserDetail = (username) => {
  return request({ url: `/admin/users/${username}`, method: 'get', headers: { token: localStorage.getItem('token') } })
}

export const updateUserStatus = (username, status, reason, secondAdmin, secondAdminPwd) => {
  return request({ url: `/admin/users/${username}/status`, method: 'post', data: { status, reason, secondAdmin, secondAdminPwd }, headers: { token: localStorage.getItem('token') } })
}

export const undoBlacklist = (username, reason) => {
  return request({ url: `/admin/users/${username}/undo-blacklist`, method: 'post', data: { reason }, headers: { token: localStorage.getItem('token') } })
}

export const updateUserInfo = (username, data) => {
  return request({ url: `/admin/users/${username}/update`, method: 'post', data, headers: { token: localStorage.getItem('token') } })
}

export const getOperationLogs = (params) => {
  return request({ url: '/admin/users/logs', method: 'get', params, headers: { token: localStorage.getItem('token') } })
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

export const listSellerApplications = () => {
  return request({ url: '/admin/seller-applications', method: 'get', headers: { token: localStorage.getItem('token') } })
}

export const approveSeller = (username) => {
  return request({ url: `/admin/approve-seller/${username}`, method: 'post', headers: { token: localStorage.getItem('token') } })
}

export const rejectSeller = (username) => {
  return request({ url: `/admin/reject-seller/${username}`, method: 'post', headers: { token: localStorage.getItem('token') } })
}
