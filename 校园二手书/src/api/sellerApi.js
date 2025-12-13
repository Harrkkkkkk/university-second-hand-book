import request from './request'

export const listMyBooks = () => {
  return request({
    url: '/books/owner/list',
    method: 'get',
    headers: { token: localStorage.getItem('token') }
  })
}

export const updateBook = (id, data) => {
  return request({
    url: `/books/${id}`,
    method: 'put',
    data,
    headers: { token: localStorage.getItem('token') }
  })
}

export const offlineBook = (id) => {
  return request({
    url: `/books/offline/${id}`,
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}

export const deleteBook = (id) => {
  return request({
    url: `/books/${id}`,
    method: 'delete',
    headers: { token: localStorage.getItem('token') }
  })
}
