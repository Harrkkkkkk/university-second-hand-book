<template>
  <div class="seller-detail">
    <page-header :title="`卖家详情 - ${sellerName}`" :goBack="goBack">
    </page-header>

    <el-card>
      <div class="seller-info">
        <div class="avatar-box">
           <el-avatar :size="64">{{ sellerName.charAt(0) }}</el-avatar>
           <h3 style="margin-top: 10px;">{{ sellerName }}</h3>
        </div>
        <div class="actions">
           <el-button type="primary" icon="el-icon-chat-dot-round" @click="toChat">在线沟通</el-button>
        </div>
      </div>

      <el-divider content-position="left">历史评价</el-divider>

      <el-table :data="reviewList" border empty-text="暂无评价">
        <el-table-column prop="buyerName" label="买家" width="140"></el-table-column>
        <el-table-column label="评分" width="160">
          <template #default="scope">成色{{ scope.row.scoreCondition }}，服务{{ scope.row.scoreService }}</template>
        </el-table-column>
        <el-table-column prop="tags" label="标签">
          <template #default="scope">
            <el-tag v-for="t in (scope.row.tags || [])" :key="t" style="margin-right:4px;">{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="图片" width="160">
          <template #default="scope">
            <div v-if="scope.row.images && scope.row.images.length">
              <el-image 
                v-for="(img, idx) in scope.row.images" 
                :key="idx" 
                :src="resolveImageUrl(img)" 
                :preview-src-list="scope.row.images.map(resolveImageUrl)"
                style="width: 40px; height: 40px; margin-right: 4px;"
                fit="cover"
                preview-teleported
              />
            </div>
            <span v-else style="color:#999;font-size:12px;">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评论"></el-table-column>
        <el-table-column prop="createTime" label="时间" width="180"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import { ElMessage } from 'element-plus'
import { listSellerReviews } from '@/api/reviewApi'

const route = useRoute()
const router = useRouter()

const sellerName = ref(route.params.name || '')
const reviewList = ref([])

const goBack = () => {
  router.back()
}

const resolveImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/book-api/')) return url
  if (url.startsWith('/files/')) return `/book-api${url}`
  return url
}

/**
 * Function: toChat
 * Description: Initiates a chat with the seller.
 */
const toChat = () => {
  const currentUsername = sessionStorage.getItem('username')
  if (sellerName.value === currentUsername) {
    ElMessage.warning('无法与自己聊天')
    return
  }
  router.push({ path: '/chat', query: { peer: sellerName.value } })
}

/**
 * Function: loadReviews
 * Description: Loads reviews received by the seller.
 */
const loadReviews = async () => {
  try {
    const res = await listSellerReviews(sellerName.value)
    reviewList.value = res || []
  } catch (e) {
    ElMessage.error('加载卖家评论失败')
  }
}

onMounted(() => {
  if (!sellerName.value) {
    ElMessage.error('卖家信息缺失')
    return
  }
  loadReviews()
})
</script>

<style scoped>
.seller-detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
.seller-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}
.avatar-box {
  display: flex;
  align-items: center;
  gap: 20px;
}
</style>
