<template>
  <div class="book-detail">
    <page-header title="商品详情" :goBack="goBack"></page-header>
    
    <!-- Status Alert -->
    <el-alert
      v-if="book.status && book.status !== 'on_sale'"
      :title="book.status === 'sold' ? '该商品已售出' : '该商品已下架'"
      type="warning"
      show-icon
      :closable="false"
      style="margin-bottom: 20px;"
    />

    <div v-if="loading" class="loading-container" style="padding: 40px;">
       <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="error" class="error-container">
       <el-empty description="数据加载失败">
          <el-button type="primary" @click="loadData">刷新页面</el-button>
       </el-empty>
    </div>

    <el-row v-else :gutter="24">
      <!-- Left: Cover -->
      <el-col :span="8">
         <el-card shadow="hover">
            <el-image 
               :src="book.coverUrl || 'https://picsum.photos/300/420'" 
               class="book-cover" 
               :preview-src-list="[book.coverUrl || 'https://picsum.photos/300/420']"
               fit="cover">
               <template #placeholder>
                 <div class="image-slot" style="display: flex; justify-content: center; align-items: center; height: 100%; background: #f5f7fa; color: #909399;">
                    Loading...
                 </div>
               </template>
            </el-image>
         </el-card>
      </el-col>
      
      <!-- Right: Info -->
      <el-col :span="16">
         <!-- Basic Info -->
         <el-card shadow="hover" class="info-card">
            <template #header>
               <div class="card-header">
                 <span style="font-weight: bold; font-size: 16px;">教材基础信息</span>
               </div>
            </template>
            <h1 class="book-title">{{ book.bookName }}</h1>
            <el-descriptions :column="2" border>
               <el-descriptions-item label="作者">{{ book.author }}</el-descriptions-item>
               <el-descriptions-item label="出版社">{{ book.publisher || '-' }}</el-descriptions-item>
               <el-descriptions-item label="出版日期">{{ book.publishDate || '-' }}</el-descriptions-item>
               <el-descriptions-item label="ISBN">{{ book.isbn || '-' }}</el-descriptions-item>
            </el-descriptions>
         </el-card>

         <!-- Transaction Info -->
         <el-card shadow="hover" class="info-card" style="margin-top: 20px;">
            <template #header>
               <div class="card-header">
                 <span style="font-weight: bold; font-size: 16px;">交易信息</span>
               </div>
            </template>
            <div class="price-section" style="margin-bottom: 15px;">
               <span class="sell-price">¥{{ book.sellPrice }}</span>
               <span class="original-price">原价 ¥{{ book.originalPrice }}</span>
            </div>
            <el-descriptions :column="2" border>
               <el-descriptions-item label="成色等级">{{ book.conditionLevel || '无' }}</el-descriptions-item>
               <el-descriptions-item label="库存数量">{{ book.stock }}</el-descriptions-item>
               <el-descriptions-item label="成色描述" :span="2">{{ book.description || '暂无描述' }}</el-descriptions-item>
            </el-descriptions>
         </el-card>

         <!-- Seller Info -->
         <el-card shadow="hover" class="info-card" style="margin-top: 20px;">
            <template #header>
               <div class="card-header">
                 <span style="font-weight: bold; font-size: 16px;">卖家信息</span>
               </div>
            </template>
            <div class="seller-info" style="display: flex; align-items: center;">
               <el-avatar :size="50" icon="el-icon-user-solid" style="background: #409EFF;">{{ book.sellerName ? book.sellerName.charAt(0).toUpperCase() : 'U' }}</el-avatar>
               <div class="seller-details" style="margin-left: 15px; flex: 1;">
                  <div class="seller-name" style="font-size: 18px; font-weight: bold; margin-bottom: 5px;">
                     {{ book.sellerName }}
                     <el-tag size="small" type="success" style="margin-left: 10px;">{{ book.sellerType === 'teacher' ? '教师' : '学生' }}</el-tag>
                  </div>
                  <div class="seller-stats" style="font-size: 14px; color: #666;">
                     <span>交易评分: <span style="color: #ff9900; font-weight: bold;">{{ sellerStats.score || '5.0' }}</span></span>
                     <span style="margin-left: 20px;">已售: {{ sellerStats.soldCount || 0 }}本</span>
                  </div>
               </div>
               <!-- <el-button type="text" @click="toSellerDetail(book.sellerName)">查看更多</el-button> -->
            </div>
         </el-card>

         <!-- Actions -->
         <div class="actions-bar" style="margin-top: 30px; display: flex; gap: 15px;">
            <el-button type="primary" size="large" @click="buyNow" :disabled="book.status !== 'on_sale' || isOwnBook || book.stock <= 0">
              {{ isOwnBook ? '本人发布' : (book.stock <= 0 ? '已售罄' : '立即购买') }}
            </el-button>
            <el-button type="warning" size="large" @click="addToCart" :disabled="book.status !== 'on_sale' || isOwnBook || book.stock <= 0">
              加入购物车
            </el-button>
            <el-button type="info" size="large" @click="chat" :disabled="isOwnBook">在线沟通</el-button>
            <el-button 
               :type="isCollected ? 'default' : 'warning'" 
               :icon="isCollected ? 'el-icon-star-on' : 'el-icon-star-off'" 
               circle
               size="large"
               @click="toggleCollect"
               :title="isCollected ? '取消收藏' : '收藏商品'"
            ></el-button>
         </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { getBookDetail } from '@/api/bookApi'
