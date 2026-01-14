/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: cartApi.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: API wrapper for Shopping Cart operations.
 *              - Handles listing, adding, removing, and clearing cart items.
 * Others:
 * Function List:
 * 1. listCart - Retrieves the user's shopping cart items.
 * 2. addToCart - Adds a book to the shopping cart.
 * 3. removeFromCart - Removes a book from the cart (or decreases quantity).
 * 4. clearCart - Removes all items from the cart.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import request from './request'

/**
 * Function: listCart
 * Description: Retrieves all items in the current user's shopping cart.
 * Calls: GET /cart/list
 * Input: None (uses token for auth)
 * Output: Promise resolving to cart item list
 * Return: Promise
 */
export const listCart = () => {
  return request({
    url: '/cart/list',
    method: 'get',
    headers: { token: sessionStorage.getItem('token') }
  })
}

/**
 * Function: addToCart
 * Description: Adds a book to the user's shopping cart.
 * Calls: POST /cart/add/{bookId}
 * Input: bookId (Number/String)
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const addToCart = (bookId) => {
  return request({
    url: `/cart/add/${bookId}`,
    method: 'post',
    headers: { token: sessionStorage.getItem('token') }
  })
}

/**
 * Function: removeFromCart
 * Description: Removes a book from the cart.
 *              If count is provided, decreases quantity by count.
 *              If count is not provided, removes the item entirely.
 * Calls: DELETE /cart/remove/{bookId}
 * Input: bookId (Number/String), count (Number, optional)
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const removeFromCart = (bookId, count) => {
  return request({
    url: `/cart/remove/${bookId}`,
    method: 'delete',
    params: count ? { count } : undefined,
    headers: { token: sessionStorage.getItem('token') }
  })
}

/**
 * Function: clearCart
 * Description: Clears all items from the user's shopping cart.
 * Calls: POST /cart/clear
 * Input: None
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const clearCart = () => {
  return request({
    url: '/cart/clear',
    method: 'post',
    headers: { token: sessionStorage.getItem('token') }
  })
}
