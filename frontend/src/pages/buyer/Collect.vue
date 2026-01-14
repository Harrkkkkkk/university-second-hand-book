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
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon class="title-icon"><StarFilled /></el-icon>
          我的收藏
        </h2>
        <p class="page-subtitle">这里保存了您感兴趣的教材，快去看看有没有降价吧</p>
      </div>
      <div class="header-actions">
        <el-button @click="router.push('/buyer/home')" :icon="ArrowLeft">返回首页</el-button>
      </div>
    </div>

    <!-- 收藏列表 -->
    <div v-loading="loading" class="collect-container">
      <transition-group name="list" tag="div" class="book-grid" v-if="collectList.length > 0">
        <div v-for="book in collectList" :key="book.id" class="book-card" @click="toBookDetail(book.id)">
          <div class="card-image-wrapper">
            <img :src="book.coverUrl || 'https://picsum.photos/200/280'" class="book-cover" alt="教材封面" loading="lazy">
            <div class="card-overlay">
              <el-button type="primary" round size="small" @click.stop="toBookDetail(book.id)">查看详情</el-button>
            </div>
            <div class="remove-btn" @click.stop="removeCollect(book.id)">
              <el-icon><Delete /></el-icon>
            </div>
          </div>
          
          <div class="card-content">
            <h3 class="book-title" :title="book.bookName">{{ book.bookName }}</h3>
            <div class="book-meta">
              <div class="price-row">
                <span class="currency">¥</span>
                <span class="price">{{ book.sellPrice }}</span>
              </div>
              <div class="status-tag">
                <el-tag size="small" effect="plain" type="success" v-if="book.stock > 0">有货</el-tag>
                <el-tag size="small" effect="plain" type="info" v-else>无货</el-tag>
              </div>
            </div>
          </div>
        </div>
      </transition-group>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="暂无收藏的教材" :image-size="200">
          <template #extra>
            <el-button type="primary" size="large" round @click="router.push('/buyer/home')">
              去逛逛市场
            </el-button>
          </template>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { StarFilled, Delete } from '@element-plus/icons-vue'
import { listFavorites, removeFavorite } from '@/api/collectApi'

const router = useRouter()
const collectList = ref([])
const loading = ref(false)

/**
 * Function: loadFavorites
 * Description: Loads the current user's favorite books.
 */
const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await listFavorites()
    // Ensure data consistency
    collectList.value = (res || []).map(item => ({
      ...item,
      // Fallback if stock is undefined
      stock: item.stock !== undefined ? item.stock : 1
    }))
  } catch (e) {
    ElMessage.error('加载收藏失败')
  } finally {
    loading.value = false
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
  ElMessageBox.confirm(
    '确定要将这本教材从收藏夹移除吗？', 
    '取消收藏', 
    {
      confirmButtonText: '移除',
      cancelButtonText: '保留',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(() => {
    removeFavorite(id).then(() => {
      collectList.value = collectList.value.filter(item => item.id !== id)
      ElMessage.success('已从收藏夹移除')
    }).catch(() => {
      ElMessage.error('操作失败')
    })
  }).catch(() => {})
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.buyer-collect {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 80vh;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: #fff;
  padding: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.header-content .page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  color: #ff9900;
}

.page-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* 书籍网格 */
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
}

.book-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #ebeef5;
  position: relative;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: transparent;
}

.card-image-wrapper {
  position: relative;
  padding-top: 133%; /* 3:4 aspect ratio */
  overflow: hidden;
  background: #f5f7fa;
}

.book-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.book-card:hover .book-cover {
  transform: scale(1.05);
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.book-card:hover .card-overlay {
  opacity: 1;
}

.remove-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f56c6c;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 2;
  opacity: 0;
  transform: translateY(-10px);
}

.book-card:hover .remove-btn {
  opacity: 1;
  transform: translateY(0);
}

.remove-btn:hover {
  background: #f56c6c;
  color: #fff;
}

.card-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.book-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  height: 2.8em;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.book-meta {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-row {
  color: #f56c6c;
  font-weight: 700;
}

.currency {
  font-size: 14px;
  margin-right: 2px;
}

.price {
  font-size: 20px;
}

/* 列表动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.4s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }
  
  .book-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .card-content {
    padding: 12px;
  }
  
  .book-title {
    font-size: 14px;
  }
  
  .price {
    font-size: 16px;
  }
}
</style>
