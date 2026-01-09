/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: notificationApi.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: API wrapper for Notification operations.
 *              - Handles retrieving, counting, and marking notifications as read.
 *              - Supports system-wide announcements (admin only).
 * Others:
 * Function List:
 * 1. getUnreadCount - Gets the count of unread notifications.
 * 2. listNotifications - Lists all notifications for the user.
 * 3. markRead - Marks a specific notification as read.
 * 4. markAllRead - Marks all notifications as read.
 * 5. announce - Sends a system-wide announcement (Admin).
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import request from '@/api/request'

/**
 * Function: getUnreadCount
 * Description: Retrieves the number of unread notifications for the current user.
 * Calls: GET /notifications/unread-count
 * Input: None
 * Output: Promise resolving to count (Number)
 * Return: Promise
 */
export const getUnreadCount = async () => {
  const res = await request({
    url: '/notifications/unread-count',
    method: 'get',
    headers: { token: sessionStorage.getItem('token') }
  })
  return res ? res.count : 0
}

/**
 * Function: listNotifications
 * Description: Retrieves all notifications for the current user.
 * Calls: GET /notifications/list
 * Input: None
 * Output: Promise resolving to list of notifications
 * Return: Promise
 */
export const listNotifications = async () => {
  return await request({
    url: '/notifications/list',
    method: 'get',
    headers: { token: sessionStorage.getItem('token') }
  })
}

/**
 * Function: markRead
 * Description: Marks a specific notification as read.
 * Calls: POST /notifications/read/{id}
 * Input: id (Number)
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const markRead = async (id) => {
  return await request({
    url: `/notifications/read/${id}`,
    method: 'post',
    headers: { token: sessionStorage.getItem('token') }
  })
}

/**
 * Function: markAllRead
 * Description: Marks all notifications for the current user as read.
 * Calls: POST /notifications/mark-all-read
 * Input: None
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const markAllRead = async () => {
  return await request({
    url: '/notifications/mark-all-read',
    method: 'post',
    headers: { token: sessionStorage.getItem('token') }
  })
}

/**
 * Function: announce
 * Description: Sends a system-wide announcement to all users (Admin only).
 * Calls: POST /notifications/announce
 * Input: data (Object) - { title, content }
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const announce = async (data) => {
  return await request({
    url: '/notifications/announce',
    method: 'post',
    data,
    headers: { token: sessionStorage.getItem('token') }
  })
}

export const listSettlements = async () => {
  return await request({
    url: '/notifications/settlements',
    method: 'get',
    headers: { token: sessionStorage.getItem('token') }
  })
}

export const exportSettlements = async () => {
  return await request({
    url: '/notifications/settlements/export',
    method: 'get',
    headers: { token: sessionStorage.getItem('token') },
    responseType: 'text'
  })
}
