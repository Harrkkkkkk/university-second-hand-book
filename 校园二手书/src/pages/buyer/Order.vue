<template>
  <div class="buyer-order">
    <page-header title="买家中心 - 我的订单">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <!-- 订单筛选 -->
    <el-card class="filter-card">
      <el-radio-group v-model="orderStatus" @change="filterOrder">
        <el-radio label="all">全部订单</el-radio>
        <el-radio label="pending">待付款</el-radio>
        <el-radio label="paid">已付款</el-radio>
        <el-radio label="received">已收货</el-radio>
        <el-radio label="cancelled">已取消</el-radio>
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
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag type="warning" v-if="scope.row.status === 'pending'">待付款</el-tag>
          <el-tag type="primary" v-if="scope.row.status === 'paid'">已付款</el-tag>
          <el-tag type="success" v-if="scope.row.status === 'received'">已收货</el-tag>
          <el-tag type="danger" v-if="scope.row.status === 'cancelled'">已取消</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button v-if="scope.row.status === 'pending'" type="primary" size="small" @click="payOrder(scope.row.id)">立即付款</el-button>
          <el-button v-if="scope.row.status === 'pending'" type="danger" size="small" @click="cancelOrder(scope.row.id)">取消订单</el-button>
          <el-button v-if="scope.row.status === 'paid'" type="success" size="small" @click="confirmReceive(scope.row.id)">确认收货</el-button>
          <el-button type="text" size="small" @click="viewOrderDetail(scope.row.id)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'

const router = useRouter()
// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

// 订单状态筛选
const orderStatus = ref('all')

// 模拟订单数据
const orderList = ref([
  { id: 101, bookName: 'Java编程思想', sellerName: '卖家1', price: 30, createTime: '2025-12-01 10:00', status: 'pending' },
  { id: 102, bookName: 'Python入门到精通', sellerName: '卖家1', price: 25, createTime: '2025-12-02 14:30', status: 'paid' },
  { id: 103, bookName: '红楼梦', sellerName: '卖家2', price: 15, createTime: '2025-11-28 09:15', status: 'received' },
  { id: 104, bookName: '数据结构与算法', sellerName: '卖家1', price: 20, createTime: '2025-12-03 16:20', status: 'cancelled' }
])

// 筛选订单
const filterOrder = () => {
  if (orderStatus.value === 'all') {
    // 显示全部
    return
  }
  // 模拟筛选逻辑
  ElMessage.info(`筛选状态：${orderStatus.value}`)
}

// 付款
const payOrder = (id) => {
  ElMessageBox.confirm('确认付款？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(() => {
    const order = orderList.value.find(item => item.id === id)
    if (order) order.status = 'paid'
    ElMessage.success('付款成功！')
  })
}

// 取消订单
const cancelOrder = (id) => {
  ElMessageBox.confirm('确认取消订单？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(() => {
    const order = orderList.value.find(item => item.id === id)
    if (order) order.status = 'cancelled'
    ElMessage.success('订单已取消！')
  })
}

// 确认收货
const confirmReceive = (id) => {
  ElMessageBox.confirm('确认收货？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  }).then(() => {
    const order = orderList.value.find(item => item.id === id)
    if (order) order.status = 'received'
    ElMessage.success('确认收货成功！')
  })
}

// 查看订单详情
const viewOrderDetail = (id) => {
  ElMessage.info(`查看订单ID${id}详情`)
}
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