<!--
 * Copyright (C), 2024-2025, WiseBookPal Tech. Co., Ltd.
 * File name: PublishBook.vue
 * Author: WiseBookPal Team Version: 2.0 Date: 2026-01-08
 * Description: Premium page for sellers to publish new books.
 *              Features a 2-column layout with live preview and smart OCR.
-->
<template>
  <div class="publish-page">
    <div class="publish-container">
      
      <!-- Page Header -->
      <div class="page-header">
        <h2 class="page-title">发布闲置教材</h2>
        <p class="page-subtitle">填写教材信息，让知识继续传递</p>
      </div>

      <div class="content-wrapper">
        <!-- Left: Form Section -->
        <div class="form-section">
          <el-card class="form-card" shadow="never">
            <!-- Smart Import -->
            <div class="smart-import-box">
              <div class="import-header">
                <el-icon><MagicStick /></el-icon>
                <span>智能填表</span>
              </div>
              <div class="import-content">
                <el-input 
                  v-model="isbnInput" 
                  placeholder="输入 ISBN 码 (如 9787...)" 
                  class="isbn-input"
                  clearable
                >
                  <template #append>
                    <el-button @click="fetchBookByIsbn" :loading="fetchingIsbn">
                      <el-icon><Search /></el-icon> 自动获取
                    </el-button>
                  </template>
                </el-input>
                <p class="import-tip">输入 ISBN 码可自动填充书名、作者、封面等信息</p>
              </div>
            </div>

            <el-form 
              :model="publishForm" 
              :rules="publishRules" 
              ref="publishFormRef" 
              label-position="top"
              class="publish-form"
            >
              <!-- 1. Basic Info -->
              <h3 class="section-title">基本信息</h3>
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="教材名称" prop="bookName">
                    <el-input v-model="publishForm.bookName" placeholder="请输入完整书名" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="作者" prop="author">
                    <el-input v-model="publishForm.author" placeholder="请输入作者姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="出版社" prop="publisher">
                    <el-input v-model="publishForm.publisher" placeholder="请输入出版社" />
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 2. Price & Stock -->
              <h3 class="section-title">价格与库存</h3>
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="出售价格" prop="sellPrice">
                    <el-input-number 
                      v-model="publishForm.sellPrice" 
                      :min="0" 
                      :precision="2" 
                      :step="1"
                      class="full-width"
                    >
                      <template #prefix>¥</template>
                    </el-input-number>
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="教材原价" prop="originalPrice">
                    <el-input-number 
                      v-model="publishForm.originalPrice" 
                      :min="0" 
                      :precision="2" 
                      :step="1"
                      class="full-width"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="库存数量" prop="stock">
                    <el-input-number v-model="publishForm.stock" :min="1" :max="999" class="full-width" />
                  </el-form-item>
                </el-col>
              </el-row>

              <!-- 3. Condition & Description -->
              <h3 class="section-title">成色与描述</h3>
              <el-form-item label="新旧程度" prop="conditionLevel">
                <el-radio-group v-model="publishForm.conditionLevel" class="condition-group">
                  <el-radio-button label="全新">全新</el-radio-button>
                  <el-radio-button label="九成新">九成新</el-radio-button>
                  <el-radio-button label="八成新">八成新</el-radio-button>
                </el-radio-group>
                <el-button link type="primary" size="small" @click="showConditionStandard" style="margin-left: 10px;">
                  <el-icon><InfoFilled /></el-icon> 成色标准
                </el-button>
              </el-form-item>

              <el-form-item label="详细描述" prop="description">
                <el-input 
                  v-model="publishForm.description" 
                  type="textarea" 
                  rows="4" 
                  placeholder="请描述教材的具体状况，如是否有笔记、划痕、光盘等..."
                />
              </el-form-item>

              <!-- 4. Cover Image -->
              <h3 class="section-title">封面图片</h3>
              <el-form-item prop="coverUrl" class="cover-upload-item">
                <div class="upload-wrapper">
                  <el-upload
                    action="#"
                    list-type="picture-card"
                    :auto-upload="false"
                    :on-change="handleFileSelect"
                    :on-remove="handleRemove"
                    :file-list="fileList"
                    :limit="1"
                    class="cover-uploader"
                  >
                    <div class="upload-placeholder">
                      <el-icon class="upload-icon"><Camera /></el-icon>
                      <span>点击上传</span>
                    </div>
                  </el-upload>
                  
                  <div class="upload-actions" v-if="publishForm.coverUrl">
                    <el-button type="success" plain size="small" @click="recognizeBookInfo" :loading="recognizing">
                      <el-icon><View /></el-icon> 识别文字
                    </el-button>
                  </div>
                </div>
              </el-form-item>

              <!-- Submit Actions -->
              <div class="form-actions">
                <el-button size="large" @click="resetForm">重置</el-button>
                <el-button type="primary" size="large" @click="submitPublish" :loading="submitting" class="submit-btn">
                  立即发布
                </el-button>
              </div>
            </el-form>
          </el-card>
        </div>

        <!-- Right: Preview Section -->
        <div class="preview-section">
          <div class="preview-sticky">
            <h3 class="preview-title">实时预览</h3>
            <div class="preview-card-wrapper">
              <el-card class="book-card-preview" :body-style="{ padding: '0px' }">
                <div class="preview-cover">
                  <el-image 
                    v-if="publishForm.coverUrl" 
                    :src="publishForm.coverUrl" 
                    fit="cover" 
                    class="cover-img"
                  />
                  <div v-else class="empty-cover">
                    <span>封面预览</span>
                  </div>
                  <div class="condition-badge">{{ publishForm.conditionLevel || '二手' }}</div>
                </div>
                <div class="preview-info">
                  <h4 class="preview-book-name">{{ publishForm.bookName || '教材名称' }}</h4>
                  <p class="preview-author">{{ publishForm.author || '作者姓名' }}</p>
                  <div class="preview-price-row">
                    <span class="sell-price">¥{{ publishForm.sellPrice || '0.00' }}</span>
                    <span class="orig-price">¥{{ publishForm.originalPrice || '0.00' }}</span>
                  </div>
                </div>
              </el-card>
            </div>
            
            <div class="tips-card">
              <h4>💡 发布小贴士</h4>
              <ul>
                <li>上传清晰的封面能增加曝光率</li>
                <li>如实描述成色可减少交易纠纷</li>
                <li>合理定价能更快卖出哦</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Image Cropper Dialog -->
    <el-dialog
      v-model="cropperVisible"
      title="裁剪封面"
      width="600px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="cropper-container">
        <vue-cropper
          ref="cropperRef"
          :img="cropperImg"
          :outputSize="0.8"
          outputType="jpeg"
          :autoCrop="true"
          :autoCropWidth="300"
          :autoCropHeight="420"
          :fixedBox="false"
          :centerBox="true"
          mode="contain"
        ></vue-cropper>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cropperVisible = false">取消</el-button>
          <el-button type="primary" @click="finishCrop" :loading="uploadingCover">
            确认使用
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Camera, MagicStick, Search, InfoFilled, View } from '@element-plus/icons-vue'
import { addBook, uploadFile } from '@/api/bookApi'
import { recognizeText } from '@/api/ocrApi'
import { getBookInfoByIsbn } from '@/api/isbnApi'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'

