<template>
  <div class="admin-dashboard">
    <page-header title="管理员后台">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户管理" name="userManage">
          <!-- 搜索栏 -->
          <div style="margin-bottom: 20px; display: flex; gap: 10px;">
            <el-input v-model="userSearchKeyword" placeholder="请输入用户ID/手机号/学号" style="width: 300px;" clearable @clear="loadUsers" @keyup.enter="loadUsers" />
            <el-button type="primary" @click="loadUsers">搜索</el-button>
            <el-button @click="showOperationLogs">查看操作日志</el-button>
          </div>

          <el-table :data="userList" border v-loading="loadingUsers">
            <el-table-column prop="studentId" label="学号" width="120" />
            <el-table-column prop="username" label="昵称/账号" width="120" />
            <el-table-column prop="phone" label="手机号" width="120" />
            <el-table-column label="账号状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="creditScore" label="信用分" width="80" />
            <el-table-column label="注册时间" width="160">
              <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="最近登录" width="160">
              <template #default="scope">{{ formatDate(scope.row.lastLoginTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="250">
              <template #default="scope">
                <el-button size="small" @click="viewUserDetail(scope.row)">详情</el-button>
                <el-button size="small" type="primary" @click="openEditUser(scope.row)">修改</el-button>
                
                <el-button 
                  v-if="scope.row.status === 'normal'" 
                  size="small" 
                  type="warning" 
                  @click="openStatusDialog(scope.row, 'blacklist')"
                >拉黑</el-button>
                <el-button 
                  v-if="scope.row.status === 'blacklist'" 
                  size="small" 
                  type="success" 
                  @click="handleUndoBlacklist(scope.row)"
                >撤销拉黑</el-button>
                <el-button 
                  v-if="scope.row.status === 'blacklist'" 
                  size="small" 
                  type="success" 
                  @click="openStatusDialog(scope.row, 'normal')"
                >解除黑名单</el-button>

                <el-popconfirm title="确认注销该账号？" @confirm="() => handleUpdateStatus(scope.row, 'deleted', '管理员注销')">
                  <template #reference>
                    <el-button v-if="scope.row.status !== 'deleted'" type="danger" size="small">注销</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <!-- 用户详情弹窗 -->
          <el-dialog v-model="userDetailVisible" title="用户详情" width="600px">
            <el-descriptions :column="1" border v-if="currentUser.username">
              <el-descriptions-item label="学号">{{ currentUser.studentId }}</el-descriptions-item>
              <el-descriptions-item label="昵称/账号">{{ currentUser.username }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ currentUser.gender || '-' }}</el-descriptions-item>
              <el-descriptions-item label="信用分">{{ currentUser.creditScore }}</el-descriptions-item>
              <el-descriptions-item label="账号状态">
                <el-tag :type="getStatusType(currentUser.status)">{{ getStatusText(currentUser.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="注册时间">{{ formatDate(currentUser.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="最近登录">{{ formatDate(currentUser.lastLoginTime) }}</el-descriptions-item>
              <el-descriptions-item label="收货地址">
                <div v-if="currentUser.addresses && currentUser.addresses.length">
                  <div v-for="(addr, idx) in currentUser.addresses" :key="idx" style="margin-bottom: 5px;">
                    <el-tag size="small" v-if="addr.isDefault">默认</el-tag>
                    {{ addr.name }} {{ addr.phone }} {{ addr.address }}
                  </div>
                </div>
                <span v-else>暂无</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-dialog>

          <!-- 状态变更弹窗 -->
          <el-dialog v-model="statusDialogVisible" title="账号状态管理" width="500px">
            <el-form :model="statusForm" label-width="100px">
              <el-form-item label="当前用户">
                <span>{{ currentUser.username }}</span>
              </el-form-item>
              <el-form-item label="目标状态">
                <el-radio-group v-model="statusForm.status">
                  <el-radio label="blacklist">黑名单</el-radio>
                  <el-radio label="normal">正常</el-radio>
                  <el-radio label="permanent_blacklist">永久黑名单</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="原因/备注" required>
                <el-input type="textarea" v-model="statusForm.reason" placeholder="请输入操作原因（必填）" />
              </el-form-item>
              
              <!-- 永久黑名单双人审核 -->
              <div v-if="statusForm.status === 'permanent_blacklist'" style="border: 1px dashed #f56c6c; padding: 10px; border-radius: 4px; margin-bottom: 10px;">
                <p style="color: #f56c6c; margin: 0 0 10px 0;">永久黑名单需双人审核</p>
                <el-form-item label="审核员账号">
                  <el-input v-model="statusForm.secondAdmin" placeholder="另一管理员账号" />
                </el-form-item>
                <el-form-item label="审核员密码">
                  <el-input type="password" v-model="statusForm.secondAdminPwd" placeholder="另一管理员密码" />
                </el-form-item>
              </div>
            </el-form>
            <template #footer>
              <el-button @click="statusDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="submitStatusChange">确认</el-button>
            </template>
          </el-dialog>

          <!-- 编辑用户信息弹窗 -->
          <el-dialog v-model="editUserVisible" title="修改用户信息" width="500px">
            <el-form :model="editUserForm" label-width="80px">
              <el-form-item label="手机号">
                <el-input v-model="editUserForm.phone" />
              </el-form-item>
              <el-form-item label="学号">
                <el-input v-model="editUserForm.studentId" />
              </el-form-item>
              <el-form-item label="信用分">
                <el-input-number v-model="editUserForm.creditScore" :min="0" :max="100" />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="editUserVisible = false">取消</el-button>
              <el-button type="primary" @click="submitEditUser">保存</el-button>
            </template>
          </el-dialog>

          <!-- 操作日志弹窗 -->
          <el-dialog v-model="logsVisible" title="操作日志" width="800px">
            <div style="margin-bottom: 10px;">
              <el-input v-model="logSearchKeyword" placeholder="搜索用户ID/操作类型" style="width: 200px; margin-right: 10px;" clearable @clear="loadLogs" />
              <el-button type="primary" @click="loadLogs">搜索</el-button>
            </div>
            <el-table :data="logList" border height="400">
              <el-table-column prop="id" label="ID" width="60" />
              <el-table-column prop="operator" label="操作人" width="100" />
              <el-table-column prop="targetUser" label="目标用户" width="100" />
              <el-table-column prop="operationType" label="操作类型" width="120" />
              <el-table-column prop="detail" label="详情" show-overflow-tooltip />
              <el-table-column label="时间" width="160">
                <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
              </el-table-column>
            </el-table>
          </el-dialog>
        </el-tab-pane>
        <el-tab-pane label="教材审核" name="bookAudit">
          <el-table :data="bookAuditList" border>
            <el-table-column type="expand">
              <template #default="scope">
                <div class="book-audit-expand">
                  <img v-if="scope.row.coverUrl" :src="resolveCoverUrl(scope.row.coverUrl)" class="book-audit-cover" alt="封面" />
                  <div class="book-audit-meta">
                    <div>作者：{{ scope.row.author || '-' }}</div>
                    <div>ISBN：{{ scope.row.isbn || '-' }}</div>
                    <div>出版社：{{ scope.row.publisher || '-' }}</div>
                    <div>出版时间：{{ scope.row.publishDate || '-' }}</div>
                    <div>成色：{{ scope.row.conditionLevel || '-' }}</div>
                    <div>库存：{{ scope.row.stock ?? '-' }}</div>
                    <div>原价：¥{{ scope.row.originalPrice ?? '-' }}</div>
                    <div>售价：¥{{ scope.row.sellPrice ?? '-' }}</div>
                    <div>卖家类型：{{ scope.row.sellerType || '-' }}</div>
                    <div style="margin-top:8px; white-space: pre-wrap;">描述：{{ scope.row.description || '暂无描述' }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="bookName" label="教材名称"></el-table-column>
            <el-table-column prop="sellerName" label="卖家"></el-table-column>
            <el-table-column prop="conditionLevel" label="成色" width="100"></el-table-column>
            <el-table-column prop="sellPrice" label="售价" width="100">
              <template #default="scope">¥{{ scope.row.sellPrice }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="success" size="small" @click="approveBook(scope.row.id)">通过</el-button>
                <el-button type="danger" size="small" @click="rejectBook(scope.row.id)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="投诉审核" name="complaintAudit">
          <el-table :data="complaints" border>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="orderId" label="订单ID" width="120" />
            <el-table-column prop="username" label="投诉人" width="160" />
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="detail" label="详情" />
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="success" size="small" @click="approveComplaint(scope.row.id)">通过</el-button>
                <el-button type="danger" size="small" @click="rejectComplaint(scope.row.id)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="卖家资质审核" name="sellerAudit">
          <el-table :data="sellerApplications" border>
            <el-table-column prop="username" label="申请账号" width="180" />
            <el-table-column label="当前状态" width="120">
              <template #default="scope">
                <el-tag type="warning">{{ scope.row.sellerStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="收款码" width="120">
              <template #default="scope">
                <el-button
                  v-if="scope.row.paymentCodeUrl"
                  type="primary"
                  size="small"
                  @click="openPaymentCode(scope.row.paymentCodeUrl)"
                >查看</el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="success" size="small" @click="handleApproveSeller(scope.row.username)">通过</el-button>
                <el-button type="danger" size="small" @click="handleRejectSeller(scope.row.username)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="发布公告" name="announce">
          <el-form label-width="80px" style="max-width: 600px; margin-top: 20px;">
            <el-form-item label="标题">
              <el-input v-model="announceTitle" placeholder="请输入公告标题"></el-input>
            </el-form-item>
            <el-form-item label="内容">
              <el-input type="textarea" v-model="announceContent" rows="5" placeholder="请输入公告内容"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleAnnounce">发布全员公告</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="paymentCodeVisible" title="收款码" width="420px">
      <img v-if="paymentCodeUrl" :src="paymentCodeUrl" style="width: 100%; max-height: 520px; object-fit: contain;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, setUserRole, deleteUser, listUnderReviewBooks, approveBook as apiApproveBook, rejectBook as apiRejectBook, listComplaints, approveComplaint as apiApproveComplaint, rejectComplaint as apiRejectComplaint, listSellerApplications, approveSeller, rejectSeller, updateUserStatus, undoBlacklist, updateUserInfo, getOperationLogs, getUserDetail } from '@/api/adminApi'
import { announce } from '@/api/notificationApi'

// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

const activeTab = ref('userManage')
const userList = ref([])
const userSearchKeyword = ref('')
const loadingUsers = ref(false)
const currentUser = ref({})
const userDetailVisible = ref(false)

// 状态管理
const statusDialogVisible = ref(false)
const statusForm = ref({ status: '', reason: '', secondAdmin: '', secondAdminPwd: '' })
const openStatusDialog = (user, targetStatus) => {
  currentUser.value = { ...user }
  statusForm.value = { status: targetStatus, reason: '', secondAdmin: '', secondAdminPwd: '' }
  statusDialogVisible.value = true
}
const submitStatusChange = async () => {
  if (!statusForm.value.reason) {
    ElMessage.warning('请输入操作原因')
    return
  }
  await handleUpdateStatus(currentUser.value, statusForm.value.status, statusForm.value.reason, statusForm.value.secondAdmin, statusForm.value.secondAdminPwd)
  statusDialogVisible.value = false
}

// 用户编辑
const editUserVisible = ref(false)
const editUserForm = ref({ phone: '', studentId: '', creditScore: 100 })
const openEditUser = (user) => {
  currentUser.value = { ...user }
  editUserForm.value = { phone: user.phone, studentId: user.studentId, creditScore: user.creditScore }
  editUserVisible.value = true
}
const submitEditUser = async () => {
  try {
    await updateUserInfo(currentUser.value.username, editUserForm.value)
    ElMessage.success('修改成功')
    editUserVisible.value = false
    loadUsers()
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  }
}

// 日志管理
const logsVisible = ref(false)
const logList = ref([])
const logSearchKeyword = ref('')
const showOperationLogs = () => {
  logsVisible.value = true
  loadLogs()
}
const loadLogs = async () => {
  try {
    const res = await getOperationLogs({ keyword: logSearchKeyword.value })
    logList.value = res || []
  } catch (e) {
    ElMessage.error('加载日志失败')
  }
}

// 辅助函数
const getStatusType = (status) => {
  if (status === 'blacklist') return 'danger'
  if (status === 'deleted') return 'info'
  return 'success'
}
const getStatusText = (status) => {
  const map = { normal: '正常', blacklist: '黑名单', deleted: '已注销' }
  return map[status] || status
}
const formatDate = (ts) => {
  if (!ts) return '-'
  return new Date(ts).toLocaleString()
}

const bookAuditList = ref([])
const complaints = ref([])
const sellerApplications = ref([])
const paymentCodeVisible = ref(false)
const paymentCodeUrl = ref('')

const resolveCoverUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/book-api/')) return url
  if (url.startsWith('/files/')) return `/book-api${url}`
  return url
}

// 公告相关
const announceTitle = ref('')
const announceContent = ref('')
const handleAnnounce = async () => {
  if (!announceTitle.value || !announceContent.value) {
    ElMessage.warning('请输入标题和内容')
    return
  }
  try {
    await announce({ title: announceTitle.value, content: announceContent.value })
    ElMessage.success('公告发布成功')
    announceTitle.value = ''
    announceContent.value = ''
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

const loadUsers = async () => {
  loadingUsers.value = true
  try { userList.value = await listUsers(userSearchKeyword.value) || [] } catch { ElMessage.error('加载用户失败') }
  finally { loadingUsers.value = false }
}
const viewUserDetail = async (user) => {
  // Reset first
  currentUser.value = { ...user }
  userDetailVisible.value = true
  try {
    const res = await getUserDetail(user.username)
    if (res) {
       // Merge user details and addresses
       currentUser.value = { ...res.user, addresses: res.addresses }
    }
  } catch (e) {
    ElMessage.error('获取用户详情失败')
  }
}
const handleUpdateStatus = async (user, status, reason, secondAdmin, secondAdminPwd) => {
  try {
    const res = await updateUserStatus(user.username, status, reason, secondAdmin, secondAdminPwd)
    if (res && res.riskWarning) {
       ElMessage.warning(res.riskWarning)
    } else if (res && res.success === false) {
       ElMessage.error(res.message || '操作失败')
    } else {
       ElMessage.success('操作成功')
    }
    loadUsers()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
const handleUndoBlacklist = async (user) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入撤销原因', '撤销黑名单', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '原因不能为空'
    })
    
    await undoBlacklist(user.username, reason)
    ElMessage.success('撤销成功，账号已恢复正常')
    loadUsers()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '撤销失败')
    }
  }
}

const changeRole = async (username, role) => {
  try { await setUserRole(username, role); ElMessage.success('角色已更新') } catch { ElMessage.error('更新失败') }
}
const removeUser = async (username) => {
  // try { await deleteUser(username); ElMessage.success('已删除'); loadUsers() } catch { ElMessage.error('删除失败') }
  // 改为调用 handleUpdateStatus
  await handleUpdateStatus({ username }, 'deleted', '管理员直接删除')
}

const loadBooks = async () => {
  try { bookAuditList.value = await listUnderReviewBooks() || [] } catch { ElMessage.error('加载待审商品失败') }
}
const approveBook = async (id) => {
  try { await apiApproveBook(id); ElMessage.success('已通过'); loadBooks() } catch { ElMessage.error('操作失败') }
}
const rejectBook = async (id) => {
  try { await apiRejectBook(id); ElMessage.success('已驳回'); loadBooks() } catch { ElMessage.error('操作失败') }
}

const loadComplaints = async () => {
  try { complaints.value = await listComplaints() || [] } catch { ElMessage.error('加载投诉失败') }
}
const approveComplaint = async (id) => {
  try { await apiApproveComplaint(id); ElMessage.success('投诉已处理（通过）'); loadComplaints() } catch { ElMessage.error('操作失败') }
}
const rejectComplaint = async (id) => {
  try { await apiRejectComplaint(id); ElMessage.success('投诉已处理（驳回）'); loadComplaints() } catch { ElMessage.error('操作失败') }
}

const loadSellerApps = async () => {
  try { sellerApplications.value = await listSellerApplications() || [] } catch { /* ignore */ }
}
const handleApproveSeller = async (username) => {
  try { await approveSeller(username); ElMessage.success('已批准卖家资格'); loadSellerApps() } catch { ElMessage.error('操作失败') }
}
const handleRejectSeller = async (username) => {
  try { await rejectSeller(username); ElMessage.success('已驳回卖家资格'); loadSellerApps() } catch { ElMessage.error('操作失败') }
}

const openPaymentCode = (url) => {
  paymentCodeUrl.value = url
  paymentCodeVisible.value = true
}

import { watch } from 'vue'
watch(activeTab, (val) => {
  if (val === 'userManage') loadUsers()
  if (val === 'bookAudit') loadBooks()
  if (val === 'complaintAudit') loadComplaints()
  if (val === 'sellerAudit') loadSellerApps()
})

onMounted(() => {
  loadUsers()
  loadBooks()
  loadComplaints()
  loadSellerApps()
})
</script>

<style scoped>
.admin-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.book-audit-expand {
  display: flex;
  gap: 16px;
  padding: 10px 0;
}
.book-audit-cover {
  width: 120px;
  height: 160px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #eee;
}
.book-audit-meta {
  flex: 1;
  line-height: 1.9;
  color: #303133;
}
</style>
