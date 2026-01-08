<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: MessageCenter.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: User message center.
 *              Combines chat conversations and system notifications.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
<template>
  <div class="message-center-container">
    <!-- Header Area -->
    <div class="header-section">
      <page-header title="消息中心" :goBack="goBack" />
    </div>

    <div class="main-content">
      <!-- Modern Tabs -->
      <div class="custom-tabs">
        <div 
          class="tab-item" 
          :class="{ active: activeModule === 'threads' }"
          @click="activeModule = 'threads'"
        >
          <el-icon class="tab-icon"><ChatDotRound /></el-icon>
          <span>会话列表</span>
          <div class="active-bar"></div>
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeModule === 'notifications' }"
          @click="activeModule = 'notifications'"
        >
          <el-icon class="tab-icon"><Bell /></el-icon>
          <span>系统通知</span>
          <el-badge :is-dot="hasUnreadNotifications" class="tab-badge" />
          <div class="active-bar"></div>
        </div>
      </div>

      <!-- Threads Module -->
      <div v-if="activeModule === 'threads'" class="module-container fade-slide-in">
        <div class="action-bar">
          <el-input 
            v-model="peer" 
            placeholder="输入用户名发起新会话..." 
            class="search-input"
            @keyup.enter="startChat"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
            <template #append>
              <el-button @click="startChat">
                <el-icon style="margin-right: 4px"><Position /></el-icon> 发起
              </el-button>
            </template>
          </el-input>
        </div>

        <div class="list-container">
          <el-empty v-if="threads.length === 0" description="暂无会话消息" :image-size="120" />
          
          <div 
            v-for="thread in threads" 
            :key="thread.id" 
            class="thread-card"
            @click="openChat(thread)"
          >
            <div class="thread-avatar-wrapper">
              <el-avatar :size="56" class="thread-avatar" :src="getAvatar(thread.peer)">
                {{ thread.peer.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="online-status" v-if="false"></span> <!-- Future: online status -->
            </div>
            
            <div class="thread-content">
              <div class="thread-header">
                <span class="thread-name">{{ thread.peer }}</span>
                <span class="thread-time">{{ formatTime(thread.lastTime) }}</span>
              </div>
              <div class="thread-body">
                <span class="thread-message">{{ thread.lastContent }}</span>
                <el-badge :value="thread.unread" :max="99" v-if="thread.unread > 0" class="unread-badge" />
              </div>
            </div>
            
            <div class="thread-hover-action">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- Notifications Module -->
      <div v-else class="module-container fade-slide-in">
        <div class="action-bar right-aligned">
          <el-button type="primary" plain size="default" round @click="handleMarkAllRead" :disabled="notifications.every(n => n.read)">
            <el-icon style="margin-right: 4px"><Check /></el-icon> 全部已读
          </el-button>
        </div>

        <div class="list-container">
          <el-empty v-if="notifications.length === 0" description="暂无通知消息" :image-size="120" />
          
          <div 
            v-for="note in notifications" 
            :key="note.id" 
            class="notification-card"
            :class="{ 'is-unread': !note.read }"
            @click="!note.read && handleMarkRead(note)"
          >
            <div class="note-icon-wrapper" :class="getNoteTypeClass(note.type)">
              <el-icon v-if="note.type === 'system'"><BellFilled /></el-icon>
              <el-icon v-else-if="note.type === 'order'"><GoodsFilled /></el-icon>
              <el-icon v-else><InfoFilled /></el-icon>
            </div>
            
            <div class="note-content">
              <div class="note-header">
                <span class="note-title">{{ note.title }}</span>
                <span class="note-time">{{ formatTime(note.createTime) }}</span>
              </div>
              <div class="note-text">{{ note.content }}</div>
            </div>
            
            <div class="note-status">
              <div v-if="!note.read" class="unread-dot"></div>
            </div>
          </div>
        </div>

        <!-- Admin Announcement Panel -->
        <div v-if="role==='admin'" class="admin-panel">
          <div class="admin-panel-header">
            <el-icon><DataBoard /></el-icon> 发布全员公告
          </div>
          <div class="admin-panel-body">
            <el-input v-model="annTitle" placeholder="公告标题" class="mb-2" />
            <el-input v-model="annContent" type="textarea" placeholder="公告内容..." :rows="3" class="mb-2" />
            <div class="admin-actions">
              <el-button type="primary" @click="sendAnnounce">
                <el-icon style="margin-right: 4px"><Promotion /></el-icon> 立即发布
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { ElMessage } from 'element-plus'
import { listNotifications, announce, markRead, markAllRead } from '@/api/notificationApi'
import { getConversations } from '@/api/chatApi'
import { 
  ChatDotRound, Bell, Search, Position, ArrowRight, Check, 
  BellFilled, GoodsFilled, InfoFilled, DataBoard, Promotion 
} from '@element-plus/icons-vue'

const router = useRouter()
const goBack = () => router.back()
const logout = () => logoutAndBackToLogin()

const role = localStorage.getItem('role') || ''
const notifications = ref([])
const threads = ref([])
const peer = ref('')
const activeModule = ref('threads') // Default to threads as it's more interactive
const annTitle = ref('平台公告')
const annContent = ref('')

// Computed to show dot on Notification tab
const hasUnreadNotifications = computed(() => {
  return notifications.value.some(n => !n.read)
})

const getAvatar = (name) => {
  // Generate a consistent generic avatar or use a service
  return '' // Fallback to initial
}

const getNoteTypeClass = (type) => {
  if (type === 'system') return 'type-system'
  if (type === 'order') return 'type-order'
  return 'type-info'
}

/**
 * Function: load
 * Description: Loads notifications and conversation threads.
 */
const load = async () => {
  try { notifications.value = await listNotifications() || [] } catch { /* ignore */ }
  try { threads.value = await getConversations() || [] } catch { /* ignore */ }
}

/**
 * Function: handleMarkRead
 * Description: Marks a single notification as read.
 */
const handleMarkRead = async (row) => {
  try {
    await markRead(row.id)
    row.read = true
  } catch (e) {
    console.error(e)
  }
}

/**
 * Function: handleMarkAllRead
 * Description: Marks all notifications as read.
 */
const handleMarkAllRead = async () => {
  try {
    await markAllRead()
    ElMessage.success('全部已读')
    notifications.value.forEach(n => n.read = true)
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

/**
 * Function: formatTime
 * Description: Smart time formatting
 */
const formatTime = (ts) => {
  if (!ts) return ''
  const date = new Date(ts)
  const now = new Date()
  const diff = now - date
  
  // Within 24 hours
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  // Within current year
  if (date.getFullYear() === now.getFullYear()) {
    return date.toLocaleDateString([], { month: '2-digit', day: '2-digit' })
  }
  return date.toLocaleDateString()
}

/**
 * Function: openChat
 * Description: Navigates to the chat page for a specific conversation.
 */
const openChat = (row) => router.push({ path: '/chat', query: { peer: row.peer, orderId: row.orderId, bookId: row.bookId } })

/**
 * Function: startChat
 * Description: Initiates a new chat with a specified user.
 */
const startChat = () => {
  if (!peer.value) return
  router.push({ path: '/chat', query: { peer: peer.value } })
}

/**
 * Function: sendAnnounce
 * Description: Admin only - Publishes a new system announcement.
 */
const sendAnnounce = async () => {
  if (!annTitle.value || !annContent.value) {
    ElMessage.warning('请填写完整公告信息')
    return
  }
  try { 
    await announce({ title: annTitle.value, content: annContent.value })
    ElMessage.success('公告已发布')
    annContent.value = ''
    load() 
  } catch { 
    ElMessage.error('发布失败') 
  }
}

onMounted(load)
</script>

<style scoped>
.message-center-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  min-height: 80vh;
}

.header-section {
  margin-bottom: 20px;
}

/* Custom Tabs */
.custom-tabs {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 6px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  position: relative;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  cursor: pointer;
  font-weight: 600;
  color: #606266;
  position: relative;
  transition: all 0.3s ease;
  border-radius: 8px;
}

.tab-item:hover {
  background-color: #f5f7fa;
  color: var(--el-color-primary);
}

.tab-item.active {
  color: var(--el-color-primary);
  background-color: #ecf5ff;
}

.tab-icon {
  font-size: 1.2rem;
}

.tab-badge {
  position: absolute;
  top: 8px;
  right: 20%;
}

/* Module Containers */
.module-container {
  background: transparent;
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Thread Cards */
.thread-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02);
}

.thread-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: var(--el-color-primary-light-8);
}

.thread-avatar-wrapper {
  position: relative;
  margin-right: 16px;
}

.thread-avatar {
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  font-weight: bold;
  font-size: 1.2rem;
}

.thread-content {
  flex: 1;
  min-width: 0; /* Text truncation fix */
}

.thread-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.thread-name {
  font-weight: 600;
  font-size: 1.05rem;
  color: #303133;
}

.thread-time {
  font-size: 0.85rem;
  color: #909399;
}

.thread-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.thread-message {
  color: #606266;
  font-size: 0.95rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 80%;
}

.thread-hover-action {
  margin-left: 12px;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s ease;
  color: var(--el-color-primary);
}

.thread-card:hover .thread-hover-action {
  opacity: 1;
  transform: translateX(0);
}

/* Notification Cards */
.notification-card {
  display: flex;
  padding: 20px;
  background: white;
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
  position: relative;
  overflow: hidden;
}

.notification-card.is-unread {
  background: #fdfdfd;
  border-left: 4px solid var(--el-color-primary);
}

.notification-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.note-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
  font-size: 1.4rem;
}

