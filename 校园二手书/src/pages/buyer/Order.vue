<template>
  <div class="buyer-order">
    <page-header title="买家中心 - 我的订单">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <!-- 订单筛选 -->
    <el-card class="filter-card">
      <el-radio-group v-model="orderStatus">
        <el-radio label="all">全部订单</el-radio>
        <el-radio label="pending">待付款</el-radio>
        <el-radio label="paid">已付款</el-radio>
        <el-radio label="received">已收货</el-radio>
        <el-radio label="cancelled">已取消</el-radio>
        <el-radio label="expired">已超时</el-radio>
      </el-radio-group>
    </el-card>

    <!-- 订单列表 -->
    <el-table :data="orderList" border style="margin-top: 20px;">
      <el-table-column prop="id" label="订单ID" width="100"></el-table-column>
      <el-table-column prop="bookName" label="教材名称"></el-table-column>
      <el-table-column prop="sellerName" label="卖家"></el-table-column>
      <el-table-column prop="price" label="订单金额" width="100">
        <template #default="scope">¥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="180"></el-table-column>
      <el-table-column label="支付倒计时" width="130">
        <template #default="scope">
          <span v-if="scope.row.status === 'pending'">{{ getRemainingText(scope.row) }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag type="warning" v-if="scope.row.status === 'pending'">待付款</el-tag>
          <el-tag type="primary" v-if="scope.row.status === 'paid'">已付款</el-tag>
          <el-tag type="success" v-if="scope.row.status === 'received'">已收货</el-tag>
          <el-tag type="danger" v-if="scope.row.status === 'cancelled'">已取消</el-tag>
          <el-tag type="info" v-if="scope.row.status === 'expired'">已超时</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="340">
        <template #default="scope">
          <el-button v-if="scope.row.status === 'pending'" type="primary" size="small" @click="payOrder(scope.row.id)">立即付款</el-button>
          <el-button v-if="scope.row.status === 'pending'" type="danger" size="small" @click="cancelOrder(scope.row.id)">取消订单</el-button>
          <el-button v-if="scope.row.status === 'paid'" type="success" size="small" @click="confirmReceive(scope.row.id)">确认收货</el-button>
          <el-button v-if="scope.row.status === 'received'" type="warning" size="small" @click="toEvaluate(scope.row.id)">去评价</el-button>
          <el-button v-if="scope.row.status === 'paid'" type="info" size="small" @click="toComplaint(scope.row.id)">发起投诉</el-button>
          <el-button type="text" size="small" @click="viewOrderDetail(scope.row.id)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { listOrders, payOrder as apiPayOrder, cancelOrder as apiCancelOrder, receiveOrder as apiReceiveOrder } from '@/api/orderApi'

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

const getRemainingText = (order) => {
  const expireAt = Number(order?.expireAt || 0)
  if (!expireAt) return '--:--'
  const left = expireAt - nowMs.value
  return formatMs(left)
}

// 付款
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
  tickTimer = null
})
</script>

<style scoped>
.buyer-order {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.filter-card {
  margin-bottom: 20px;
}
</style>
