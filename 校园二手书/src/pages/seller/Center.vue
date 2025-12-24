<template>
  <div class="seller-center">
    <page-header title="卖家中心">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <el-card>
      <div style="margin-bottom: 12px;">
        <el-tag type="success">好评率：{{ (goodRate.goodRate || 0).toFixed(1) }}%</el-tag>
        <span style="margin-left:8px; color:#666;">总评价：{{ goodRate.totalReviews || 0 }}，好评：{{ goodRate.positiveReviews || 0 }}</span>
      </div>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的教材" name="myBooks">
          <el-button type="primary" @click="toPublish">发布新教材</el-button>
          <el-table :data="bookList" border style="margin-top: 20px;">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="bookName" label="教材名称"></el-table-column>
            <el-table-column prop="sellPrice" label="售价" width="100"></el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag type="success" v-if="scope.row.status === 'on_sale'">上架中</el-tag>
                <el-tag type="warning" v-else-if="scope.row.status === 'under_review'">待审核</el-tag>
                <el-tag type="info" v-else-if="scope.row.status === 'offline' && (scope.row.stock || 0) <= 0">已售罄</el-tag>
                <el-tag type="info" v-else-if="scope.row.status === 'offline'">已下架</el-tag>
                <el-tag type="danger" v-else>已驳回</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="primary" size="small" @click="openEdit(scope.row)">编辑</el-button>
                <el-button type="warning" size="small" @click="offline(scope.row.id)">下架</el-button>
                <el-button type="danger" size="small" @click="remove(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-dialog v-model="editVisible" title="编辑教材" width="500px">
            <el-form :model="editForm" label-width="100px">
              <el-form-item label="教材封面">
                <div style="display:flex; align-items:center; gap:12px; flex-wrap: wrap;">
                  <img
                    v-if="editForm.coverUrl"
                    :src="resolveCoverUrl(editForm.coverUrl)"
                    style="width: 88px; height: 120px; object-fit: cover; border-radius: 4px; border: 1px solid #eee;"
                    alt="封面"
                  />
                  <el-upload
                    action="#"
                    list-type="picture-card"
                    :auto-upload="false"
                    :on-change="handleEditCoverChange"
                    :disabled="uploadingCover"
                  >
                    <i class="el-icon-plus"></i>
                  </el-upload>
                </div>
              </el-form-item>
              <el-form-item label="教材名称">
                <el-input v-model="editForm.bookName" />
              </el-form-item>
              <el-form-item label="售价">
                <el-input-number v-model="editForm.sellPrice" :min="1" :max="9999" />
              </el-form-item>
              <el-form-item label="成色">
                <el-select v-model="editForm.conditionLevel">
                  <el-option label="全新" value="全新" />
                  <el-option label="九成新" value="九成新" />
                  <el-option label="八成新" value="八成新" />
                </el-select>
              </el-form-item>
              <el-form-item label="库存">
                <el-input-number v-model="editForm.stock" :min="1" :max="999" />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="editVisible=false">取消</el-button>
              <el-button type="primary" @click="submitEdit">保存</el-button>
            </template>
          </el-dialog>
        </el-tab-pane>
        <el-tab-pane label="我的订单" name="myOrders">
          <el-table :data="orderList" border>
            <el-table-column prop="id" label="订单ID" width="100"></el-table-column>
            <el-table-column prop="bookName" label="教材名称"></el-table-column>
            <el-table-column prop="buyerName" label="买家"></el-table-column>
            <el-table-column prop="price" label="订单金额" width="100"></el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="orderStatusTagType(scope.row.status)">{{ orderStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="收到的评价" name="receivedReviews">
          <el-table :data="receivedReviews" border>
            <el-table-column prop="orderId" label="订单ID" width="100"></el-table-column>
            <el-table-column prop="buyerName" label="买家" width="140"></el-table-column>
            <el-table-column label="评分" width="160">
              <template #default="scope">成色{{ scope.row.scoreCondition }}，服务{{ scope.row.scoreService }}</template>
            </el-table-column>
            <el-table-column prop="tags" label="标签">
              <template #default="scope">
                <el-tag v-for="t in (scope.row.tags || [])" :key="t" style="margin-right:4px;">{{ t }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="comment" label="评论"></el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="收到的投诉" name="receivedComplaints">
          <el-table :data="receivedComplaints" border>
            <el-table-column prop="orderId" label="订单ID" width="100"></el-table-column>
            <el-table-column prop="buyerName" label="买家" width="140"></el-table-column>
            <el-table-column prop="type" label="类型" width="140"></el-table-column>
            <el-table-column prop="detail" label="详情"></el-table-column>
            <el-table-column prop="status" label="状态" width="120"></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { listMyBooks, updateBook, offlineBook, deleteBook } from '@/api/sellerApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listReceivedReviews, getGoodRate } from '@/api/reviewApi'
import { listReceivedComplaints } from '@/api/complaintApi'
import { listSellerOrders } from '@/api/orderApi'
import { uploadFile } from '@/api/bookApi'

const router = useRouter()
// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

const activeTab = ref('myBooks')
const editVisible = ref(false)
const editForm = ref({})
const bookList = ref([])
const receivedReviews = ref([])
const receivedComplaints = ref([])
const goodRate = ref({})
const orderList = ref([])
const uploadingCover = ref(false)

const loadMyBooks = async () => {
  try {
    const res = await listMyBooks()
    bookList.value = res || []
  } catch (e) {
    ElMessage.error('加载我的教材失败')
  }
}

const loadMyOrders = async () => {
  try {
    const res = await listSellerOrders()
    orderList.value = res || []
  } catch (e) {
    ElMessage.error('加载我的订单失败')
  }
}

const resolveCoverUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/book-api/')) return url
  if (url.startsWith('/files/')) return `/book-api${url}`
  return url
}

const handleEditCoverChange = async (file) => {
  const raw = file?.raw
  if (!raw) return
  uploadingCover.value = true
  try {
    const res = await uploadFile(raw)
    if (res && res.url) {
      editForm.value.coverUrl = res.url
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error('封面上传失败')
    }
  } catch (e) {
    ElMessage.error('封面上传失败')
  } finally {
    uploadingCover.value = false
  }
}

const orderStatusText = (status) => {
  if (status === 'pending') return '待付款'
  if (status === 'paid') return '已付款'
  if (status === 'received') return '已收货'
  if (status === 'cancelled') return '已取消'
  if (status === 'expired') return '已超时'
  return status || '-'
}

const orderStatusTagType = (status) => {
  if (status === 'paid') return 'success'
  if (status === 'received') return 'success'
  if (status === 'pending') return 'warning'
  if (status === 'cancelled') return 'info'
  if (status === 'expired') return 'danger'
  return 'info'
}

// 模拟我的教材数据
const openEdit = (row) => {
  editForm.value = { id: row.id, bookName: row.bookName, sellPrice: row.sellPrice, conditionLevel: row.conditionLevel, stock: row.stock, coverUrl: row.coverUrl }
  editVisible.value = true
}

const submitEdit = async () => {
  try {
    const res = await updateBook(editForm.value.id, editForm.value)
    if (!res || !res.id) throw new Error('更新失败')
    ElMessage.success('更新成功，待审核')
    editVisible.value = false
    loadMyBooks()
  } catch (e) {
    ElMessage.error('更新失败')
  }
}

const offline = (id) => {
  ElMessageBox.confirm('确认下架该教材？', '提示', { confirmButtonText: '确认', cancelButtonText: '取消' }).then(() => {
    offlineBook(id).then(() => { ElMessage.success('下架成功'); loadMyBooks() }).catch(() => ElMessage.error('下架失败'))
  })
}

const remove = (id) => {
  ElMessageBox.confirm('确认删除该教材？', '提示', { confirmButtonText: '确认', cancelButtonText: '取消' }).then(() => {
    deleteBook(id).then(() => { ElMessage.success('删除成功'); loadMyBooks() }).catch(() => ElMessage.error('删除失败'))
  })
}

// 跳转到发布教材页
const toPublish = () => {
  router.push('/publish')
}


import { onMounted } from 'vue'
onMounted(() => {
  loadMyBooks()
  listReceivedReviews().then(res => { receivedReviews.value = res || [] }).catch(() => {})
  listReceivedComplaints().then(res => { receivedComplaints.value = res || [] }).catch(() => {})
  getGoodRate().then(res => { goodRate.value = res || {} }).catch(() => {})
})

watch(activeTab, (v) => {
  if (v === 'myBooks') loadMyBooks()
  if (v === 'myOrders') loadMyOrders()
})
</script>

<style scoped>
.seller-center {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
