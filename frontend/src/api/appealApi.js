/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: appealApi.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-11
 * Description: Frontend API wrapper for Blacklist Appeal operations.
 */

import request from './request'

/**
 * Function: submitAppeal
 * Description: Submit a blacklist appeal.
 * Method: POST /appeals/submit
 * Input: data (Object) - { reason, evidence }
 */
export const submitAppeal = (data) => {
    return request({
        url: '/appeals/submit',
        method: 'post',
        data,
        headers: {
            token: sessionStorage.getItem('token')
        }
    })
}

/**
 * Function: getAppealList
 * Description: Get list of appeals (Admin).
 * Method: GET /appeals/admin/list
 * Input: params (Object) - { status, page, size }
 */
export const getAppealList = (params) => {
    return request({
        url: '/appeals/admin/list',
        method: 'get',
        params,
        headers: {
            token: sessionStorage.getItem('token')
        }
    })
}

/**
 * Function: auditAppeal
 * Description: Audit an appeal (Admin).
 * Method: POST /appeals/admin/{id}/audit
 * Input: id (Long), data (Object) - { status, auditReason }
 */
export const auditAppeal = (id, data) => {
    return request({
        url: `/appeals/admin/${id}/audit`,
        method: 'post',
        data,
        headers: {
            token: sessionStorage.getItem('token')
        }
    })
}
