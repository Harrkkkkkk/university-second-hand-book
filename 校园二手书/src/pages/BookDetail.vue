<template>
  <div class="book-detail">
    <page-header title="教材详情" :goBack="goBack">
    </page-header>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <img :src="book.coverUrl || 'https://picsum.photos/300/420'" class="book-cover" alt="教材封面">
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <h2 class="book-name">{{ book.bookName }}</h2>
          <p class="book-author">作者：{{ book.author }}</p>
          <p class="book-publisher">出版社：{{ book.publisher || '未知出版社' }}</p>
          <p class="book-price">
            售价：¥{{ book.sellPrice }}
            <span class="original-price">原价：¥{{ book.originalPrice }}</span>
          </p>
          <p class="book-stock">库存：{{ book.stock }}</p>
          <p class="book-seller">
            卖家：
            <el-button type="text" @click="toSellerDetail(book.sellerName)">
              {{ book.sellerName }}
            </el-button>
          </p>
          <p class="book-desc">描述：{{ book.description || '暂无描述' }}</p>
          <div class="book-actions" style="margin-top: 20px;">
            <el-button type="primary" @click="addToCart" :disabled="book.stock <= 0 || isOwnBook">
              {{ isOwnBook ? '本人发布' : (book.stock <= 0 ? '已售罄' : '加入购物车') }}
            </el-button>
            <el-button type="success" @click="buyNow" :disabled="book.stock <= 0 || isOwnBook">
              {{ isOwnBook ? '本人发布' : '立即购买' }}
            </el-button>
            <el-button 
              :type="isCollected ? 'default' : 'warning'" 
              :icon="isCollected ? 'el-icon-star-on' : 'el-icon-star-off'" 
              @click="toggleCollect"
            >
              {{ isCollected ? '已收藏' : '收藏' }}
            </el-button>
            <el-button type="info" @click="chat" :disabled="isOwnBook">
              {{ isOwnBook ? '本人发布' : '在线沟通' }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { getBookDetail } from '@/api/bookApi'
import { createOrder } from '@/api/orderApi'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/collectApi'
import { addToCart as apiAddToCart } from '@/api/cartApi'

const route = useRoute()
const router = useRouter()

const book = ref({})
const isCollected = ref(false)

const isOwnBook = computed(() => {
  const currentUsername = localStorage.getItem('username')
  return book.value.sellerName && currentUsername && book.value.sellerName === currentUsername
})

const goBack = () => {
  router.back()
}

const toSellerDetail = (name) => {
  if (name) {
    router.push({ name: 'SellerDetail', params: { name } })
  }
}

// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

const addToCart = async () => {
  try {
    await apiAddToCart(book.value.id)
    ElMessage.success('加入购物车成功！')
  } catch (e) {
    const msg = e.response?.data || '加入购物车失败'
    ElMessage.error(msg)
  }
}

const buyNow = async () => {
  try {
    const res = await createOrder(book.value.id)
    if (!res || !res.id) {
      ElMessage.error('下单失败')
      return
    }
    ElMessage.success('下单成功，前往我的订单付款')
    router.push('/buyer/order')
  } catch (e) {
    ElMessage.error('下单失败')
  }
}

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

onMounted(async () => {
  const bookId = Number(route.params.id)
  try {
    const res = await getBookDetail(bookId)
    book.value = res || {}
    // Check collect status
    checkCollectStatus(bookId)
  } catch (e) {
    ElMessage.error('加载教材详情失败')
  }
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
  object-fit: cover;
}
.book-name {
  font-size: 24px;
  margin: 0 0 10px 0;
}
.book-author, .book-publisher, .book-seller {
  font-size: 16px;
  color: #666;
  margin: 0 0 5px 0;
}
.book-price {
  font-size: 20px;
  color: #e64340;
  margin: 0 0 10px 0;
}
.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-left: 10px;
}
.book-desc {
  font-size: 16px;
  line-height: 1.6;
  color: #333;
}
</style>
