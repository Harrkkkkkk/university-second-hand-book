<template>
  <div class="admin-dashboard">
    <page-header title="管理员后台">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户管理" name="userManage">
          <el-table :data="userList" border>
            <el-table-column prop="username" label="账号"></el-table-column>
            <el-table-column prop="role" label="角色" width="180">
              <template #default="scope">
                <el-select v-model="scope.row.role" size="small" style="width:140px" @change="role => changeRole(scope.row.username, role)">
                  <el-option label="买家" value="buyer" />
                  <el-option label="卖家" value="seller" />
                  <el-option label="管理员" value="admin" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template #default="scope">
                <el-popconfirm title="确认删除该用户？" @confirm="() => removeUser(scope.row.username)">
                  <template #reference>
                    <el-button type="danger" size="small">删除</el-button>
                  </template>
                </el-popconfirm>
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
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'
import { ElMessage } from 'element-plus'
import { listUsers, setUserRole, deleteUser, listUnderReviewBooks, approveBook as apiApproveBook, rejectBook as apiRejectBook, listComplaints, approveComplaint as apiApproveComplaint, rejectComplaint as apiRejectComplaint } from '@/api/adminApi'

// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

const activeTab = ref('userManage')
const userList = ref([])
const bookAuditList = ref([])
const complaints = ref([])

const loadUsers = async () => {
  try { userList.value = await listUsers() || [] } catch { ElMessage.error('加载用户失败') }
}
const changeRole = async (username, role) => {
  try { await setUserRole(username, role); ElMessage.success('角色已更新') } catch { ElMessage.error('更新失败') }
}
const removeUser = async (username) => {
  try { await deleteUser(username); ElMessage.success('已删除'); loadUsers() } catch { ElMessage.error('删除失败') }
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
  try { await apiApproveComplaint(id); ElMessage.success('已通过'); loadComplaints() } catch { ElMessage.error('操作失败') }
}
const rejectComplaint = async (id) => {
  try { await apiRejectComplaint(id); ElMessage.success('已驳回'); loadComplaints() } catch { ElMessage.error('操作失败') }
}

onMounted(() => { loadUsers(); loadBooks(); loadComplaints() })
</script>

<style scoped>
.admin-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
