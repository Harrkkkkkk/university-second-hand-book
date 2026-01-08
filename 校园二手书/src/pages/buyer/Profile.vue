<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Profile.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: User profile management page.
 *              Includes personal info, address management, and security settings.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
<template>
  <div class="buyer-profile">
    <!-- 顶部背景卡片 -->
    <div class="profile-header-card">
      <div class="profile-cover">
        <div class="cover-shapes">
          <div class="shape shape-1"></div>
          <div class="shape shape-2"></div>
        </div>
      </div>
      
      <div class="profile-user-info">
        <div class="avatar-wrapper">
          <el-avatar :size="100" :src="userInfo.avatar || 'https://picsum.photos/100/100'" class="user-avatar" @error="() => true">
            <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"/>
          </el-avatar>
          <div class="avatar-edit-badge">
            <el-icon><Camera /></el-icon>
          </div>
        </div>
        
        <div class="user-meta">
          <h2 class="username">{{ username }}</h2>
          <div class="user-tags">
            <el-tag effect="dark" round size="small" :type="role === 'seller' ? 'success' : 'primary'">
              {{ roleName }}
            </el-tag>
            <el-tag v-if="userInfo.isVerified" effect="plain" round size="small" type="success" class="verify-tag">
              <el-icon><CircleCheckFilled /></el-icon> 已实名
            </el-tag>
            <el-tag v-else effect="plain" round size="small" type="info" class="verify-tag" @click="goToVerify" style="cursor: pointer">
              <el-icon><Warning /></el-icon> 未实名
            </el-tag>
          </div>
          <p class="user-bio">暂无个性签名</p>
        </div>

        <div class="user-stats">
          <div class="stat-item">
            <span class="stat-value">0</span>
            <span class="stat-label">购买</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">0</span>
            <span class="stat-label">发布</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">0</span>
            <span class="stat-label">收藏</span>
          </div>
        </div>
      </div>
    </div>

    <div class="profile-body">
      <el-row :gutter="24">
        <!-- 左侧导航菜单 -->
        <el-col :span="6" :xs="24">
          <el-card shadow="hover" class="menu-card" :body-style="{ padding: '10px 0' }">
            <el-menu
              :default-active="activeTab"
              class="profile-menu-vertical"
              @select="handleMenuSelect"
            >
              <el-menu-item index="1">
                <el-icon><User /></el-icon>
                <span>个人资料</span>
              </el-menu-item>
              <el-menu-item index="2">
                <el-icon><Location /></el-icon>
                <span>收货地址</span>
              </el-menu-item>
              <el-menu-item index="3">
                <el-icon><Lock /></el-icon>
                <span>账号安全</span>
              </el-menu-item>
              <el-menu-item index="5">
                <el-icon><Postcard /></el-icon>
                <span>实名认证</span>
              </el-menu-item>
              <el-menu-item index="4" @click="goToCollect">
                <el-icon><Star /></el-icon>
                <span>我的收藏</span>
                <el-icon class="external-link-icon"><ArrowRight /></el-icon>
              </el-menu-item>
            </el-menu>
          </el-card>
        </el-col>

        <!-- 右侧内容区域 -->
        <el-col :span="18" :xs="24">
          <div class="content-wrapper">
            <!-- 个人资料面板 -->
            <transition name="fade-slide" mode="out-in">
              <el-card v-if="activeTab === '1'" shadow="hover" class="content-card">
                <template #header>
                  <div class="card-header">
                    <span class="header-title">基本信息</span>
                    <el-button type="primary" plain round size="small" @click="saveInfo" :loading="loadingUserInfo">保存修改</el-button>
                  </div>
                </template>
                
                <el-form :model="userInfo" label-width="100px" class="premium-form" label-position="top">
                  <el-row :gutter="24">
                    <el-col :span="12" :xs="24">
                      <el-form-item label="用户名">
                        <el-input v-model="userInfo.username" disabled prefix-icon="User"></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12" :xs="24">
                      <el-form-item label="手机号码">
                        <el-input v-model="userInfo.phone" placeholder="未绑定手机号" prefix-icon="Iphone"></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12" :xs="24">
                      <el-form-item label="电子邮箱">
                        <el-input v-model="userInfo.email" placeholder="未绑定邮箱" prefix-icon="Message"></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12" :xs="24">
                      <el-form-item label="性别">
                        <el-radio-group v-model="userInfo.gender" class="gender-radio">
                          <el-radio-button label="male">
                            <el-icon><Male /></el-icon> 男
                          </el-radio-button>
                          <el-radio-button label="female">
                            <el-icon><Female /></el-icon> 女
                          </el-radio-button>
                          <el-radio-button label="secret">
                            <el-icon><Lock /></el-icon> 保密
                          </el-radio-button>
                        </el-radio-group>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
              </el-card>

              <!-- 地址管理面板 -->
              <el-card v-else-if="activeTab === '2'" shadow="hover" class="content-card">
                <template #header>
                  <div class="card-header">
                    <span class="header-title">收货地址</span>
                    <el-button type="primary" icon="Plus" round size="small" @click="addAddress">新增地址</el-button>
                  </div>
                </template>

                <div v-loading="addressLoading" class="address-grid">
                  <el-empty v-if="addressList.length === 0" description="暂无收货地址" />
                  
                  <div 
                    v-else
                    v-for="item in addressList" 
                    :key="item.id" 
                    class="address-card"
                    :class="{ 'is-default': item.isDefault }"
                  >
                    <div class="address-card-header">
                      <span class="name">{{ item.name }}</span>
                      <span class="phone">{{ item.phone }}</span>
                      <el-tag v-if="item.isDefault" size="small" type="danger" effect="dark" class="default-tag">默认</el-tag>
                    </div>
                    <div class="address-body">
                      <p>{{ item.address }}</p>
                    </div>
                    <div class="address-actions">
                      <el-button type="primary" link icon="Edit" @click="editAddress(item)">编辑</el-button>
                      <el-button type="danger" link icon="Delete" @click="deleteAddress(item)">删除</el-button>
                    </div>
                  </div>
                </div>
              </el-card>

              <!-- 账号安全面板 -->
              <el-card v-else-if="activeTab === '3'" shadow="hover" class="content-card">
                <template #header>
                  <div class="card-header">
                    <span class="header-title">账号安全</span>
                  </div>
                </template>

                <div class="security-list">
                  <div class="security-item">
                    <div class="security-icon"><el-icon><Key /></el-icon></div>
                    <div class="security-info">
                      <h3>登录密码</h3>
                      <p>建议定期修改密码以保护账号安全</p>
                    </div>
                    <el-button round @click="changePassword">修改</el-button>
                  </div>

                  <div class="security-item">
                    <div class="security-icon wechat"><el-icon><ChatDotRound /></el-icon></div>
                    <div class="security-info">
                      <h3>微信绑定</h3>
                      <p>绑定微信可用于快捷登录</p>
                    </div>
                    <el-button type="success" plain round @click="bindWechat">已绑定</el-button>
                  </div>
                  
                  <div class="security-item">
                    <div class="security-icon danger"><el-icon><SwitchButton /></el-icon></div>
                    <div class="security-info">
                      <h3>注销账号</h3>
                      <p>注销后无法恢复，请谨慎操作</p>
                    </div>
                    <el-button type="danger" plain round @click="handleDeleteAccount">注销</el-button>
                  </div>
                </div>
              </el-card>
              
              <!-- 实名认证面板 -->
              <el-card v-else-if="activeTab === '5'" shadow="hover" class="content-card">
                <template #header>
                  <div class="card-header">
                    <span class="header-title">实名认证</span>
                  </div>
                </template>
                
                <div class="verify-container">
                  <div v-if="userInfo.isVerified" class="verify-status success">
                     <el-icon class="status-icon"><CircleCheckFilled /></el-icon>
                     <h3>已通过实名认证</h3>
                     <p class="verify-detail">真实姓名：{{ formatName(userInfo.realName) }}</p>
                     <p class="verify-detail">证件号码：{{ '******************' }}</p>
                  </div>
                  <div v-else class="verify-status pending">
                     <div class="verify-illustration">
                        <el-icon class="illustration-icon"><Postcard /></el-icon>
                     </div>
                     <h3>尚未完成实名认证</h3>
                     <p>实名认证后可发布商品，提高账号可信度</p>
                     <el-button type="primary" size="large" round @click="goToVerify" class="verify-btn">立即认证</el-button>
                  </div>
                </div>
              </el-card>
            </transition>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 地址弹窗 -->
    <el-dialog 
      v-model="addressDialogVisible" 
      :title="addressDialogMode === 'edit' ? '编辑地址' : '新增地址'" 
      width="500px"
      align-center
      class="premium-dialog"
    >
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="80px" label-position="top">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收货人" prop="name">
              <el-input v-model="addressForm.name" placeholder="请填写收货人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="addressForm.phone" placeholder="请填写手机号码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址" prop="address">
          <el-input 
            v-model="addressForm.address" 
            type="textarea" 
            :rows="3" 
            placeholder="街道、楼牌号等" 
            resize="none"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault" label="设为默认收货地址" border />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false" round>取消</el-button>
        <el-button type="primary" :loading="addressSaving" @click="submitAddress" round>保存</el-button>
      </template>
    </el-dialog>

    <!-- 密码弹窗 -->
    <el-dialog 
      v-model="passwordDialogVisible" 
      title="修改登录密码" 
      width="420px"
      align-center
      class="premium-dialog"
    >
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false" round>取消</el-button>
        <el-button type="primary" :loading="passwordSaving" @click="submitPassword" round>确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, Location, Lock, Postcard, Star, 
  Camera, CircleCheckFilled, Warning, 
  Male, Female, Plus, Edit, Delete,
  Key, ChatDotRound, SwitchButton,
  Iphone, Message, ArrowRight
} from '@element-plus/icons-vue'
import { getUserInfo, updateUserProfile, listAddresses, addAddress as apiAddAddress, updateAddress as apiUpdateAddress, deleteAddress as apiDeleteAddress, changePassword as apiChangePassword, deleteAccount } from '@/api/userApi'

