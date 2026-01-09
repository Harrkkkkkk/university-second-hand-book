// 第一步：不再清空存储，改为使用sessionStorage
// sessionStorage.clear() // 移除此行以保持刷新登录状态

import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/global.css' // Global styles
import App from './App.vue'
import router from './router/index.js'

// 第二步：入口处强制校验，未登录则硬跳登录页
router.beforeEach((to, from, next) => {
    const token = sessionStorage.getItem('token')
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
    const token = sessionStorage.getItem('token')
    if (!token && window.location.pathname !== '/login') {
        window.location.href = '/login' // 硬跳转，绕过所有缓存
    }
}