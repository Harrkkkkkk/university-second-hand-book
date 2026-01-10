/**
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: index.js
 * Author: WiseBookPal Team   Version: 1.0   Date: 2026-01-02
 * Description: Vue Router configuration.
 *              - Defines application routes for Buyers, Sellers, and Admins.
 *              - Implements global navigation guards for authentication and role-based access control (RBAC).
 * History:
 * <author>          <time>          <version>          <desc>
 * WiseBookPal Team  2026-01-02      1.0                Initial implementation
 */

import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 登录页（核心：默认跳转页）
import Login from '@/pages/Login.vue'
import Register from '@/pages/Register.vue'
import IdentityVerify from '@/pages/IdentityVerify.vue'
import Appeal from '@/pages/common/Appeal.vue'

// 买家专属页面
import BuyerHome from '@/pages/buyer/Home.vue'
import BuyerOrder from '@/pages/buyer/Order.vue'
import BuyerCollect from '@/pages/buyer/Collect.vue'
import BuyerProfile from '@/pages/buyer/Profile.vue' // 买家个人中心
import BuyerCart from '@/pages/buyer/Cart.vue'
import BuyerEvaluate from '@/pages/buyer/Evaluate.vue'
import BuyerComplaint from '@/pages/buyer/Complaint.vue'
import BuyerSellerDetail from '@/pages/buyer/SellerDetail.vue'
import Chat from '@/pages/Chat.vue'

// 公共页面
import BookDetail from '@/pages/BookDetail.vue'
import MessageCenter from '@/pages/MessageCenter.vue'

// 卖家页面
import PublishBook from '@/pages/PublishBook.vue'
import SellerCenter from '@/pages/seller/Center.vue'
import SellerApply from '@/pages/seller/Apply.vue'


// 管理员页面
import AdminDashboard from '@/pages/admin/Dashboard.vue'
import AdminAppealList from '@/pages/admin/AppealList.vue'

// 路由配置（核心：默认跳登录页）
const routes = [
    // 根路由强制跳登录页（解决首次进入直接进买家页问题）
    { path: '/', redirect: '/login' },
    // 登录页
    { path: '/login', name: 'Login', component: Login },
    { path: '/register', name: 'Register', component: Register },
    { path: '/verify-identity', name: 'IdentityVerify', component: IdentityVerify, meta: { requireAuth: true, roles: ['buyer', 'seller', 'admin'] } },
    { path: '/appeal', name: 'Appeal', component: Appeal, meta: { requireAuth: true, roles: ['buyer', 'seller', 'admin'] } },

    // ---------------------- 买家专属路由 ----------------------
    {
        path: '/buyer/home',
        name: 'BuyerHome',
        component: BuyerHome,
        meta: { requireAuth: true, roles: ['buyer', 'seller'] } // 需登录+买家/卖家角色
    },
    {
        path: '/buyer/order',
        name: 'BuyerOrder',
        component: BuyerOrder,
        meta: { requireAuth: true, roles: ['buyer', 'seller'] }
    },
    {
        path: '/buyer/collect',
        name: 'BuyerCollect',
        component: BuyerCollect,
        meta: { requireAuth: true, roles: ['buyer', 'seller'] }
    },
    {
        path: '/buyer/cart',
        name: 'BuyerCart',
        component: BuyerCart,
        meta: { requireAuth: true, roles: ['buyer', 'seller'] }
    },
    {
        path: '/buyer/evaluate',
        name: 'BuyerEvaluate',
        component: BuyerEvaluate,
        meta: { requireAuth: true, roles: ['buyer', 'seller'] }
    },
    {
        path: '/buyer/complaint',
        name: 'BuyerComplaint',
        component: BuyerComplaint,
        meta: { requireAuth: true, roles: ['buyer', 'seller'] }
    },
    {
        path: '/seller/detail/:name',
        name: 'SellerDetail',
        component: BuyerSellerDetail,
        meta: { requireAuth: true, roles: ['buyer', 'seller', 'admin'] }
    },
    {
        path: '/chat',
        name: 'Chat',
        component: Chat,
        meta: { requireAuth: true, roles: ['buyer','seller','admin'] }
    },
    {
        path: '/messages',
        name: 'MessageCenter',
        component: MessageCenter,
        meta: { requireAuth: true, roles: ['buyer','seller','admin'] }
    },
    {
        path: '/buyer/profile', // 买家个人中心路由
        name: 'BuyerProfile',
        component: BuyerProfile,
        meta: { requireAuth: true, roles: ['buyer', 'seller'] }
    },

    // ---------------------- 公共路由（所有角色可访问） ----------------------
    {
        path: '/book/:id', // 教材详情页（带参数）
        name: 'BookDetail',
        component: BookDetail,
        meta: { requireAuth: true, roles: ['buyer', 'seller', 'admin'] }
    },


    // ---------------------- 卖家专属路由 ----------------------
    {
        path: '/seller/center',
        name: 'SellerCenter',
        component: SellerCenter,
        meta: { requireAuth: true, roles: ['seller', 'buyer'] } // 买家申请通过后也可以访问
    },
    {
        path: '/seller/apply',
        name: 'SellerApply',
        component: SellerApply,
        meta: { requireAuth: true, roles: ['buyer'] }
    },
    {
        path: '/publish', // 发布教材
        name: 'PublishBook',
        component: PublishBook,
        meta: { requireAuth: true, roles: ['seller'] }
    },

    // ---------------------- 管理员专属路由 ----------------------
    {
        path: '/admin/dashboard', // 管理员后台
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: { requireAuth: true, roles: ['admin'] }
    },
    {
        path: '/admin/appeals', // 管理员-申诉处理
        name: 'AdminAppealList',
        component: AdminAppealList,
        meta: { requireAuth: true, roles: ['admin'] }
    },

    // 404页面（可选，路由匹配失败时跳转）
    {
        path: '/:pathMatch(.*)*',
        redirect: '/login' // 未知路由跳登录页
    }
]

