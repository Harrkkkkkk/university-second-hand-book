<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Home.vue (Buyer)
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Buyer homepage.
 *              Displays book listings with advanced search, filtering, and bulk cart actions.
 *              Also features hot book recommendations.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
<template>
  <div class="buyer-home">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
      
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="highlight">WiseBookPal</span> · 让知识循环流动
        </h1>
        <p class="hero-subtitle">全校最受信赖的二手教材交易平台，学长学姐都在用</p>
        
        <div class="hero-search-box">
          <el-autocomplete
            v-model="searchKey"
            :fetch-suggestions="querySearchAsync"
            placeholder="搜索教材名称 / 作者 "
            :trigger-on-focus="false"
            @select="handleSelect"
            @keyup.enter="searchBooks"
            class="hero-search-input"
            size="large"
          >
            <template #prefix>
              <el-icon class="search-icon"><Search /></el-icon>
            </template>
            <template #suffix>
              <el-button type="primary" @click="searchBooks" class="search-btn" style="border-radius: 4px; margin-right: -14px;">搜索</el-button>
            </template>
          </el-autocomplete>
        </div>
        
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-num">10k+</span>
            <span class="stat-label">在售教材</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">5k+</span>
            <span class="stat-label">累计交易</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">98%</span>
            <span class="stat-label">好评率</span>
          </div>
        </div>
      </div>
    </div>

    <div class="main-container">
      <!-- Category Nav -->
      <div class="category-nav">
        <div 
          v-for="cat in categories" 
          :key="cat.value"
          class="category-pill"
          :class="{ active: category === cat.value }"
          @click="selectCategory(cat.value)"
        >
          <el-icon v-if="cat.icon"><component :is="cat.icon" /></el-icon>
          <span>{{ cat.label }}</span>
        </div>
      </div>

      <!-- Filters & Actions Toolbar -->
      <div class="toolbar-section">
        <div class="filters-left">
          <el-select v-model="conditionLevel" placeholder="成色要求" size="large" class="filter-select" clearable @change="loadBooks">
            <template #prefix><el-icon><files /></el-icon></template>
            <el-option label="全新" value="全新" />
            <el-option label="九成新" value="九成新" />
            <el-option label="八成新" value="八成新" />
          </el-select>
          
          <el-select v-model="sortBy" placeholder="排序方式" size="large" class="filter-select" @change="loadBooks">
            <template #prefix><el-icon><Sort /></el-icon></template>
            <el-option label="综合排序" value="comprehensive" />
            <el-option label="价格: 低到高" value="price_asc" />
            <el-option label="价格: 高到低" value="price_desc" />
            <el-option label="最新发布" value="created_desc" />
          </el-select>

          <div class="price-range">
            <el-input v-model.number="minPrice" type="number" placeholder="¥ 最低" size="large" class="price-input" />
            <span class="range-separator">-</span>
            <el-input v-model.number="maxPrice" type="number" placeholder="¥ 最高" size="large" class="price-input" />
            <el-button circle :icon="Search" @click="loadBooks" type="primary" plain></el-button>
          </div>
        </div>

        <div class="actions-right">
          <el-button @click="resetFilter" plain round icon="RefreshRight" size="large" style="padding-left: 0px;">重置</el-button>
          <el-badge :value="selectedIds.length" :hidden="selectedIds.length === 0" type="primary">
            <el-button type="success" @click="bulkAddToCart" round icon="ShoppingCart" class="bulk-btn" size="large" style="padding-left: 0px;">
              批量加购
            </el-button>
          </el-badge>
        </div>
      </div>

      <!-- Book List -->
      <div class="book-list-wrapper" v-loading="loading" element-loading-text="加载中...">
        <template v-if="bookList.length > 0">
          <div class="book-grid">
            <div 
              class="book-card-item" 
              v-for="book in bookList" 
              :key="book.id"
            >
              <div class="book-card-inner">
                <!-- Selection Checkbox -->
                <div class="card-select">
                  <el-checkbox v-model="selectedMap[book.id]" @change="onSelectChange(book.id)" size="large" />
                </div>
                
                <!-- Cover Image -->
                <div class="book-cover-wrapper" @click="toBookDetail(book.id)">
                  <div class="condition-badge" :class="getConditionClass(book.conditionLevel)">
                    {{ book.conditionLevel || '二手' }}
                  </div>
                  <el-image 
                    :src="book.coverUrl || 'https://picsum.photos/200/280'" 
                    class="book-cover" 
                    fit="cover" 
                    loading="lazy"
                  >
                    <template #placeholder>
                      <div class="image-placeholder">
                        <el-icon class="is-loading"><Loading /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  
                  <!-- Hover Actions -->
                  <div class="hover-actions">
                    <el-button type="primary" circle :icon="View" @click.stop="toBookDetail(book.id)" title="查看详情"></el-button>
                    <el-button type="success" circle :icon="ShoppingCart" @click.stop="addToCart(book.id)" title="加入购物车"></el-button>
                  </div>
                </div>
                
                <!-- Info -->
                <div class="book-info">
                  <div class="info-main">
                    <h3 class="book-title" :title="book.bookName" @click="toBookDetail(book.id)">{{ book.bookName }}</h3>
                    <div class="book-author" :title="book.author">{{ book.author }}</div>
                  </div>
                  
                  <div class="seller-row">
                    <el-avatar :size="20" :src="book.sellerAvatar" class="seller-avatar">
                      {{ book.sellerName ? book.sellerName.charAt(0).toUpperCase() : 'U' }}
                    </el-avatar>
                    <span class="seller-name">{{ book.sellerName }}</span>
                  </div>

                  <div class="book-footer">
                    <div class="price-box">
                      <span class="currency">¥</span>
                      <span class="price">{{ book.sellPrice }}</span>
                    </div>
                    
                    <el-button 
                      class="collect-btn"
                      circle 
                      size="small" 
                      :type="collectedMap[book.id] ? 'warning' : 'info'" 
                      :plain="!collectedMap[book.id]"
                      @click="collectBook(book.id)"
                      :icon="collectedMap[book.id] ? StarFilled : Star"
                    ></el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- No Results / Hot Recommendations -->
        <div v-else class="no-results-section">
          <el-empty description="暂无符合条件的教材" :image-size="200" />
          
          <div class="recommendation-box" v-if="hotBooks.length > 0">
            <div class="rec-header">
              <el-icon class="fire-icon"><Trophy /></el-icon>
              <h3>热门推荐</h3>
              <span class="rec-subtitle">全校同学都在抢的好书</span>
            </div>
            <div class="book-grid">
              <div 
                class="book-card-item" 
                v-for="book in hotBooks" 
                :key="'hot-'+book.id"
              >
                <div class="book-card-inner">
                  <div class="book-cover-wrapper" @click="toBookDetail(book.id)">
                    <div class="hot-badge">HOT</div>
                    <el-image :src="book.coverUrl" class="book-cover" fit="cover" loading="lazy" />
                  </div>
                  <div class="book-info">
                    <h3 class="book-title" @click="toBookDetail(book.id)">{{ book.bookName }}</h3>
                    <div class="book-footer">
                      <div class="price-box">
                        <span class="currency">¥</span>
                        <span class="price">{{ book.sellPrice }}</span>
                      </div>
                      <el-button type="primary" size="small" icon="ShoppingCart" circle @click="addToCart(book.id)" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="pagination-container" v-if="bookList.length > 0">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[8, 12, 16, 24, 32]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            background
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBookPage, getHotBooks, getSearchSuggestions } from '@/api/bookApi'
import { addFavorite, removeFavorite, getCollectedIds } from '@/api/collectApi'
import { addToCart as apiAddToCart } from '@/api/cartApi'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { Files, Loading, RefreshRight, Search, ShoppingCart, Sort, Star, StarFilled, Trophy, View } from '@element-plus/icons-vue'

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

