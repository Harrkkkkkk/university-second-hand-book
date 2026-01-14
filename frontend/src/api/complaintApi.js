/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: complaintApi.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: API wrapper for Complaint operations.
 *              - Handles retrieving complaints received by the user.
 * Others:
 * Function List:
 * 1. listReceivedComplaints - Retrieves complaints filed against the current user.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import request from './request'

/**
 * Function: listReceivedComplaints
 * Description: Retrieves a list of complaints where the current user is the target (respondent).
 * Calls: GET /complaints/received
 * Input: None
 * Output: Promise resolving to list of complaints
 * Return: Promise
 */
export const listReceivedComplaints = () => {
  return request({ url: '/complaints/received', method: 'get', headers: { token: sessionStorage.getItem('token') } })
}
