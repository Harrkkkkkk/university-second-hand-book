<template>
  <div class="admin-dashboard">
    <page-header title="管理员后台 - 内容审核" :goBack="goBack">
      <el-button type="text" @click="logout" style="color:#e64340">退出登录</el-button>
    </page-header>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">待审核商品</div>
      </template>
      <el-table :data="underReview" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="bookName" label="教材名称" />
        <el-table-column prop="sellerName" label="卖家" width="120" />
        <el-table-column prop="sellPrice" label="售价" width="100" />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button type="success" size="small" @click="approve(scope.row.id)">通过</el-button>
            <el-button type="danger" size="small" @click="reject(scope.row.id)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
  </template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { listUnderReviewBooks, approveBook, rejectBook } from '@/api/adminApi'
import { ElMessage } from 'element-plus'

const router = useRouter()
const goBack = () => router.back()
const logout = () => logoutAndBackToLogin()
const underReview = ref([])

const load = async () => {
  try { underReview.value = await listUnderReviewBooks() || [] } catch { ElMessage.error('加载待审核商品失败') }
}

const approve = async (id) => {
  try { await approveBook(id); ElMessage.success('已通过'); load() } catch { ElMessage.error('操作失败') }
}
const reject = async (id) => {
  try { await rejectBook(id); ElMessage.success('已驳回'); load() } catch { ElMessage.error('操作失败') }
}

onMounted(load)
</script>

<style scoped>
.admin-dashboard { max-width: 1200px; margin: 0 auto; padding: 20px; }
.card-header { font-weight: bold; }
</style>
