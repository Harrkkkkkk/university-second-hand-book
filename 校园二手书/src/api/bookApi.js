/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: bookApi.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: Frontend API wrapper for Book-related operations.
 *              - Communicates with backend BookController and FileController.
 *              - Handles book listing, details, search, and file uploads.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import request from './request'

/**
 * Function: addBook
 * Description: Creates a new book listing (seller only).
 * Method: POST /books/add
 * Input: data (Object) - { bookName, author, price, etc. }
 */
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

/**
 * Function: getBookPage
 * Description: Retrieves a paginated list of books with optional filters.
 * Method: GET /books/page
 * Input: params (Object) - { page, size, keyword, category, etc. }
 */
export const getBookPage = (params) => {
    return request({
        url: '/books/page',
        method: 'get',
        params
    })
}

/**
 * Function: getBookDetail
 * Description: Retrieves detailed information for a specific book.
 * Method: GET /books/{id}
 * Input: id (Number) - Book ID
 */
export const getBookDetail = (id) => {
    return request({
        url: `/books/${id}`,
        method: 'get'
    })
}

/**
 * Function: getHotBooks
 * Description: Retrieves a list of popular/hot books.
 * Method: GET /books/hot
 * Input: limit (Number) - Number of books to return (default 6)
 */
export const getHotBooks = (limit = 6) => {
    return request({
        url: '/books/hot',
        method: 'get',
        params: { limit }
    })
}

/**
 * Function: getSearchSuggestions
 * Description: specific search suggestions based on keyword.
 * Method: GET /books/suggestions
 * Input: keyword (String)
 */
export const getSearchSuggestions = (keyword) => {
    return request({
        url: '/books/suggestions',
        method: 'get',
        params: { keyword }
    })
}

/**
 * Function: uploadFile
 * Description: Uploads a file (e.g., book cover image).
 * Method: POST /files/upload
 * Input: file (File)
 * Output: File URL
 */
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
