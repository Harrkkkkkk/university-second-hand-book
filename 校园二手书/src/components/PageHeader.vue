<template>
  <div class="page-header">
    <h2 class="title">{{ title }}</h2>
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
.title {
  margin: 0;
  color: #333;
}
.actions {
  display: flex;
  gap: 10px;
}
</style>