import { ElMessage } from 'element-plus'
import router from '@/router/index.js'

// 退出登录并返回登录页
export const logoutAndBackToLogin = () => {
    // 清空本地存储的登录状态
    localStorage.clear()
    // 提示退出成功
    ElMessage.success('已退出登录，返回登录页！')
    // 跳转到登录页
    router.push('/login')
}