const router = useRouter()
// 当前激活的标签页
const activeTab = ref('1')
// 用户名（从localStorage读取）
const username = ref(localStorage.getItem('username') || '买家用户')
const role = ref(localStorage.getItem('role') || 'buyer')

const roleName = computed(() => {
  const map = {
    buyer: '买家',
    seller: '卖家',
    admin: '管理员'
  }
  return map[role.value] || '用户'
})

const loadingUserInfo = ref(false)

const userInfo = ref({
  username: username.value,
  phone: '',
  email: '',
  gender: 'secret',
  realName: '',
  isVerified: false,
  avatar: ''
})

const addressList = ref([])
const addressLoading = ref(false)

const addressDialogVisible = ref(false)
const addressDialogMode = ref('add')
const addressSaving = ref(false)
const addressFormRef = ref()
const addressForm = ref({
  id: null,
  name: '',
  phone: '',
  address: '',
  isDefault: false
})
const addressRules = {
  name: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }]
}

const passwordDialogVisible = ref(false)
const passwordSaving = ref(false)
const passwordFormRef = ref()
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) callback(new Error('两次输入的新密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

// 侧边栏菜单切换
const handleMenuSelect = (key) => {
  if (key === '4') {
    // Collect is handled by click event
    return
  }
  activeTab.value = key
}

const goToCollect = () => {
  router.push('/buyer/collect')
}

const formatName = (name) => {
  if (!name) return ''
  return name.length > 1 ? '*' + name.substring(1) : name
}

/**
 * Function: loadUser
 * Description: Fetches and displays current user information.
 */
const loadUser = async () => {
  loadingUserInfo.value = true
  try {
    const res = await getUserInfo()
    if (res && res.username) {
      username.value = res.username
      userInfo.value.username = res.username
      localStorage.setItem('username', res.username)
    }
    if (res && res.role) {
      role.value = res.role
      localStorage.setItem('role', res.role)
    }
    userInfo.value.phone = res && res.phone ? res.phone : ''
    userInfo.value.email = res && res.email ? res.email : ''
    userInfo.value.gender = res && res.gender ? res.gender : 'secret'
    userInfo.value.realName = res && res.realName ? res.realName : ''
    userInfo.value.isVerified = res && res.isVerified ? true : false
    
    // Sync to localStorage
    if (userInfo.value.realName) localStorage.setItem('realName', userInfo.value.realName)
    localStorage.setItem('isVerified', userInfo.value.isVerified ? '1' : '0')
  } catch (e) {
    ElMessage.error('加载个人信息失败')
  } finally {
    loadingUserInfo.value = false
  }
}

const goToVerify = () => {
  router.push('/verify-identity?redirect=/buyer/home')
}

/**
 * Function: loadAddresses
 * Description: Fetches the user's address list.
 */
const loadAddresses = async () => {
  addressLoading.value = true
  try {
    const res = await listAddresses()
    addressList.value = Array.isArray(res) ? res : []
  } catch (e) {
    addressList.value = []
    ElMessage.error('加载地址失败')
  } finally {
    addressLoading.value = false
  }
}

/**
 * Function: saveInfo
 * Description: Updates the user's profile information.
 */
const saveInfo = async () => {
  try {
    await updateUserProfile({
      phone: userInfo.value.phone,
      email: userInfo.value.email,
      gender: userInfo.value.gender
    })
    ElMessage.success('个人信息保存成功！')
    await loadUser()
  } catch (e) {
    ElMessage.error('个人信息保存失败')
  }
}

/**
 * Function: addAddress
 * Description: Opens the dialog to add a new address.
 */
const addAddress = () => {
  addressDialogMode.value = 'add'
  addressForm.value = { id: null, name: '', phone: '', address: '', isDefault: false }
  addressDialogVisible.value = true
  nextTick(() => addressFormRef.value && addressFormRef.value.clearValidate())
}

/**
 * Function: editAddress
 * Description: Opens the dialog to edit an existing address.
 * Input: row (Object) - Address object
 */
const editAddress = (row) => {
  addressDialogMode.value = 'edit'
  addressForm.value = {
    id: row.id,
    name: row.name || '',
    phone: row.phone || '',
    address: row.address || '',
    isDefault: !!row.isDefault
  }
  addressDialogVisible.value = true
  nextTick(() => addressFormRef.value && addressFormRef.value.clearValidate())
}

/**
 * Function: deleteAddress
 * Description: Deletes an address after confirmation.
 * Input: row (Object) - Address object
 */
const deleteAddress = (row) => {
  ElMessageBox.confirm('确认删除该地址吗？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        await apiDeleteAddress(row.id)
        ElMessage.success('地址删除成功！')
        await loadAddresses()
      } catch (e) {
        ElMessage.error('地址删除失败')
      }
    })
    .catch(() => {})
}

