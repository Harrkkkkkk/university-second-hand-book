<template>
  <div class="book-detail-page">
    <div class="detail-container">
      <div class="breadcrumb-nav">
         <el-breadcrumb separator="/">
           <el-breadcrumb-item :to="{ path: '/buyer/home' }">首页</el-breadcrumb-item>
           <el-breadcrumb-item>教材详情</el-breadcrumb-item>
           <el-breadcrumb-item>{{ book.bookName }}</el-breadcrumb-item>
         </el-breadcrumb>
      </div>
      
      <el-alert
        v-if="book.status && book.status !== 'on_sale'"
        :title="book.status === 'sold' ? '该商品已售出' : '该商品已下架'"
        type="warning"
        show-icon
        :closable="false"
        class="status-alert"
      />

      <div v-if="loading" class="loading-container">
         <el-skeleton :rows="10" animated />
      </div>
      
      <div v-else-if="error" class="error-container">
         <el-empty description="数据加载失败">
            <el-button type="primary" @click="loadData">刷新页面</el-button>
         </el-empty>
      </div>

      <div class="content-wrapper" v-else>
         <div class="left-column">
            <div class="cover-card">
               <el-image 
                 :src="book.coverUrl || 'https://picsum.photos/300/420'" 
                 class="main-cover" 
                 :preview-src-list="[book.coverUrl || 'https://picsum.photos/300/420']"
                 fit="cover"
                 :z-index="9999"
                 :preview-teleported="true"
               >
                 <template #placeholder>
                   <div class="image-placeholder">
                     <el-icon class="is-loading"><Loading /></el-icon>
                   </div>
                 </template>
               </el-image>
            </div>
         </div>

         <div class="middle-column">
            <h1 class="detail-title">{{ book.bookName }}</h1>
            <div class="book-meta-grid">
              <div class="meta-item">
                <span class="label">作者</span>
                <span class="value">{{ book.author || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="label">出版社</span>
                <span class="value">{{ book.publisher || '未知' }}</span>
              </div>
              <div class="meta-item">
                <span class="label">ISBN</span>
                <span class="value">{{ book.isbn || '-' }}</span>
              </div>
              <div class="meta-item">
                <span class="label">成色</span>
                <span class="value">{{ book.conditionLevel || '-' }}</span>
              </div>
            </div>

            <div class="description-section">
               <h3 class="section-title">商品详情</h3>
               <p class="desc-text">{{ book.description || '卖家暂无详细描述...' }}</p>
            </div>

            <div class="reviews-section" v-if="sellerReviews.length">
               <h3 class="section-title">卖家评价 ({{ sellerReviews.length }})</h3>
               <div v-for="review in sellerReviews" :key="review.id" class="review-item">
                 <div class="review-header">
                   <span class="review-user">{{ review.buyerName }}</span>
                   <el-rate v-model="review.scoreService" disabled text-color="#ff9900" size="small" />
                   <span class="review-time">{{ review.createTime }}</span>
                 </div>
                 <div class="review-content">{{ review.comment }}</div>
                 <div class="review-images" v-if="review.images && review.images.length">
                    <el-image 
                      v-for="(img, idx) in review.images" 
                      :key="idx" 
                      :src="resolveCoverUrl(img)" 
                      :preview-src-list="review.images.map(resolveCoverUrl)"
                      class="review-img"
                      fit="cover"
                      preview-teleported
                    />
                 </div>
               </div>
            </div>
         </div>

         <div class="right-column">
           <div class="buy-card">
             <div class="price-section">
               <div class="price-row">
                 <span class="currency">¥</span>
                 <span class="current-price">{{ book.sellPrice }}</span>
               </div>
               <div class="original-price">原价 ¥{{ book.originalPrice }}</div>
             </div>

             <div class="tags-container">
               <el-tag effect="dark" type="danger" v-if="book.conditionLevel === '全新'">全新</el-tag>
               <el-tag effect="plain" type="warning" v-else>{{ book.conditionLevel }}</el-tag>
               <el-tag effect="plain" type="info">库存 {{ book.stock }}</el-tag>
             </div>

             <div class="action-area">
               <el-button
                 type="primary"
                 size="large"
                 class="action-btn buy-btn"
                 @click="buyNow"
                 :disabled="!canTrade"
               >
                 {{ isOwnBook ? '本人发布' : (Number(book.stock || 0) <= 0 ? '已售罄' : '立即购买') }}
               </el-button>

               <el-button
                 type="success"
                 size="large"
                 class="action-btn"
                 @click="addToCart"
                 :disabled="!canTrade"
               >
                 加入购物车
               </el-button>

               <div class="secondary-actions">
                 <el-button
                   size="small"
                   round
                   @click="toSellerDetail"
                   :disabled="!book.sellerName"
                 >
                   查看卖家
                 </el-button>
                 <el-button
                   size="small"
                   circle
                   :type="isCollected ? 'warning' : 'default'"
                   :plain="!isCollected"
                   :icon="isCollected ? StarFilled : Star"
                   @click="toggleCollect"
                   title="收藏"
                 />
               </div>
             </div>

             <div class="seller-info">
               <div class="seller-header">
                 <el-avatar :size="44" :style="{ background: 'linear-gradient(135deg, #667eea, #764ba2)' }">
                   {{ book.sellerName ? book.sellerName.charAt(0).toUpperCase() : 'U' }}
                 </el-avatar>
                 <div class="seller-details">
                   <div class="seller-name">
                     {{ book.sellerName || '-' }}
                     <el-tag
                       v-if="book.sellerType"
                       size="small"
                       type="success"
                       effect="plain"
                       class="seller-tag"
                     >
                       {{ book.sellerType === 'teacher' ? '教师' : '学生' }}
                     </el-tag>
                   </div>
                   <div class="seller-sub">
                     信誉评分 <span class="score-val">{{ sellerStats.score || '5.0' }}</span>
                     · 已售 {{ sellerStats.soldCount || 0 }}
                   </div>
                 </div>
               </div>

               <el-button class="contact-btn" round @click="chat" :disabled="isOwnBook">
                 联系卖家
               </el-button>

               <div class="safety-tip">
                 请优先使用平台内沟通与支付，线下交易注意核验身份与商品状态。
               </div>
             </div>
           </div>
         </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Star, StarFilled } from '@element-plus/icons-vue'
import { getBookDetail } from '@/api/bookApi'
import { createOrder } from '@/api/orderApi'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/collectApi'
import { addToCart as apiAddToCart } from '@/api/cartApi'
import { getSellerStats } from '@/api/userApi'
import { listSellerReviews } from '@/api/reviewApi'

const route = useRoute()
const router = useRouter()

const book = ref({})
const sellerReviews = ref([])
const sellerStats = ref({})
const isCollected = ref(false)
const loading = ref(true)
const error = ref(false)

const isOwnBook = computed(() => {
  const currentUsername = sessionStorage.getItem('username')
  return book.value.sellerName && currentUsername && book.value.sellerName === currentUsername
})

const canTrade = computed(() => {
  const stock = Number(book.value.stock || 0)
  return book.value.status === 'on_sale' && !isOwnBook.value && stock > 0
})

const resolveCoverUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/book-api/')) return url
  if (url.startsWith('/files/')) return `/book-api${url}`
  return `/book-api/files/${url}`
}

