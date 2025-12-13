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
        <el-col :span="12">
          <el-row :gutter="10">
            <el-col :span="6">
              <el-select v-model="conditionLevel" placeholder="成色">
                <el-option label="全部" value=""></el-option>
                <el-option label="全新" value="全新"></el-option>
                <el-option label="九成新" value="九成新"></el-option>
                <el-option label="八成新" value="八成新"></el-option>
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-input v-model.number="minPrice" placeholder="最低价"></el-input>
            </el-col>
            <el-col :span="6">
              <el-input v-model.number="maxPrice" placeholder="最高价"></el-input>
            </el-col>
            <el-col :span="6">
              <el-select v-model="sortBy" placeholder="排序">
                <el-option label="价格从低到高" value="price_asc"></el-option>
                <el-option label="价格从高到低" value="price_desc"></el-option>
                <el-option label="发布时间从新到旧" value="created_desc"></el-option>
              </el-select>
            </el-col>
          </el-row>
          <div style="margin-top:10px;">
            <el-button type="primary" @click="searchBooks">搜索</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </div>
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
              <el-button type="warning" size="small" icon="el-icon-star-off" @click="collectBook(book.id)">收藏</el-button>
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
import { getBookPage } from '@/api/bookApi'
import { addFavorite } from '@/api/collectApi'
import { addToCart as apiAddToCart } from '@/api/cartApi'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'

const router = useRouter()
// 搜索筛选
const searchKey = ref('')
const category = ref('')
const conditionLevel = ref('')
const sortBy = ref('created_desc')
const minPrice = ref(undefined)
const maxPrice = ref(undefined)
const currentPage = ref(1)
const pageSize = ref(4)
const total = ref(0)

// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

const bookList = ref([])

const loadBooks = async () => {
  try {
    const params = { bookName: searchKey.value, pageNum: currentPage.value, pageSize: pageSize.value, sortBy: sortBy.value }
    if (conditionLevel.value) params.conditionLevel = conditionLevel.value
    if (minPrice.value != null) params.minPrice = minPrice.value
    if (maxPrice.value != null) params.maxPrice = maxPrice.value
    const res = await getBookPage(params)
    bookList.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('加载教材失败')
  }
}

// 搜索教材
const searchBooks = () => {
  const sanitized = searchKey.value.replace(/[^\w\u4e00-\u9fa5]/g, '')
  if (sanitized !== searchKey.value) {
    searchKey.value = sanitized
    ElMessage.warning('已为您过滤无效字符，请确认搜索内容')
  }
  currentPage.value = 1
  loadBooks()
}

// 重置筛选
const resetFilter = () => {
  searchKey.value = ''
  category.value = ''
  conditionLevel.value = ''
  sortBy.value = 'created_desc'
  minPrice.value = undefined
  maxPrice.value = undefined
  currentPage.value = 1
  loadBooks()
}

// 跳转到教材详情
const toBookDetail = (id) => {
  router.push(`/book/${id}`)
}

// 加入购物车
const addToCart = async (id) => {
  try {
    await apiAddToCart(id)
    ElMessage.success(`教材ID${id}已加入购物车！`)
  } catch (e) {
    ElMessage.error('加入购物车失败')
  }
}

// 收藏教材
const collectBook = async (id) => {
  try {
    await addFavorite(id)
    ElMessage.success(`教材ID${id}已收藏！`)
  } catch (e) {
    ElMessage.error('收藏失败')
  }
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadBooks()
}
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadBooks()
}

import { onMounted } from 'vue'
onMounted(() => {
  loadBooks()
})
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