const router = useRouter()
const publishFormRef = ref(null)
const fileList = ref([])
const uploadingCover = ref(false)
const recognizing = ref(false)
const fetchingIsbn = ref(false)
const submitting = ref(false)
const isbnInput = ref('')

// Cropper
const cropperVisible = ref(false)
const cropperImg = ref('')
const cropperRef = ref(null)

const publishForm = ref({
  bookName: '',
  author: '',
  publisher: '',
  originalPrice: undefined,
  sellPrice: undefined,
  description: '',
  conditionLevel: '九成新',
  stock: 1,
  coverUrl: '',
  isbn: ''
})

const publishRules = {
  bookName: [{ required: true, message: '请输入教材名称', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者姓名', trigger: 'blur' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'change' }],
  sellPrice: [{ required: true, message: '请输入售价', trigger: 'change' }],
  conditionLevel: [{ required: true, message: '请选择成色', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'change' }]
}

/* --- Handlers --- */

const handleFileSelect = (file) => {
  if (!file.raw) return
  cropperImg.value = URL.createObjectURL(file.raw)
  cropperVisible.value = true
  fileList.value = [] // clear pending
}

const finishCrop = () => {
  if (!cropperRef.value) return
  cropperRef.value.getCropBlob(async (blob) => {
    uploadingCover.value = true
    try {
      const fileToUpload = new File([blob], "cover.jpg", { type: "image/jpeg" })
      const res = await uploadFile(fileToUpload)
      if (res && res.url) {
        publishForm.value.coverUrl = res.url
        fileList.value = [{ name: 'cover.jpg', url: res.url }]
        ElMessage.success('封面已上传')
        cropperVisible.value = false
      }
    } catch (e) {
      ElMessage.error('上传失败，请重试')
    } finally {
      uploadingCover.value = false
    }
  })
}

const handleRemove = () => {
  publishForm.value.coverUrl = ''
  fileList.value = []
}

const recognizeBookInfo = async () => {
  if (!publishForm.value.coverUrl) return
  recognizing.value = true
  try {
    const response = await fetch(publishForm.value.coverUrl)
    const blob = await response.blob()
    const file = new File([blob], "ocr.jpg", { type: "image/jpeg" })
    const res = await recognizeText(file)
    if (res?.lines?.length) {
      if (!publishForm.value.bookName) publishForm.value.bookName = res.lines[0]
      publishForm.value.description = (publishForm.value.description ? publishForm.value.description + '\n' : '') + '识别信息：\n' + res.lines.join('\n')
      ElMessage.success('识别成功')
    } else {
      ElMessage.info('未识别到文字')
    }
  } catch (e) {
    ElMessage.error('识别失败')
  } finally {
    recognizing.value = false
  }
}

const fetchBookByIsbn = async () => {
  if (!isbnInput.value) return ElMessage.warning('请输入ISBN')
  fetchingIsbn.value = true
  try {
    const res = await getBookInfoByIsbn(isbnInput.value)
    if (res) {
      publishForm.value.isbn = isbnInput.value
      if (res.bookName) publishForm.value.bookName = res.bookName
      if (res.author) publishForm.value.author = res.author
      if (res.publisher) publishForm.value.publisher = res.publisher
      if (res.price) {
        const p = parseFloat(res.price.replace(/[^\d.]/g, ''))
        if (!isNaN(p)) publishForm.value.originalPrice = p
      }
      if (res.description) publishForm.value.description = res.description
      if (res.coverUrl) {
        publishForm.value.coverUrl = res.coverUrl
        fileList.value = [{ name: 'ISBN Cover', url: res.coverUrl }]
      }
      ElMessage.success('信息已自动填充')
    } else {
      ElMessage.info('未找到书籍信息')
    }
  } catch (e) {
    ElMessage.error('获取失败')
  } finally {
    fetchingIsbn.value = false
  }
}

const showConditionStandard = () => {
  ElMessageBox.alert(
    '全新：几乎未使用\n九成新：轻微使用痕迹\n八成新：有明显使用痕迹但不缺页',
    '成色标准',
    { confirmButtonText: '了解' }
  )
}

const submitPublish = async () => {
  try {
    await publishFormRef.value.validate()
    if (!publishForm.value.coverUrl) return ElMessage.warning('请上传封面')
    
    submitting.value = true
    const res = await addBook(publishForm.value)
    if (res?.id) {
      ElMessage.success('发布成功！')
      resetForm()
      router.push('/buyer/home')
    } else {
      throw new Error('Failed')
    }
  } catch (e) {
    ElMessage.error('发布失败，请检查表单')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  publishFormRef.value.resetFields()
  fileList.value = []
  publishForm.value.coverUrl = ''
  isbnInput.value = ''
}
</script>

<style scoped>
.publish-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding-bottom: 40px;
}

.publish-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 10px 0;
}

.page-subtitle {
  color: #909399;
  font-size: 16px;
}

.content-wrapper {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.form-section {
  flex: 2;
  min-width: 0;
}

.preview-section {
  flex: 1;
  min-width: 300px;
}

/* Smart Import Box */
.smart-import-box {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  border: 1px solid #c2e7b0;
}

.import-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #67C23A;
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 12px;
}

.import-tip {
  font-size: 12px;
  color: #606266;
  margin: 8px 0 0;
}

/* Form Styles */
.form-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 20px 0 20px;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
}