const loadData = async () => {
  loading.value = true
  error.value = false
  const bookId = Number(route.params.id)
  try {
    const res = await getBookDetail(bookId)
    if (!res) throw new Error('Not found')
    book.value = res
    
    if (book.value.sellerName) {
       try {
          const stats = await getSellerStats(book.value.sellerName)
          sellerStats.value = stats || {}
          
          const reviews = await listSellerReviews(book.value.sellerName)
          sellerReviews.value = reviews || []
       } catch (e) {
          console.error('Failed to load seller info', e)
       }
    }

    await checkCollectStatus(bookId)
  } catch (e) {
    console.error(e)
    error.value = true
    ElMessage.error('加载教材详情失败')
  } finally {
    loading.value = false
  }
}

const checkIdentity = () => {
  const isVerified = sessionStorage.getItem('isVerified') === '1'
  if (!isVerified) {
     ElMessageBox.confirm(
       '购买商品前需要先完成实名认证，是否前往认证？',
       '提示',
       {
         confirmButtonText: '去认证',
         cancelButtonText: '取消',
         type: 'warning'
       }
     ).then(() => {
       const bookId = route.params.id
       router.push(`/verify-identity?redirect=/book/${bookId}`)
     }).catch(() => {})
     return false
  }
  return true
}

/**
 * Function: addToCart
 * Description: Adds the current book to the shopping cart.
 */
