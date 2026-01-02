/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: orderApi.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: Frontend API wrapper for Order-related operations.
 *              - Communicates with backend OrderController.
 *              - Handles order creation, payment, cancellation, and status updates.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import request from './request'

/**
 * Function: listOrders
 * Description: Lists all orders for the current user (buyer view).
 * Method: GET /orders/list
 */
export const listOrders = () => {
  return request({
    url: '/orders/list',
    method: 'get',
    headers: { token: localStorage.getItem('token') }
  })
}

/**
 * Function: listSellerOrders
 * Description: Lists all orders for the current user (seller view).
 * Method: GET /orders/seller/list
 */
export const listSellerOrders = () => {
  return request({
    url: '/orders/seller/list',
    method: 'get',
    headers: { token: localStorage.getItem('token') }
  })
}

/**
 * Function: createOrder
 * Description: Creates a new order for a specific book.
 * Method: POST /orders/create
 * Input: bookId (Number)
 */
export const createOrder = (bookId) => {
  return request({
    url: '/orders/create',
    method: 'post',
    params: { bookId },
    headers: { token: localStorage.getItem('token') }
  })
}

/**
 * Function: payOrder
 * Description: Simulates payment for an order.
 * Method: POST /orders/pay/{id}
 * Input: id (Number) - Order ID
 */
export const payOrder = (id) => {
  return request({
    url: `/orders/pay/${id}`,
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}

/**
 * Function: cancelOrder
 * Description: Cancels an unpaid order.
 * Method: POST /orders/cancel/{id}
 * Input: id (Number) - Order ID
 */
export const cancelOrder = (id) => {
  return request({
    url: `/orders/cancel/${id}`,
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}

/**
 * Function: receiveOrder
 * Description: Marks an order as received (completed).
 * Method: POST /orders/receive/{id}
 * Input: id (Number) - Order ID
 */
export const receiveOrder = (id) => {
  return request({
    url: `/orders/receive/${id}`,
    method: 'post',
    headers: { token: localStorage.getItem('token') }
  })
}
