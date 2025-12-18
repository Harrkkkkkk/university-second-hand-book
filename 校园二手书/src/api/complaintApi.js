import request from './request'

export const listReceivedComplaints = () => {
  return request({ url: '/complaints/received', method: 'get', headers: { token: localStorage.getItem('token') } })
}

