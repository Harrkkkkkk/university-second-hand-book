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
  <div class="chat-page">
    <page-header title="在线沟通" :goBack="goBack">
    </page-header>
    <el-card>
      <div>与 {{ peer }} 的对话（{{ contextLabel }}）</div>
      <div class="chat-box">
        <div v-for="m in messages" :key="m.id" :class="m.fromUser===username? 'msg me':'msg peer'">
          <div class="meta">{{ m.fromUser }} · {{ formatTime(m.createTime) }}</div>
          <div class="content">
            <template v-if="m.type === 'image'">
              <el-image 
                :src="m.content" 
                :preview-src-list="[m.content]"
                style="max-width: 200px; border-radius: 4px;"
              />
            </template>
            <template v-else>
              {{ m.content }}
            </template>
          </div>
        </div>
      </div>
      <div class="input-bar">
        <el-button :icon="Plus" circle @click="chooseFile" />
        <input type="file" ref="fileInput" style="display:none" accept="image/*" @change="handleFileChange">
        <el-input v-model="text" placeholder="输入消息..." @keyup.enter.native="send"></el-input>
        <el-button type="primary" @click="send">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { sendMessage, getHistory, markRead } from '@/api/chatApi'
import { uploadFile } from '@/api/bookApi'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const username = localStorage.getItem('username')

const goBack = () => {
  router.back()
}
const peer = route.query.peer || ''
const bookId = route.query.bookId ? Number(route.query.bookId) : undefined
const orderId = route.query.orderId ? Number(route.query.orderId) : undefined
const contextLabel = bookId? `教材ID ${bookId}` : (orderId? `订单ID ${orderId}`: '通用')
const logout = () => logoutAndBackToLogin()

const messages = ref([])
const text = ref('')
const fileInput = ref(null)

const chooseFile = () => {
  fileInput.value.click()
}

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
      load()
    }
  } catch (err) {
    ElMessage.error('图片发送失败')
  } finally {
    e.target.value = '' // Reset input
  }
}

/**
 * Function: load
 * Description: Loads chat history and marks messages as read.
 */
const load = async () => {
  try {
    messages.value = await getHistory({ peer, bookId, orderId }) || []
    if (peer) {
      await markRead(peer)
    }
  } catch { ElMessage.error('加载历史失败') }
}

/**
 * Function: send
 * Description: Sends a new message to the peer.
 */
const send = async () => {
  if (!text.value) return
  try {
    await sendMessage({ toUser: peer, content: text.value, bookId, orderId, type: 'text' })
    text.value = ''
    load()
  } catch { ElMessage.error('发送失败') }
}

/**
 * Function: formatTime
 * Description: Formats a timestamp into a readable string.
 */
const formatTime = ts => new Date(ts).toLocaleString()
onMounted(load)
</script>

<style scoped>
.chat-page { max-width: 900px; margin: 0 auto; padding: 20px; }
.chat-box { border:1px solid #eee; min-height: 300px; padding: 10px; margin: 10px 0; background:#fafafa; }
.msg { margin: 8px 0; }
.msg.me { text-align: right; }
.msg .meta { font-size: 12px; color:#999 }
.msg .content { display:inline-block; max-width: 70%; padding:8px 12px; border-radius:6px; background:#fff; }
.msg.me .content { background:#d9f7be; }
.input-bar { display:flex; gap:8px; }
</style>
