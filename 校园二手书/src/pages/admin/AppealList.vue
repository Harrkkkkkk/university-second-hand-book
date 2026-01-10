<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: AppealList.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-11
 * Description: Admin page for managing user blacklist appeals.
-->
<template>
  <div class="admin-appeal-list">
    <page-header title="管理员后台 - 申诉处理" :goBack="goBack" />

    <el-card class="mt-20">
      <template #header>
        <div class="card-header">
          <span>待处理申诉</span>
          <el-button type="primary" link @click="fetchList">刷新</el-button>
        </div>
      </template>

      <el-table :data="list" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="reason" label="申诉原因" show-overflow-tooltip />
        <el-table-column label="证明图片" width="120">
          <template #default="scope">
            <el-image 
              v-if="scope.row.proofImage" 
              :src="scope.row.proofImage" 
              :preview-src-list="[scope.row.proofImage]"
              fit="cover" 
              style="width: 80px; height: 80px"
              preview-teleported
            />
            <span v-else class="text-gray">无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="evidence" label="补充说明" show-overflow-tooltip />
        <el-table-column prop="createTime" label="提交时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="success" size="small" @click="audit(scope.row, 'approved')">通过</el-button>
            <el-button type="danger" size="small" @click="audit(scope.row, 'rejected')">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchList"
        />
      </div>
    </el-card>

    <!-- Audit Dialog -->
    <el-dialog v-model="auditDialog.visible" title="审核处理" width="500px">
      <el-form :model="auditDialog.form" label-width="80px">
        <el-form-item label="处理结果">
          <el-tag :type="auditDialog.status === 'approved' ? 'success' : 'danger'">
            {{ auditDialog.status === 'approved' ? '通过申诉' : '驳回申诉' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="处理意见">
          <el-input 
            v-model="auditDialog.form.auditReason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入处理意见（必填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit" :loading="auditDialog.loading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { getAppealList, auditAppeal } from '@/api/appealApi'

const router = useRouter()
const goBack = () => router.push('/admin/dashboard') // Assuming admin dashboard exists, or just router.back()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const auditDialog = reactive({
  visible: false,
  loading: false,
  status: '',
  id: null,
  form: {
    auditReason: ''
  }
})

const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString()
}

const fetchList = async () => {
  loading.value = true
  try {
    // Only show pending appeals by default
    const res = await getAppealList({ page: page.value, size: size.value, status: 'pending' })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('获取申诉列表失败')
  } finally {
    loading.value = false
  }
}

const audit = (row, status) => {
  auditDialog.id = row.id
  auditDialog.status = status
  auditDialog.form.auditReason = status === 'approved' ? '申诉通过，解除黑名单' : '申诉驳回，维持黑名单'
  auditDialog.visible = true
}

const submitAudit = async () => {
  if (!auditDialog.form.auditReason) {
    ElMessage.warning('请输入处理意见')
    return
  }
  
  auditDialog.loading = true
  try {
    await auditAppeal(auditDialog.id, {
      status: auditDialog.status,
      auditReason: auditDialog.form.auditReason
    })
    ElMessage.success('操作成功')
    auditDialog.visible = false
    fetchList()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    auditDialog.loading = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.admin-appeal-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.mt-20 {
  margin-top: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.text-gray {
  color: #909399;
  font-size: 12px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
