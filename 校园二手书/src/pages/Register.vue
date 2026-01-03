<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Register.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: User registration page.
 *              Allows new users to create an account with username and password.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 * 2. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Added validation rules for username and password.
-->
<template>
  <div class="register-page">
    <el-card class="register-card" shadow="hover">
      <h2 class="register-title">用户注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="register-btn" @click="submit">注册</el-button>
          <el-button type="text" @click="toLogin">去登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { register } from '@/api/userApi'

const router = useRouter()
const formRef = ref(null)
const form = ref({ username: '', password: '' })
const rules = ref({
  username: [
    { required: true, message: '请输入账号', trigger: ['blur', 'change'] },
    { max: 16, message: '账号长度不得超过16个字符', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: ['blur', 'change'] },
    { min: 6, max: 25, message: '密码长度必须在6-25个字符之间', trigger: ['blur', 'change'] },
    { pattern: /^(?=.*[a-zA-Z])(?=.*[0-9]).*$/, message: '密码必须同时包含字母和数字', trigger: ['blur', 'change'] }
  ]
})

/**
 * Function: submit
 * Description: Validates form and submits registration request.
 */
const submit = async () => {
  try {
    await formRef.value.validate()
    const res = await register({ username: form.value.username, password: form.value.password, roles: ['buyer'] })
    if (!res || res.success !== true) throw new Error('注册失败')
    
    ElMessageBox.confirm(
      '注册成功！是否立即前往登录并进行实名认证？',
      '提示',
      {
        confirmButtonText: '去认证',
        cancelButtonText: '跳过',
        type: 'success'
      }
    ).then(() => {
      router.push('/login?redirect=/verify-identity')
    }).catch(() => {
      router.push('/login')
    })
    
  } catch (e) {
    if (e !== 'cancel') {
        ElMessage.error(e.message || '注册失败')
    }
  }
}

/**
 * Function: toLogin
 * Description: Navigates to the login page.
 */
const toLogin = () => router.push('/login')
</script>

<style scoped>
.register-page { min-height: 100vh; display: flex; justify-content: center; align-items: center; background-color: #f5f7fa; }
.register-card { width: 420px; padding: 30px; }
.register-title { text-align: center; margin-bottom: 20px; color: #409eff; }
.register-btn { width: 100%; }
</style>
