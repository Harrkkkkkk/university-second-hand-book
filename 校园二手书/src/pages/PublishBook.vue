<template>
  <div class="publish-book">
    <page-header title="发布二手教材" :goBack="goBack">
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
          <el-input-number v-model="publishForm.originalPrice" :min="1" :max="9999" :step="1" placeholder="请输入教材原价" style="width: 100%" />
        </el-form-item>
        <el-form-item label="售价" prop="sellPrice">
          <el-input-number v-model="publishForm.sellPrice" :min="1" :max="9999" :step="1" placeholder="请输入出售价格" style="width: 100%" />
        </el-form-item>
        <el-form-item label="成色" prop="conditionLevel">
          <el-select v-model="publishForm.conditionLevel" placeholder="请选择成色">
            <el-option label="全新" value="全新" />
            <el-option label="九成新" value="九成新" />
            <el-option label="八成新" value="八成新" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="publishForm.stock" :min="1" :max="999" :step="1" style="width: 100%" />
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
              :disabled="uploadingCover"
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { addBook, uploadFile } from '@/api/bookApi'

const router = useRouter()
const goBack = () => router.back()
const publishFormRef = ref(null)
const fileList = ref([])
const uploadingCover = ref(false)

// 发布表单
const publishForm = ref({
  bookName: '',
  author: '',
  originalPrice: 1,
  sellPrice: 1,
  description: '',
  conditionLevel: '九成新',
  stock: 1,
  coverUrl: ''
})

// 表单校验规则
const publishRules = ref({
  bookName: [{ required: true, message: '请输入教材名称', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者姓名', trigger: 'blur' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'change' }],
  sellPrice: [{ required: true, message: '请输入售价', trigger: 'change' }],
  conditionLevel: [{ required: true, message: '请选择成色', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'change' }]
})

const handleFileChange = async (file) => {
  const raw = file?.raw
  if (!raw) return
  uploadingCover.value = true
  try {
    const res = await uploadFile(raw)
    if (res && res.url) {
      publishForm.value.coverUrl = res.url
      ElMessage.success('封面上传成功')
    } else {
      ElMessage.error('封面上传失败')
    }
  } catch (e) {
    ElMessage.error('封面上传失败')
  } finally {
    uploadingCover.value = false
  }
}

const submitPublish = async () => {
  try {
    await publishFormRef.value.validate()
    const res = await addBook(publishForm.value)
    if (!res || !res.id) {
      ElMessage.error('发布失败')
      return
    }
    ElMessage.success('教材发布成功！')
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
