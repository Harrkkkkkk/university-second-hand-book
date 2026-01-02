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

export const getHotBooks = (limit = 6) => {
    return request({
        url: '/books/hot',
        method: 'get',
        params: { limit }
    })
}

export const getSearchSuggestions = (keyword) => {
    return request({
        url: '/books/suggestions',
        method: 'get',
        params: { keyword }
    })
}

export const uploadFile = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/files/upload',
        method: 'post',
        data: formData,
        headers: {
            token: localStorage.getItem('token')
        }
    })
}
