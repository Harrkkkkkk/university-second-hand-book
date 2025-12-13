<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <h2 class="login-title">校园二手教材交易平台</h2>

      <!-- 角色切换 -->
      <el-radio-group v-model="role" class="role-group">
        <el-radio label="buyer">买家</el-radio>
        <el-radio label="seller">卖家</el-radio>
        <el-radio label="admin">管理员</el-radio>
      </el-radio-group>

      <!-- 登录表单 -->
  <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入账号"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="submitLogin">登录</el-button>
          <el-button type="text" @click="useTestAccount">使用测试账号</el-button>
          <el-button type="text" @click="toRegister">没有账号？去注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { login as apiLogin } from '@/api/userApi'

const router = useRouter()
const loginFormRef = ref(null)

// 角色（默认买家）
const role = ref('buyer')

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
const testAccounts = {
  buyer: { username: 'buyer1', password: '123456' },
  seller: { username: 'seller1', password: '123456' },
  admin: { username: 'admin1', password: '123456' }
}

// 使用测试账号
const useTestAccount = () => {
  loginForm.value = { ...testAccounts[role.value] }
}

const submitLogin = async () => {
  try {
    await loginFormRef.value.validate()
    const res = await apiLogin({
      username: loginForm.value.username,
      password: loginForm.value.password,
      role: role.value
    })
    if (!res || !res.token) {
      ElMessage.error('登录失败')
      return
    }
    localStorage.setItem('token', res.token)
    localStorage.setItem('role', res.role)
    localStorage.setItem('username', res.username)
    ElMessage.success('登录成功！')
    if (res.role === 'admin') {
      router.push('/admin/dashboard')
    } else if (res.role === 'seller') {
      router.push('/seller/center')
    } else {
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
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}
.login-card {
  width: 400px;
  padding: 30px;
}
.login-title {
  text-align: center;
  margin-bottom: 20px;
  color: #409eff;
}
.role-group {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
.login-btn {
  width: 100%;
}
</style>
