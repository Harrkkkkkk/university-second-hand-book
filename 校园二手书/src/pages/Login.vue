<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Login.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: User login page.
 *              Provides functionality for user authentication and redirection based on roles.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
<template>
  <div class="login-page">
    <div class="login-container">
      <el-card class="login-card" :body-style="{ padding: '40px' }">
        <div class="header">
          <h2 class="title">WiseBookPal</h2>
          <p class="subtitle">校园二手书交易平台</p>
        </div>
        <el-form
          :model="loginForm"
          :rules="loginRules"
          ref="loginFormRef"
          label-position="top"
          size="large"
          class="login-form"
        >
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入账号"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              class="full-width-btn submit-btn"
              @click="submitLogin"
              round
            >
              登录
            </el-button>
            <div class="links">
              <el-button type="primary" link @click="useTestAccount">使用测试账号</el-button>
              <el-button type="primary" link @click="toRegister">没有账号？去注册</el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { login as apiLogin } from '@/api/userApi'

const router = useRouter()
const route = useRoute()
const loginFormRef = ref(null)

// 登录表单
const loginForm = ref({
  username: '',
  password: ''
})

// 表单校验规则
const loginRules = ref({
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

// 测试账号（模拟数据）
const useTestAccount = () => {
  loginForm.value = { username: 'buyer1', password: '123456' }
}

const submitLogin = async () => {
  try {
    await loginFormRef.value.validate()
    const res = await apiLogin({
      username: loginForm.value.username,
      password: loginForm.value.password,
      role: 'user' // 默认传一个值，后端会根据用户名自动判断角色
    })
    if (!res || !res.token) {
      ElMessage.error('登录失败')
      return
    }
    sessionStorage.setItem('token', res.token)
    sessionStorage.setItem('role', res.role)
    sessionStorage.setItem('username', res.username)
    if (res.sellerStatus) sessionStorage.setItem('sellerStatus', res.sellerStatus)
    if (res.realName) sessionStorage.setItem('realName', res.realName)
    sessionStorage.setItem('isVerified', res.isVerified ? '1' : '0')
    
    ElMessage.success('登录成功！')
    
    // Check if there is a redirect query param
    const redirect = route.query.redirect
    if (redirect) {
      router.push(redirect)
      return
    }

    if (res.role === 'admin') {
      router.push('/admin/dashboard')
    } else {
      // 统一跳转到买家首页，卖家功能在菜单中入口
      router.push('/buyer/home')
    }
  } catch (error) {
    const code = error?.response?.status
    if (code === 401) {
      ElMessage.error('账号或密码错误')
    } else if (code === 500) {
      ElMessage.error('服务异常或后端未启动，请检查后端')
    } else {
      ElMessage.error('登录失败：' + (error?.message || '未知错误'))
    }
  }
}

const toRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}
.login-container {
  width: 100%;
  max-width: 500px;
}
.login-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  overflow: hidden;
}
.header {
  text-align: center;
  margin-bottom: 30px;
}
.title {
  margin: 0;
  font-size: 28px;
  color: #303133;
  font-weight: 600;
  letter-spacing: 1px;
}
.subtitle {
  margin: 10px 0 0;
  color: #909399;
  font-size: 14px;
}
.login-form .el-form-item {
  margin-bottom: 24px;
}
.full-width-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 1px;
}
.submit-btn {
  background: linear-gradient(90deg, #409eff 0%, #3a8ee6 100%);
  border: none;
  transition: all 0.3s ease;
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}
.links {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}
</style>
