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
    <div class="page-header">
      <h2>个人中心</h2>
      <el-button type="text" @click="goBack" style="color: #409eff;">返回</el-button>
    </div>

    <el-row :gutter="20">
      <!-- 左侧侧边栏 -->
      <el-col :span="6">
        <el-card shadow="hover" class="profile-sidebar">
          <div class="avatar-box">
            <img src="https://picsum.photos/100/100" alt="头像" class="avatar">
            <h3 class="username">{{ username }}</h3>
            <p class="role-tag">{{ roleName }}</p>
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
            <el-table v-if="addressList.length" :data="addressList" border style="width: 100%;">
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
            <el-empty v-else description="暂无地址" />
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

        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="addressDialogVisible" :title="addressDialogMode === 'edit' ? '编辑地址' : '添加地址'" width="520px">
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="90px">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入收货人" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="addressForm.address" type="textarea" :rows="3" placeholder="请输入收货地址" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addressSaving" @click="submitAddress">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="460px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSaving" @click="submitPassword">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserInfo, updateUserProfile, listAddresses, addAddress as apiAddAddress, updateAddress as apiUpdateAddress, deleteAddress as apiDeleteAddress, changePassword as apiChangePassword } from '@/api/userApi'

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
  gender: 'secret'
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
    router.push('/buyer/collect')
    return
  }
  activeTab.value = key
}

// 返回上一页
const goBack = () => {
  router.back()
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
  } catch (e) {
    ElMessage.error('加载个人信息失败')
  } finally {
    loadingUserInfo.value = false
  }
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
  ElMessage.success('QQ绑定成功！')
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
