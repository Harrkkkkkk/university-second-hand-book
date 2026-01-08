<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Chat.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Real-time chat page for users.
 *              Supports sending messages, viewing history, and context-aware chats (book/order).
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
 * 2. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Added image upload and display support.
-->
<template>
  <div class="chat-page-container">
    <div class="chat-main-card">
      <!-- Header -->
      <div class="chat-header">
        <div class="header-left">
          <el-button circle plain :icon="ArrowLeft" @click="goBack" class="back-btn" />
          <div class="peer-info">
            <el-avatar :size="40" class="peer-avatar">{{ peer.charAt(0).toUpperCase() }}</el-avatar>
            <div class="peer-details">
              <span class="peer-name">{{ peer }}</span>
              <span class="context-label">{{ contextLabel }}</span>
            </div>
          </div>
        </div>
        <div class="header-right">
           <el-dropdown trigger="click">
             <el-button circle plain :icon="MoreFilled" />
             <template #dropdown>
               <el-dropdown-menu>
                 <el-dropdown-item>查看资料</el-dropdown-item>
                 <el-dropdown-item divided style="color: var(--el-color-danger)">屏蔽用户</el-dropdown-item>
               </el-dropdown-menu>
             </template>
           </el-dropdown>
        </div>
      </div>

      <!-- Messages Area -->
      <div class="chat-messages" ref="scrollContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <span class="empty-text">暂无消息，打个招呼吧 👋</span>
        </div>

        <div 
          v-for="(m, index) in messages" 
          :key="m.id" 
          class="message-row"
          :class="m.fromUser === username ? 'row-me' : 'row-peer'"
        >
          <!-- Time separator (show if diff > 5 mins) -->
          <div v-if="shouldShowTime(index)" class="time-separator">
            {{ formatTime(m.createTime) }}
          </div>

          <div class="message-content-wrapper">
            <el-avatar 
              :size="36" 
              class="msg-avatar" 
              v-if="m.fromUser !== username"
            >
              {{ m.fromUser.charAt(0).toUpperCase() }}
            </el-avatar>

            <div class="bubble-wrapper">
              <div class="bubble">
                <template v-if="m.type === 'image'">
                  <el-image 
                    :src="m.content" 
                    :preview-src-list="[m.content]"
                    class="chat-image"
                    fit="cover"
                  >
                    <template #placeholder>
                      <div class="image-slot">Loading...</div>
                    </template>
                  </el-image>
                </template>
                <template v-else>
                  {{ m.content }}
                </template>
              </div>
              <div class="msg-status" v-if="m.fromUser === username">
                <!-- Can add read status here later -->
              </div>
            </div>

            <el-avatar 
              :size="36" 
              class="msg-avatar" 
              v-if="m.fromUser === username"
              style="background: var(--el-color-primary-light-5)"
            >
              {{ username.charAt(0).toUpperCase() }}
            </el-avatar>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="chat-input-area">
        <div class="toolbar">
          <el-tooltip content="发送图片" placement="top">
            <el-button circle plain :icon="Picture" @click="chooseFile" size="small" />
          </el-tooltip>
          <!-- Hidden File Input -->
          <input type="file" ref="fileInput" style="display:none" accept="image/*" @change="handleFileChange">
        </div>
        
        <div class="input-wrapper">
          <el-input
            v-model="text"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 4 }"
            placeholder="输入消息..."
            class="chat-textarea"
            @keydown.enter.prevent="handleEnter"
            resize="none"
          />
          <el-button type="primary" class="send-btn" @click="send" :disabled="!text.trim()">
            发送 <el-icon class="el-icon--right"><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { sendMessage, getHistory, markRead } from '@/api/chatApi'