/**
 * Function: submitAddress
 * Description: Submits the address form (add or update).
 */
const submitAddress = async () => {
  if (!addressFormRef.value) return
  try {
    await addressFormRef.value.validate()
  } catch (e) {
    return
  }
  addressSaving.value = true
  try {
    const payload = {
      name: addressForm.value.name,
      phone: addressForm.value.phone,
      address: addressForm.value.address,
      isDefault: addressForm.value.isDefault
    }
    if (addressDialogMode.value === 'edit') {
      await apiUpdateAddress(addressForm.value.id, payload)
      ElMessage.success('地址修改成功！')
    } else {
      await apiAddAddress(payload)
      ElMessage.success('地址添加成功！')
    }
    addressDialogVisible.value = false
    await loadAddresses()
  } catch (e) {
    ElMessage.error(addressDialogMode.value === 'edit' ? '地址修改失败' : '地址添加失败')
  } finally {
    addressSaving.value = false
  }
}

// 修改密码
const changePassword = () => {
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  passwordDialogVisible.value = true
  nextTick(() => passwordFormRef.value && passwordFormRef.value.clearValidate())
}

/**
 * Function: submitPassword
 * Description: Submits the password change request.
 */
const submitPassword = async () => {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
  } catch (e) {
    return
  }
  passwordSaving.value = true
  try {
    await apiChangePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功！')
    passwordDialogVisible.value = false
  } catch (e) {
    ElMessage.error('密码修改失败')
  } finally {
    passwordSaving.value = false
  }
}

