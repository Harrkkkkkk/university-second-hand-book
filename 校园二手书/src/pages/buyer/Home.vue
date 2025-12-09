<template>
  <div class="buyer-home">
    <page-header title="买家中心 - 教材选购">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <!-- 搜索+筛选区 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-input v-model="searchKey" placeholder="输入教材名称/作者搜索" suffix-icon="el-icon-search" @keyup.enter="searchBooks"></el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="category" placeholder="教材分类">
            <el-option label="计算机类" value="computer"></el-option>
            <el-option label="文学类" value="literature"></el-option>
            <el-option label="经管类" value="economy"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="searchBooks">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 教材列表区 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6" v-for="book in bookList" :key="book.id">
        <el-card shadow="hover" class="book-card">
          <img :src="book.coverUrl || 'https://picsum.photos/200/280'" class="book-cover" alt="教材封面">
          <div class="book-info">
            <h3 class="book-name">{{ book.bookName }}</h3>
            <p class="book-author">作者：{{ book.author }}</p>
            <p class="book-price">¥{{ book.sellPrice }} <span class="original-price">原价¥{{ book.originalPrice }}</span></p>
            <p class="book-seller">卖家：{{ book.sellerName }}</p>
            <div class="book-actions">
              <el-button type="primary" size="small" @click="toBookDetail(book.id)">查看详情</el-button>
              <el-button type="success" size="small" @click="addToCart(book.id)">加入购物车</el-button>
              <el-button type="warning" size="small" icon="el-icon-star-off" @click="collectBook(book.id)"></el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分页 -->
    <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[4, 8, 12]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        style="margin-top: 20px; text-align: center;"
    >
    </el-pagination>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'

const router = useRouter()
// 搜索筛选
const searchKey = ref('')
const category = ref('')
const currentPage = ref(1)
const pageSize = ref(4)
const total = ref(8)

// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

// 模拟教材数据（买家视角）
const bookList = ref([
  { id: 1, bookName: 'Java编程思想', author: 'Bruce Eckel', sellPrice: 30, originalPrice: 108, sellerName: '卖家1', coverUrl: '' },
  { id: 2, bookName: 'Python入门到精通', author: '张三', sellPrice: 25, originalPrice: 89, sellerName: '卖家1', coverUrl: '' },
  { id: 3, bookName: '红楼梦', author: '曹雪芹', sellPrice: 15, originalPrice: 45, sellerName: '卖家2', coverUrl: '' },
  { id: 4, bookName: '经济学原理', author: '曼昆', sellPrice: 20, originalPrice: 68, sellerName: '卖家2', coverUrl: '' },
  { id: 5, bookName: '数据结构与算法', author: '李四', sellPrice: 20, originalPrice: 79, sellerName: '卖家1', coverUrl: '' },
  { id: 6, bookName: '西游记', author: '吴承恩', sellPrice: 12, originalPrice: 38, sellerName: '卖家2', coverUrl: '' },
  { id: 7, bookName: 'MySQL实战', author: '王五', sellPrice: 28, originalPrice: 99, sellerName: '卖家1', coverUrl: '' },
  { id: 8, bookName: '财务管理', author: '赵六', sellPrice: 18, originalPrice: 59, sellerName: '卖家2', coverUrl: '' }
])

// 搜索教材
const searchBooks = () => {
  // 模拟搜索逻辑
  ElMessage.info(`搜索关键词：${searchKey.value}，分类：${category.value || '全部'}`)
}

// 重置筛选
const resetFilter = () => {
  searchKey.value = ''
  category.value = ''
}

// 跳转到教材详情
const toBookDetail = (id) => {
  router.push(`/book/${id}`)
}

// 加入购物车
const addToCart = (id) => {
  ElMessage.success(`教材ID${id}已加入购物车！`)
}

// 收藏教材
const collectBook = (id) => {
  ElMessage.success(`教材ID${id}已收藏！`)
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
}
const handleCurrentChange = (val) => {
  currentPage.value = val
}
</script>

<style scoped>
.buyer-home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.search-card {
  margin-bottom: 20px;
}
.book-card {
  height: 450px;
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
.book-author {
  font-size: 14px;
  color: #666;
  margin: 0 0 5px 0;
}
.book-price {
  font-size: 16px;
  color: #e64340;
  margin: 0 0 5px 0;
}
.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
  margin-left: 5px;
}
.book-seller {
  font-size: 12px;
  color: #666;
  margin: 0 0 10px 0;
}
.book-actions {
  display: flex;
  gap: 5px;
}
</style>