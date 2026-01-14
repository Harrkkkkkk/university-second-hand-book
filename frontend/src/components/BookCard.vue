<template>
  <el-card class="book-card" shadow="hover">
    <div class="book-info">
      <img v-if="book.coverUrl" :src="book.coverUrl" class="book-cover" alt="封面">
      <div class="book-content">
        <h3 class="book-name">{{ book.bookName }}</h3>
        <p class="book-author">作者：{{ book.author }}</p>
        <p class="book-publisher">出版社：{{ book.publisher }}</p>
        <div class="book-price">
          <span class="original-price">原价：¥{{ book.originalPrice }}</span>
          <span class="sell-price">售价：¥{{ book.sellPrice }}</span>
        </div>
        <el-button type="primary" size="small" @click="toDetail">查看详情</el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

const props = defineProps({
  book: {
    type: Object,
    required: true
  }
})

/**
 * Function: toDetail
 * Description: Navigates to the book detail page.
 */
const toDetail = () => {
  router.push({ name: 'BookDetail', params: { id: props.book.id } })
}
</script>

<style scoped>
.book-card {
  width: 100%;
  margin-bottom: 20px;
}
.book-info {
  display: flex;
  gap: 20px;
}
.book-cover {
  width: 120px;
  height: 160px;
  object-fit: cover;
}
.book-content {
  flex: 1;
}
.book-name {
  font-size: 18px;
  margin: 0 0 10px 0;
}
.book-author, .book-publisher {
  margin: 5px 0;
  color: #666;
}
.book-price {
  margin: 10px 0;
}
.original-price {
  text-decoration: line-through;
  color: #999;
  margin-right: 10px;
}
.sell-price {
  color: #e64340;
  font-size: 16px;
  font-weight: bold;
}
</style>