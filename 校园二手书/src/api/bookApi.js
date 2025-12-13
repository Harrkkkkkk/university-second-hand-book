import request from './request'

export const addBook = (data) => {
    return request({
        url: '/books/add',
        method: 'post',
        data,
        headers: {
            token: localStorage.getItem('token')
        }
    })
}

export const getBookPage = (params) => {
    return request({
        url: '/books/page',
        method: 'get',
        params
    })
}

export const getBookDetail = (id) => {
    return request({
        url: `/books/${id}`,
        method: 'get'
    })
}
