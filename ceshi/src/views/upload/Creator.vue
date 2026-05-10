<template>
  <div class="creator-page">
    <!-- 创作中心页面内容 -->
    <h2>绘卷工坊</h2>
    <p>创作者管理界面</p>
    
    <!-- 视频数量统计 -->
    <div class="video-stats">
      <h3>我的视频 ({{ videoCount }})</h3>
      <button class="refresh-btn" @click="loadVideos">刷新列表</button>
    </div>
    
    <!-- 视频列表 -->
    <div class="video-list" v-if="videos.length > 0">
      <div class="video-item" v-for="video in videos" :key="video.videoId">
        <div class="video-info">
          <div class="video-cover">
            <img :src="getVideoCoverUrl(video)" :alt="video.title">
          </div>
          <div class="video-details">
            <h4>{{ video.title }}</h4>
            <p class="video-meta">
              <span>播放量: {{ video.playCount || 0 }}</span>
              <span>发布时间: {{ formatDate(video.createTime) }}</span>
            </p>
          </div>
        </div>
        <div class="video-actions">
          <button class="edit-btn" @click="editVideo(video)">编辑</button>
          <button class="delete-btn" @click="deleteVideo(video.videoId)">删除</button>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <p>暂无视频，快去上传第一个视频吧！</p>
    </div>
    
    <!-- 编辑视频弹窗 -->
    <div class="edit-dialog" v-if="showEditDialog">
      <div class="edit-dialog-content">
        <h3>编辑视频</h3>
        <form @submit.prevent="updateVideo">
          <!-- 视频文件修改 -->
          <div class="form-group">
            <label>视频文件</label>
            <div class="file-upload">
              <input type="file" ref="videoFileInput" @change="handleVideoFileChange" accept="video/mp4,video/avi,video/mov">
              <button type="button" class="upload-btn" @click="$refs.videoFileInput.click()">
                选择视频文件
              </button>
              <p class="file-hint">支持MP4、AVI、MOV等格式，支持大文件上传</p>
              <p v-if="videoFile" class="file-name">{{ videoFile.name }}</p>
            </div>
          </div>
          
          <!-- 视频标题 -->
          <div class="form-group">
            <label for="video-title">* 视频标题</label>
            <div class="input-with-count">
              <input type="text" id="video-title" v-model="editForm.title" required maxlength="50">
              <span class="input-count">{{ (editForm.title || '').length }} / 50</span>
            </div>
          </div>
          
          <!-- 视频描述 -->
          <div class="form-group">
            <label for="video-description">视频描述</label>
            <div class="textarea-with-count">
              <textarea id="video-description" v-model="editForm.description" maxlength="200"></textarea>
              <span class="input-count">{{ (editForm.description || '').length }} / 200</span>
            </div>
          </div>
          
          <!-- 视频分类 -->
          <div class="form-group">
            <label for="video-category">* 视频分类</label>
            <select id="video-category" v-model="editForm.categoryId" required>
              <option value="">请选择视频分类</option>
              <option v-for="category in categories" :key="category.categoryId" :value="category.categoryId">
                {{ category.categoryName }}
              </option>
            </select>
          </div>
          
          <!-- 视频封面 -->
          <div class="form-group">
            <label>视频封面</label>
            <div class="cover-upload">
              <div class="cover-preview" :style="{ backgroundImage: coverUrl ? `url(${coverUrl})` : 'none' }">
                <input type="file" ref="coverFileInput" @change="handleCoverFileChange" accept="image/jpeg,image/png,image/gif" style="display: none;">
                <button type="button" class="cover-upload-btn" @click="$refs.coverFileInput.click()">
                  <span class="upload-icon">↑</span>
                </button>
              </div>
              <p class="cover-hint">支持JPG、PNG、GIF等格式，建议尺寸16:9</p>
            </div>
          </div>
          
          <!-- 标签 -->
          <div class="form-group">
            <label for="video-tags">标签</label>
            <div class="tags-input-container">
              <input 
                type="text" 
                id="video-tags" 
                v-model="tagInput" 
                @keydown.enter.prevent="addTag" 
                placeholder="按回车键Enter创建标签"
              >
              <div class="tags-list" v-if="tags.length > 0">
                <span class="tag-item" v-for="(tag, index) in tags" :key="index">
                  {{ tag }}
                  <button type="button" class="tag-remove" @click="removeTag(index)">×</button>
                </span>
              </div>
              <p class="tags-hint">还可以添加 {{ 10 - tags.length }} 个标签</p>
            </div>
            
            <!-- 推荐标签 -->
            <div class="recommended-tags" v-if="recommendedTags.length > 0">
              <span class="recommended-label">推荐标签：</span>
              <button 
                type="button" 
                class="recommended-tag" 
                v-for="tag in recommendedTags" 
                :key="tag"
                @click="addRecommendedTag(tag)"
                :disabled="tags.includes(tag) || tags.length >= 10"
              >
                {{ tag }}
              </button>
            </div>
          </div>
          
          <!-- 表单操作 -->
          <div class="form-actions">
            <button type="button" class="cancel-btn" @click="cancelEdit">取消</button>
            <button type="submit" class="submit-btn">保存修改</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { ElMessage } from 'element-plus'