import { uploadFile } from '@/api/bookApi'
import { ElMessage } from 'element-plus'
import { Plus, Picture, Promotion, ArrowLeft, MoreFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const username = localStorage.getItem('username') || '我'

const goBack = () => router.back()

const peer = route.query.peer || '用户'
const bookId = route.query.bookId ? Number(route.query.bookId) : undefined
const orderId = route.query.orderId ? Number(route.query.orderId) : undefined
const contextLabel = bookId ? `教材ID ${bookId}` : (orderId ? `订单ID ${orderId}` : '在线沟通')

const messages = ref([])
const text = ref('')
const fileInput = ref(null)
const scrollContainer = ref(null)

const chooseFile = () => fileInput.value.click()

const handleFileChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  try {
    const res = await uploadFile(file)
    if (res && res.url) {
      await sendMessage({ 
        toUser: peer, 
        content: res.url, 
        bookId, 
        orderId, 
        type: 'image' 
      })
      await load()
    }
  } catch (err) {
    ElMessage.error('图片发送失败')
  } finally {
    e.target.value = '' 
  }
}

const load = async () => {
  try {
    const res = await getHistory({ peer, bookId, orderId })
    messages.value = res || []
    if (peer) {
      await markRead(peer)
    }
    scrollToBottom()
  } catch { 
    // Silent fail or minimal notify
  }
}

const send = async () => {
  if (!text.value.trim()) return
  try {
    await sendMessage({ toUser: peer, content: text.value, bookId, orderId, type: 'text' })
    text.value = ''
    await load()
  } catch { 
    ElMessage.error('发送失败') 
  }
}

const handleEnter = (e) => {
  if (!e.shiftKey) {
    send()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (scrollContainer.value) {
      scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
    }
  })
}

// Helper to check if time gap is large enough to show timestamp
const shouldShowTime = (index) => {
  if (index === 0) return true
  const current = messages.value[index]
  const prev = messages.value[index - 1]
  return (current.createTime - prev.createTime) > 5 * 60 * 1000 // 5 mins
}

const formatTime = (ts) => {
  const date = new Date(ts)
  const now = new Date()
  
  if (date.getDate() === now.getDate()) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleString([], { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

// Auto poll or refresh could go here, for now just load once
onMounted(load)
</script>

<style scoped>
.chat-page-container {
  display: flex;
  justify-content: center;
  padding: 20px;
  height: calc(100vh - 40px);
  background-color: #f5f7fa;
  box-sizing: border-box;
}

.chat-main-card {
  width: 100%;
  max-width: 800px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Header */
.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.peer-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.peer-avatar {
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  font-weight: bold;
}

.peer-details {
  display: flex;
  flex-direction: column;
}

.peer-name {
  font-weight: 600;
  font-size: 1rem;
  color: #303133;
}

.context-label {
  font-size: 0.75rem;
  color: #909399;
}

/* Messages */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f9fafc;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-state {
  text-align: center;
  margin-top: 40px;
  color: #c0c4cc;
}

.time-separator {
  text-align: center;
  font-size: 0.75rem;
  color: #c0c4cc;
  margin: 10px 0;
}

.message-row {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.message-content-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  max-width: 80%;
}

.row-me {
  align-items: flex-end;
}

.row-me .message-content-wrapper {
  flex-direction: row;
  justify-content: flex-end;
}

.row-peer {
  align-items: flex-start;
}

.msg-avatar {
  flex-shrink: 0;
}

.bubble {
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 0.95rem;
  line-height: 1.5;
  word-break: break-word;
  position: relative;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.row-me .bubble {
  background: var(--el-color-primary);
  color: white;
  border-bottom-right-radius: 2px;
}

.row-peer .bubble {
  background: white;
  color: #303133;
  border: 1px solid #ebeef5;
  border-bottom-left-radius: 2px;
}

.chat-image {
  max-width: 200px;
  border-radius: 8px;
  display: block;
}

/* Input Area */
.chat-input-area {
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.toolbar {
  margin-bottom: 8px;
  display: flex;
  gap: 8px;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 10px;
  background-color: #f5f7fa;
  border: none;
  box-shadow: none;
}

.chat-textarea :deep(.el-textarea__inner):focus {
  background-color: white;
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

.send-btn {
  border-radius: 12px;
  height: 40px;
  padding: 0 20px;
}

/* Scrollbar */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}
.chat-messages::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}
.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

@media (max-width: 768px) {
  .chat-page-container {
    padding: 0;
    height: 100vh;
  }
  .chat-main-card {
    border-radius: 0;
    max-width: 100%;
  }
  .message-content-wrapper {
    max-width: 90%;
  }
}
</style>
