<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Appeal.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-11
 * Description: Page for blacklisted users to submit appeals.
-->
<template>
  <div class="appeal-container">
    <page-header title="账号申诉" :goBack="goBack" />
    
    <el-card class="appeal-card">
      <template #header>
        <div class="card-header">
          <span>提交申诉材料</span>
          <el-tag type="danger">当前状态：黑名单</el-tag>
        </div>
      </template>
      
      <el-alert
        title="您的账号已被加入黑名单，受限功能包括：发布教材、购买教材、发表评论。"
        type="error"
        :closable="false"
        show-icon
        class="mb-20"
      />

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="top">
        <el-form-item label="申诉原因" prop="reason">
          <el-input 
            v-model="form.reason" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细说明您的情况，解释为何应该解除黑名单..."
          />
        </el-form-item>
        
        <el-form-item label="证明图片" prop="proofImage">
          <el-upload
            v-model:file-list="fileList"
            action="#"
            list-type="picture-card"
            :http-request="handleUpload"
            :limit="1"
            :on-remove="handleRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="form-tip">支持上传一张证明图片（如聊天记录截图）</div>
        </el-form-item>

        <el-form-item label="补充说明（可选）" prop="evidence">
          <el-input v-model="form.evidence" placeholder="如有其他链接可在此填写" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submit" :loading="loading">提交申诉</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { submitAppeal } from '@/api/appealApi'
import { uploadFile } from '@/api/bookApi'

const router = useRouter()
const goBack = () => router.back()
const loading = ref(false)
const formRef = ref(null)
const fileList = ref([])

const form = ref({
  reason: '',
  evidence: '',
  proofImage: ''
})

const handleUpload = async (options) => {
  try {
    const res = await uploadFile(options.file)
    if (res && res.url) {
      form.value.proofImage = res.url
      ElMessage.success('上传成功')
    } else {
      ElMessage.error('上传失败')
      fileList.value = [] // Clear on failure
    }
  } catch (e) {
    ElMessage.error('上传出错')
    fileList.value = []
  }
}

const handleRemove = () => {
  form.value.proofImage = ''
  fileList.value = []
}

const rules = {
  reason: [
    { required: true, message: '请输入申诉原因', trigger: 'blur' },
    { min: 10, message: '原因描述至少10个字符', trigger: 'blur' }
  ]
}

const submit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await submitAppeal(form.value)
        ElMessage.success('申诉提交成功，请耐心等待管理员审核')
        router.push('/buyer/home')
      } catch (error) {
        console.error(error)
        ElMessage.error(error.message || '提交申诉失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.appeal-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.appeal-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mb-20 {
  margin-bottom: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
