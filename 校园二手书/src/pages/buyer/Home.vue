<template>
  <div class="buyer-home">
    <page-header title="买家中心 - 教材选购">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <!-- 搜索+筛选区 -->
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-autocomplete
            v-model="searchKey"
            :fetch-suggestions="querySearchAsync"
            placeholder="输入教材名称/作者/ISBN搜索"
            :trigger-on-focus="false"
            @select="handleSelect"
            @keyup.enter="searchBooks"
            style="width: 100%"
          >
            <template #suffix>
              <el-icon class="el-input__icon" @click="searchBooks"><search /></el-icon>
            </template>
          </el-autocomplete>
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
                <el-option label="综合排序" value="comprehensive"></el-option>
                <el-option label="价格从低到高" value="price_asc"></el-option>
                <el-option label="价格从高到低" value="price_desc"></el-option>
                <el-option label="发布时间从新到旧" value="created_desc"></el-option>
              </el-select>
        </el-col>
      </el-row>
      </el-col>
      </el-row>
      <div style="margin-top:10px;">
        <el-button type="primary" @click="searchBooks">搜索</el-button>
        <el-button @click="resetFilter">重置</el-button>
        <el-button type="success" @click="bulkAddToCart" style="margin-left:8px;">批量加入购物车</el-button>
      </div>
    </el-card>

    <!-- 教材列表区 -->
    <div v-loading="loading">
      <el-row :gutter="20" style="margin-top: 20px;" v-if="bookList.length > 0">
        <el-col :span="6" v-for="book in bookList" :key="book.id">
          <el-card shadow="hover" class="book-card">
            <el-checkbox v-model="selectedMap[book.id]" @change="onSelectChange(book.id)" style="position:absolute; z-index:1;"></el-checkbox>
            <img :src="book.coverUrl || 'https://picsum.photos/200/280'" class="book-cover" alt="教材封面">
            <div class="book-info">
              <h3 class="book-name">{{ book.bookName }}</h3>
              <p class="book-author">作者：{{ book.author }}</p>
              <p class="book-price">¥{{ book.sellPrice }} <span class="original-price">原价¥{{ book.originalPrice }}</span></p>
              <p class="book-seller">卖家：{{ book.sellerName }}</p>
              <div class="book-actions">
                <el-button type="primary" size="small" @click="toBookDetail(book.id)">查看详情</el-button>
                <el-button type="success" size="small" @click="addToCart(book.id)">加入购物车</el-button>
                <el-button 
                  :type="collectedMap[book.id] ? 'default' : 'warning'" 
                  size="small" 
                  :icon="collectedMap[book.id] ? 'el-icon-star-on' : 'el-icon-star-off'" 
                  @click="collectBook(book.id)"
                >
                  {{ collectedMap[book.id] ? '已收藏' : '收藏' }}
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 无结果显示热门推荐 -->
      <div v-else class="no-results-box" style="margin-top: 40px;">
        <el-empty description="未找到相关教材，为您推荐热门教材" />
        <h3 style="margin-left: 20px; border-left: 4px solid #409EFF; padding-left: 10px;">热门教材推荐</h3>
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="6" v-for="book in hotBooks" :key="'hot-'+book.id">
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
                  <el-button 
                    :type="collectedMap[book.id] ? 'default' : 'warning'" 
                    size="small" 
                    :icon="collectedMap[book.id] ? 'el-icon-star-on' : 'el-icon-star-off'" 
                    @click="collectBook(book.id)"
                  >
                    {{ collectedMap[book.id] ? '已收藏' : '收藏' }}
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 分页 -->
    <el-pagination
        v-if="bookList.length > 0"
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
import { getBookPage, getHotBooks, getSearchSuggestions } from '@/api/bookApi'
import { addFavorite, removeFavorite, getCollectedIds } from '@/api/collectApi'
import { addToCart as apiAddToCart } from '@/api/cartApi'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
// 搜索筛选
const searchKey = ref('')
const category = ref('')
const conditionLevel = ref('')
const sortBy = ref('comprehensive') // Default to comprehensive
const minPrice = ref(undefined)
const maxPrice = ref(undefined)
const currentPage = ref(1)
const pageSize = ref(4)
const total = ref(0)
const loading = ref(false)

// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

const bookList = ref([])
const hotBooks = ref([])
const selectedIds = ref([])
const selectedMap = ref({})
const collectedMap = ref({})

const loadCollectedIds = async () => {
  try {
    const ids = await getCollectedIds()
    if (ids) {
      const m = {}
      ids.forEach(id => { m[id] = true })
      collectedMap.value = m
    }
  } catch (e) {
    console.error('加载收藏列表失败', e)
  }
}

const loadBooks = async () => {
  loading.value = true
  try {
    const params = { bookName: searchKey.value, pageNum: currentPage.value, pageSize: pageSize.value, sortBy: sortBy.value }
    if (conditionLevel.value) params.conditionLevel = conditionLevel.value
    if (minPrice.value != null) params.minPrice = minPrice.value
    if (maxPrice.value != null) params.maxPrice = maxPrice.value
    
    // Check if we need to filter special chars logic here? 
    // Actually the check is done in searchBooks before calling loadBooks, but loadBooks is also called by pagination/reset.
    // It's better to ensure searchKey is clean or just trust the input if it comes from internal state.
    
    const res = await getBookPage(params)
    bookList.value = res.records || []
    total.value = res.total || 0
    
    if (bookList.value.length === 0) {
      // Load hot books
      const hots = await getHotBooks(10)
      hotBooks.value = hots || []
    } else {
      hotBooks.value = []
    }
    
    // Load collected status
    loadCollectedIds()
  } catch (e) {
    ElMessage.error('加载教材失败')
  } finally {
    loading.value = false
  }
}

const querySearchAsync = async (queryString, cb) => {
  if (!queryString) {
    cb([])
    return
  }
  try {
    const suggestions = await getSearchSuggestions(queryString)
    // suggestions is array of strings. Element autocomplete expects array of objects { value: '...' }
    const results = suggestions.map(s => ({ value: s }))
    cb(results)
  } catch (e) {
    cb([])
  }
}

const handleSelect = (item) => {
  searchKey.value = item.value
  searchBooks()
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
  sortBy.value = 'comprehensive'
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

const onSelectChange = (id) => {
  const checked = !!selectedMap.value[id]
  const set = new Set(selectedIds.value)
  if (checked) set.add(id)
  else set.delete(id)
  selectedIds.value = Array.from(set)
}

const bulkAddToCart = async () => {
  if (!selectedIds.value.length) { ElMessage.warning('请先选择教材'); return }
  try {
    for (const id of selectedIds.value) {
      await apiAddToCart(id)
    }
    ElMessage.success(`已批量加入 ${selectedIds.value.length} 本教材到购物车`)
    selectedIds.value = []
    selectedMap.value = {}

  } catch (e) {
    ElMessage.error('批量加入购物车失败')
  }
}

// 收藏教材
const collectBook = async (id) => {
  try {
    if (collectedMap.value[id]) {
      await removeFavorite(id)
      delete collectedMap.value[id]
      ElMessage.success(`教材ID${id}已取消收藏`)
    } else {
      await addFavorite(id)
      collectedMap.value[id] = true
      ElMessage.success(`教材ID${id}已收藏！`)
    }
  } catch (e) {
    ElMessage.error('操作失败')
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

const loadHotBooks = async () => {
  try {
    hotBooks.value = await getHotBooks(4) || []
  } catch (e) {
    console.error('加载热门书籍失败', e)
  }
}

import { onMounted } from 'vue'
onMounted(() => {
  loadBooks()
  loadHotBooks()
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
.hot-card {
  position: relative;
  border: 1px solid #ffd04b;
}
.hot-badge {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #f56c6c;
  color: white;
  padding: 2px 8px;
  font-size: 12px;
  border-bottom-left-radius: 8px;
  z-index: 10;
}
</style>
