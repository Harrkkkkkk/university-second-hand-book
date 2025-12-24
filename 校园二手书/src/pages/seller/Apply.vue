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
          title="申请须知"
          type="info"
          description="提交申请后，管理员将在1-3个工作日内审核您的资质。审核通过后，您将获得卖家权限。"
          show-icon
          :closable="false"
          style="margin: 20px 0;"
        />
        
        <div class="actions">
          <el-button type="primary" size="large" @click="handleApply" :loading="loading">立即申请</el-button>
          <el-button size="large" @click="$router.push('/buyer/home')">暂不申请</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applySeller } from '@/api/userApi'

const router = useRouter()
const loading = ref(false)

const handleApply = async () => {
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
