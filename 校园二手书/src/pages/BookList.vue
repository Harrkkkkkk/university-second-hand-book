<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: BookList.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Book listing page.
 *              Displays a list of books with search, filter, and pagination functionalities.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
<template>
  <div class="book-list-page">
    <page-header title="二手教材列表"></page-header>

    <el-card class="search-card">
      <el-form :model="searchForm" inline @submit.prevent="searchBooks">
        <el-form-item label="教材名称">
          <el-input v-model="searchForm.bookName" placeholder="请输入教材名称"></el-input>
        </el-form-item>
        <el-form-item label="价格区间">
          <el-input v-model.number="searchForm.minPrice" type="number" placeholder="最低价格" style="width: 100px;"></el-input>
          <span style="margin: 0 5px;">-</span>
          <el-input v-model.number="searchForm.maxPrice" type="number" placeholder="最高价格" style="width: 100px;"></el-input>
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="searchForm.sortBy" placeholder="选择排序" style="width: 160px;">
            <el-option label="默认" value="id" />
            <el-option label="价格升序" value="price_asc" />
            <el-option label="价格降序" value="price_desc" />
            <el-option label="最新上架" value="created_desc" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchBooks">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="book-list">
      <book-card v-for="book in bookList" :key="book.id" :book="book"></book-card>
      <div v-if="bookList.length === 0" style="padding:20px; text-align:center; color:#666">暂无搜索结果，看看热门推荐</div>
      <div v-if="bookList.length === 0" style="margin-top:10px;">
        <book-card v-for="book in recommendList" :key="'rec-'+book.id" :book="book"></book-card>
      </div>
    </div>

    <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="searchForm.pageNum"
        :page-sizes="[10, 20, 50]"
        :page-size="searchForm.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
    </el-pagination>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getBookPage, getHotBooks } from '@/api/bookApi'
import BookCard from '@/components/BookCard.vue'
import PageHeader from '@/components/PageHeader.vue'

const searchForm = ref({
  bookName: '',
  minPrice: '',
  maxPrice: '',
  pageNum: 1,
  pageSize: 10,
  sortBy: 'id'
})

const bookList = ref([])
const total = ref(0)
const recommendList = ref([])
let lastSortChange = 0

/**
 * Function: loadBooks
 * Description: Fetches a page of books based on current search criteria.
 *              Loads hot books if no results are found.
 */
const loadBooks = async () => {
  try {
    const params = { ...searchForm.value }
    if (params.minPrice === '') delete params.minPrice
    if (params.maxPrice === '') delete params.maxPrice
    const res = await getBookPage(params)
    bookList.value = res.records || []
    total.value = res.total || 0
    if (bookList.value.length === 0) {
      const rec = await getHotBooks(6)
      recommendList.value = rec || []
    } else {
      recommendList.value = []
    }
  } catch (error) {
    console.error('加载商品失败：', error)
  }
}

/**
 * Function: searchBooks
 * Description: Resets page to 1 and loads books with new filters.
 */
const searchBooks = () => {
  searchForm.value.pageNum = 1
  loadBooks()
}

/**
 * Function: resetSearch
 * Description: Clears all filters and reloads the default book list.
 */
const resetSearch = () => {
  searchForm.value = {
    bookName: '',
    minPrice: '',
    maxPrice: '',
    pageNum: 1,
    pageSize: 10
  }
  loadBooks()
}

/**
 * Function: handleSizeChange
 * Description: Updates page size and reloads.
 * Input: val (Number)
 */
const handleSizeChange = (val) => {
  searchForm.value.pageSize = val
  loadBooks()
}

/**
 * Function: handleCurrentChange
 * Description: Updates current page number and reloads.
 * Input: val (Number)
 */
const handleCurrentChange = (val) => {
  searchForm.value.pageNum = val
  loadBooks()
}

/**
 * Function: handleSortChange
 * Description: Handles sort criteria changes with debouncing (800ms).
 */
const handleSortChange = () => {
  const now = Date.now()
  if (now - lastSortChange < 800) return
  lastSortChange = now
  searchForm.value.pageNum = 1
  loadBooks()
}

watch(() => searchForm.value.sortBy, handleSortChange)

onMounted(() => {
  loadBooks()
})
</script>

<style scoped>
.book-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.search-card {
  margin-bottom: 20px;
}
.book-list {
  margin-bottom: 20px;
}
.el-pagination {
  text-align: center;
}
</style>
