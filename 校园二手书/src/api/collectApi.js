import request from './request'

export const listFavorites = () => {
  return request({
    url: '/favorites/list',
    method: 'get',
    headers: { token: localStorage.getItem('token') }
  })
}

export const addFavorite = (bookId) => {
  return request({
    url: `/favorites/add/${bookId}`,
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}

export const removeFavorite = (bookId) => {
  return request({
    url: `/favorites/remove/${bookId}`,
    method: 'delete',
    headers: { token: localStorage.getItem('token') }
  })
}
