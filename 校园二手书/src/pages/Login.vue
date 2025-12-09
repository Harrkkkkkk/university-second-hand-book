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
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

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

// 提交登录（纯前端模拟）
const submitLogin = async () => {
  try {
    // 表单校验
    await loginFormRef.value.validate()

    // 模拟账号密码校验
    const testAccount = testAccounts[role.value]
    if (
        loginForm.value.username !== testAccount.username ||
        loginForm.value.password !== testAccount.password
    ) {
      ElMessage.error('账号或密码错误！')
      return
    }

    // 登录成功：强制写入localStorage
    localStorage.setItem('token', `mock-token-${role.value}`)
    localStorage.setItem('role', role.value)
    localStorage.setItem('username', loginForm.value.username)
    ElMessage.success('登录成功！')

    // 根据角色跳转不同页面（修改买家路径）
    if (role.value === 'admin') {
      router.push('/admin/dashboard')
    } else if (role.value === 'seller') {
      router.push('/seller/center')
    } else {
      router.push('/buyer/home')
    }
  } catch (error) {
    ElMessage.error('登录失败：' + error.message)
  }
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