const addToCart = async () => {
  try {
    await apiAddToCart(book.value.id)
    ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error(e.message || '加入购物车失败')
  }
}

/**
 * Function: buyNow
 * Description: Directly creates an order for the current book.
 *              Redirects to the order list upon success.
 */
const buyNow = async () => {
  if (!checkIdentity()) return
  try {
    await createOrder(book.value.id)
    ElMessage.success('下单成功！')
    router.push('/buyer/order')
  } catch (e) {
    ElMessage.error(e.message || '下单失败')
  }
}

/**
 * Function: toggleCollect
 * Description: Toggles the collection status (favorite) of the book.
 */
const toggleCollect = async () => {
  try {
    if (isCollected.value) {
      await removeFavorite(book.value.id)
      isCollected.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(book.value.id)
      isCollected.value = true
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const chat = () => {
  router.push({ path: '/chat', query: { peer: book.value.sellerName } })
}

const toSellerDetail = () => {
  if (!book.value.sellerName) return
  router.push(`/seller/detail/${encodeURIComponent(book.value.sellerName)}`)
}

const checkCollectStatus = async (id) => {
  try {
    const res = await checkFavorite(id)
    if (res && res.collected) {
      isCollected.value = true
    } else {
      isCollected.value = false
    }
  } catch (e) {
    console.error('检查收藏状态失败', e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.book-detail-page {
  min-height: calc(100vh - var(--nav-height));
  background: var(--bg-color);
  padding-bottom: 60px;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb-nav {
  margin-bottom: 24px;
}

.status-alert {
  margin-bottom: 20px;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 320px 1fr 340px;
  gap: 32px;
  align-items: start;
}

.left-column {
  position: sticky;
  top: 84px;
}

.cover-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  background: #fff;
}

.main-cover {
  width: 100%;
  height: 420px;
  display: block;
  object-fit: cover;
}

.reviews-section {
  margin-top: 40px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.review-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #eee;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.review-user {
  font-weight: 500;
  font-size: 14px;
}

.review-time {
  color: #999;
  font-size: 12px;
  margin-left: auto;
}

.review-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 10px;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.review-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #eee;
}

.image-placeholder {
  height: 100%;
  min-height: 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.middle-column {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.06);
  min-height: 500px;
}

.detail-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 24px 0;
  line-height: 1.4;
}

.book-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
  background: #f7f9ff;
  padding: 20px;
  border-radius: 12px;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-item .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.meta-item .value {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 12px 0;
}

.desc-text {
  font-size: 16px;
  color: #4a4a4a;
  line-height: 1.8;
  white-space: pre-wrap;
  margin: 0;
}

.reviews-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.review-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #f0f2f5;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 14px;
}

.reviewer-name {
  font-weight: 600;
  color: #606266;
}

.review-time {
  color: #909399;
  font-size: 12px;
  margin-left: auto;
}

.review-content {
  color: #303133;
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.review-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.tags-container {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.right-column {
  position: sticky;
  top: 84px;
}

.buy-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  border: 1px solid #ebeef5;
}

.price-section {
  margin-bottom: 16px;
}

.price-row {
  display: flex;
  align-items: baseline;
  color: #f56c6c;
  font-weight: 700;
}

.currency {
  font-size: 20px;
  margin-right: 4px;
}

.current-price {
  font-size: 36px;
  line-height: 1;
}

.original-price {
  font-size: 13px;
  color: #909399;
  text-decoration: line-through;
  margin-top: 4px;
}

.action-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.action-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  margin: 0 !important;
}

.buy-btn {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.secondary-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
}

.seller-info {
  margin-top: 10px;
}

.seller-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.seller-details {
  flex: 1;
}

.seller-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.seller-tag {
  transform: scale(0.92);
}

.seller-sub {
  font-size: 12px;
  color: #909399;
}

.score-val {
  font-weight: 700;
  color: #303133;
}

.contact-btn {
  width: 100%;
}

.safety-tip {
  margin-top: 16px;
  background: #fdf6ec;
  padding: 12px;
  border-radius: 8px;
  font-size: 12px;
  color: #e6a23c;
  line-height: 1.4;
}

@media (max-width: 992px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }
  
  .left-column, .right-column {
    position: static;
  }
  
  .middle-column {
    min-height: auto;
  }
}
</style>
