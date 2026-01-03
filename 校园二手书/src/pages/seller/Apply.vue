<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Apply.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Seller application page.
 *              Allows users to apply for seller status.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
<template>
  <div class="seller-apply-page">
    <el-card class="apply-card">
      <div class="header">
        <i class="el-icon-shop header-icon"></i>
        <h2>申请成为卖家</h2>
      </div>
      
      <div class="content">
        <p class="intro">成为卖家后，您可以：</p>
        <ul class="benefits">
          <li><i class="el-icon-check"></i> 发布闲置教材，赚取零花钱</li>
          <li><i class="el-icon-check"></i> 管理自己的商品和订单</li>
          <li><i class="el-icon-check"></i> 查看买家评价和反馈</li>
        </ul>

        <el-alert
          v-if="cooldownMsg"
          title="无法申请"
          type="error"
          :description="cooldownMsg"
          show-icon
          :closable="false"
          style="margin: 20px 0;"
        />

        <el-alert
          v-else
          title="申请须知"
          type="info"
          description="提交申请后，管理员将在1-3个工作日内审核您的资质。审核通过后，您将获得卖家权限。"
          show-icon
          :closable="false"
          style="margin: 20px 0;"
        />
        
        <div class="actions">
          <el-button type="primary" size="large" @click="handleApply" :loading="loading" :disabled="isCooldown">
            {{ isCooldown ? '冷却中' : '立即申请' }}
          </el-button>
          <el-button size="large" @click="$router.push('/buyer/home')">暂不申请</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applySeller, getUserInfo } from '@/api/userApi'

const router = useRouter()
const loading = ref(false)
const cooldownMsg = ref('')
const isCooldown = ref(false)
let timer = null

onMounted(async () => {
  try {
    const res = await getUserInfo()
    if (res) {
      const u = res
      if (u.sellerStatus === 'REJECTED' && u.lastAuditTime) {
        checkCooldown(u.lastAuditTime)
      }
    }
  } catch (e) {
    console.error(e)
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const checkCooldown = (lastAuditTime) => {
  const update = () => {
    const diff = Date.now() - lastAuditTime
    const cooldownTime = 20 * 60 * 1000
    if (diff < cooldownTime) {
      isCooldown.value = true
      const remaining = cooldownTime - diff
      const min = Math.floor(remaining / 60000)
      const sec = Math.floor((remaining % 60000) / 1000)
      cooldownMsg.value = `您的申请已被驳回，请在 ${min} 分 ${sec} 秒后再次申请`
    } else {
      isCooldown.value = false
      cooldownMsg.value = ''
      if (timer) clearInterval(timer)
    }
  }
  update()
  timer = setInterval(update, 1000)
}

/**
 * Function: handleApply
 * Description: Submits a seller application request.
 */
const handleApply = async () => {
  // Check if user is verified
  const isVerified = localStorage.getItem('isVerified') === '1'
  if (!isVerified) {
    ElMessageBox.confirm(
      '申请成为卖家需要先完成实名认证，是否前往认证？',
      '提示',
      {
        confirmButtonText: '去认证',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.push('/verify-identity?redirect=/seller/apply')
    }).catch(() => {})
    return
  }

  if (isCooldown.value) {
    ElMessage.warning('请等待冷却时间结束')
    return
  }
  try {
    await ElMessageBox.confirm('确定要申请成为卖家吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    loading.value = true
    await applySeller()
    localStorage.setItem('sellerStatus', 'PENDING')
    
    // 使用 ElMessageBox 显示成功信息，用户点击确定后跳转
    await ElMessageBox.alert('您的卖家资格申请已提交，请耐心等待管理员审核。', '提交成功', {
      confirmButtonText: '返回首页',
      type: 'success',
      callback: () => {
        router.push('/')
      }
    })

  } catch (e) {
    if (e !== 'cancel') {
        ElMessage.error('申请提交失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.seller-apply-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  background-color: #f5f7fa;
}

.apply-card {
  width: 500px;
  padding: 40px;
  text-align: center;
}

.header-icon {
  font-size: 60px;
  color: #409eff;
  margin-bottom: 20px;
}

.intro {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
}

.benefits {
  list-style: none;
  padding: 0;
  margin: 0 0 30px 0;
  text-align: left;
  display: inline-block;
}

.benefits li {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.benefits li i {
  color: #67c23a;
  margin-right: 10px;
  font-weight: bold;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}

</style>