const getConditionClass = (level) => {
  if (!level) return 'badge-good'
  if (level === '全新') return 'badge-new'
  if (level === '九成新') return 'badge-like-new'
  return 'badge-good'
}

/**
 * Function: loadCollectedIds
 * Description: Fetches the list of book IDs collected by the current user.
 */
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

/**
 * Function: loadBooks
 * Description: Fetches books based on search filters.
 *              Falls back to hot books if no results found.
 */
const loadBooks = async () => {
  loading.value = true
  try {
    const params = { bookName: searchKey.value, pageNum: currentPage.value, pageSize: pageSize.value, sortBy: sortBy.value }
    if (conditionLevel.value) params.conditionLevel = conditionLevel.value
    if (minPrice.value !== '' && minPrice.value != null) params.minPrice = minPrice.value
    if (maxPrice.value !== '' && maxPrice.value != null) params.maxPrice = maxPrice.value
    
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

/**
 * Function: querySearchAsync
 * Description: Fetches search suggestions for autocomplete.
 * Input: queryString (String), cb (Function)
 */
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
/**
 * Function: searchBooks
 * Description: Filters invalid characters and triggers book search.
 */
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
/**
 * Function: resetFilter
 * Description: Resets all search filters to default values.
 */
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
/**
 * Function: addToCart
 * Description: Adds a single book to cart.
 * Input: id (Number)
 */
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

/**
 * Function: bulkAddToCart
 * Description: Adds multiple selected books to cart.
 */
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
/**
 * Function: collectBook
 * Description: Toggles collection status for a book.
 * Input: id (Number)
 */
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
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* Hero Section */
.hero-section {
  position: relative;
  height: 400px;
  background: linear-gradient(135deg, #1c2434 0%, #2c3e50 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  overflow: hidden;
  margin-bottom: -60px; /* Overlap effect */
  padding-bottom: 60px;
}

.hero-bg-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.shape {
  position: absolute;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  animation: float 20s infinite ease-in-out;
}

.shape-1 { width: 500px; height: 500px; top: -100px; left: -100px; animation-delay: 0s; }
.shape-2 { width: 300px; height: 300px; bottom: 50px; right: 10%; animation-delay: -5s; }
.shape-3 { width: 150px; height: 150px; top: 20%; right: 30%; animation-delay: -10s; background: rgba(64, 158, 255, 0.1); }

@keyframes float {
  0% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(10deg); }
  100% { transform: translateY(0) rotate(0deg); }
}

.hero-content {
  text-align: center;
  z-index: 2;
  width: 100%;
  max-width: 900px;
  padding: 0 20px;
  position: relative;
}

.hero-title {
  font-size: 3rem;
  font-weight: 800;
  margin-bottom: 12px;
  letter-spacing: -1px;
}

.hero-title .highlight {
  background: linear-gradient(120deg, #409EFF, #36cfc9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  font-size: 1.25rem;
  opacity: 0.9;
  margin-bottom: 40px;
  font-weight: 300;
}

.hero-search-box {
  max-width: 680px;
  margin: 0 auto 50px;
}

.hero-search-input :deep(.el-input__wrapper) {
  border-radius: 4px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
  padding-left: 20px;
  height: 56px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.hero-search-input :deep(.el-input__inner) {
  font-size: 16px;
  height: 56px;
}

.search-btn {
  height: 40px;
  padding: 0 24px;
  font-size: 16px;
  margin-right: -4px;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  align-items: center;
}

.stat-item {
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.7;
}

.stat-divider {
  width: 1px;
  height: 24px;
  background: rgba(255,255,255,0.2);
}

/* Main Container */
.main-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px 60px;
  position: relative;
  z-index: 10;
}

/* Category Nav */
.category-nav {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 30px;
}

.category-pill {
  background: white;
  padding: 10px 20px;
  border-radius: 30px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transition: all 0.3s;
  color: #606266;
  font-weight: 500;
}

.category-pill:hover, .category-pill.active {
  background: #409EFF;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(64, 158, 255, 0.2);
}

/* Toolbar */
.toolbar-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 40px;
}

.filters-left {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-select {
  width: 150px;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f5f7fa;
  padding: 4px;
  border-radius: 8px;
}

.price-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
  width: 80px;
}

.range-separator {
  color: #909399;
}

/* Book Grid */
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
}

.book-card-item {
  height: 100%;
}

.book-card-inner {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #f0f0f0;
}

.book-card-inner:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.1);
  border-color: transparent;
}

