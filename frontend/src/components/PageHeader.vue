<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: PageHeader.vue
 * Author: WiseBookPal Team Version: 1.0 Date: 2026-01-02
 * Description: Reusable page header component.
 *              Includes title, back button (optional), and action slots.
 * History:
 * 1. Date: 2026-01-02
 *    Author: WiseBookPal Team
 *    Modification: Initial implementation
-->
<template>
  <div class="page-header-wrapper">
    <el-page-header @back="handleBack" class="custom-page-header" :title="''">
      <template #icon v-if="goBack">
        <div class="back-icon-btn">
          <el-icon><ArrowLeft /></el-icon>
        </div>
      </template>
      <template #icon v-else>
        <!-- Empty icon slot if no back function -->
        <span></span>
      </template>
      <template #content>
        <span class="page-title">{{ title }}</span>
      </template>
      <template #extra>
        <div class="actions">
          <slot></slot>
        </div>
      </template>
    </el-page-header>
  </div>
</template>

<script setup>
import { ArrowLeft } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  goBack: {
    type: Function,
    default: null
  }
})

const handleBack = () => {
  if (props.goBack) {
    props.goBack()
  }
}
</script>

<style scoped>
.page-header-wrapper {
  margin-bottom: 20px;
  background-color: transparent;
}

.custom-page-header :deep(.el-page-header__left) {
  margin-right: 16px;
}

/* Hide the default back arrow if we are providing our own via slot, 
   but el-page-header icon slot replaces the arrow. */

.back-icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #606266;
  font-size: 18px;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.back-icon-btn:hover {
  background-color: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.back-icon-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.2);
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 0.5px;
  line-height: 36px;
}

.actions {
  display: flex;
  gap: 10px;
}
</style>