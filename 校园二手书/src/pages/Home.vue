<template>
  <div class="home-page">
    <page-header title="校园二手教材交易平台"></page-header>
    <el-card class="welcome-card">
      <h3>欢迎使用二手教材交易平台！</h3>
      <p>👉 点击导航栏「教材列表」查看所有二手教材</p>
      <p>👉 点击导航栏「发布教材」发布你的二手教材</p>
    </el-card>
  </div>
</template>

<script setup>
import PageHeader from '@/components/PageHeader.vue'
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.welcome-card {
  text-align: center;
  padding: 40px;
}
</style>