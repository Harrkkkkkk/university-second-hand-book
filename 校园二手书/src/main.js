// 第一步：强制清空localStorage（清除旧登录状态）
localStorage.clear()

import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/global.css' // Global styles
import App from './App.vue'
import router from './router/index.js'

// 第二步：入口处强制校验，未登录则硬跳登录页
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.meta.requireAuth && !token) {
        next('/login')
        return
    }
    next()
})

// 第三步：创建应用
const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')

// 第四步：页面加载后再次强制跳登录页（双重保障）
window.onload = () => {
    const token = localStorage.getItem('token')
    if (!token && window.location.pathname !== '/login') {
        window.location.href = '/login' // 硬跳转，绕过所有缓存
    }
}