// 绑定微信
const bindWechat = () => {
  ElMessage.success('微信绑定成功！')
}

// 绑定QQ
const bindQQ = () => {
  ElMessage.info('QQ绑定功能开发中...')
}

const handleDeleteAccount = () => {
  ElMessageBox.confirm(
    '注销账号后，您的所有数据（包括订单、收藏、发布的商品等）将被永久删除且无法恢复。确定要继续吗？',
    '危险操作警告',
    {
      confirmButtonText: '确定注销',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      await deleteAccount()
      ElMessage.success('账号已注销')
      localStorage.clear()
      router.push('/login')
    } catch (e) {
      ElMessage.error('注销失败：' + (e.response?.data?.message || e.message))
    }
  }).catch(() => {
    // canceled
  })
}

watch(activeTab, (tab) => {
  if (tab === '2') loadAddresses()
})

onMounted(async () => {
  await loadUser()
  if (activeTab.value === '2') await loadAddresses()
})
</script>

<style scoped>
.buyer-profile {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 80vh;
}

/* 顶部卡片 */
.profile-header-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  position: relative;
}

.profile-cover {
  height: 160px;
  background: linear-gradient(135deg, #409eff 0%, #36d1dc 100%);
  position: relative;
  overflow: hidden;
}

.cover-shapes .shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}
.cover-shapes .shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -50px;
}
.cover-shapes .shape-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: 50px;
}