// 注入全局工具
const Request = inject('Request', null)
const Api = inject('Api', null)

// 数据状态
const videos = ref([])
const videoCount = ref(0)
const showEditDialog = ref(false)
const editForm = ref({
  videoId: null,
  title: '',
  description: '',
  tags: '',
  categoryId: '',
  videoUrl: '',
  coverUrl: ''
})
const videoFile = ref(null)
const coverFile = ref(null)
const coverUrl = ref('')
const tagInput = ref('')
const tags = ref([])
const categories = ref([])
const recommendedTags = ref(['录屏', '直播', '记录', '自用', '屏幕录制', '游戏', '教程', '生活', '娱乐', '科技'])

// 加载视频列表
const loadVideos = async () => {
  try {
    // 调用后端API获取视频列表
    const response = await Request({
      url: `${Api.loadMyVideos}`,
      method: 'GET'
    })
    
    if (response && response.success) {
      videos.value = response.data
      // 加载视频数量
      await loadVideoCount()
    }
  } catch (error) {
    console.error('加载视频列表失败:', error)
    ElMessage.error('加载视频列表失败')
  }
}

// 加载视频数量
const loadVideoCount = async () => {
  try {
    // 调用后端API获取视频数量
    const response = await Request({
      url: `${Api.getMyVideoCount}`,
      method: 'GET'
    })
    
    if (response && response.success) {
      videoCount.value = response.data
    }
  } catch (error) {
    console.error('加载视频数量失败:', error)
  }
}

// 处理视频文件选择
const handleVideoFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    videoFile.value = file
    console.log('选择了视频文件:', file.name)
  }
}

// 处理封面文件选择
const handleCoverFileChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    coverFile.value = file
    // 预览封面
    const reader = new FileReader()
    reader.onload = (e) => {
      coverUrl.value = e.target.result
    }
    reader.readAsDataURL(file)
    console.log('选择了封面文件:', file.name)
  }
}

// 添加标签
const addTag = () => {
  try {
    const tag = tagInput.value.trim()
    if (tag && !tags.value.includes(tag) && tags.value.length < 10) {
      tags.value.push(tag)
      tagInput.value = ''
      console.log('添加标签:', tag)
    } else if (tags.value.includes(tag)) {
      console.log('标签已存在:', tag)
    } else if (tags.value.length >= 10) {
      console.log('标签数量已达上限')
    }
  } catch (error) {
    console.error('添加标签失败:', error)
  }
}

// 移除标签
const removeTag = (index) => {
  try {
    const removedTag = tags.value[index]
    tags.value.splice(index, 1)
    console.log('移除标签:', removedTag)
  } catch (error) {
    console.error('移除标签失败:', error)
  }
}

