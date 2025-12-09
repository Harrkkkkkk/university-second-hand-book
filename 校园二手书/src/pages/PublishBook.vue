<template>
  <div class="publish-book">
    <page-header title="发布二手教材">
      <el-button type="text" @click="logout" style="color: #e64340;">退出登录</el-button>
    </page-header>

    <el-card>
      <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef" label-width="100px">
        <el-form-item label="教材名称" prop="bookName">
          <el-input v-model="publishForm.bookName" placeholder="请输入教材名称"></el-input>
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="publishForm.author" placeholder="请输入作者姓名"></el-input>
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input v-model="publishForm.originalPrice" type="number" placeholder="请输入教材原价"></el-input>
        </el-form-item>
        <el-form-item label="售价" prop="sellPrice">
          <el-input v-model="publishForm.sellPrice" type="number" placeholder="请输入出售价格"></el-input>
        </el-form-item>
        <el-form-item label="教材描述" prop="description">
          <el-input v-model="publishForm.description" type="textarea" rows="5" placeholder="请描述教材成色、是否有笔记等"></el-input>
        </el-form-item>
        <el-form-item label="教材封面">
          <el-upload
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :on-change="handleFileChange"
          >
            <i class="el-icon-plus"></i>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitPublish">发布教材</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { logoutAndBackToLogin } from '@/utils/auth.js'

const publishFormRef = ref(null)

// 发布表单
const publishForm = ref({
  bookName: '',
  author: '',
  originalPrice: '',
  sellPrice: '',
  description: ''
})

// 表单校验规则
const publishRules = ref({
  bookName: [{ required: true, message: '请输入教材名称', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者姓名', trigger: 'blur' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur', type: 'number' }],
  sellPrice: [{ required: true, message: '请输入售价', trigger: 'blur', type: 'number' }]
})

// 退出登录
const logout = () => {
  logoutAndBackToLogin()
}

// 文件上传（模拟）
const handleFileChange = (file) => {
  ElMessage.info(`已选择文件：${file.name}`)
}

// 提交发布
const submitPublish = async () => {
  try {
    await publishFormRef.value.validate()
    // 模拟发布成功
    ElMessage.success('教材发布成功！')
    // 重置表单
    publishFormRef.value.resetFields()
  } catch (error) {
    ElMessage.error('发布失败，请检查表单！')
  }
}

// 重置表单
const resetForm = () => {
  publishFormRef.value.resetFields()
}
</script>

<style scoped>
.publish-book {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
</style>