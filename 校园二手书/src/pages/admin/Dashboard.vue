<template>
  <div class="admin-dashboard">
    <page-header title="管理员后台">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户管理" name="userManage">
          <el-table :data="userList" border>
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="username" label="账号"></el-table-column>
            <el-table-column prop="role" label="角色" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.role === 'buyer'">买家</el-tag>
                <el-tag type="warning" v-if="scope.row.role === 'seller'">卖家</el-tag>
                <el-tag type="danger" v-if="scope.row.role === 'admin'">管理员</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="nickname" label="昵称"></el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="primary" size="small">编辑</el-button>
                <el-button type="danger" size="small">禁用</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="教材审核" name="bookAudit">
          <el-table :data="bookAuditList" border>
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="bookName" label="教材名称"></el-table-column>
            <el-table-column prop="sellerName" label="卖家"></el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="success" size="small">通过</el-button>
                <el-button type="danger" size="small">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'

// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

const activeTab = ref('userManage')

// 模拟用户列表
const userList = ref([
  { id: 1, username: 'buyer1', role: 'buyer', nickname: '买家1' },
  { id: 2, username: 'seller1', role: 'seller', nickname: '卖家1' },
  { id: 3, username: 'admin1', role: 'admin', nickname: '管理员1' }
])

// 模拟教材审核列表
const bookAuditList = ref([
  { id: 1, bookName: 'Java编程思想', sellerName: '卖家1' },
  { id: 2, bookName: 'Python入门到精通', sellerName: '卖家1' }
])
</script>

<style scoped>
.admin-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>