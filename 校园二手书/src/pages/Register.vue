<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Register.vue
 * Author: WiseBookPal Team Version: 1.2 Date: 2026-01-07
 * Description: User registration page with enhanced UI/UX and multi-step process.
 *              Step 1: Create Account (Username/Password) - Auto Login
 *              Step 2: Real-name Verification (Optional)
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 * 2. Date: 2026-01-07
 *    Author: WiseBookPal Team
 *    Modification: Added step wizard, inline verification, and UI enhancements
-->
<template>
  <div class="register-page">
    <div class="register-container">
      <el-card class="register-card" :body-style="{ padding: '40px' }">
        <div class="header">
          <h2 class="register-title">加入 WiseBookPal</h2>
          <p class="subtitle">开启您的校园二手书之旅</p>
        </div>

        <!-- Steps Indicator -->
        <div class="steps-wrapper">
          <el-steps :active="currentStep" finish-status="success" align-center class="custom-steps">
            <el-step title="创建账号" description="设置账号密码">
              <template #icon>
                <el-icon><User /></el-icon>
              </template>
            </el-step>
            <el-step title="实名认证" description="保障交易安全">
              <template #icon>
                <el-icon><CreditCard /></el-icon>
              </template>
            </el-step>
          </el-steps>
        </div>

        <!-- Step 1: Account Info -->
        <transition name="fade-slide" mode="out-in">
          <div v-if="currentStep === 0" key="step1" class="step-content">
            <el-form :model="form" :rules="rules" ref="formRef" label-position="top" size="large" class="register-form">
              <el-form-item label="账号" prop="username">
                <el-input 
                  v-model="form.username" 
                  placeholder="请输入账号（不得超过16个字符）" 
                  :prefix-icon="User" 
                  clearable
                />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input 
                  v-model="form.password" 
                  type="password" 
                  placeholder="请输入密码（6-25位，含字母数字）" 
                  :prefix-icon="Lock" 
                  show-password 
                  clearable
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  :prefix-icon="Lock"
                  show-password
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="full-width-btn submit-btn" :loading="loading" @click="handleNextStep" round>
                  下一步
                  <el-icon class="el-icon--right"><ArrowRight /></el-icon>
                </el-button>
              </el-form-item>
              <div class="login-link">
                <span>已有账号？</span>
                <el-button type="primary" link @click="toLogin">立即登录</el-button>
              </div>
            </el-form>
          </div>

          <!-- Step 2: Verification -->
          <div v-else-if="currentStep === 1" key="step2" class="step-content">
            <div class="verification-header">
              <el-alert
                title="安全提示"
                type="success"
                :closable="false"
                show-icon
              >
                <template #default>
                  账号已创建！为了保障交易安全，建议您现在完成实名认证。
                </template>
              </el-alert>
            </div>
            
            <el-form :model="verifyForm" :rules="verifyRules" ref="verifyFormRef" label-position="top" size="large" class="register-form">
              <el-form-item label="真实姓名" prop="name">
                <el-input 
                  v-model="verifyForm.name" 
                  placeholder="请输入您的真实姓名" 
                  :prefix-icon="Postcard" 
                  clearable
                />
              </el-form-item>
              <el-form-item label="学号" prop="studentId">
                <el-input 
                  v-model="verifyForm.studentId" 
                  placeholder="请输入您的学号" 
                  :prefix-icon="School" 
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" class="full-width-btn submit-btn" :loading="loading" @click="submitVerification" round>
                  完成认证
                </el-button>
                <el-button class="full-width-btn skip-btn" text @click="skipVerification">
                  跳过，稍后认证
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </transition>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowRight, CreditCard, Postcard, School } from '@element-plus/icons-vue'
import { register, login, verifyIdentity } from '@/api/userApi'

const router = useRouter()
const currentStep = ref(0)
const loading = ref(false)

// Step 1 Form
const formRef = ref(null)
const form = reactive({ username: '', password: '', confirmPassword: '' })
const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { max: 16, message: '账号长度不得超过16个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 25, message: '密码长度必须在6-25个字符之间', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*[0-9]).*$/, message: '密码必须同时包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

// Step 2 Form
const verifyFormRef = ref(null)
const verifyForm = reactive({ name: '', studentId: '' })
const verifyRules = {
  name: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }]
}

const toLogin = () => {
  router.push('/login')
}

/**
 * Function: handleNextStep
 * Description: Registers the user and auto-logs in to prepare for Step 2.
 */
const handleNextStep = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 1. Register
    const regRes = await register({ 
      username: form.username, 
      password: form.password, 
      roles: ['buyer'] 
    })
    
    if (!regRes || regRes.success !== true) {
      throw new Error('注册失败，账号可能已存在')
    }
    
    // 2. Auto Login to get Token
    const loginRes = await login({
      username: form.username,
      password: form.password,
      role: 'buyer'
    })
    
    if (!loginRes || !loginRes.token) {
      throw new Error('注册成功但自动登录失败，请手动登录')
    }
    
    // Store token
    sessionStorage.setItem('token', loginRes.token)
    sessionStorage.setItem('role', loginRes.role)
    sessionStorage.setItem('username', loginRes.username)
    sessionStorage.setItem('isVerified', '0') // Not verified yet
    
    ElMessage.success('账号创建成功，请进行实名认证')
    currentStep.value = 1
    
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    loading.value = false
  }
}

/**
 * Function: submitVerification
 * Description: Submits identity verification and navigates to home.
 */
const submitVerification = async () => {
  if (!verifyFormRef.value) return
  
  try {
    await verifyFormRef.value.validate()
    loading.value = true
    
    await verifyIdentity({
      name: verifyForm.name,
      studentId: verifyForm.studentId
    })
    
    // Update session storage
    sessionStorage.setItem('isVerified', '1')
    sessionStorage.setItem('realName', verifyForm.name)
    
    ElMessage.success('认证成功，欢迎加入！')
    router.push('/buyer/home')
    
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '认证失败，请检查信息或稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * Function: skipVerification
 * Description: Skips verification and navigates to home.
 */
const skipVerification = () => {
  ElMessage.info('已跳过实名认证，您可以在个人中心随时完成认证')
  router.push('/buyer/home')
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 500px;
}

.register-card {
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

.register-title {
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

.steps-wrapper {
  margin-bottom: 40px;
  padding: 0 10px;
}

/* Custom Steps Styling */
:deep(.el-step__title) {
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-step__description) {
  font-size: 12px;
}

.step-content {
  padding: 0 10px;
}

.register-form .el-form-item {
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

.skip-btn {
  margin-top: 15px;
  color: #909399;
}

.skip-btn:hover {
  color: #606266;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #606266;
}

.verification-header {
  margin-bottom: 25px;
}

/* Animations */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