.profile-user-info {
  padding: 0 30px 30px;
  display: flex;
  align-items: flex-end;
  position: relative;
  margin-top: -50px;
}

.avatar-wrapper {
  position: relative;
  padding: 4px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  border: 2px solid #fff;
}

.avatar-edit-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  background: #409eff;
  color: #fff;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-edit-badge:hover {
  transform: scale(1.1);
  background: #66b1ff;
}

.user-meta {
  margin-left: 24px;
  flex: 1;
  padding-bottom: 10px;
}

.username {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
}

.user-tags {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.user-bio {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.user-stats {
  display: flex;
  align-items: center;
  padding-bottom: 15px;
}

.stat-item {
  text-align: center;
  padding: 0 20px;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.stat-divider {
  width: 1px;
  height: 24px;
  background: #e4e7ed;
}

/* 侧边菜单 */
.menu-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  margin-bottom: 20px;
}

.profile-menu-vertical {
  border-right: none;
}

.profile-menu-vertical :deep(.el-menu-item) {
  height: 56px;
  line-height: 56px;
  margin: 4px 12px;
  border-radius: 8px;
  color: #606266;
}

.profile-menu-vertical :deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 600;
}

.profile-menu-vertical :deep(.el-menu-item:hover) {
  background-color: #f5f7fa;
}

.external-link-icon {
  margin-left: auto;
  font-size: 14px;
  color: #c0c4cc;
}

/* 内容卡片 */
.content-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  position: relative;
  padding-left: 12px;
}

.header-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background: #409eff;
  border-radius: 2px;
}

/* 表单样式 */
.premium-form {
  padding: 20px 0;
}

.gender-radio :deep(.el-radio-button__inner) {
  padding: 10px 20px;
}

/* 地址卡片 */
.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 10px 0;
}

.address-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;
  position: relative;
  cursor: default;
}

.address-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #c6e2ff;
}

.address-card.is-default {
  border-color: #409eff;
  background: #f0f9eb;
}

.address-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.address-card-header .name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.address-card-header .phone {
  color: #606266;
}

.address-body {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 16px;
  min-height: 42px;
}

.address-actions {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
}

/* 安全设置 */
.security-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 10px 0;
}

.security-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  background: #f8f9fa;
  transition: all 0.3s;
}

.security-item:hover {
  background: #f0f2f5;
  transform: translateY(-2px);
}

.security-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #ecf5ff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 20px;
}

.security-icon.wechat {
  background: #e1f3d8;
  color: #67c23a;
}

.security-icon.danger {
  background: #fef0f0;
  color: #f56c6c;
}

.security-info {
  flex: 1;
}

.security-info h3 {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
}

.security-info p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

/* 实名认证 */
.verify-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.verify-status {
  text-align: center;
}

.status-icon {
  font-size: 64px;
  color: #67c23a;
  margin-bottom: 20px;
}

.illustration-icon {
  font-size: 80px;
  color: #dcdfe6;
  margin-bottom: 20px;
}

.verify-detail {
  font-size: 16px;
  color: #606266;
  margin: 8px 0;
}

.verify-btn {
  margin-top: 20px;
  width: 200px;
}

/* 过渡动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .profile-user-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
    margin-top: -60px;
  }

  .user-meta {
    margin-left: 0;
    margin-top: 16px;
    margin-bottom: 20px;
  }
  
  .user-tags {
    justify-content: center;
  }

  .profile-menu-vertical {
    display: flex;
    overflow-x: auto;
    padding-bottom: 10px;
  }

  .profile-menu-vertical :deep(.el-menu-item) {
    flex: 0 0 auto;
    margin: 0 4px;
  }
  
  .address-grid {
    grid-template-columns: 1fr;
  }
}
</style>
