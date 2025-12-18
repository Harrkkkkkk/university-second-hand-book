<template>
  <div class="complaint-page">
    <page-header title="发起投诉" :goBack="goBack">
    </page-header>
    <el-card>
      <el-form :model="form" label-width="120px">
        <el-form-item label="订单ID"><el-input v-model="form.orderId" /></el-form-item>
        <el-form-item label="投诉类型">
          <el-select v-model="form.type" placeholder="选择类型">
            <el-option label="虚假描述" value="虚假描述" />
            <el-option label="逾期未发货" value="逾期未发货" />
            <el-option label="盗版教材" value="盗版教材" />
            <el-option label="恶意骚扰" value="恶意骚扰" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="投诉详情">
          <el-input v-model="form.detail" type="textarea" rows="4" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">提交投诉</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { ElMessage } from 'element-plus'

const router = useRouter()
const goBack = () => router.back()
const logout = () => logoutAndBackToLogin()
const form = ref({ orderId: '', type: '', detail: '' })

import request from '@/api/request'
const submit = async () => {
  try {
    const res = await request({ url: '/complaints/add', method: 'post', data: form.value, headers: { token: localStorage.getItem('token') } })
    if (!res || !res.id) throw new Error('失败')
    ElMessage.success('投诉提交成功')
  } catch { ElMessage.error('提交失败') }
}
</script>

<style scoped>
.complaint-page { max-width: 900px; margin: 0 auto; padding: 20px; }
</style>
