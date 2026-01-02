/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: userApi.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: Frontend API wrapper for User-related operations.
 *              - Communicates with backend UserController.
 *              - Handles authentication, profile management, and address management.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import request from './request'

/**
 * Function: login
 * Description: Authenticates a user.
 * Method: POST /user/login
 * Input: data (Object) - { username, password }
 */
export const login = (data) => {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}

/**
 * Function: logout
 * Description: Logs out the current user.
 * Method: POST /user/logout
 */
export const logout = () => {
    return request({
        url: '/user/logout',
        method: 'post',
        headers: {
            token: localStorage.getItem('token') // 携带token
        }
    })
}

/**
 * Function: getUserInfo
 * Description: Retrieves current user's profile information.
 * Method: GET /user/info
 */
export const getUserInfo = () => {
    return request({
        url: '/user/info',
        method: 'get',
        headers: {
            token: localStorage.getItem('token')
        }
    })
}

/**
 * Function: register
 * Description: Registers a new user.
 * Method: POST /user/register
 * Input: data (Object) - { username, password, phone, studentId }
 */
export const register = (data) => {
    return request({
        url: '/user/register',
        method: 'post',
        data
    })
}

/**
 * Function: applySeller
 * Description: Submits a request to become a seller.
 * Method: POST /user/apply-seller
 */
export const applySeller = () => {
    return request({
        url: '/user/apply-seller',
        method: 'post',
        headers: {
            token: localStorage.getItem('token')
        }
    })
}

/**
 * Function: updateUserProfile
 * Description: Updates user profile details (e.g., phone, email).
 * Method: PUT /user/profile
 */
export const updateUserProfile = (data) => {
    return request({
        url: '/user/profile',
        method: 'put',
        data,
        headers: {
            token: localStorage.getItem('token')
        }
    })
}

/**
 * Function: changePassword
 * Description: Changes the user's password.
 * Method: POST /user/change-password
 */
export const changePassword = (data) => {
    return request({
        url: '/user/change-password',
        method: 'post',
        data,
        headers: {
            token: localStorage.getItem('token')
        }
    })
}

/**
 * Function: listAddresses
 * Description: Lists all shipping addresses for the user.
 * Method: GET /user/addresses
 */
export const listAddresses = () => {
    return request({
        url: '/user/addresses',
        method: 'get',
        headers: {
            token: localStorage.getItem('token')
        }
    })
}

/**
 * Function: addAddress
 * Description: Adds a new shipping address.
 * Method: POST /user/addresses
 */
export const addAddress = (data) => {
    return request({
        url: '/user/addresses',
        method: 'post',
        data,
        headers: {
            token: localStorage.getItem('token')
        }
    })
}

/**
 * Function: updateAddress
 * Description: Updates an existing address.
 * Method: PUT /user/addresses/{id}
 */
export const updateAddress = (id, data) => {
    return request({
        url: `/user/addresses/${id}`,
        method: 'put',
        data,
        headers: {
            token: localStorage.getItem('token')
        }
    })
}

/**
 * Function: getSellerStats
 * Description: Gets public statistics for a seller (credit score, sales count).
 * Method: GET /user/{username}/stats
 */
export const getSellerStats = (username) => {
    return request({
        url: `/user/${username}/stats`,
        method: 'get'
    })
}

/**
 * Function: deleteAddress
 * Description: Deletes a shipping address.
 * Method: DELETE /user/addresses/{id}
 */
export const deleteAddress = (id) => {
    return request({
        url: `/user/addresses/${id}`,
        method: 'delete',
        headers: {
            token: localStorage.getItem('token')
        }
    })
}
