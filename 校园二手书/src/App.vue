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
          <!-- 买家功能（对所有人开放，除管理员外） -->
          <el-menu-item v-if="role !== 'admin'" index="1" @click="toBuyerHome">教材选购</el-menu-item>
          <el-menu-item v-if="role !== 'admin'" index="2" @click="toBuyerOrder">我的订单</el-menu-item>
          <el-menu-item v-if="role !== 'admin'" index="3" @click="toBuyerCollect">我的收藏</el-menu-item>
          <el-menu-item v-if="role !== 'admin'" index="4" @click="toBuyerCart">我的购物车</el-menu-item>

          <!-- 卖家专属菜单（仅通过审核的卖家显示） -->
          <el-menu-item v-if="role !== 'admin' && sellerStatus === 'APPROVED'" index="5" @click="toSellerCenter">卖家中心</el-menu-item>
          <el-menu-item v-if="role !== 'admin' && sellerStatus === 'APPROVED'" index="6" @click="toPublish">发布教材</el-menu-item>
          <el-menu-item v-if="role !== 'admin' && (sellerStatus === 'NONE' || sellerStatus === 'REJECTED')" index="7" @click="toSellerApply">申请成为卖家</el-menu-item>
          <el-menu-item v-if="role !== 'admin' && sellerStatus === 'PENDING'" index="7" disabled>卖家资质审核中</el-menu-item>

          <!-- 管理员专属菜单 -->
          <el-menu-item v-if="role === 'admin'" index="1" @click="toAdminDashboard">后台管理</el-menu-item>

          <!-- 三个点下拉菜单（还原为 el-sub-menu，悬停展开显示选项） -->
          <el-menu
              index="99"
              popper-class="user-dropdown-menu"
              :popper-append-to-body="true"
              style="display: inline-block; padding: 0 60px; line-height: 60px;"
          >
            <template #title>
              <div style="display: flex; align-items: center; height: 100%;">
                <el-badge :is-dot="unreadCount > 0" class="item">
                  <i class="el-icon-more" style="font-size: 30px; color: white;"></i>
                </el-badge>
              </div>
            </template>
            <el-menu-item index="99-1" @click="toUserCenter">个人中心</el-menu-item>
            <el-menu-item index="99-3" @click="toMessageCenter">
              消息中心
              <el-badge v-if="unreadCount > 0" :value="unreadCount" class="mark" type="danger" />
            </el-menu-item>
            <el-menu-item index="99-2" @click="handleLogout">退出登录</el-menu-item>
          </el-menu>
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
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { getUnreadCount } from '@/api/notificationApi'
import { getUserInfo } from '@/api/userApi'

// 路由实例
const router = useRouter()
const route = useRoute()

// 登录状态（从 localStorage 读取）
const token = ref(localStorage.getItem('token') || '')
const role = ref(localStorage.getItem('role') || '')
const username = ref(localStorage.getItem('username') || '')
const sellerStatus = ref(localStorage.getItem('sellerStatus') || 'NONE')
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
            localStorage.setItem('role', userInfo.role)
            role.value = userInfo.role
        }
        if (userInfo.sellerStatus && userInfo.sellerStatus !== sellerStatus.value) {
            localStorage.setItem('sellerStatus', userInfo.sellerStatus)
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

// 监听路由变化，同步登录状态
watch(
    () => route.path,
    () => {
      token.value = localStorage.getItem('token') || ''
      role.value = localStorage.getItem('role') || ''
      username.value = localStorage.getItem('username') || ''
      sellerStatus.value = localStorage.getItem('sellerStatus') || 'NONE'
      roleName.value = {
        buyer: '买家',
        seller: '卖家',
        admin: '管理员'
      }[role.value] || '买家'
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
