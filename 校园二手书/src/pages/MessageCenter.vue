<template>
  <div class="message-center">
    <page-header title="消息中心">
      <el-button type="text" @click="logout" style="color:#e64340">退出登录</el-button>
    </page-header>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header><div class="card-header">会话列表</div></template>
          <el-input v-model="peer" placeholder="输入用户名发起会话" @keyup.enter.native="startChat" />
          <el-table :data="threads" border style="margin-top:10px;">
            <el-table-column prop="peer" label="对方" width="160" />
            <el-table-column prop="lastContent" label="最近消息" />
            <el-table-column prop="lastTime" label="时间" width="180">
              <template #default="scope">{{ formatTime(scope.row.lastTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="primary" size="small" @click="openChat(scope.row)">打开</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><div class="card-header">系统通知</div></template>
          <el-table :data="notifications" border>
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="title" label="标题" width="200" />
            <el-table-column prop="content" label="内容" />
            <el-table-column prop="createTime" label="时间" width="180">
              <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
            </el-table-column>
          </el-table>
          <div v-if="role==='admin'" style="margin-top:10px;">
            <el-input v-model="annTitle" placeholder="公告标题" style="margin-bottom:8px;" />
            <el-input v-model="annContent" type="textarea" placeholder="公告内容" rows="3" />
            <el-button type="success" style="margin-top:8px;" @click="sendAnnounce">发布公告</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { ElMessage } from 'element-plus'
import { listMyNotifications, announce } from '@/api/notificationApi'
import { getConversations } from '@/api/chatApi'

const router = useRouter()
const logout = () => logoutAndBackToLogin()

const role = localStorage.getItem('role') || ''
const notifications = ref([])
const threads = ref([])
const peer = ref('')
const annTitle = ref('平台公告')
const annContent = ref('')

const load = async () => {
  try { notifications.value = await listMyNotifications() || [] } catch { /* ignore */ }
  try { threads.value = await getConversations() || [] } catch { /* ignore */ }
}

const formatTime = ts => ts ? new Date(ts).toLocaleString() : ''
const openChat = (row) => router.push({ path: '/chat', query: { peer: row.peer, orderId: row.orderId, bookId: row.bookId } })
const startChat = () => {
  if (!peer.value) return
  router.push({ path: '/chat', query: { peer: peer.value } })
}

const sendAnnounce = async () => {
  try { await announce({ title: annTitle.value, content: annContent.value }); ElMessage.success('公告已发布'); load() } catch { ElMessage.error('发布失败') }
}

onMounted(load)
</script>

<style scoped>
.message-center { max-width: 1200px; margin: 0 auto; padding: 20px; }
.card-header { font-weight: bold; }
</style>
