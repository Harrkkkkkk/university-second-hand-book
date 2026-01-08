<template>
  <div class="buyer-order">
    <div class="page-header-card">
       <h2 class="page-title">我的订单</h2>
       <p class="page-subtitle">查看和管理您的所有交易订单</p>
    </div>

    <div class="order-container">
      <!-- 订单筛选 -->
      <el-card class="filter-card" shadow="hover">
        <el-radio-group v-model="orderStatus" size="large" class="status-radio-group">
          <el-radio-button label="all">全部订单</el-radio-button>
          <el-radio-button label="pending">待付款</el-radio-button>
          <el-radio-button label="paid">已付款</el-radio-button>
          <el-radio-button label="received">已收货</el-radio-button>
          <el-radio-button label="cancelled">已取消</el-radio-button>
        </el-radio-group>
      </el-card>

      <!-- 订单列表 -->
      <el-card class="list-card" shadow="hover">
        <el-table :data="orderList" style="width: 100%" :header-cell-style="{background:'#f5f7fa', color:'#606266'}">
          <el-table-column prop="id" label="订单号" width="100" />
          <el-table-column label="教材信息" min-width="200">
             <template #default="scope">
               <div class="book-info">
                 <span class="book-name">{{ scope.row.bookName }}</span>
                 <span class="seller-name">卖家: {{ scope.row.sellerName }}</span>
               </div>
             </template>
          </el-table-column>
          <el-table-column prop="price" label="金额" width="120">
            <template #default="scope">
               <span class="price-text">¥{{ scope.row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="下单时间" width="180" />
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag effect="light" type="warning" v-if="scope.row.status === 'pending'">待付款</el-tag>
              <el-tag effect="light" type="primary" v-if="scope.row.status === 'paid'">已付款</el-tag>
              <el-tag effect="light" type="success" v-if="scope.row.status === 'received'">已收货</el-tag>
              <el-tag effect="light" type="danger" v-if="scope.row.status === 'cancelled'">已取消</el-tag>
              <el-tag effect="light" type="info" v-if="scope.row.status === 'expired'">已超时</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <div class="action-column">
                <template v-if="scope.row.status === 'pending'">
                   <span class="timer-text" v-if="getRemainingText(scope.row) !== '--:--'">
                     <el-icon><Timer /></el-icon> {{ getRemainingText(scope.row) }}
                   </span>
                   <el-button type="primary" size="small" round @click="payOrder(scope.row.id)">付款</el-button>
                   <el-button type="danger" size="small" link @click="cancelOrder(scope.row.id)">取消</el-button>
                </template>
                
                <el-button v-if="scope.row.status === 'paid'" type="success" size="small" round @click="confirmReceive(scope.row.id)">确认收货</el-button>
                <el-button v-if="scope.row.status === 'received'" type="warning" size="small" plain @click="toEvaluate(scope.row.id)">去评价</el-button>
                <el-button v-if="scope.row.status === 'paid'" type="info" size="small" link @click="toComplaint(scope.row.id)">投诉</el-button>
                <el-button type="primary" link size="small" @click="viewOrderDetail(scope.row.id)">详情</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="empty-placeholder" v-if="orderList.length === 0">
           <el-empty description="暂无相关订单" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { listOrders, payOrder as apiPayOrder, cancelOrder as apiCancelOrder, receiveOrder as apiReceiveOrder } from '@/api/orderApi'
import { Timer } from '@element-plus/icons-vue'

const router = useRouter()
// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

// 订单状态筛选
const orderStatus = ref('all')

const allOrders = ref([])
const nowMs = ref(Date.now())
let tickTimer = null
let lastReloadAt = 0

const orderList = computed(() => {
  if (orderStatus.value === 'all') {
    return allOrders.value
  }
  return allOrders.value.filter(order => order.status === orderStatus.value)
})

/**
 * Function: loadOrders
 * Description: Fetches all orders for the current user.
 */
const loadOrders = async () => {
  try {
    const res = await listOrders()
    allOrders.value = res || []
  } catch (e) {
    ElMessage.error('加载订单失败')
  }
}

const pad2 = (n) => (n < 10 ? `0${n}` : `${n}`)
const formatMs = (ms) => {
  const safe = Math.max(0, Math.floor(ms / 1000))
  const m = Math.floor(safe / 60)
  const s = safe % 60
  return `${pad2(m)}:${pad2(s)}`
}

/**
 * Function: getRemainingText
 * Description: Calculates and formats remaining time for pending orders.
 * Input: order (Object)
 * Return: String (mm:ss)
 */
const getRemainingText = (order) => {
  const expireAt = Number(order?.expireAt || 0)
  if (!expireAt) return '--:--'
  const left = expireAt - nowMs.value
  return formatMs(left)
}

// 付款
/**
 * Function: payOrder
 * Description: Initiates payment for a pending order.
 * Input: id (Number)
 */
const payOrder = (id) => {
  ElMessageBox.confirm('确认付款？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(() => {
    apiPayOrder(id).then((res) => {
      const order = allOrders.value.find(item => item.id === id)
      if (order) order.status = res.status
      ElMessage.success('付款成功！')
    }).catch(() => {
      ElMessage.error('付款失败')
    })
  })
}

// 取消订单
/**
 * Function: cancelOrder
 * Description: Cancels a pending order.
 * Input: id (Number)
 */
const cancelOrder = (id) => {
  ElMessageBox.confirm('确认取消订单？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(() => {
    apiCancelOrder(id).then((res) => {
      const order = allOrders.value.find(item => item.id === id)
      if (order) order.status = res.status
      ElMessage.success('订单已取消！')
    }).catch(() => {
      ElMessage.error('取消失败')
    })
  })
}

