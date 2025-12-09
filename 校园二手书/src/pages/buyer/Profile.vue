<template>
  <div class="buyer-profile">
    <div class="page-header">
      <h2>买家个人中心</h2>
      <el-button type="text" @click="goBack" style="color: #409eff;">返回</el-button>
    </div>

    <el-row :gutter="20">
      <!-- 左侧侧边栏 -->
      <el-col :span="6">
        <el-card shadow="hover" class="profile-sidebar">
          <div class="avatar-box">
            <img src="https://picsum.photos/100/100" alt="头像" class="avatar">
            <h3 class="username">{{ username }}</h3>
            <p class="role-tag">买家</p>
          </div>
          <el-menu
              default-active="1"
              class="profile-menu"
              @select="handleMenuSelect"
          >
            <el-menu-item index="1">
              <i class="el-icon-user"></i>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="2">
              <i class="el-icon-location"></i>
              <span>我的地址</span>
            </el-menu-item>
            <el-menu-item index="3">
              <i class="el-icon-lock"></i>
              <span>账户安全</span>
            </el-menu-item>
            <el-menu-item index="4">
              <i class="el-icon-star-on"></i>
              <span>我的收藏</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧内容区 -->
      <el-col :span="18">
        <el-card shadow="hover" class="profile-content">
          <!-- 个人信息（默认显示） -->
          <div v-if="activeTab === '1'" class="info-form">
            <el-form :model="userInfo" label-width="120px">
              <el-form-item label="用户名">
                <el-input v-model="userInfo.username" disabled></el-input>
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="userInfo.phone" placeholder="请输入手机号"></el-input>
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="userInfo.email" placeholder="请输入邮箱"></el-input>
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="userInfo.gender">
                  <el-radio label="male">男</el-radio>
                  <el-radio label="female">女</el-radio>
                  <el-radio label="secret">保密</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveInfo">保存信息</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 我的地址 -->
          <div v-if="activeTab === '2'" class="address-list">
            <el-button type="primary" size="small" @click="addAddress" style="margin-bottom: 10px;">
              <i class="el-icon-plus"></i> 添加地址
            </el-button>
            <el-table :data="addressList" border style="width: 100%;">
              <el-table-column prop="name" label="收货人" width="120"></el-table-column>
              <el-table-column prop="phone" label="手机号" width="150"></el-table-column>
              <el-table-column prop="address" label="地址"></el-table-column>
              <el-table-column prop="isDefault" label="默认地址" width="100">
                <template #default="scope">
                  <el-tag v-if="scope.row.isDefault">是</el-tag>
                  <span v-else>否</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button type="text" size="small" @click="editAddress(scope.row)">编辑</el-button>
                  <el-button type="text" size="small" @click="deleteAddress(scope.row)" style="color: #f56c6c;">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 账户安全 -->
          <div v-if="activeTab === '3'" class="security-setting">
            <el-form label-width="120px">
              <el-form-item label="修改密码">
                <el-button type="primary" @click="changePassword">修改密码</el-button>
              </el-form-item>
              <el-form-item label="绑定微信">
                <el-button type="success" @click="bindWechat">立即绑定</el-button>
              </el-form-item>
              <el-form-item label="绑定QQ">
                <el-button type="info" @click="bindQQ">立即绑定</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 我的收藏（跳转已有页面） -->
          <div v-if="activeTab === '4'" class="collect-redirect">
            <el-empty description="点击下方按钮查看我的收藏">
              <el-button type="primary" @click="toCollectPage">前往我的收藏</el-button>
            </el-empty>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
// 当前激活的标签页
const activeTab = ref('1')
// 用户名（从localStorage读取）
const username = ref(localStorage.getItem('username') || '买家用户')

// 个人信息模拟数据
const userInfo = ref({
  username: username.value,
  phone: '',
  email: '',
  gender: 'secret'
})

// 地址列表模拟数据
const addressList = ref([
  {
    id: 1,
    name: '张三',
    phone: '13800138000',
    address: '北京市朝阳区XX街道XX小区1号楼1单元101',
    isDefault: true
  }
])

// 侧边栏菜单切换
const handleMenuSelect = (key) => {
  activeTab.value = key
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 保存个人信息
const saveInfo = () => {
  ElMessage.success('个人信息保存成功！')
}

// 添加地址
const addAddress = () => {
  ElMessage.info('打开添加地址弹窗')
}

// 编辑地址
const editAddress = (row) => {
  ElMessage.info(`编辑地址：${row.name}`)
}

// 删除地址
const deleteAddress = (row) => {
  addressList.value = addressList.value.filter(item => item.id !== row.id)
  ElMessage.success('地址删除成功！')
}

// 修改密码
const changePassword = () => {
  ElMessage.info('打开修改密码弹窗')
}

// 绑定微信
const bindWechat = () => {
  ElMessage.success('微信绑定成功！')
}

// 绑定QQ
const bindQQ = () => {
  ElMessage.success('QQ绑定成功！')
}

// 前往我的收藏
const toCollectPage = () => {
  router.push('/buyer/collect')
}
</script>

<style scoped>
.buyer-profile {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  color: #333;
}

/* 侧边栏样式 */
.profile-sidebar {
  height: 100%;
}
.avatar-box {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}
.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 10px;
}
.username {
  margin: 0 0 5px 0;
  font-size: 16px;
}
.role-tag {
  color: #409eff;
  font-size: 14px;
}
.profile-menu {
  border-right: none;
  margin-top: 10px;
}

/* 内容区样式 */
.profile-content {
  min-height: 500px;
}
.info-form, .address-list, .security-setting, .collect-redirect {
  padding: 20px;
}
</style>