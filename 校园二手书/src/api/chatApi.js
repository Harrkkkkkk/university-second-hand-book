/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: chatApi.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: API wrapper for Chat/Messaging operations.
 *              - Handles sending messages, retrieving history, and managing conversations.
 * Others:
 * Function List:
 * 1. sendMessage - Sends a chat message.
 * 2. getHistory - Retrieves chat history for a conversation.
 * 3. getConversations - Retrieves a list of all active conversations for the user.
 * 4. markRead - Marks messages from a specific user as read.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import request from './request'

/**
 * Function: sendMessage
 * Description: Sends a chat message to another user.
 * Calls: POST /chat/send
 * Input: data (Object) - { toUser, content, bookId, orderId }
 * Output: Promise resolving to sent message object
 * Return: Promise
 */
export const sendMessage = (data) => {
  return request({ url: '/chat/send', method: 'post', data, headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: getHistory
 * Description: Retrieves chat history with another user in a specific context (book or order).
 * Calls: GET /chat/history
 * Input: params (Object) - { peer, bookId, orderId }
 * Output: Promise resolving to list of chat messages
 * Return: Promise
 */
export const getHistory = (params) => {
  return request({ url: '/chat/history', method: 'get', params, headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: getConversations
 * Description: Retrieves a list of all active conversations for the current user.
 *              Includes last message and unread count.
 * Calls: GET /chat/conversations
 * Input: None
 * Output: Promise resolving to list of conversation summaries
 * Return: Promise
 */
export const getConversations = () => {
  return request({ url: '/chat/conversations', method: 'get', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: markRead
 * Description: Marks all unread messages from a specific peer as read.
 * Calls: POST /chat/read/{peer}
 * Input: peer (String) - Username of the sender
 * Output: Promise resolving to success message
 * Return: Promise
 */
export const markRead = (peer) => {
  return request({ url: `/chat/read/${peer}`, method: 'post', headers: { token: sessionStorage.getItem('token') } })
}
