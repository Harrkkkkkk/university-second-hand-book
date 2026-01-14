import request from './request'

export function getBookInfoByIsbn(isbn) {
  return request({
    url: '/isbn/info',
    method: 'get',
    params: { isbn }
  })
}
