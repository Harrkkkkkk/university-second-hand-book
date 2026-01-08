<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Complaint.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Complaint submission page.
 *              Allows buyers to submit complaints against orders.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
<template>
  <div class="complaint-page">
    <div class="complaint-container">
      <div class="page-header">
        <h2 class="page-title">
          <el-icon class="title-icon"><WarningFilled /></el-icon>
          发起投诉
        </h2>
        <p class="page-subtitle">我们重视您的每一次反馈，致力于维护公平的交易环境</p>
      </div>

      <el-row :gutter="40">
        <!-- 左侧表单区域 -->
        <el-col :span="16" :xs="24">
          <el-card shadow="hover" class="form-card">
            <template #header>
              <div class="card-header">
                <span>投诉信息填写</span>
                <el-tag v-if="form.orderId" type="info">订单号: {{ form.orderId }}</el-tag>
              </div>
            </template>
            
            <el-form :model="form" label-position="top" class="complaint-form">
              <el-form-item label="请选择投诉原因">
                <div class="complaint-types">
                  <div 
                    v-for="type in complaintTypes" 
                    :key="type.value"
                    class="type-card"
                    :class="{ active: form.type === type.value }"
                    @click="form.type = type.value"
                  >
                    <el-icon class="type-icon"><component :is="type.icon" /></el-icon>
                    <span class="type-label">{{ type.label }}</span>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="详细说明情况">
                <el-input 
                  v-model="form.detail" 
                  type="textarea" 
                  :rows="6" 
                  placeholder="请详细描述您遇到的问题，以便我们更快为您处理..."
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item>
                <div class="form-actions">
                  <el-button size="large" @click="goBack">取消</el-button>
                  <el-button 
                    type="danger" 
                    size="large" 
                    @click="submit" 
                    :loading="submitting"
                    :disabled="!form.type || !form.detail"
                  >
                    提交投诉
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <!-- 右侧提示区域 -->
        <el-col :span="8" :xs="24">
          <div class="tips-section">
            <el-card shadow="never" class="tip-card info">
              <template #header>
                <div class="tip-header">
                  <el-icon><InfoFilled /></el-icon>
                  <span>投诉须知</span>
                </div>
              </template>
              <ul class="tip-list">
                <li>请确保投诉内容真实有效，恶意投诉可能导致账号被限制。</li>
                <li>平台将在收到投诉后的24小时内介入处理。</li>
                <li>处理结果将通过系统消息通知您。</li>
              </ul>
            </el-card>

            <el-card shadow="never" class="tip-card security">
              <div class="security-content">
                <el-icon class="security-icon"><Lock /></el-icon>
                <h3>平台保障</h3>
                <p>WiseBookPal 全程保障您的交易安全，遇到问题请及时联系客服。</p>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  WarningFilled, InfoFilled, Lock,
  DocumentDelete, Timer, CircleClose, Warning, More
} from '@element-plus/icons-vue'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()
const goBack = () => router.back()

const submitting = ref(false)
const form = ref({ orderId: '', type: '', detail: '' })

const complaintTypes = [
  { label: '虚假描述', value: '虚假描述', icon: 'DocumentDelete' },
  { label: '逾期未发货', value: '逾期未发货', icon: 'Timer' },
  { label: '盗版教材', value: '盗版教材', icon: 'CircleClose' },
  { label: '恶意骚扰', value: '恶意骚扰', icon: 'Warning' },
  { label: '其他原因', value: '其他', icon: 'More' }
]

/**
 * Function: submit
 * Description: Submits a new complaint.
 *              Validates order ID and sends data to backend.
 */
const submit = async () => {
  if (!form.value.orderId) {
    ElMessage.error('订单ID缺失，无法提交')
    return
  }
  
  submitting.value = true
  try {
    const res = await request({ 
      url: '/complaints/add', 
      method: 'post', 
      data: form.value, 
      headers: { token: localStorage.getItem('token') } 
    })
    
    if (res && (res.id || res.success)) {
      ElMessage.success({
        message: '投诉提交成功，我们将尽快处理',
        duration: 2000,
        onClose: () => goBack()
      })
    } else {
       throw new Error('提交失败')
    }
  } catch (e) { 
    ElMessage.error('提交失败，请稍后重试') 
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  const orderId = route.query.orderId
  if (orderId) {
    form.value.orderId = Number(orderId)
  } else {
    ElMessage.warning('未找到相关订单信息')
  }
})
</script>

<style scoped>
.complaint-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 40px 20px;
}

.complaint-container {
  max-width: 1000px;
  margin: 0 auto;
}

/* 头部样式 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 32px;
  color: #303133;
  margin: 0 0 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  color: #f56c6c;
}

.page-subtitle {
  color: #909399;
  font-size: 16px;
  margin: 0;
}

/* 表单卡片 */
.form-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

/* 投诉类型选择 */
.complaint-types {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  margin-bottom: 10px;
}

.type-card {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 15px 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.type-card:hover {
  border-color: #f56c6c;
  color: #f56c6c;
  background-color: #fef0f0;
}

.type-card.active {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: #fff;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

.type-icon {
  font-size: 24px;
}

.type-label {
  font-size: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

/* 右侧提示 */
.tips-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tip-card {
  border-radius: 8px;
  border: none;
}

.tip-card.info {
  background: #fff;
}

.tip-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e6a23c;
  font-weight: 600;
}

.tip-list {
  padding-left: 20px;
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.8;
}

.tip-card.security {
  background: linear-gradient(135deg, #e1f3d8 0%, #f0f9eb 100%);
  text-align: center;
}

.security-content {
  padding: 20px 0;
}

.security-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 12px;
}

.security-content h3 {
  margin: 0 0 8px;
  color: #303133;
}

.security-content p {
  margin: 0;
  color: #606266;
  font-size: 13px;
}

@media (max-width: 768px) {
  .complaint-page {
    padding: 20px 10px;
  }
  
  .page-title {
    font-size: 24px;
  }
}
</style>
