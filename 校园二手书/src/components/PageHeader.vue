<template>
  <div class="page-header">
    <div class="title-area">
      <el-button 
        v-if="goBack" 
        icon="el-icon-back" 
        size="small" 
        @click="goBack" 
        style="margin-right: 15px;"
      >返回</el-button>
      <h2 class="title">{{ title }}</h2>
    </div>
    <div class="actions">
      <slot></slot> <!-- 插槽用于放退出按钮等操作 -->
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  goBack: {
    type: Function,
    default: null
  }
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}
.title-area {
  display: flex;
  align-items: center;
}
.title {
  margin: 0;
  color: #333;
}
.actions {
  display: flex;
  gap: 10px;
}
</style>