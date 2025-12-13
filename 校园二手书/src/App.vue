<template>
  <div id="app">
    <!-- 导航栏：仅登录后显示 -->
    <el-header v-if="token" style="text-align: center; font-size: 20px; background-color: #409eff; color: white;">
      <div class="header-content">
        <div>
          <span class="role-tag">当前角色：{{ roleName }}</span>
          <span class="username-tag">欢迎：{{ username }}</span>
        </div>
        <el-menu
            :default-active="activeIndex"
            mode="horizontal"
            background-color="#409eff"
            text-color="white"
            active-text-color="#ffd04b"
            border="false"
        >
          <!-- 买家专属菜单 -->
          <el-menu-item v-if="role === 'buyer'" index="1" @click="toBuyerHome">教材选购</el-menu-item>
          <el-menu-item v-if="role === 'buyer'" index="2" @click="toBuyerOrder">我的订单</el-menu-item>
          <el-menu-item v-if="role === 'buyer'" index="3" @click="toBuyerCollect">我的收藏</el-menu-item>
          <el-menu-item v-if="role === 'buyer'" index="4" @click="toBuyerCart">我的购物车</el-menu-item>

          <!-- 卖家专属菜单 -->
          <el-menu-item v-if="role === 'seller'" index="1" @click="toSellerCenter">卖家中心</el-menu-item>
          <el-menu-item v-if="role === 'seller'" index="2" @click="toPublish">发布教材</el-menu-item>

          <!-- 管理员专属菜单 -->
          <el-menu-item v-if="role === 'admin'" index="1" @click="toAdminDashboard">后台管理</el-menu-item>

          <!-- 三个点下拉菜单 -->
          <el-sub-menu
              index="99"
              popper-class="user-dropdown-menu"
              :popper-append-to-body="true"
              trigger="click"
              style="display: inline-block; padding: 0 15px; line-height: 60px;"
          >
            <template #title>
              <i class="el-icon-more" style="font-size: 20px; color: white;"></i>
            </template>
            <el-menu-item index="99-1" @click="toUserCenter">个人中心</el-menu-item>
            <el-menu-item index="99-3" @click="toMessageCenter">消息中心</el-menu-item>
            <el-menu-item index="99-2" @click="handleLogout">退出登录</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
    </el-header>

    <!-- 页面内容区域 -->
    <el-main>
      <router-view></router-view>
    </el-main>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logoutAndBackToLogin } from '@/utils/auth.js'

// 路由实例
const router = useRouter()
const route = useRoute()

// 登录状态（从 localStorage 读取）
const token = ref(localStorage.getItem('token') || '')
const role = ref(localStorage.getItem('role') || '')
const username = ref(localStorage.getItem('username') || '')
const activeIndex = ref('1')

// 角色名称映射
const roleName = ref({
  buyer: '买家',
  seller: '卖家',
  admin: '管理员'
}[role.value] || '买家')

// 监听路由变化，同步登录状态
watch(
    () => route.path,
    () => {
      token.value = localStorage.getItem('token') || ''
      role.value = localStorage.getItem('role') || ''
      username.value = localStorage.getItem('username') || ''
      roleName.value = {
        buyer: '买家',
        seller: '卖家',
        admin: '管理员'
      }[role.value] || '买家'
    },
    { immediate: true }
)

// 路由跳转方法
const toBuyerHome = () => {
  router.push('/buyer/home')
  activeIndex.value = '1'
}
const toBuyerOrder = () => {
  router.push('/buyer/order')
  activeIndex.value = '2'
}
const toBuyerCollect = () => {
  router.push('/buyer/collect')
  activeIndex.value = '3'
}
const toBuyerCart = () => {
  router.push('/buyer/cart')
  activeIndex.value = '4'
}
const toSellerCenter = () => {
  router.push('/seller/center')
  activeIndex.value = '1'
}
const toPublish = () => {
  router.push('/publish')
  activeIndex.value = '2'
}
const toAdminDashboard = () => {
  router.push('/admin/dashboard')
  activeIndex.value = '1'
}

const toMessageCenter = () => {
  router.push('/messages')
}

// 个人中心
const toUserCenter = () => {
  if (role.value === 'buyer') {
    router.push('/buyer/profile')
  } else if (role.value === 'seller') {
    ElMessage.info('暂未开放卖家个人中心！')
  } else if (role.value === 'admin') {
    ElMessage.info('暂未开放管理员个人中心！')
  }
}

// 退出登录
const handleLogout = () => {
  logoutAndBackToLogin()
}
</script>

<style scoped>
/* 全局样式 */
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background-color: white;
}

/* 导航栏容器 */
:deep(.el-header) {
  padding: 0 !important;
  margin: 0 !important;
  border: none !important;
  box-shadow: none !important;
  background-color: #409eff !important;
}

/* 头部内容布局 */
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

/* 用户信息标签 */
.role-tag, .username-tag {
  font-size: 14px;
  background-color: #fff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: 10px;
}

/* 隐藏下拉箭头 */
:deep(.el-sub-menu__icon-arrow) {
  display: none !important;
}

/* 🔥 关键：下拉面板样式 —— 白色背景，无蓝色！ */
:deep(.user-dropdown-menu) {
  min-width: 120px !important;
  max-width: 150px !important;
  width: 150px !important;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  border: 1px solid #ebeef5 !important;
  background-color: white !important; /* 不再是蓝色！ */
  padding: 4px 0 !important;
  margin: 4px 0 0 !important;
  z-index: 9999 !important;
}

/* 下拉菜单项 */
:deep(.user-dropdown-menu .el-menu-item) {
  padding: 0 20px !important;
  height: 36px !important;
  line-height: 36px !important;
  border: none !important;
  color: #333 !important; /* 深色文字，清晰可读 */
  background-color: transparent !important;
  font-size: 14px;
}

:deep(.user-dropdown-menu .el-menu-item:hover) {
  background-color: #f5f7fa !important; /* 悬停浅灰 */
}

/* 水平导航菜单通用样式 */
:deep(.el-menu) {
  border: none !important;
  background-color: transparent !important;
}

:deep(.el-menu--horizontal) {
  border-bottom: none !important;
}

:deep(.el-menu-item) {
  border: none !important;
  background-color: transparent !important;
  --el-menu-item-hover-bg-color: transparent !important;
}

:deep(.el-menu-item.is-active) {
  border-bottom: 2px solid #ffd04b !important;
}

/* 三个点图标 */
:deep(.el-icon-more) {
  display: inline-block !important;
  font-size: 20px;
  color: white;
}

/* 主内容区 */
.el-main {
  padding: 20px;
  background-color: white;
}
</style>
