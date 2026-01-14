/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: adminApi.js
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: API module for administrative operations.
 *              Handles admin-specific requests like user management,
 *              book reviews, and complaint handling.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 */
import request from './request'

/**
 * Function: listUnderReviewBooks
 * Description: Fetches the list of books currently pending review.
 * Called By: AdminDashboard.vue
 * Method: GET
 * URL: /admin/review/books
 * Output: Promise<Array<Book>> - List of books under review
 */
export const listUnderReviewBooks = () => {
  return request({ url: '/admin/review/books', method: 'get', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: approveBook
 * Description: Approves a book listing, making it public.
 * Input: id (Number) - ID of the book to approve
 * Method: POST
 * URL: /admin/review/books/{id}/approve
 * Output: Promise<Object> - Response result
 */
export const approveBook = (id) => {
  return request({ url: `/admin/review/books/${id}/approve`, method: 'post', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: rejectBook
 * Description: Rejects a book listing.
 * Input: id (Number) - ID of the book to reject
 * Method: POST
 * URL: /admin/review/books/{id}/reject
 * Output: Promise<Object> - Response result
 */
export const rejectBook = (id, reason) => {
  return request({ url: `/admin/review/books/${id}/reject`, method: 'post', data: { reason }, headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: listUsers
 * Description: Searches for users based on a keyword.
 * Input: keyword (String) - Search term (username/email/phone)
 * Method: GET
 * URL: /admin/users
 * Output: Promise<Array<User>> - List of matching users
 */
export const listUsers = (keyword) => {
  return request({ url: '/admin/users', method: 'get', params: { keyword }, headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: setUserRole
 * Description: Updates a user's role (e.g., to 'admin' or 'user').
 * Input: 
 *   - username (String): Target user
 *   - role (String): New role
 * Method: POST
 * URL: /admin/users/{username}/role
 * Output: Promise<Object> - Response result
 */
export const setUserRole = (username, role) => {
  return request({ url: `/admin/users/${username}/role`, method: 'post', params: { role }, headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: deleteUser
 * Description: Permanently deletes a user account.
 * Input: username (String) - User to delete
 * Method: DELETE
 * URL: /admin/users/{username}
 * Output: Promise<Object> - Response result
 */
/*
export const deleteUser = (username) => {
  return request({ url: `/admin/users/${username}`, method: 'delete', headers: { token: sessionStorage.getItem('token') } })
}
*/

/**
 * Function: getUserDetail
 * Description: Retrieves detailed information about a specific user.
 * Input: username (String) - User to retrieve
 * Method: GET
 * URL: /admin/users/{username}
 * Output: Promise<User> - User details object
 */
export const getUserDetail = (username) => {
  return request({ url: `/admin/users/${username}`, method: 'get', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: updateUserStatus
 * Description: Updates user status (e.g., blacklist), requires dual admin approval.
 * Input: 
 *   - username (String): Target user
 *   - status (String): New status
 *   - reason (String): Reason for change
 *   - secondAdmin (String): Username of second approving admin
 *   - secondAdminPwd (String): Password of second approving admin
 *   - Method: POST
 * URL: /admin/users/{username}/status
 * Output: Promise<Object> - Response result
 */
export const updateUserStatus = (username, status, reason, secondAdmin, secondAdminPwd) => {
  return request({ url: `/admin/users/${username}/status`, method: 'post', data: { status, reason, secondAdmin, secondAdminPwd }, headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: undoBlacklist
 * Description: Removes a user from the blacklist.
 * Input: 
 *   - username (String): Target user
 *   - reason (String): Reason for removal
 * Method: POST
 * URL: /admin/users/{username}/undo-blacklist
 * Output: Promise<Object> - Response result
 */
export const undoBlacklist = (username, reason) => {
  return request({ url: `/admin/users/${username}/undo-blacklist`, method: 'post', data: { reason }, headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: updateUserInfo
 * Description: Updates generic user information by admin.
 * Input: 
 *   - username (String): Target user
 *   - data (Object): Fields to update
 * Method: POST
 * URL: /admin/users/{username}/update
 * Output: Promise<Object> - Response result
 */
export const updateUserInfo = (username, data) => {
  return request({ url: `/admin/users/${username}/update`, method: 'post', data, headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: getOperationLogs
 * Description: Retrieves administrative operation logs.
 * Input: params (Object) - Query parameters (page, limit, etc.)
 * Method: GET
 * URL: /admin/users/logs
 * Output: Promise<Array<OperationLog>> - List of logs
 */
export const getOperationLogs = (params) => {
    return request({
        url: '/admin/logs',
        method: 'get',
        params,
        headers: { token: sessionStorage.getItem('token') }
    })
}

/**
 * Function: getDashboardStats
 * Description: Retrieves dashboard statistics (DAU, Transactions).
 * Input: days (Number) - Number of days to retrieve
 * Method: GET
 * URL: /admin/stats/dashboard
 */
export const getDashboardStats = (days) => {
    return request({
        url: '/admin/stats/dashboard',
        method: 'get',
        params: { days },
        headers: { token: sessionStorage.getItem('token') }
    })
}

/**
 * Function: exportStats
 * Description: Exports statistics as CSV.
 * Input: days (Number) - Number of days to export
 * Method: GET
 * URL: /admin/stats/export
 */
export const exportStats = (days) => {
    return request({
        url: '/admin/stats/export',
        method: 'get',
        params: { days },
        responseType: 'blob', // Important for file download
        headers: { token: sessionStorage.getItem('token') }
    })
}

/**
 * Function: listComplaints
 * Description: Fetches all user complaints.
 * Method: GET
 * URL: /admin/complaints
 * Output: Promise<Array<Complaint>> - List of complaints
 */
export const listComplaints = () => {
  return request({ url: '/admin/complaints', method: 'get', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: approveComplaint
 * Description: Resolves a complaint as valid/approved.
 * Input: id (Number) - Complaint ID
 * Method: POST
 * URL: /admin/complaints/{id}/approve
 * Output: Promise<Object> - Response result
 */
export const approveComplaint = (id) => {
  return request({ url: `/admin/complaints/${id}/approve`, method: 'post', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: rejectComplaint
 * Description: Rejects a complaint.
 * Input: id (Number) - Complaint ID
 * Method: POST
 * URL: /admin/complaints/{id}/reject
 * Output: Promise<Object> - Response result
 */
export const rejectComplaint = (id, reason) => {
  return request({ url: `/admin/complaints/${id}/reject`, method: 'post', data: { reason }, headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: undoBookAudit
 * Description: Undo a book audit within 24 hours.
 * Input: id (Number) - Book ID
 * Method: POST
 * URL: /admin/review/books/{id}/undo
 */
export const undoBookAudit = (id) => {
  return request({ url: `/admin/review/books/${id}/undo`, method: 'post', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: undoComplaintAudit
 * Description: Undo a complaint audit within 24 hours.
 * Input: id (Number) - Complaint ID
 * Method: POST
 * URL: /admin/complaints/{id}/undo
 */
export const undoComplaintAudit = (id) => {
  return request({ url: `/admin/complaints/${id}/undo`, method: 'post', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: listPendingReviews
 * Description: Lists reviews pending moderation.
 * Method: GET
 * URL: /admin/reviews/pending
 */
export const listPendingReviews = () => {
  return request({ url: '/admin/reviews/pending', method: 'get', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: auditReview
 * Description: Approves or rejects a review with optional reason.
 * Input: id (Number) - Review ID
 *        status (String) - 'approved' or 'rejected'
 *        reason (String) - optional reason
 * Method: POST
 * URL: /admin/reviews/{id}/audit
 */
export const auditReview = (id, status, reason) => {
  return request({ url: `/admin/reviews/${id}/audit`, method: 'post', data: { status, reason }, headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: undoReviewAudit
 * Description: Undo a review audit within 24 hours.
 * Input: id (Number) - Review ID
 * Method: POST
 * URL: /admin/reviews/{id}/undo
 */
export const undoReviewAudit = (id) => {
  return request({ url: `/admin/reviews/${id}/undo`, method: 'post', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: listSellerApplications
 * Description: Fetches pending seller applications.
 * Method: GET
 * URL: /admin/seller-applications
 * Output: Promise<Array<User>> - List of applicants
 */
export const listSellerApplications = () => {
  return request({ url: '/admin/seller-applications', method: 'get', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: approveSeller
 * Description: Approves a user's application to become a seller.
 * Input: username (String) - Applicant username
 * Method: POST
 * URL: /admin/approve-seller/{username}
 * Output: Promise<Object> - Response result
 */
export const approveSeller = (username) => {
  return request({ url: `/admin/approve-seller/${username}`, method: 'post', headers: { token: sessionStorage.getItem('token') } })
}

/**
 * Function: rejectSeller
 * Description: Rejects a user's application to become a seller.
 * Input: username (String) - Applicant username
 * Method: POST
 * URL: /admin/reject-seller/{username}
 * Output: Promise<Object> - Response result
 */
export const rejectSeller = (username) => {
  return request({ url: `/admin/reject-seller/${username}`, method: 'post', headers: { token: sessionStorage.getItem('token') } })
}