.card-select {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.2s;
}

.book-card-inner:hover .card-select,
.card-select:has(.is-checked) {
  opacity: 1;
}

.book-cover-wrapper {
  position: relative;
  padding-top: 130%; /* 10:13 Aspect Ratio */
  overflow: hidden;
  cursor: pointer;
  background: #f9f9f9;
}

.book-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.book-card-inner:hover .book-cover {
  transform: scale(1.08);
}

.condition-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  z-index: 2;
  backdrop-filter: blur(4px);
  color: white;
}

.badge-new { background: rgba(103, 194, 58, 0.9); }
.badge-like-new { background: rgba(64, 158, 255, 0.9); }
.badge-good { background: rgba(230, 162, 60, 0.9); }

.hover-actions {
  position: absolute;
  bottom: 16px;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 12px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.3s;
  z-index: 3;
}

.book-cover-wrapper:hover .hover-actions {
  opacity: 1;
  transform: translateY(0);
}

.book-info {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.info-main {
  margin-bottom: 12px;
}

.book-title {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 6px;
  line-height: 1.4;
  height: 44px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: #2c3e50;
  cursor: pointer;
  transition: color 0.2s;
}

.book-title:hover {
  color: #409EFF;
}

.book-author {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.seller-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 12px;
  color: #606266;
}

.seller-avatar {
  background: #f0f2f5;
  color: #909399;
}

.book-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f5f7fa;
}

.price-box {
  color: #f56c6c;
  font-weight: 700;
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 14px;
  margin-right: 2px;
}

.price {
  font-size: 20px;
}

/* Recommendations */
.no-results-section {
  text-align: center;
  padding: 40px 0;
}

.recommendation-box {
  margin-top: 60px;
  text-align: left;
}

.rec-header {
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.fire-icon {
  font-size: 24px;
  color: #f56c6c;
}

.rec-header h3 {
  font-size: 24px;
  margin: 0;
  color: #303133;
}

.rec-subtitle {
  color: #909399;
  font-size: 14px;
  margin-left: 8px;
}

.hot-badge {
  position: absolute;
  top: 0;
  left: 0;
  background: #f56c6c;
  color: white;
  padding: 4px 12px;
  border-bottom-right-radius: 12px;
  font-size: 12px;
  font-weight: 700;
  z-index: 2;
}

/* Pagination */
.pagination-container {
  margin-top: 60px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .hero-section {
    height: auto;
    padding: 100px 0 60px;
  }
  
  .hero-title {
    font-size: 2rem;
  }
  
  .toolbar-section {
    padding: 16px;
  }
  
  .book-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
