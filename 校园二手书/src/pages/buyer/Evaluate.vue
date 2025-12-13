<template>
  <div class="evaluate-page">
    <page-header title="订单评价">
      <el-button type="text" @click="logout" style="color:#e64340">退出登录</el-button>
    </page-header>
    <el-card>
      <el-form :model="form" label-width="120px">
        <el-form-item label="订单ID"><el-input v-model="form.orderId" /></el-form-item>
        <el-form-item label="教材成色匹配度">
          <el-rate v-model="form.scoreCondition" :max="5" />
        </el-form-item>
        <el-form-item label="卖家服务态度">
          <el-rate v-model="form.scoreService" :max="5" />
        </el-form-item>
        <el-form-item label="文字评论">
          <el-input v-model="form.comment" type="textarea" rows="4" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">提交评价</el-button>
          <el-button @click="saveDraft" style="margin-left:8px;">保存草稿</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { ElMessage } from 'element-plus'

const logout = () => logoutAndBackToLogin()
const form = ref({ orderId: '', scoreCondition: 5, scoreService: 5, comment: '' })

import request from '@/api/request'
const submit = async () => {
  try {
    const res = await request({ url: '/reviews/add', method: 'post', data: form.value, headers: { token: localStorage.getItem('token') } })
    if (!res || !res.id) throw new Error('失败')
    ElMessage.success('评价提交成功')
  } catch { ElMessage.error('提交失败') }
}

const saveDraft = async () => {
  try {
    const res = await request({ url: '/reviews/draft/save', method: 'post', data: form.value, headers: { token: localStorage.getItem('token') } })
    if (!res) throw new Error('失败')
    ElMessage.success('草稿已保存')
  } catch { ElMessage.error('保存草稿失败') }
}

onMounted(async () => {
  if (!form.value.orderId && typeof window !== 'undefined') {
    const params = new URLSearchParams(window.location.search)
    const orderId = params.get('orderId')
    if (orderId) form.value.orderId = Number(orderId)
  }
  if (form.value.orderId) {
    try {
      const draft = await request({ url: '/reviews/draft/my', method: 'get', params: { orderId: form.value.orderId }, headers: { token: localStorage.getItem('token') } })
      if (draft && draft.orderId) {
        form.value.scoreCondition = draft.scoreCondition || form.value.scoreCondition
        form.value.scoreService = draft.scoreService || form.value.scoreService
        form.value.comment = draft.comment || form.value.comment
      }
    } catch {}
  }
})
</script>

<style scoped>
.evaluate-page { max-width: 900px; margin: 0 auto; padding: 20px; }
</style>
