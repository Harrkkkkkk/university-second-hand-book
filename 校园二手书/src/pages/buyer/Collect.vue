<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Collect.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Buyer's collection (favorites) page.
 *              Allows users to view and manage their collected books.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
<template>
  <div class="buyer-collect">
    <page-header title="买家中心 - 我的收藏">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <!-- 收藏列表 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6" v-for="book in collectList" :key="book.id">
        <el-card shadow="hover" class="collect-card">
          <img :src="book.coverUrl || 'https://picsum.photos/200/280'" class="book-cover" alt="教材封面">
          <div class="book-info">
            <h3 class="book-name">{{ book.bookName }}</h3>
            <p class="book-price">¥{{ book.sellPrice }}</p>
            <div class="book-actions">
              <el-button type="primary" size="small" @click="toBookDetail(book.id)">查看详情</el-button>
              <el-button type="danger" size="small" @click="removeCollect(book.id)">取消收藏</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 空状态 -->
    <el-empty v-if="collectList.length === 0" description="暂无收藏的教材"></el-empty>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { listFavorites, removeFavorite } from '@/api/collectApi'

const router = useRouter()
// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

const collectList = ref([])

/**
 * Function: loadFavorites
 * Description: Loads the current user's favorite books.
 */
const loadFavorites = async () => {
  try {
    const res = await listFavorites()
    collectList.value = res || []
  } catch (e) {
    ElMessage.error('加载收藏失败')
  }
}

/**
 * Function: toBookDetail
 * Description: Navigates to the book detail page.
 * Input: id (Number) - Book ID
 */
const toBookDetail = (id) => {
  router.push(`/book/${id}`)
}

/**
 * Function: removeCollect
 * Description: Removes a book from favorites after confirmation.
 * Input: id (Number) - Book ID
 */
const removeCollect = (id) => {
  ElMessageBox.confirm('确认取消收藏？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(() => {
    removeFavorite(id).then(() => {
      collectList.value = collectList.value.filter(item => item.id !== id)
      ElMessage.success('取消收藏成功！')
    }).catch(() => {
      ElMessage.error('取消收藏失败')
    })
  })
}

import { onMounted } from 'vue'
onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.buyer-collect {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.collect-card {
  height: 400px;
  display: flex;
  flex-direction: column;
}
.book-cover {
  width: 100%;
  height: 280px;
  object-fit: cover;
  margin-bottom: 10px;
}
.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.book-name {
  font-size: 16px;
  margin: 0 0 5px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.book-price {
  font-size: 16px;
  color: #e64340;
  margin: 0 0 10px 0;
}
.book-actions {
  display: flex;
  gap: 5px;
}
</style>
