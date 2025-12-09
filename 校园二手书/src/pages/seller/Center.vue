<template>
  <div class="seller-center">
    <page-header title="卖家中心">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的教材" name="myBooks">
          <el-button type="primary" @click="toPublish">发布新教材</el-button>
          <el-table :data="bookList" border style="margin-top: 20px;">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="bookName" label="教材名称"></el-table-column>
            <el-table-column prop="sellPrice" label="售价" width="100"></el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag type="success" v-if="scope.row.status === 0">上架中</el-tag>
                <el-tag type="warning" v-else>已下架</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="primary" size="small">编辑</el-button>
                <el-button type="warning" size="small">下架</el-button>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="我的订单" name="myOrders">
          <el-table :data="orderList" border>
            <el-table-column prop="id" label="订单ID" width="100"></el-table-column>
            <el-table-column prop="bookName" label="教材名称"></el-table-column>
            <el-table-column prop="buyerName" label="买家"></el-table-column>
            <el-table-column prop="price" label="订单金额" width="100"></el-table-column>
            <el-table-column prop="status" label="状态" width="100"></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'

const router = useRouter()
// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

const activeTab = ref('myBooks')

// 模拟我的教材数据
const bookList = ref([
  { id: 1, bookName: 'Java编程思想', sellPrice: 30, status: 0 },
  { id: 2, bookName: 'Python入门到精通', sellPrice: 25, status: 0 },
  { id: 3, bookName: '数据结构与算法', sellPrice: 20, status: 1 }
])

// 模拟我的订单数据
const orderList = ref([
  { id: 101, bookName: 'Java编程思想', buyerName: '买家1', price: 30, status: '已付款' },
  { id: 102, bookName: 'Python入门到精通', buyerName: '买家2', price: 25, status: '已发货' }
])

// 跳转到发布教材页
const toPublish = () => {
  router.push('/publish')
}
</script>

<style scoped>
.seller-center {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>