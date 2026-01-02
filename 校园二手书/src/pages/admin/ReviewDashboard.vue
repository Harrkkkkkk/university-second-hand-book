<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: ReviewDashboard.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Admin dashboard for reviewing book listings.
 *              Allows admins to approve or reject books uploaded by sellers.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
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
        <el-table-column label="封面" width="100">
          <template #default="scope">
            <img
              v-if="scope.row.coverUrl"
              :src="resolveCoverUrl(scope.row.coverUrl)"
              style="width: 64px; height: 86px; object-fit: cover; border-radius: 4px; border: 1px solid #eee;"
              alt="封面"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
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

/**
 * Function: resolveCoverUrl
 * Description: Resolves the full URL for book covers.
 * Input: url (String)
 * Return: String (Full URL)
 */
const resolveCoverUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/book-api/')) return url
  if (url.startsWith('/files/')) return `/book-api${url}`
  return url
}

/**
 * Function: load
 * Description: Loads the list of books currently under review.
 */
const load = async () => {
  try { underReview.value = await listUnderReviewBooks() || [] } catch { ElMessage.error('加载待审核商品失败') }
}

/**
 * Function: approve
 * Description: Approves a book listing.
 * Input: id (Number) - Book ID
 */
const approve = async (id) => {
  try { await approveBook(id); ElMessage.success('已通过'); load() } catch { ElMessage.error('操作失败') }
}

/**
 * Function: reject
 * Description: Rejects a book listing.
 * Input: id (Number) - Book ID
 */
const reject = async (id) => {
  try { await rejectBook(id); ElMessage.success('已驳回'); load() } catch { ElMessage.error('操作失败') }
}

onMounted(load)
</script>

<style scoped>
.admin-dashboard { max-width: 1200px; margin: 0 auto; padding: 20px; }
.card-header { font-weight: bold; }
</style>
