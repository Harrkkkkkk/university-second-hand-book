<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: Home.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Landing page/Home page.
 *              Displays a welcome message and basic navigation instructions.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
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