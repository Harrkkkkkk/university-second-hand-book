/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: sellerApi.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: API wrapper for Seller operations (Book Management).
 *              - Handles listing, updating, offlining, and deleting books by the seller.
 * Others:
 * Function List:
 * 1. listMyBooks - Retrieves all books listed by the current user.
 * 2. updateBook - Updates details of a specific book.
 * 3. offlineBook - Takes a book offline (sets status to off_shelf).
 * 4. deleteBook - Permanently deletes a book listing.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import request from './request'

/**
 * Function: listMyBooks
 * Description: Retrieves a list of all books listed for sale by the current user.
 * Calls: GET /books/owner/list
 * Input: None
 * Output: Promise resolving to list of Book objects
 * Return: Promise
 */
export const listMyBooks = () => {
  return request({
    url: '/books/owner/list',
    method: 'get',
    headers: { token: localStorage.getItem('token') }
  })
}

/**
 * Function: updateBook
 * Description: Updates the details of a specific book listing.
 * Calls: PUT /books/{id}
 * Input: id (Number/String), data (Object) - updated book fields
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const updateBook = (id, data) => {
  return request({
    url: `/books/${id}`,
    method: 'put',
    data,
    headers: { token: localStorage.getItem('token') }
  })
}

/**
 * Function: offlineBook
 * Description: Takes a book offline (removes from public sale) without deleting it.
 * Calls: POST /books/offline/{id}
 * Input: id (Number/String)
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const offlineBook = (id) => {
  return request({
    url: `/books/offline/${id}`,
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}

/**
 * Function: deleteBook
 * Description: Permanently deletes a book listing.
 * Calls: DELETE /books/{id}
 * Input: id (Number/String)
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const deleteBook = (id) => {
  return request({
    url: `/books/${id}`,
    method: 'delete',
    headers: { token: localStorage.getItem('token') }
  })
}
