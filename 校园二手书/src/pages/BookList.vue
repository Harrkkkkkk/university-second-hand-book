<template>
  <div class="book-list-page">
    <page-header title="二手教材列表"></page-header>

    <el-card class="search-card">
      <el-form :model="searchForm" inline @submit.prevent="searchBooks">
        <el-form-item label="教材名称">
          <el-input v-model="searchForm.bookName" placeholder="请输入教材名称"></el-input>
        </el-form-item>
        <el-form-item label="价格区间">
          <el-input v-model="searchForm.minPrice" placeholder="最低价格" style="width: 100px;"></el-input>
          <span style="margin: 0 5px;">-</span>
          <el-input v-model="searchForm.maxPrice" placeholder="最高价格" style="width: 100px;"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchBooks">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="book-list">
      <book-card v-for="book in bookList" :key="book.id" :book="book"></book-card>
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
import { ref, onMounted } from 'vue'
import { getBookPage } from '@/api/bookApi'
import BookCard from '@/components/BookCard.vue'
import PageHeader from '@/components/PageHeader.vue'

const searchForm = ref({
  bookName: '',
  minPrice: '',
  maxPrice: '',
  pageNum: 1,
  pageSize: 10
})

const bookList = ref([])
const total = ref(0)

const loadBooks = async () => {
  try {
    const res = await getBookPage(searchForm.value)
    bookList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载商品失败：', error)
  }
}

const searchBooks = () => {
  searchForm.value.pageNum = 1
  loadBooks()
}

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

const handleSizeChange = (val) => {
  searchForm.value.pageSize = val
  loadBooks()
}
const handleCurrentChange = (val) => {
  searchForm.value.pageNum = val
  loadBooks()
}

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