/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: request.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: Axios instance configuration.
 *              - Sets base URL and timeout.
 *              - Configures request and response interceptors for global error handling and data extraction.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import axios from 'axios'

// Create Axios instance
// 检测环境
const isDevelopment = import.meta.env.DEV
// 生产环境中，baseURL会根据实际部署情况自动处理
// 可以是相对路径或完整的API地址
const baseURL = isDevelopment 
    ? '/book-api'  // 开发环境使用代理
    : '/book-api' // 生产环境使用相对路径

const service = axios.create({
    baseURL: baseURL,
    timeout: 5000
})

// Request Interceptor
service.interceptors.request.use(
    (config) => {
        // Add token to all requests
        const token = sessionStorage.getItem('token')
        if (token) {
            config.headers.token = token
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// Response Interceptor
service.interceptors.response.use(
    (response) => {
        // Return only the data part of the response
        return response.data
    },
    (error) => {
        let msg = error.message
        if (error.response && error.response.data) {
            // Prefer backend error message if available
            // Backend returns: {"message": "..."} or plain string
            if (error.response.data.message) {
                msg = error.response.data.message
            } else if (typeof error.response.data === 'string') {
                msg = error.response.data
            }
        }
        console.error('请求失败：', msg)
        error.message = msg // Override error message for UI display
        return Promise.reject(error)
    }
)

export default service