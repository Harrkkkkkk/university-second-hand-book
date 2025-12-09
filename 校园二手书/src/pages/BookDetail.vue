<template>
  <div class="book-detail">
    <page-header title="教材详情">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
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
          <p class="book-seller">卖家：{{ book.sellerName }}</p>
          <p class="book-desc">描述：{{ book.description || '暂无描述' }}</p>
          <div class="book-actions" style="margin-top: 20px;">
            <el-button type="primary" @click="addToCart">加入购物车</el-button>
            <el-button type="success" @click="buyNow">立即购买</el-button>
            <el-button type="warning" icon="el-icon-star-off" @click="collectBook"></el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'

const route = useRoute()
const router = useRouter()

// 模拟教材数据
const book = ref({
  id: 1,
  bookName: 'Java编程思想',
  author: 'Bruce Eckel',
  originalPrice: 108,
  sellPrice: 30,
  sellerName: '卖家1',
  description: '九成新，无笔记，无缺页，适合Java初学者学习'
})

// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

// 加入购物车
const addToCart = () => {
  ElMessage.success('加入购物车成功！')
}

// 立即购买
const buyNow = () => {
  ElMessage.info('跳转到支付页面...')
}

// 收藏教材
const collectBook = () => {
  ElMessage.success('收藏成功！')
}

// 根据路由ID加载不同教材
onMounted(() => {
  const bookId = Number(route.params.id)
  // 模拟根据ID加载数据
  const bookList = [
    { id: 1, bookName: 'Java编程思想', author: 'Bruce Eckel', originalPrice: 108, sellPrice: 30, sellerName: '卖家1' },
    { id: 2, bookName: 'Python入门到精通', author: '张三', originalPrice: 89, sellPrice: 25, sellerName: '卖家1' },
    { id: 3, bookName: '红楼梦', author: '曹雪芹', originalPrice: 45, sellPrice: 15, sellerName: '卖家2' }
  ]
  const targetBook = bookList.find(item => item.id === bookId)
  if (targetBook) {
    book.value = targetBook
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