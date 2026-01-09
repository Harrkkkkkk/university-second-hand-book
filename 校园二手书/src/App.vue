<template>
  <div id="app">
    <!-- Navigation Bar -->
    <el-header v-if="token" class="app-header">
      <div class="header-inner">
        <!-- Logo Area -->
        <div class="logo-area" @click="toBuyerHome">
          <div class="logo-icon-bg">
            <el-icon><Reading /></el-icon>
          </div>
          <span class="logo-text">WiseBookPal</span>
        </div>

        <!-- Main Navigation -->
        <el-menu
            :default-active="activeIndex"
            mode="horizontal"
            class="nav-menu"
            :ellipsis="false"
        >
          <!-- Buyer -->
          <template v-if="role !== 'admin'">
            <el-menu-item index="1" @click="toBuyerHome">教材选购</el-menu-item>
            <el-menu-item index="2" @click="toBuyerOrder">我的订单</el-menu-item>
            <el-menu-item index="3" @click="toBuyerCollect">我的收藏</el-menu-item>
            <el-menu-item index="4" @click="toBuyerCart">购物车</el-menu-item>
          </template>

          <!-- Seller -->
          <template v-if="role !== 'admin'">
             <el-sub-menu index="seller-menu">
               <template #title>卖家中心</template>
               <el-menu-item v-if="sellerStatus === 'APPROVED'" index="5" @click="toSellerCenter">数据看板</el-menu-item>
               <el-menu-item v-if="sellerStatus === 'APPROVED'" index="6" @click="toPublish">发布教材</el-menu-item>
               <el-menu-item v-if="sellerStatus === 'NONE' || sellerStatus === 'REJECTED'" index="7" @click="toSellerApply">申请成为卖家</el-menu-item>
               <el-menu-item v-if="sellerStatus === 'PENDING'" index="7" disabled>审核中</el-menu-item>
             </el-sub-menu>
          </template>

          <!-- Admin -->
          <el-menu-item v-if="role === 'admin'" index="admin" @click="toAdminDashboard">后台管理</el-menu-item>
        </el-menu>

        <!-- Right User Actions -->
        <div class="user-actions">
           <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="user-profile">
                 <el-badge :is-dot="unreadCount > 0" class="avatar-badge">
                   <el-avatar :size="36" class="user-avatar" :style="{background: role === 'admin' ? '#F56C6C' : '#409EFF'}">
                     {{ username ? username.charAt(0).toUpperCase() : 'U' }}
                   </el-avatar>
                 </el-badge>
                 <span class="username">{{ username }}</span>
                 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="message">
                    消息中心
                    <el-badge v-if="unreadCount > 0" :value="unreadCount" type="danger" />
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout" style="color: #F56C6C;">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
           </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- Main Content -->
    <el-main>
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <!-- Footer -->
    <page-footer v-if="token" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { getUnreadCount } from '@/api/notificationApi'
import { getUserInfo } from '@/api/userApi'
import { ArrowDown, Reading } from '@element-plus/icons-vue'
import PageFooter from '@/components/PageFooter.vue'

// 路由实例
const router = useRouter()
const route = useRoute()

// 登录状态（从 sessionStorage 读取）
const token = ref(sessionStorage.getItem('token') || '')
const role = ref(sessionStorage.getItem('role') || '')
const username = ref(sessionStorage.getItem('username') || '')
const sellerStatus = ref(sessionStorage.getItem('sellerStatus') || 'NONE')
const activeIndex = ref('1')
const unreadCount = ref(0)
let timer = null

// 角色名称映射
const roleName = ref({
  buyer: '买家',
  seller: '卖家',
  admin: '管理员'
}[role.value] || '买家')

// 更新未读消息数和用户状态
const updateStatus = async () => {
  if (token.value) {
    try {
      // 1. 获取未读消息
      unreadCount.value = await getUnreadCount()
      
      // 2. 同步用户最新状态（角色、卖家审核状态）
      const userInfo = await getUserInfo()
      if (userInfo) {
        if (userInfo.role && userInfo.role !== role.value) {
            sessionStorage.setItem('role', userInfo.role)
            role.value = userInfo.role
        }
        if (userInfo.sellerStatus && userInfo.sellerStatus !== sellerStatus.value) {
            sessionStorage.setItem('sellerStatus', userInfo.sellerStatus)
            sellerStatus.value = userInfo.sellerStatus
        }
        // 更新显示名称
        roleName.value = {
            buyer: '买家',
            seller: '卖家',
            admin: '管理员'
        }[role.value] || '买家'
      }
    } catch (e) {
      console.error('状态同步失败', e)
    }
  }
}

const toSellerApply = () => {
  router.push('/seller/apply')
}

// 监听路由变化，同步登录状态和导航栏高亮
watch(
    () => route.path,
    (path) => {
      token.value = sessionStorage.getItem('token') || ''
      role.value = sessionStorage.getItem('role') || ''
      username.value = sessionStorage.getItem('username') || ''
      sellerStatus.value = sessionStorage.getItem('sellerStatus') || 'NONE'
      roleName.value = {
        buyer: '买家',
        seller: '卖家',
        admin: '管理员'
      }[role.value] || '买家'
      
      // 更新导航栏高亮
      if (path.includes('/buyer/home') || path.includes('/seller/detail')) activeIndex.value = '1'
      else if (path.includes('/buyer/order')) activeIndex.value = '2'
      else if (path.includes('/buyer/collect')) activeIndex.value = '3'
      else if (path.includes('/buyer/cart')) activeIndex.value = '4'
      else if (path.includes('/seller/center')) activeIndex.value = '5'
      else if (path.includes('/publish')) activeIndex.value = '6'
      else if (path.includes('/seller/apply')) activeIndex.value = '7'
      else if (path.includes('/admin')) activeIndex.value = 'admin'
      
      updateStatus()
    },
    { immediate: true }
  )

onMounted(() => {
  updateStatus()
  timer = setInterval(updateStatus, 10000) // 每10秒轮询一次
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

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
  activeIndex.value = '5'
}
const toPublish = () => {
  router.push('/publish')
  activeIndex.value = '6'
}
const toAdminDashboard = () => {
  router.push('/admin/dashboard')
  activeIndex.value = 'admin'
}

const handleUserCommand = (command) => {
  if (command === 'profile') {
    toUserCenter()
  } else if (command === 'message') {
    router.push('/messages')
  } else if (command === 'logout') {
    handleLogout()
  }
}

// 个人中心
const toUserCenter = () => {
  if (role.value === 'buyer' || role.value === 'seller') {
    router.push('/buyer/profile')
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
/* 全局重置 */
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* Header Styles */
.app-header {
  background-color: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0;
  height: 64px;
}

.header-inner {
  max-width: 1440px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

/* Logo */
.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  margin-right: 40px;
  user-select: none;
}

.logo-icon-bg {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
  letter-spacing: 1px;
}

/* Menu */
.nav-menu {
  flex: 1;
  border-bottom: none !important;
  background: transparent;
}

:deep(.el-menu-item) {
  font-size: 15px;
  font-weight: 500;
  color: #606266;
}

:deep(.el-menu-item.is-active) {
  color: #409EFF;
  font-weight: 600;
  border-bottom-width: 3px;
}

:deep(.el-sub-menu__title) {
  font-size: 15px;
  font-weight: 500;
}

/* User Actions */
.user-actions {
  margin-left: 20px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background-color 0.3s;
}

.user-profile:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.username {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.el-main {
  padding: 0; /* Remove default padding to allow hero to span full width */
}
</style>
