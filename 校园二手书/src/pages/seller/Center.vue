<template>
  <div class="seller-center">
    <!-- Header Section -->
    <div class="dashboard-header">
      <div class="header-left">
        <h1 class="page-title">卖家中心</h1>
        <p class="page-subtitle">管理您的商品、订单与收益</p>
      </div>
      <el-button type="danger" plain size="small" @click="logout">退出登录</el-button>
    </div>

    <!-- Stats Cards -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon bg-blue">
              <el-icon><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">在售教材</div>
              <div class="stat-value">{{ bookList.filter(b => b.status === 'on_sale').length }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon bg-green">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">好评率</div>
              <div class="stat-value">{{ (goodRate.goodRate || 0).toFixed(1) }}%</div>
              <div class="stat-desc">基于 {{ goodRate.totalReviews || 0 }} 条评价</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon bg-orange">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">累计结算</div>
              <div class="stat-value">¥{{ totalSettledAmount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Main Content -->
    <el-card class="main-card" :body-style="{ padding: '0' }">
      <el-tabs v-model="activeTab" class="dashboard-tabs">
        <el-tab-pane name="myBooks">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><Reading /></el-icon>
              <span>我的教材</span>
            </span>
          </template>
          <div class="tab-content">
            <div class="action-bar">
              <el-button type="primary" size="large" :icon="Plus" @click="toPublish">发布新教材</el-button>
            </div>
            <el-table :data="bookList" border stripe highlight-current-row>
              <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
              <el-table-column label="教材信息" min-width="200">
                <template #default="scope">
                  <div class="book-info-cell">
                    <el-image 
                      :src="resolveCoverUrl(scope.row.coverUrl)" 
                      class="book-thumb" 
                      fit="cover"
                    >
                      <template #error>
                        <div class="image-slot"><el-icon><Picture /></el-icon></div>
                      </template>
                    </el-image>
                    <div class="book-details">
                      <div class="book-name">{{ scope.row.bookName }}</div>
                      <div class="book-meta">库存: {{ scope.row.stock }} | {{ scope.row.conditionLevel }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="sellPrice" label="售价" width="100" align="center">
                <template #default="scope">
                  <span class="price-text">¥{{ scope.row.sellPrice }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="scope">
                  <el-tag effect="dark" type="success" v-if="scope.row.status === 'on_sale'">上架中</el-tag>
                  <el-tag effect="plain" type="warning" v-else-if="scope.row.status === 'under_review'">待审核</el-tag>
                  <el-tag effect="plain" type="info" v-else-if="scope.row.status === 'offline' && (scope.row.stock || 0) <= 0">已售罄</el-tag>
                  <el-tag effect="plain" type="info" v-else-if="scope.row.status === 'offline'">已下架</el-tag>
                  <el-tag effect="dark" type="danger" v-else>已驳回</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" align="center" fixed="right">
                <template #default="scope">
                  <el-button type="primary" link @click="openEdit(scope.row)">编辑</el-button>
                  <el-button type="warning" link @click="offline(scope.row.id)" v-if="scope.row.status === 'on_sale'">下架</el-button>
                  <el-button type="danger" link @click="remove(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane name="myOrders">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><List /></el-icon>
              <span>订单管理</span>
            </span>
          </template>
          <div class="tab-content">
            <el-table :data="orderList" border stripe>
              <el-table-column prop="id" label="订单ID" width="100" align="center"></el-table-column>
              <el-table-column prop="bookName" label="教材名称"></el-table-column>
              <el-table-column prop="buyerName" label="买家" width="120"></el-table-column>
              <el-table-column prop="price" label="金额" width="100" align="center">
                <template #default="scope">¥{{ scope.row.price }}</template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120" align="center">
                <template #default="scope">
                  <el-tag :type="orderStatusTagType(scope.row.status)" effect="light">{{ orderStatusText(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane name="settlements">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><Money /></el-icon>
              <span>结算记录</span>
            </span>
          </template>
          <div class="tab-content">
            <div class="action-bar">
              <el-button type="success" plain :icon="Download" @click="onExportSettlements">导出Excel</el-button>
            </div>
            <el-table :data="settlementList" border stripe>
              <el-table-column prop="orderId" label="订单号" width="120" align="center"></el-table-column>
              <el-table-column prop="bookName" label="教材名称"></el-table-column>
              <el-table-column prop="amount" label="交易金额" width="120" align="right">
                <template #default="scope">¥{{ scope.row.amount }}</template>
              </el-table-column>
              <el-table-column prop="received" label="到账金额" width="120" align="right">
                <template #default="scope"><span class="text-success">¥{{ scope.row.received }}</span></template>
              </el-table-column>
              <el-table-column prop="time" label="结算时间" width="180" align="center"></el-table-column>
              <el-table-column prop="voucher" label="凭证号" width="220" show-overflow-tooltip></el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane name="receivedReviews">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><ChatLineSquare /></el-icon>
              <span>评价管理</span>
            </span>
          </template>
          <div class="tab-content">
            <el-table :data="receivedReviews" border stripe>
              <el-table-column prop="buyerName" label="买家" width="120"></el-table-column>
              <el-table-column label="评分" width="140">
                <template #default="scope">
                  <div class="rating-box">
                    <span>成色 {{ scope.row.scoreCondition }}</span>
                    <span>服务 {{ scope.row.scoreService }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="comment" label="评论内容"></el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane name="receivedComplaints">
          <template #label>
            <span class="custom-tab-label">
              <el-icon><Warning /></el-icon>
              <span>投诉记录</span>
            </span>
          </template>
          <div class="tab-content">
            <el-table :data="receivedComplaints" border stripe>
              <el-table-column prop="orderId" label="订单ID" width="100" align="center"></el-table-column>
              <el-table-column prop="type" label="类型" width="140"></el-table-column>
              <el-table-column prop="detail" label="详情"></el-table-column>
              <el-table-column prop="status" label="状态" width="120" align="center">
                <template #default="scope">
                   <el-tag :type="scope.row.status === 'resolved' ? 'success' : 'danger'">{{ scope.row.status }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Edit Dialog -->
    <el-dialog v-model="editVisible" title="编辑教材" width="500px" destroy-on-close>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="封面">
          <div class="upload-wrapper">
            <img
              v-if="editForm.coverUrl"
              :src="resolveCoverUrl(editForm.coverUrl)"
              class="preview-img"
            />
            <el-upload
              action="#"
              :show-file-list="false"
              :auto-upload="false"
              :on-change="handleEditCoverChange"
              :disabled="uploadingCover"
              class="cover-uploader"
            >
              <el-button size="small">更换封面</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="editForm.bookName" />
        </el-form-item>
        <el-form-item label="售价">
          <el-input-number v-model="editForm.sellPrice" :min="1" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="成色">
          <el-select v-model="editForm.conditionLevel">
            <el-option label="全新" value="全新" />
            <el-option label="九成新" value="九成新" />
            <el-option label="八成新" value="八成新" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="editForm.stock" :min="1" :max="999" controls-position="right" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="editForm.description" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { listMyBooks, updateBook, offlineBook, deleteBook } from '@/api/sellerApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listReceivedReviews, getGoodRate } from '@/api/reviewApi'
import { listReceivedComplaints } from '@/api/complaintApi'
import { listSellerOrders } from '@/api/orderApi'
import { uploadFile } from '@/api/bookApi'
import { listSettlements, exportSettlements } from '@/api/notificationApi'
import { 
  Goods, Star, Wallet, Reading, Plus, Picture, List, Money, 
  ChatLineSquare, Warning, Download 
} from '@element-plus/icons-vue'

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
const settlementList = ref([])

const totalSettledAmount = computed(() => {
  return settlementList.value.reduce((sum, item) => {
    const val = parseFloat(item.received)
    return sum + (isNaN(val) ? 0 : val)
  }, 0).toFixed(2)
})

/**
 * Function: loadMyBooks
 * Description: Fetches the list of books published by the seller.
 */
const loadMyBooks = async () => {
  try {
    const res = await listMyBooks()
    bookList.value = res || []
  } catch (e) {
    ElMessage.error('加载我的教材失败')
  }
}

/**
 * Function: loadMyOrders
 * Description: Fetches the list of orders for the seller's books.
 */
const loadMyOrders = async () => {
  try {
    const res = await listSellerOrders()
    orderList.value = res || []
  } catch (e) {
    ElMessage.error('加载我的订单失败')
  }
}

/**
 * Function: resolveCoverUrl
 * Description: Resolves the full URL for book covers.
 * Input: url (String)
 * Return: String (Full URL)
 */
const resolveCoverUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/book-api/')) return url
  if (url.startsWith('/files/')) return `/book-api${url}`
  return url
}

/**
 * Function: handleEditCoverChange
 * Description: Handles book cover update during editing.
 * Input: file (Object)
 */
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

/**
 * Function: orderStatusText
 * Description: Returns display text for order status.
 * Input: status (String)
 * Return: String
 */
const orderStatusText = (status) => {
  if (status === 'pending') return '待付款'
  if (status === 'paid') return '已付款'
  if (status === 'received') return '已收货'
  if (status === 'cancelled') return '已取消'
  if (status === 'expired') return '已超时'
  return status || '-'
}

/**
 * Function: orderStatusTagType
 * Description: Returns Element Plus tag type for order status.
 * Input: status (String)
 * Return: String
 */
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
  editForm.value = { 
    id: row.id, 
    bookName: row.bookName, 
    sellPrice: row.sellPrice, 
    conditionLevel: row.conditionLevel, 
    stock: row.stock, 
    coverUrl: row.coverUrl,
    description: row.description 
  }
  editVisible.value = true
}

/**
 * Function: submitEdit
 * Description: Submits book updates.
 */
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

/**
 * Function: offline
 * Description: Takes a book offline.
 * Input: id (Number)
 */
const offline = (id) => {
  ElMessageBox.confirm('确认下架该教材？', '提示', { confirmButtonText: '确认', cancelButtonText: '取消' }).then(() => {
    offlineBook(id).then(() => { ElMessage.success('下架成功'); loadMyBooks() }).catch(() => ElMessage.error('下架失败'))
  })
}

/**
 * Function: remove
 * Description: Deletes a book listing.
 * Input: id (Number)
 */
const remove = (id) => {
  ElMessageBox.confirm('确认删除该教材？', '提示', { confirmButtonText: '确认', cancelButtonText: '取消' }).then(() => {
    deleteBook(id).then(() => { ElMessage.success('删除成功'); loadMyBooks() }).catch(() => ElMessage.error('删除失败'))
  })
}

// 跳转到发布教材页
const toPublish = () => {
  router.push('/publish')
}

const parseField = (text, startToken, endToken) => {
  if (!text || !startToken) return ''
  const s = text.indexOf(startToken)
  if (s < 0) return ''
  const start = s + startToken.length
  if (!endToken) return text.substring(start).trim()
  const e = text.indexOf(endToken, start)
  return (e < 0 ? text.substring(start) : text.substring(start, e)).trim()
}

const loadSettlements = async () => {
  try {
    const res = await listSettlements()
    const list = Array.isArray(res) ? res : []
    settlementList.value = list.map(it => {
      const content = it.content || ''
      const orderId = parseField(content, '订单#', '（')
      const bookName = parseField(content, '（', '）')
      const amount = parseField(content, '交易金额: ', '\n')
      const received = parseField(content, '到账金额: ', '\n')
      const time = parseField(content, '结算时间: ', '\n')
      const voucher = parseField(content, '凭证号: ', null)
      return { id: it.id, orderId, bookName, amount, received, time, voucher, rawCreateTime: it.createTime }
    })
  } catch (e) {
    ElMessage.error('加载结算记录失败')
  }
}

const onExportSettlements = async () => {
  try {
    const text = await exportSettlements()
    const blob = new Blob([text || ''], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'settlement_vouchers.csv'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadMyBooks()
  listReceivedReviews().then(res => { receivedReviews.value = res || [] }).catch(() => {})
  listReceivedComplaints().then(res => { receivedComplaints.value = res || [] }).catch(() => {})
  getGoodRate().then(res => { goodRate.value = res || {} }).catch(() => {})
  loadSettlements().catch(() => {})
})

watch(activeTab, (v) => {
  if (v === 'myBooks') loadMyBooks()
  if (v === 'myOrders') loadMyOrders()
  if (v === 'settlements') loadSettlements()
})
</script>

<style scoped>
.seller-center {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-subtitle {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  margin-right: 16px;
}

.bg-blue { background: linear-gradient(135deg, #409EFF, #79bbff); }
.bg-green { background: linear-gradient(135deg, #67C23A, #95d475); }
.bg-orange { background: linear-gradient(135deg, #E6A23C, #f3d19e); }

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.main-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  overflow: hidden;
}

.dashboard-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
  background: #fcfcfc;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 20px;
}

.dashboard-tabs :deep(.el-tabs__item) {
  height: 50px;
  line-height: 50px;
  font-size: 15px;
}

.custom-tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-content {
  padding: 24px;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.book-info-cell {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 0;
}

.book-thumb {
  width: 50px;
  height: 70px;
  border-radius: 4px;
  background: #f5f7fa;
  flex-shrink: 0;
}

.book-details {
  flex: 1;
  min-width: 0;
}

.book-name {
  font-weight: 500;
  margin-bottom: 4px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.book-meta {
  font-size: 12px;
  color: #909399;
}

.price-text {
  font-weight: bold;
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
  font-weight: 500;
}

.rating-box {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

.upload-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.preview-img {
  width: 80px;
  height: 110px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
</style>
