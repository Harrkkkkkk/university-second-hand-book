<template>
  <footer class="page-footer">
    <div class="footer-content">
      <div class="footer-section brand">
        <div class="logo">
          <el-icon class="logo-icon"><Reading /></el-icon>
          <span>WiseBookPal</span>
        </div>
        <p class="slogan">让每一本教材都找到新的主人，让知识在校园里循环流动。</p>
      </div>
      
      <div class="footer-section links">
        <h3>快速链接</h3>
        <ul>
          <li><a @click="$router.push('/buyer/home')">教材选购</a></li>
          <li><a @click="$router.push('/publish')">发布教材</a></li>
          <li><a @click="$router.push('/buyer/order')">我的订单</a></li>
        </ul>
      </div>
      
      <div class="footer-section contact">
        <h3>联系我们</h3>
        <p>Email: 3179857925@qq.com</p>
        <p>校内地址: 武汉大学国软C3</p>
      </div>
    </div>
    <div class="footer-bottom">
      <p>&copy; 2026 WiseBookPal Tech. All rights reserved.</p>
    </div>
  </footer>
</template>

<script setup>
import { Reading } from '@element-plus/icons-vue'
</script>

<style scoped>
.page-footer {
  background-color: #fff;
  border-top: 1px solid #ebeef5;
  padding: 40px 0 20px;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  padding: 0 20px 40px;
  flex-wrap: wrap;
  gap: 40px;
}

.footer-section {
  flex: 1;
  min-width: 200px;
}

.brand .logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 16px;
}

.brand .logo-icon {
  color: #409EFF;
  font-size: 24px;
}

.slogan {
  color: #909399;
  font-size: 14px;
  line-height: 1.6;
  max-width: 300px;
}

.footer-section h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 20px;
  font-weight: 600;
}

.links ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.links li {
  margin-bottom: 12px;
}

.links a {
  color: #606266;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.3s;
}

.links a:hover {
  color: #409EFF;
}

.contact p {
  color: #606266;
  margin-bottom: 10px;
  font-size: 14px;
}

.footer-bottom {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}
</style>
