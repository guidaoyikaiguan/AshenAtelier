<template>
  <div class="publish-moment-page">
    <!-- 顶部导航栏 -->
    <Header />
    
    <!-- 左侧导航栏 -->
    <Sidebar />
    
    <!-- 主内容区 -->
    <div class="main-content">
      <div class="publish-container">
        <div class="publish-header">
          <h2>记录旅途见闻</h2>
        </div>
        
        <!-- 内容输入 -->
        <div class="content-input">
          <el-input 
            type="textarea" 
            placeholder="有什么想和大家分享的？" 
            :rows="5" 
            v-model="content"
          />
        </div>
        
        <!-- 图片上传 -->
        <div class="image-upload">
          <el-upload
            class="upload-demo"
            :action="qiniuUploadUrl"
            :data="uploadData"
            :headers="uploadHeaders"
            :multiple="true"
            :limit="9"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            list-type="picture-card"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                最多上传9张图片，单张不超过5MB
              </div>
            </template>
          </el-upload>
        </div>
        
        <!-- 发布按钮 -->
        <div class="publish-button">
          <el-button type="primary" @click="publishMoment">记录旅途见闻</el-button>
          <el-button @click="goBack">取消</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const Request = inject('Request')
const Api = inject('Api')

// 内容
const content = ref('')

// 图片URL列表
const imageUrls = ref([])

// 七牛云上传配置
const qiniuUploadUrl = ref('https://upload-z2.qiniup.com') // 华南地区上传地址
const uploadData = ref({})
const uploadHeaders = ref({})

// 上传前处理
const beforeUpload = async (file) => {
  // 验证文件大小
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过5MB')
    return false
  }
  
  // 验证文件类型
  const isImage = /^image\//.test(file.type)
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  
  // 获取七牛云上传令牌
  try {
    const result = await Request({
      url: '/api/moment/getQiniuToken' // 使用正确的后端接口路径
    })
    
    if (result && result.success && result.data) {
      uploadData.value = {
        token: result.data
      }
      ElMessage.success('获取上传令牌成功')
      return true
    } else {
      ElMessage.error('获取上传令牌失败')
      return false
    }
  } catch (error) {
    console.error('获取上传令牌失败:', error)
    ElMessage.error('获取上传令牌失败')
    return false
  }
}

// 上传成功处理
const handleUploadSuccess = (response, file, fileList) => {
  // 构建完整的图片URL
  const qiniuDomain = process.env.VUE_APP_QINIU_DOMAIN || 'http://tet31ocbs.hn-bkt.clouddn.com'
  const imageUrl = `${qiniuDomain}/${response.key}`
  imageUrls.value.push(imageUrl)
  ElMessage.success('图片上传成功')
}

// 上传失败处理
const handleUploadError = (error, file, fileList) => {
  ElMessage.error('图片上传失败')
}

// 发布动态
const publishMoment = async () => {
  if (!content.value.trim() && imageUrls.value.length === 0) {
    ElMessage.warning('请输入内容或上传图片')
    return
  }
  
  try {
    const result = await Request({
      url: '/api/moment/publishMoment', // 使用正确的后端接口路径
      method: 'POST',
      data: {
        content: content.value,
        images: imageUrls.value
      }
    })
    
    if (result && result.success) {
      ElMessage.success('动态发布成功')
      router.push('/moment')
    } else {
      ElMessage.error('动态发布失败')
    }
  } catch (error) {
    console.error('发布动态失败:', error)
    ElMessage.error('动态发布失败')
  }
}

// 返回
const goBack = () => {
  router.push('/moment')
}

onMounted(() => {
  // 页面加载时的初始化操作
})
</script>

<style lang="scss" scoped>
/* 主内容区 */
.main-content {
  margin-top: 60px; /* 60px header */
  margin-left: 200px; /* 200px sidebar */
  padding: 0 20px;
  min-height: calc(100vh - 60px);
}

/* 发布容器 */
.publish-container {
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  padding: 30px;
  margin: 20px auto;
  max-width: 800px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.publish-header {
  margin-bottom: 30px;
}

.publish-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

/* 内容输入 */
.content-input {
  margin-bottom: 30px;
}

/* 图片上传 */
.image-upload {
  margin-bottom: 30px;
}

/* 发布按钮 */
.publish-button {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
}
</style>