.type-system { background: #f0f9eb; color: #67c23a; }
.type-order { background: #ecf5ff; color: #409eff; }
.type-info { background: #f4f4f5; color: #909399; }

.note-content {
  flex: 1;
}

.note-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.note-title {
  font-weight: 600;
  color: #303133;
  font-size: 1rem;
}

.note-time {
  font-size: 0.85rem;
  color: #909399;
}

.note-text {
  color: #606266;
  font-size: 0.95rem;
  line-height: 1.5;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: var(--el-color-danger);
  border-radius: 50%;
  margin-left: 12px;
}

/* Action Bar */
.action-bar {
  margin-bottom: 16px;
}

.action-bar.right-aligned {
  display: flex;
  justify-content: flex-end;
}

.search-input {
  width: 100%;
  max-width: 400px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px 0 0 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.search-input :deep(.el-input-group__append) {
  border-radius: 0 20px 20px 0;
  background-color: var(--el-color-primary);
  color: white;
  border: none;
}

/* Admin Panel */
.admin-panel {
  margin-top: 32px;
  background: white;
  border-radius: 12px;
  border: 1px dashed #dcdfe6;
  padding: 24px;
}

.admin-panel-header {
  font-weight: 600;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.mb-2 { margin-bottom: 12px; }

/* Animations */
.fade-slide-in {
  animation: fadeSlideIn 0.4s ease-out forwards;
}

@keyframes fadeSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .message-center-container { padding: 10px; }
  .thread-card { padding: 12px; }
  .thread-avatar { width: 40px; height: 40px; }
  .note-icon-wrapper { width: 36px; height: 36px; font-size: 1rem; }
}
</style>