// 添加推荐标签
const addRecommendedTag = (tag) => {
  try {
    if (!tags.value.includes(tag) && tags.value.length < 10) {
      tags.value.push(tag)
      console.log('添加推荐标签:', tag)
    } else if (tags.value.includes(tag)) {
      console.log('推荐标签已存在:', tag)
    } else if (tags.value.length >= 10) {
      console.log('标签数量已达上限')
    }
  } catch (error) {
    console.error('添加推荐标签失败:', error)
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    console.log('开始加载分类列表...')
    const response = await Request({
      url: `${Api.loadCategories}`,
      method: 'GET'
    })
    if (response && response.success) {
      categories.value = response.data
      console.log('分类列表加载成功:', categories.value)
    } else {
      console.error('分类列表加载失败')
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 编辑视频
const editVideo = (video) => {
  // 初始化表单数据
  editForm.value = {
    videoId: video.videoId,
    title: video.title || '',
    description: video.description || '',
    tags: video.tags || '',
    categoryId: video.categoryId || video.categroyId || '',
    videoUrl: video.videoUrl || '',
    coverUrl: video.coverUrl || video.cover || ''
  }
  
  // 初始化标签
  if (video.tags) {
    let tagsStr = video.tags.toString().trim()
    if (tagsStr.startsWith('(') && tagsStr.endsWith(')')) {
      tagsStr = tagsStr.substring(1, tagsStr.length - 1)
    }
    tags.value = tagsStr.split(',').map(tag => tag.trim()).filter(tag => tag)
  } else {
    tags.value = []
  }
  
  // 初始化封面预览
  if (video.coverUrl || video.cover) {
    let coverPath = video.coverUrl || video.cover
    if (coverPath && !coverPath.startsWith('http')) {
      coverPath = `${coverPath}`
    }
    coverUrl.value = coverPath
  } else {
    coverUrl.value = ''
  }
  
  // 加载分类列表
  loadCategories()
  
  // 重置文件引用
  videoFile.value = null
  coverFile.value = null
  
  showEditDialog.value = true
}

// 更新视频
const updateVideo = async () => {
  try {
    // 处理标签
    editForm.value.tags = tags.value.join(',')
    
    // 上传视频文件（如果有）
    if (videoFile.value) {
      // 使用分块上传视频文件
      try {
        const file = videoFile.value
        const chunkSize = 1024 * 1024 * 5 // 5MB 每块
        const chunks = Math.ceil(file.size / chunkSize)
        const fileName = file.name
        const fileHash = Date.now() + '_' + fileName
        
        // 分块上传
        for (let i = 0; i < chunks; i++) {
          const start = i * chunkSize
          const end = Math.min(start + chunkSize, file.size)
          const chunk = file.slice(start, end)
          
          const formData = new FormData()
          formData.append('file', chunk)
          formData.append('chunkIndex', i)
          formData.append('totalChunks', chunks)
          formData.append('fileHash', fileHash)
          formData.append('fileName', fileName)
          
          await Request({
            url: `${Api.uploadVideoChunk}`,
            method: 'POST',
            headers: {
              'Content-Type': 'multipart/form-data'
            },
            data: formData
          })
        }
        
        // 合并分块
        const mergeResponse = await Request({
          url: `${Api.mergeVideoChunks}`,
          method: 'POST',
          data: {
            fileHash: fileHash,
            fileName: fileName,
            totalChunks: chunks
          }
        })
        
        if (mergeResponse && mergeResponse.success) {
          // 将相对路径拼接成完整的URL路径
          editForm.value.videoUrl = window.location.origin + mergeResponse.data
        } else {
          ElMessage.error('视频文件上传失败')
          return
        }
      } catch (error) {
        console.error('视频分块上传失败:', error)
        ElMessage.error('视频文件上传失败')
        return
      }
    }
    
    // 上传封面文件（如果有）
    if (coverFile.value) {
      // 调用上传封面文件接口
      const formData = new FormData()
      formData.append('file', coverFile.value)
      
      const coverUploadResponse = await Request({
        url: `${Api.uploadVideoCover}`, // 使用专门的封面上传接口
        method: 'POST',
        data: formData
      })
      
      if (coverUploadResponse && coverUploadResponse.success) {
        // 将相对路径拼接成完整的URL路径，使用固定的8080端口
        editForm.value.coverUrl = '' + coverUploadResponse.data
      } else {
        ElMessage.error('封面文件上传失败')
        return
      }
    }
    
    // 调用后端API更新视频
    const response = await Request({
      url: `${Api.updateMyVideo}`,
      method: 'POST',
      params: {
        videoId: editForm.value.videoId
      },
      data: editForm.value
    })
    
    if (response && response.success) {
      ElMessage.success('视频更新成功')
      showEditDialog.value = false
      // 重新加载视频列表
      await loadVideos()
    } else {
      ElMessage.error('视频更新失败')
    }
  } catch (error) {
    console.error('更新视频失败:', error)
    ElMessage.error('更新视频失败')
  }
}

// 删除视频
const deleteVideo = async (videoId) => {
  if (confirm('确定要删除这个视频吗？')) {
    try {
      // 调用后端API删除视频
      const response = await Request({
        url: `${Api.deleteMyVideo}`,
        method: 'POST',
        params: {
          videoId: videoId
        }
      })
      
      if (response && response.success) {
        ElMessage.success('视频删除成功')
        // 重新加载视频列表
        await loadVideos()
      } else {
        ElMessage.error('视频删除失败')
      }
    } catch (error) {
      console.error('删除视频失败:', error)
      ElMessage.error('删除视频失败')
    }
  }
}

// 获取视频封面URL
const getVideoCoverUrl = (video) => {
  let coverUrl = video.coverUrl || video.cover || ''
  if (coverUrl && !coverUrl.startsWith('http')) {
    coverUrl = `${coverUrl}`
  }
  return coverUrl || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=video%20thumbnail&image_size=landscape_4_3'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString()
}

// 取消编辑
const cancelEdit = () => {
  showEditDialog.value = false
  // 重置表单
  resetEditForm()
}

// 重置编辑表单
const resetEditForm = () => {
  editForm.value = {
    videoId: null,
    title: '',
    description: '',
    tags: '',
    categoryId: '',
    videoUrl: '',
    coverUrl: ''
  }
  videoFile.value = null
  coverFile.value = null
  coverUrl.value = ''
  tagInput.value = ''
  tags.value = []
}

// 页面挂载时加载数据
onMounted(() => {
  loadVideos()
})
</script>

<style scoped>
/* 创作中心页面样式 */
.creator-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
}

.video-stats {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  background-color: rgba(255,255,255,0.04);
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.video-count {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.video-count-label {
  font-size: 16px;
  font-weight: normal;
  color: #606266;
}

.upload-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
}

.upload-btn:hover {
  background-color: #66b1ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.video-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-top: 30px;
}

.video-item {
  background-color: rgba(255,255,255,0.04);
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.video-item:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}

.video-info {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  align-items: flex-start;
}

.video-cover {
  flex-shrink: 0;
}

.video-cover img {
  width: 120px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.video-item:hover .video-cover img {
  transform: scale(1.05);
}

.video-details {
  flex: 1;
  min-width: 0;
}

.video-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.video-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.video-meta-item {
  margin: 0;
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 6px;
}

.video-meta-icon {
  font-size: 12px;
}

.video-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.edit-btn, .delete-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  flex: 1;
  text-align: center;
}

.edit-btn {
  background-color: #ecf5ff;
  color: #409eff;
  border: 1px solid #d9ecff;
}

.edit-btn:hover {
  background-color: #d9ecff;
  color: #66b1ff;
  transform: translateY(-1px);
}

.delete-btn {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
}

.delete-btn:hover {
  background-color: #fbc4c4;
  color: #f78989;
  transform: translateY(-1px);
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #909399;
  font-size: 16px;
  background-color: rgba(255,255,255,0.04);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-top: 30px;
}

.empty-state-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.empty-state-text {
  margin: 0;
  font-size: 18px;
  color: #606266;
}

.empty-state-subtext {
  margin: 8px 0 24px 0;
  font-size: 14px;
  color: #909399;
}

/* 编辑弹窗样式 */
.edit-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.edit-dialog-content {
  background-color: rgba(255,255,255,0.04);
  border-radius: 16px;
  padding: 40px;
  width: 90%;
  max-width: 700px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  animation: dialogFadeIn 0.3s ease;
}

@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.edit-dialog-content h3 {
  margin: 0 0 30px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
  font-weight: 600;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  background-color: var(--bg-secondary);
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #409eff;
  background-color: rgba(255,255,255,0.04);
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 120px;
  line-height: 1.5;
}

.form-group select {
  background-color: rgba(255,255,255,0.04);
  cursor: pointer;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #f0f2f5;
}

.cancel-btn, .submit-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 100px;
}

.cancel-btn {
  background-color: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
}

.cancel-btn:hover {
  background-color: #e4e7ed;
  color: #303133;
  transform: translateY(-1px);
}

.submit-btn {
  background-color: #409eff;
  color: white;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
}

.submit-btn:hover {
  background-color: #66b1ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.submit-btn:active {
  transform: translateY(0);
}

/* 刷新按钮样式 */
.refresh-btn {
  padding: 12px 24px;
  background-color: #f5f7fa;
  color: #606266;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-block;
}

.refresh-btn:hover {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
  color: #409eff;
  transform: translateY(-1px);
}

/* 文件上传样式 */
.file-upload {
  margin-top: 12px;
}

.file-upload input[type="file"] {
  display: none;
}

.upload-btn {
  padding: 12px 24px;
  background-color: #f5f7fa;
  color: #606266;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-block;
}

.upload-btn:hover {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
  color: #409eff;
  transform: translateY(-1px);
}

.file-hint {
  margin: 12px 0 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.file-name {
  margin: 12px 0 0 0;
  font-size: 14px;
  color: #606266;
  word-break: break-all;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

/* 输入框带计数 */
.input-with-count,
.textarea-with-count {
  position: relative;
}

.input-count {
  position: absolute;
  right: 16px;
  bottom: 12px;
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

/* 封面上传样式 */
.cover-upload {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  margin-top: 12px;
}

.cover-preview {
  width: 240px;
  height: 135px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #f5f7fa;
}

.cover-preview:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
  transform: translateY(-2px);
}

.cover-upload-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  opacity: 0.8;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.cover-upload-btn:hover {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
}

.upload-icon {
  font-size: 28px;
}

.cover-hint {
  margin: 0;
  font-size: 13px;
  color: #909399;
  align-self: center;
  line-height: 1.4;
}

/* 标签输入样式 */
.tags-input-container {
  margin-top: 12px;
}

.tags-input-container input[type="text"] {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  background-color: var(--bg-secondary);
}

.tags-input-container input[type="text"]:focus {
  outline: none;
  border-color: #409eff;
  background-color: rgba(255,255,255,0.04);
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 16px 0;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  background-color: #ecf5ff;
  color: #409eff;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(64, 158, 255, 0.2);
  transition: all 0.3s ease;
}

.tag-item:hover {
  background-color: #d9ecff;
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

.tag-remove {
  margin-left: 8px;
  background: none;
  border: none;
  color: #409eff;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  font-weight: bold;
  transition: color 0.3s ease;
}

.tag-remove:hover {
  color: #66b1ff;
}

.tags-hint {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

/* 推荐标签样式 */
.recommended-tags {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.recommended-label {
  font-size: 14px;
  color: #606266;
  margin-right: 12px;
  font-weight: 600;
}

.recommended-tag {
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 16px;
  padding: 4px 14px;
  margin-right: 10px;
  margin-bottom: 10px;
  font-size: 13px;
  cursor: pointer;
  color: #606266;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.recommended-tag:hover:not(:disabled) {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
  color: #409eff;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.2);
}

.recommended-tag:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .creator-page {
    padding: 16px;
  }
  
  .video-stats {
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }
  
  .video-list {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .video-info {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .video-cover img {
    width: 100%;
    height: 180px;
  }
  
  .edit-dialog-content {
    padding: 24px;
    width: 95%;
  }
  
  .cover-upload {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .cover-preview {
    width: 100%;
    height: 180px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .cancel-btn, .submit-btn {
    width: 100%;
  }
}
</style>
