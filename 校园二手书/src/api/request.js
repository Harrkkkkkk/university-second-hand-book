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
const service = axios.create({
    baseURL: '/book-api', // Proxy target defined in vite.config.js
    timeout: 5000
})

// Request Interceptor
service.interceptors.request.use(
    (config) => {
        // Can add global headers here if needed (e.g., token)
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
        console.error('请求失败：', error.message)
        return Promise.reject(error)
    }
)

export default service