// 创建路由实例（禁用缓存+严格模式）
const router = createRouter({
    history: createWebHistory(), // HTML5历史模式（无#）
    routes,
    strict: true, // 禁用模糊匹配，防止路由冲突
    sensitive: true, // 路由区分大小写（可选，增强精准性）
    scrollBehavior: () => ({ top: 0 }) // 页面跳转后回到顶部
})

// ---------------------- 核心：路由守卫（强制登录+权限控制） ----------------------
/**
 * Global Navigation Guard
 * Description: Checks authentication and role permissions before every route change.
 * Logic:
 * 1. If route requires auth and user is not logged in -> Redirect to /login
 * 2. If user is logged in but has insufficient role -> Redirect to role-specific home
 * 3. If user is logged in and tries to access /login -> Redirect to role-specific home
 */
router.beforeEach((to, from, next) => {
    // 从sessionStorage读取登录状态（强制校验）
    const token = sessionStorage.getItem('token') || ''
    const userRole = sessionStorage.getItem('role') || ''

    // 规则1：所有需要权限的页面，未登录直接跳登录页
    if (to.meta.requireAuth) {
        // 未登录：拦截并跳登录页
        if (!token) {
            ElMessage.warning('请先登录后再访问！')
            next('/login')
            return
        }
        // 已登录但角色不匹配：拦截并提示
        if (userRole && !to.meta.roles.includes(userRole)) {
            ElMessage.error(`当前${userRole === 'seller' ? '卖家' : userRole === 'admin' ? '管理员' : '买家'}角色无权限访问该页面！`)
            // 跳转到对应角色的首页
            if (userRole === 'buyer') next('/buyer/home')
            else if (userRole === 'seller') next('/seller/center')
            else if (userRole === 'admin') next('/admin/dashboard')
            return
        }
    }

    // 规则2：已登录状态访问登录页，自动跳对应角色首页
    if (to.path === '/login' && token) {
        if (userRole === 'buyer') {
            next('/buyer/home') // 买家跳首页
        } else if (userRole === 'seller') {
            next('/seller/center') // 卖家跳中心
        } else if (userRole === 'admin') {
            next('/admin/dashboard') // 管理员跳后台
        }
        return
    }

    // 规则3：无特殊限制，正常跳转
    next()
})

export default router