import { createOrder } from '@/api/orderApi'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/collectApi'
import { addToCart as apiAddToCart } from '@/api/cartApi'
import { getSellerStats } from '@/api/userApi'

const route = useRoute()
const router = useRouter()

const book = ref({})
const sellerStats = ref({})
const isCollected = ref(false)
const loading = ref(true)
const error = ref(false)

const isOwnBook = computed(() => {
  const currentUsername = localStorage.getItem('username')
  return book.value.sellerName && currentUsername && book.value.sellerName === currentUsername
})

const goBack = () => {
  router.back()
}

const toSellerDetail = (name) => {
  if (name) {
    // router.push({ name: 'SellerDetail', params: { name } })
    ElMessage.info('卖家详情页开发中')
  }
}

/**
 * Function: loadData
 * Description: Loads book details, seller stats, and collection status.
 *              Handles 404 errors and displays loading state.
 */
const loadData = async () => {
  loading.value = true
  error.value = false
  const bookId = Number(route.params.id)
  try {
    const res = await getBookDetail(bookId)
    if (!res) throw new Error('Not found')
    book.value = res
    
    // Fetch seller stats
    if (book.value.sellerName) {
       try {
          const stats = await getSellerStats(book.value.sellerName)
          sellerStats.value = stats || {}
       } catch (e) {
          console.error('Failed to load seller stats', e)
       }
    }

    // Check collect status
    checkCollectStatus(bookId)
  } catch (e) {
    console.error(e)
    error.value = true
    ElMessage.error('加载教材详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * Function: addToCart
 * Description: Adds the current book to the shopping cart.
 */
const addToCart = async () => {
  try {
    await apiAddToCart(book.value.id)
    ElMessage.success('加入购物车成功！')
  } catch (e) {
    const msg = e.response?.data || '加入购物车失败'
    ElMessage.error(msg)
  }
}

/**
 * Function: buyNow
 * Description: Creates an order for the book immediately and redirects to payment.
 */
const buyNow = async () => {
  try {
    // Create order directly? Or redirect to confirm page?
    // Requirement says "Immediate purchase flow". Usually creates order and goes to payment.
    const res = await createOrder(book.value.id)
    if (!res || !res.id) {
      ElMessage.error('下单失败')
      return
    }
    ElMessage.success('下单成功，前往订单页付款')
    router.push('/buyer/order')
  } catch (e) {
    ElMessage.error('下单失败: ' + (e.response?.data?.message || e.message))
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

/**
 * Function: chat
 * Description: Navigates to the chat page with the seller.
 */
const chat = () => {
  router.push({ path: '/chat', query: { peer: book.value.sellerName } })
}

/**
 * Function: checkCollectStatus
 * Description: Checks if the current user has collected this book.
 * Input: id (Number)
 */
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
.book-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.book-cover {
  width: 100%;
  height: 420px;
  border-radius: 4px;
}
.book-title {
  font-size: 24px;
  margin: 0 0 20px 0;
  color: #303133;
}
.sell-price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
}
.original-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
  margin-left: 15px;
}
.info-card :deep(.el-card__header) {
  padding: 15px 20px;
  background-color: #f5f7fa;
}
</style>