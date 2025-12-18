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
import { ElMessage } from 'element-plus'
import { register } from '@/api/userApi'

const router = useRouter()
const formRef = ref(null)
const form = ref({ username: '', password: '' })
const rules = ref({
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const submit = async () => {
  try {
    await formRef.value.validate()
    const res = await register({ username: form.value.username, password: form.value.password, roles: ['buyer'] })
    if (!res || res.success !== true) throw new Error('注册失败')
    ElMessage.success('注册成功！请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error('注册失败')
  }
}

const toLogin = () => router.push('/login')
</script>

<style scoped>
.register-page { min-height: 100vh; display: flex; justify-content: center; align-items: center; background-color: #f5f7fa; }
.register-card { width: 420px; padding: 30px; }
.register-title { text-align: center; margin-bottom: 20px; color: #409eff; }
.register-btn { width: 100%; }
</style>
