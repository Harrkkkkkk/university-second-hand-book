<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Evaluate.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Order evaluation page.
 *              Allows buyers to rate and review completed orders.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
<template>
  <div class="evaluate-page">
    <page-header title="订单评价" :goBack="goBack">
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
        <el-form-item label="评价标签">
          <el-checkbox-group v-model="form.tags">
            <el-checkbox v-for="t in tagOptions" :key="t" :label="t">{{ t }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            action="/book-api/files/upload"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :headers="uploadHeaders"
            :file-list="fileList"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="文字评论">
          <el-input v-model="form.comment" type="textarea" rows="4" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" :disabled="submitting || hasSubmitted" @click="submit">提交评价</el-button>
          <el-button @click="saveDraft" style="margin-left:8px;">保存草稿</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { ElMessage } from 'element-plus'

const router = useRouter()
const goBack = () => router.back()
const logout = () => logoutAndBackToLogin()
const form = ref({ orderId: '', scoreCondition: 5, scoreService: 5, comment: '', tags: [], images: [] })
const fileList = ref([])
const uploadHeaders = computed(() => ({ token: sessionStorage.getItem('token') }))
const tagOptions = ['书籍很新','笔记实用','发货快','沟通顺畅']
const submitting = ref(false)
const hasSubmitted = ref(false)

const handleUploadSuccess = (response, uploadFile) => {
  if (response && response.url) {
    form.value.images.push(response.url)
  }
}

const handleRemove = (uploadFile) => {
  const url = uploadFile.response ? uploadFile.response.url : uploadFile.url
  const index = form.value.images.indexOf(url)
  if (index > -1) {
    form.value.images.splice(index, 1)
  }
}

import request from '@/api/request'

/**
 * Function: submit
 * Description: Submits the evaluation (review) for an order.
 */
const submit = async () => {
  try {
    if (submitting.value || hasSubmitted.value) return
    submitting.value = true
    const res = await request({ url: '/reviews/add', method: 'post', data: form.value, headers: { token: sessionStorage.getItem('token') } })
    if (!res || !res.id) throw new Error('失败')
    ElMessage.success('评价提交成功')
    hasSubmitted.value = true
    // 自动返回到订单页面，避免重复提交
    setTimeout(() => {
      router.push({ path: '/buyer/order' })
    }, 800)
  } catch { ElMessage.error('提交失败') }
  finally {
    submitting.value = false
  }
}

/**
 * Function: saveDraft
 * Description: Saves the current evaluation as a draft.
 */
const saveDraft = async () => {
  try {
    const res = await request({ url: '/reviews/draft/save', method: 'post', data: form.value, headers: { token: sessionStorage.getItem('token') } })
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
      const draft = await request({ url: '/reviews/draft/my', method: 'get', params: { orderId: form.value.orderId }, headers: { token: sessionStorage.getItem('token') } })
      if (draft && draft.orderId) {
        form.value.scoreCondition = draft.scoreCondition || form.value.scoreCondition
        form.value.scoreService = draft.scoreService || form.value.scoreService
        form.value.comment = draft.comment || form.value.comment
        form.value.tags = draft.tags || form.value.tags
        form.value.images = draft.images || []
        if (form.value.images.length > 0) {
           fileList.value = form.value.images.map(url => ({ name: 'image', url: url }))
        }
      }
    } catch {}
  }
})
</script>

<style scoped>
.evaluate-page { max-width: 900px; margin: 0 auto; padding: 20px; }
</style>