// 确认收货
/**
 * Function: confirmReceive
 * Description: Marks an order as received.
 * Input: id (Number)
 */
const confirmReceive = (id) => {
  ElMessageBox.confirm('确认收货？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(() => {
    apiReceiveOrder(id).then((res) => {
      const order = allOrders.value.find(item => item.id === id)
      if (order) order.status = res.status
      ElMessage.success('确认收货成功！')
    }).catch(() => {
      ElMessage.error('确认收货失败')
    })
  })
}

// 查看订单详情
const viewOrderDetail = (id) => {
  ElMessage.info(`查看订单ID${id}详情`)
}

const toEvaluate = (id) => router.push({ path: '/buyer/evaluate', query: { orderId: id } })
const toComplaint = (id) => router.push({ path: '/buyer/complaint', query: { orderId: id } })

onMounted(() => {
  loadOrders()
  tickTimer = setInterval(() => {
    nowMs.value = Date.now()
    const shouldReload = allOrders.value.some(o => o && o.status === 'pending' && Number(o.expireAt || 0) > 0 && nowMs.value > Number(o.expireAt))
    if (shouldReload && nowMs.value - lastReloadAt > 10000) {
      lastReloadAt = nowMs.value
      loadOrders()
    }
  }, 1000)
})

onBeforeUnmount(() => {
  if (tickTimer) clearInterval(tickTimer)
})
</script>

<style scoped>
.buyer-order {
  min-height: calc(100vh - 64px);
  background-color: #f5f7fa;
  padding: 20px;
}

.page-header-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 30px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.page-subtitle {
  margin: 8px 0 0;
  opacity: 0.9;
  font-size: 14px;
}

.order-container {
  max-width: 1200px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.list-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.status-radio-group :deep(.el-radio-button__inner) {
  border: none;
  background: #f5f7fa;
  margin-right: 10px;
  border-radius: 8px;
  padding: 10px 20px;
}

.status-radio-group :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #409EFF;
  color: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.book-info {
  display: flex;
  flex-direction: column;
}

.book-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.seller-name {
  font-size: 12px;
  color: #909399;
}

.price-text {
  font-weight: 700;
  color: #F56C6C;
}

.action-column {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.timer-text {
  color: #E6A23C;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-right: 8px;
  background: #fdf6ec;
  padding: 2px 6px;
  border-radius: 4px;
}
</style>
