/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: reviewApi.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: API wrapper for Review/Rating operations.
 *              - Handles retrieving reviews for users and sellers.
 *              - Calculates seller reputation (good rating rate).
 * Others:
 * Function List:
 * 1. listReceivedReviews - Retrieves reviews received by the current user.
 * 2. listSellerReviews - Retrieves reviews for a specific seller.
 * 3. getGoodRate - Calculates the percentage of good reviews for the current user.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import request from './request'

/**
 * Function: listReceivedReviews
 * Description: Retrieves all reviews received by the current user.
 * Calls: GET /reviews/received
 * Input: None
 * Output: Promise resolving to list of reviews
 * Return: Promise
 */
export const listReceivedReviews = () => {
  return request({ url: '/reviews/received', method: 'get', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: listSellerReviews
 * Description: Retrieves reviews for a specific seller by their username.
 * Calls: GET /reviews/seller/{sellerName}
 * Input: sellerName (String)
 * Output: Promise resolving to list of reviews
 * Return: Promise
 */
export const listSellerReviews = (sellerName) => {
  const encoded = encodeURIComponent(sellerName || '')
  return request({ url: `/reviews/seller/${encoded}`, method: 'get', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: getGoodRate
 * Description: Calculates and retrieves the "good rating" percentage for the current user.
 *              Typically used for displaying seller reputation.
 * Calls: GET /reviews/stats/good-rate
 * Input: None
 * Output: Promise resolving to percentage string (e.g., "98%")
 * Return: Promise
 */
export const getGoodRate = () => {
  return request({ url: '/reviews/stats/good-rate', method: 'get', headers: { token: sessionStorage.getItem('token') } })
}
