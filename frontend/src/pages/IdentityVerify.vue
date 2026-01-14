<template>
  <div class="verify-container">
    <div class="verify-card">
      <h2>实名认证</h2>
      <p class="desc">为了保障您的账户安全，请进行实名认证</p>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入真实姓名" />
        </el-form-item>
        
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="form.studentId" placeholder="请输入学号" />
        </el-form-item>
        
        <div class="actions">
          <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
          <el-button type="primary" :loading="loading" @click="submitVerify">提交认证</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { verifyIdentity } from '@/api/userApi'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  name: '',
  studentId: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }]
}

const submitVerify = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await verifyIdentity(form)
        ElMessage.success('实名认证成功')
        // Update session storage
        sessionStorage.setItem('isVerified', '1')
        sessionStorage.setItem('realName', form.name)
        
        // Redirect back to where they came from, or home
        const redirect = route.query.redirect || '/buyer/home'
        router.push(redirect)
      } catch (error) {
        // Error handled by request interceptor usually, but if not:
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.verify-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  background-color: #f5f7fa;
}

.verify-card {
  width: 400px;
  padding: 30px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 10px;
  color: #303133;
}

.desc {
  text-align: center;
  color: #909399;
  margin-bottom: 30px;
  font-size: 14px;
}

.actions {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}

.actions .el-button {
  width: 45%;
}
</style>