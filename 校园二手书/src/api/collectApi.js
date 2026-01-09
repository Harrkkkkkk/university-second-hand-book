/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: collectApi.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: API wrapper for Favorites/Collection operations.
 *              - Handles adding, removing, and listing favorite books.
 * Others:
 * Function List:
 * 1. listFavorites - Retrieves detailed list of favorite books.
 * 2. addFavorite - Adds a book to favorites.
 * 3. removeFavorite - Removes a book from favorites.
 * 4. checkFavorite - Checks if a book is in favorites.
 * 5. getCollectedIds - Retrieves list of favorite book IDs.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import request from './request'

/**
 * Function: listFavorites
 * Description: Retrieves detailed information about books in the user's favorites.
 * Calls: GET /favorites/list
 * Input: None
 * Output: Promise resolving to list of Book objects
 * Return: Promise
 */
export const listFavorites = () => {
  return request({
    url: '/favorites/list',
    method: 'get',
    headers: { token: sessionStorage.getItem('token') }
  })
}

/**
 * Function: addFavorite
 * Description: Adds a book to the user's favorites list.
 * Calls: POST /favorites/add/{bookId}
 * Input: bookId (Number/String)
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const addFavorite = (bookId) => {
  return request({
    url: `/favorites/add/${bookId}`,
    method: 'post',
    headers: { token: sessionStorage.getItem('token') }
  })
}

/**
 * Function: removeFavorite
 * Description: Removes a book from the user's favorites list.
 * Calls: DELETE /favorites/remove/{bookId}
 * Input: bookId (Number/String)
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const removeFavorite = (bookId) => {
  return request({
    url: `/favorites/remove/${bookId}`,
    method: 'delete',
    headers: { token: sessionStorage.getItem('token') }
  })
}

/**
 * Function: checkFavorite
 * Description: Checks if a specific book is currently in the user's favorites.
 * Calls: GET /favorites/check/{bookId}
 * Input: bookId (Number/String)
 * Output: Promise resolving to boolean status
 * Return: Promise
 */
export const checkFavorite = (bookId) => {
  return request({
    url: `/favorites/check/${bookId}`,
    method: 'get',
    headers: { token: sessionStorage.getItem('token') }
  })
}

/**
 * Function: getCollectedIds
 * Description: Retrieves the list of IDs of all books in the user's favorites.
 *              Useful for initializing UI state (e.g., heart icons).
 * Calls: GET /favorites/ids
 * Input: None
 * Output: Promise resolving to list of book IDs
 * Return: Promise
 */
export const getCollectedIds = () => {
  return request({
    url: '/favorites/ids',
    method: 'get',
    headers: { token: sessionStorage.getItem('token') }
  })
}