.full-width {
  width: 100%;
}

.upload-wrapper {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  height: 100%;
}

.upload-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.form-actions {
  margin-top: 40px;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.submit-btn {
  width: 160px;
  font-weight: bold;
}

/* Preview Styles */
.preview-sticky {
  position: sticky;
  top: 100px;
}

.preview-title {
  font-size: 18px;
  margin-bottom: 16px;
  color: #303133;
}

.book-card-preview {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}

.preview-cover {
  height: 280px;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.empty-cover {
  color: #909399;
  font-size: 14px;
}

.condition-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0,0,0,0.6);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.preview-info {
  padding: 16px;
}

.preview-book-name {
  font-size: 16px;
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.preview-author {
  font-size: 12px;
  color: #909399;
  margin: 0 0 12px;
}

.preview-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.sell-price {
  color: #F56C6C;
  font-size: 20px;
  font-weight: bold;
}

.orig-price {
  color: #C0C4CC;
  font-size: 12px;
  text-decoration: line-through;
}

.tips-card {
  margin-top: 20px;
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.tips-card h4 {
  margin: 0 0 12px;
  color: #E6A23C;
}

.tips-card ul {
  padding-left: 20px;
  margin: 0;
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}

.cropper-container {
  height: 400px;
}

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
  .preview-section {
    display: none; /* Hide preview on mobile to save space */
  }
}
</style>
