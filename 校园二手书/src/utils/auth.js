/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: auth.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: Authentication utility functions.
 *              - Handles logout and navigation to login page.
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import { ElMessage } from 'element-plus'
import router from '@/router/index.js'

/**
 * Function: logoutAndBackToLogin
 * Description: Clears local storage (token, user info) and redirects to login page.
 * Input: None
 * Output: Redirects to /login
 */
export const logoutAndBackToLogin = () => {
    // 清空本地存储的登录状态
    localStorage.clear()
    // 提示退出成功
    ElMessage.success('已退出登录，返回登录页！')
    // 跳转到登录页
    router.push('/login')
}
