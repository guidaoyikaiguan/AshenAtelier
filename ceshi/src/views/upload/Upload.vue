<template>
  <div class="upload-page">
    <h2>提交旅途绘卷</h2>
    
    <div class="upload-container">
      <!-- 视频文件上传 -->
      <div class="upload-section">
        <h3>选择视频文件</h3>
        <div class="video-uploader">
          <el-button type="primary" @click="triggerFileInput" :disabled="uploading">
            <el-icon v-if="!uploading"><Upload /></el-icon>
            <el-icon v-else><Loading /></el-icon>
            {{ uploading ? '上传中...' : '选择视频文件' }}
          </el-button>
          <input 
            type="file" 
            ref="fileInput" 
            class="file-input" 
            accept="video/*" 
            @change="handleFileSelect"
          />
          <div class="el-upload__tip">
            支持 MP4、AVI、MOV 等格式，支持大文件上传
          </div>
        </div>
        
        <!-- 已选择文件信息 -->
        <div v-if="selectedFile" class="file-info">
          <el-icon><VideoCamera /></el-icon>
          <span>{{ selectedFile.name }}</span>
          <span class="file-size">({{ formatFileSize(selectedFile.size) }})</span>
          <el-button type="text" size="small" @click="cancelUpload" v-if="!uploading">取消</el-button>
        </div>
        
        <!-- 上传进度 -->
        <div v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress">
          <el-progress :percentage="uploadProgress" status="success" />
          <div class="progress-info">
            <span>上传进度: {{ uploadProgress }}%</span>
            <span>已上传: {{ formatFileSize(uploadedSize) }} / {{ formatFileSize(totalSize) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 视频信息填写 -->
      <div class="upload-section">
        <h3>填写视频信息</h3>
        <el-form :model="videoForm" :rules="rules" ref="videoFormRef" label-width="100px">
          <el-form-item label="视频标题" prop="title">
            <el-input v-model="videoForm.title" placeholder="请输入视频标题" maxlength="50" show-word-limit @input="fetchAISuggestedTags" @blur="fetchAISuggestedTags" />
          </el-form-item>

          <el-form-item label="视频描述" prop="description">
            <el-input type="textarea" v-model="videoForm.description" placeholder="请输入视频描述" :rows="4" maxlength="200" show-word-limit @input="fetchAISuggestedTags" @blur="fetchAISuggestedTags" />
          </el-form-item>
          
          <el-form-item label="视频分类" prop="categroyId">
            <el-select v-model="videoForm.categroyId" placeholder="请选择视频分类">
              <el-option v-for="category in categoryList" :key="category.categoryId" :label="category.categoryName" :value="category.categoryId" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="视频封面">
            <el-upload
              class="avatar-uploader"
              action=""
              :show-file-list="false"
              :http-request="uploadCover"
              :disabled="coverUploading"
              accept="image/*"
              :before-upload="beforeUploadCover"
            >
              <img v-if="videoForm.coverUrl" :src="videoForm.coverUrl" class="avatar" />
              <el-icon v-else><Upload /></el-icon>
            </el-upload>
            <div class="el-upload__tip">
              支持 JPG、PNG、GIF 等格式，建议尺寸 16:9
            </div>
          </el-form-item>
          
          <el-form-item label="标签">
            <div class="tags-input-container">
              <div class="tags-container">
                <span 
                  v-for="(tag, index) in videoForm.tags" 
                  :key="index" 
                  class="tag-item"
                >
                  {{ tag }}
                  <span class="tag-remove" @click="removeTag(index)">×</span>
                </span>
                <input
                  v-model="tagInput"
                  type="text"
                  class="tag-input"
                  placeholder="按回车键Enter创建标签"
                  @keyup.enter="addTag"
                  :disabled="videoForm.tags.length >= 10"
                />
              </div>
              <div class="tags-info">
                <span class="tags-count">还可以添加 {{ 10 - videoForm.tags.length }} 个标签</span>
              </div>
              <div class="recommended-tags">
                <span class="recommended-label">推荐标签：</span>
                <span
                  v-for="(recommendedTag, index) in recommendedTags"
                  :key="index"
                  class="recommended-tag"
                  @click="addRecommendedTag(recommendedTag)"
                  :disabled="videoForm.tags.length >= 10 || videoForm.tags.includes(recommendedTag)"
                >
                  {{ recommendedTag }}
                </span>
              </div>
              <div class="ai-suggested-tags" v-if="aiSuggestedTags.length > 0 || aiTagsLoading">
                <span class="ai-suggested-label">
                  <el-icon v-if="aiTagsLoading" class="is-loading"><Loading /></el-icon>
                  AI 推荐标签：
                </span>
                <span
                  v-for="(item, index) in aiSuggestedTags"
                  :key="index"
                  class="ai-suggested-tag"
                  @click="addAISuggestedTag(item)"
                >
                  {{ item.tag }}
                  <span class="ai-relevance">{{ Math.round(item.relevance * 100) }}%</span>
                </span>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提交旅途绘卷</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Loading, VideoCamera } from '@element-plus/icons-vue'
import { useLoginStore } from '@/stores/loginStore'
import { inject } from 'vue'
import { useRouter } from 'vue-router'

const Request = inject('Request')
const Api = inject('Api')
const loginStore = useLoginStore()
const router = useRouter()

// 上传组件引用
const videoFormRef = ref(null)
const fileInput = ref(null)

// 上传状态
const uploadProgress = ref(0)
const submitLoading = ref(false)
const uploading = ref(false)
const coverUploading = ref(false)

// 视频文件信息
const selectedFile = ref(null)
const uploadedSize = ref(0)
const totalSize = ref(0)
const fileHash = ref('')
const chunks = ref([])
const uploadedChunks = ref(new Set())

// 分片配置
const CHUNK_SIZE = 5 * 1024 * 1024; // 5MB per chunk
const CONCURRENT_UPLOADS = 3; // 并发上传数

// 上传请求头
const uploadHeaders = ref({});

// 更新上传请求头
const updateUploadHeaders = () => {
  const token = loginStore.getToken();
  if (token) {
    uploadHeaders.value['Authorization'] = 'Bearer ' + token;
  } else {
    delete uploadHeaders.value['Authorization'];
  }
};

// 初始化时更新请求头
updateUploadHeaders();

// 视频信息表单
const videoForm = reactive({
  title: '',
  description: '',
  categroyId: '',
  videoUrl: '',
  coverUrl: '',
  duration: '0', // 视频时长（秒）
  status: 0, // 0：待审核
  tags: [] // 视频标签
})

// 标签输入
const tagInput = ref('')

// 推荐标签
const recommendedTags = ref(['录屏', '直播', '记录', '自用', '屏幕录制', '游戏', '教程', '生活', '娱乐', '科技'])

// AI 建议标签
const aiSuggestedTags = ref([])
const aiTagsLoading = ref(false)
let aiTagsDebounceTimer = null

// 获取 AI 标签建议（防抖）
const fetchAISuggestedTags = () => {
  if (!videoForm.title || videoForm.title.trim().length < 2) {
    aiSuggestedTags.value = []
    return
  }
  clearTimeout(aiTagsDebounceTimer)
  aiTagsDebounceTimer = setTimeout(async () => {
    aiTagsLoading.value = true
    try {
      console.log('[AI Tags] Fetching suggestions for:', videoForm.title)
      const result = await Request({
        url: Api.aiTagsSuggest,
        method: 'GET',
        params: {
          title: videoForm.title,
          description: videoForm.description || ''
        }
      })
      console.log('[AI Tags] Response:', result)
      if (result && result.data && result.data.suggestions) {
        aiSuggestedTags.value = result.data.suggestions
        console.log('[AI Tags] Got', result.data.suggestions.length, 'suggestions')
      } else {
        console.log('[AI Tags] No suggestions in response. result:', result)
      }
    } catch (error) {
      console.error('[AI Tags] Failed:', error.message, error.response?.status, error.response?.data)
    } finally {
      aiTagsLoading.value = false
    }
  }, 1500) // 1.5秒防抖
}

// 添加 AI 建议标签
const addAISuggestedTag = (tagItem) => {
  const tag = typeof tagItem === 'string' ? tagItem : tagItem.tag
  if (videoForm.tags.length < 10 && !videoForm.tags.includes(tag)) {
    videoForm.tags.push(tag)
  }
}

// 添加标签
const addTag = () => {
  const tag = tagInput.value.trim()
  if (tag && videoForm.tags.length < 10 && !videoForm.tags.includes(tag)) {
    videoForm.tags.push(tag)
    tagInput.value = ''
  }
}

// 移除标签
const removeTag = (index) => {
  videoForm.tags.splice(index, 1)
}

// 添加推荐标签
const addRecommendedTag = (tag) => {
  if (videoForm.tags.length < 10 && !videoForm.tags.includes(tag)) {
    videoForm.tags.push(tag)
  }
}

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入视频标题', trigger: 'blur' }],
  categroyId: [{ required: true, message: '请选择视频分类', trigger: 'blur' }]
}

// 分类列表
const categoryList = ref([])

// 加载分类列表
const loadCategories = async () => {
  try {
    const result = await Request({
      url: Api.loadCategories
    })
    if (result && result.data) {
      categoryList.value = result.data
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    // 接口不存在时，使用默认分类
    categoryList.value = [
      { categoryId: 1, categoryName: '默认分类' }
    ]
  }
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value.click();
};

// 处理文件选择
const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  selectedFile.value = file;
  totalSize.value = file.size;
  uploadedSize.value = 0;
  uploadProgress.value = 0;
  
  // 自动填充视频标题（去除扩展名）
  const fileNameWithoutExt = file.name.substring(0, file.name.lastIndexOf('.'));
  videoForm.title = fileNameWithoutExt;
  
  // 计算视频时长
  calculateVideoDuration(file).then(duration => {
    videoForm.duration = Math.round(duration).toString(); // 四舍五入到整数秒并转换为字符串
    console.log('Video duration:', videoForm.duration, 'seconds');
  }).catch(error => {
    console.error('Failed to calculate video duration:', error);
    videoForm.duration = '0'; // 计算失败时设为0字符串
  });
  
  // 开始分片上传
  startChunkUpload(file);
};

// 计算视频时长
const calculateVideoDuration = (file) => {
  return new Promise((resolve, reject) => {
    const video = document.createElement('video');
    video.preload = 'metadata';
    
    video.onloadedmetadata = () => {
      resolve(video.duration);
    };
    
    video.onerror = () => {
      reject(new Error('Failed to load video metadata'));
    };
    
    video.src = URL.createObjectURL(file);
  });
};

// 取消上传
const cancelUpload = () => {
  selectedFile.value = null;
  uploadedSize.value = 0;
  totalSize.value = 0;
  uploadProgress.value = 0;
  fileHash.value = '';
  chunks.value = [];
  uploadedChunks.value = new Set();
  uploading.value = false;
  
  // 清空文件输入
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

// 计算文件哈希值（简化版，支持中文文件名）
const calculateFileHash = async (file) => {
  return new Promise((resolve) => {
    // 使用文件名、大小和时间戳生成简单的哈希值
    // 使用更安全的哈希生成方法，支持中文等非Latin1字符
    const hashInput = file.name + file.size + Date.now();
    // 使用简单的字符串哈希算法，避免btoa的编码问题
    let hash = 0;
    for (let i = 0; i < hashInput.length; i++) {
      const char = hashInput.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // 转换为32位整数
    }
    // 将哈希值转换为字符串，并确保长度一致
    const hashStr = Math.abs(hash).toString(36).padStart(16, '0').substring(0, 16);
    resolve(hashStr);
  });
};

// 分割文件
const splitFile = (file) => {
  const chunks = [];
  const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
  
  for (let i = 0; i < totalChunks; i++) {
    const start = i * CHUNK_SIZE;
    const end = Math.min(start + CHUNK_SIZE, file.size);
    const chunk = file.slice(start, end);
    
    chunks.push({
      index: i,
      start,
      end,
      size: end - start,
      chunk
    });
  }
  
  return chunks;
};

// 开始分片上传
const startChunkUpload = async (file) => {
  try {
    uploading.value = true;
    
    // 检查登录状态
    console.log('Login status:', loginStore.isLoggedIn);
    console.log('User info:', loginStore.userInfo);
    console.log('Token:', loginStore.getToken());
    
    if (!loginStore.isLoggedIn) {
      ElMessage.error('请先登录');
      uploading.value = false;
      return;
    }
    
    // 更新上传请求头
    updateUploadHeaders();
    console.log('Upload headers:', uploadHeaders.value);
    
    // 计算文件哈希值
    fileHash.value = await calculateFileHash(file);
    console.log('File hash:', fileHash.value);
    
    // 分割文件
    chunks.value = splitFile(file);
    console.log('Total chunks:', chunks.value.length);
    
    // 重置上传状态
    uploadedChunks.value = new Set();
    uploadedSize.value = 0;
    uploadProgress.value = 0;
    
    // 上传分片
    await uploadChunks();
    
    // 合并分片
    await mergeChunks();
  } catch (error) {
    console.error('Chunk upload failed:', error);
    ElMessage.error('上传失败，请重试');
    uploading.value = false;
  }
};

// 上传分片
const uploadChunks = async () => {
  // 重置上传状态
  uploadedChunks.value = new Set();
  uploadedSize.value = 0;
  uploadProgress.value = 0;
  
  // 创建分片上传任务
  const chunkTasks = chunks.value.map((chunk) => {
    return async () => {
      // 跳过已上传的分片
      if (uploadedChunks.value.has(chunk.index)) {
        return;
      }
      
      try {
        await uploadChunk(chunk);
        uploadedChunks.value.add(chunk.index);
        uploadedSize.value += chunk.size;
        uploadProgress.value = Math.round((uploadedSize.value / totalSize.value) * 100);
        console.log(`Chunk ${chunk.index} uploaded successfully`);
      } catch (error) {
        console.error(`Upload chunk ${chunk.index} failed:`, error);
        // 分片上传失败，抛出错误
        throw new Error(`Failed to upload chunk ${chunk.index}`);
      }
    };
  });
  
  // 并发上传分片，控制并发数
  const concurrentUpload = async (tasks, concurrency) => {
    const results = [];
    const executing = [];
    
    for (const task of tasks) {
      const p = Promise.resolve().then(() => task());
      results.push(p);
      
      if (concurrency <= tasks.length) {
        const e = p.then(() => {
          const index = executing.indexOf(e);
          if (index > -1) {
            executing.splice(index, 1);
          }
        });
        executing.push(e);
        
        if (executing.length >= concurrency) {
          await Promise.race(executing);
        }
      }
    }
    
    return Promise.all(results);
  };
  
  // 执行并发上传
  await concurrentUpload(chunkTasks, CONCURRENT_UPLOADS);
};

// 上传单个分片
const uploadChunk = async (chunk) => {
  try {
    const formData = new FormData();
    formData.append('file', chunk.chunk);
    formData.append('chunkIndex', chunk.index);
    formData.append('totalChunks', chunks.value.length);
    formData.append('fileHash', fileHash.value);
    formData.append('fileName', selectedFile.value.name);
    
    console.log(`Uploading chunk ${chunk.index} with headers:`, uploadHeaders.value);
    
    const response = await Request({
      url: '/api/video/uploadVideoChunk',
      method: 'POST',
      data: formData
    });
    
    console.log(`Response for chunk ${chunk.index}:`, response);
    
    // 检查后端返回的响应格式
    if (!response || !response.success) {
      throw new Error('Chunk upload failed');
    }
    
    return response;
  } catch (error) {
    console.error(`Error uploading chunk ${chunk.index}:`, error);
    throw new Error(`Chunk upload failed: ${error.message || error}`);
  }
};

// 合并分片
const mergeChunks = async () => {
  try {
    console.log('Merging chunks...');
    console.log('File hash:', fileHash.value);
    console.log('File name:', selectedFile.value.name);
    console.log('Total chunks:', chunks.value.length);
    
    const response = await Request({
      url: '/api/video/mergeVideoChunks',
      method: 'POST',
      data: {
        fileHash: fileHash.value,
        fileName: selectedFile.value.name,
        totalChunks: chunks.value.length
      }
    });
    
    console.log('Merge chunks response:', response);
    
    if (response && response.success) {
      console.log('Video URL:', response.data);
      videoForm.videoUrl = response.data;
      ElMessage.success('视频文件上传成功');
      uploading.value = false;
    } else {
      console.error('Merge chunks failed:', response);
      throw new Error('Merge chunks failed');
    }
  } catch (error) {
    console.error('Merge chunks failed:', error);
    ElMessage.error('上传失败，请重试');
    uploading.value = false;
  }
};

// 提交视频信息
const handleSubmit = async () => {
  if (!videoFormRef.value) return
  
  try {
    await videoFormRef.value.validate()
    if (!videoForm.videoUrl) {
      ElMessage.error('请先上传视频文件')
      return
    }
    
    // 将tags数组转换为逗号分隔的字符串
    const formData = {
      ...videoForm,
      tags: videoForm.tags.join(',')
    }
    
    submitLoading.value = true
    const result = await Request({
      url: Api.uploadVideo,
      method: 'post',
      data: formData
    })
    
    if (result && result.success) {
      ElMessage.success('视频上传成功，等待审核')
      resetForm()
    } else {
      ElMessage.error('视频上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

// 验证封面图片
const beforeUploadCover = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件！');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('封面图片大小不能超过 2MB！');
    return false;
  }
  return true;
};

// 上传封面图片
const uploadCover = async (file) => {
  try {
    coverUploading.value = true;
    
    const formData = new FormData();
    formData.append('file', file.file);
    
    const response = await Request({
      url: '/api/video/uploadVideoCover',
      method: 'POST',
      data: formData
    });
    
    if (response && response.success) {
      // 拼接完整的封面图片 URL
      videoForm.coverUrl = '' + response.data;
      ElMessage.success('封面上传成功');
    } else {
      ElMessage.error('封面上传失败，请重试');
    }
  } catch (error) {
    console.error('封面上传失败:', error);
    ElMessage.error('封面上传失败，请重试');
  } finally {
    coverUploading.value = false;
  }
};

// 重置表单
const resetForm = () => {
  if (videoFormRef.value) {
    videoFormRef.value.resetFields()
  }
  videoForm.videoUrl = ''
  videoForm.coverUrl = ''
  videoForm.duration = '0' // 重置视频时长为字符串
  selectedFile.value = null
  uploadedSize.value = 0
  totalSize.value = 0
  uploadProgress.value = 0
  fileHash.value = ''
  chunks.value = []
  uploadedChunks.value = new Set()
  uploading.value = false
  coverUploading.value = false
  
  // 清空文件输入
  if (fileInput.value) {
    fileInput.value.value = '';
  }
}

// 页面挂载时加载分类列表
onMounted(() => {
  // 确保登录状态
  if (!loginStore.isLoggedIn) {
    ElMessage.error('请先登录');
    // 可以跳转到登录页面
  }
  // 更新请求头
  updateUploadHeaders();
  // 加载分类列表
  loadCategories();
})
</script>

<style scoped>
.upload-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.upload-container {
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 24px;
}

.upload-section {
  margin-bottom: 30px;
}

.upload-section h3 {
  margin-bottom: 16px;
  color: #303133;
  font-size: 16px;
}

.video-uploader {
  margin-bottom: 20px;
  position: relative;
}

.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: -1;
}

.file-info {
  margin: 16px 0;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-size {
  color: #909399;
  font-size: 14px;
}

.upload-progress {
  margin-top: 16px;
}

.progress-info {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

/* 封面上传样式 */
.avatar-uploader {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 200px;
  height: 112px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader .avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-uploader .el-icon {
  font-size: 24px;
  color: #d9d9d9;
}

/* 标签输入样式 */
.tags-input-container {
  width: 100%;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 40px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background-color: #ecf5ff;
  color: #409eff;
  border-radius: 16px;
  font-size: 13px;
}

.tag-remove {
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
}

.tag-remove:hover {
  color: #66b1ff;
}

.tag-input {
  flex: 1;
  min-width: 150px;
  border: none;
  outline: none;
  font-size: 13px;
  padding: 4px 0;
}

.tag-input::placeholder {
  color: #c0c4cc;
}

.tags-info {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.recommended-tags {
  margin-top: 12px;
}

.recommended-label {
  font-size: 13px;
  color: #606266;
  margin-right: 8px;
}

.recommended-tag {
  display: inline-block;
  padding: 4px 12px;
  margin-right: 8px;
  margin-bottom: 8px;
  background-color: #f0f9eb;
  color: #67c23a;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.recommended-tag:hover {
  background-color: #e1f3d8;
}

.recommended-tag[disabled] {
  background-color: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

.ai-suggested-tags {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
}

.ai-suggested-label {
  font-size: 13px;
  color: #606266;
  margin-right: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
}

.ai-suggested-tag {
  display: inline-block;
  padding: 4px 12px;
  margin-right: 8px;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #f0f5ff, #e8f0fe);
  color: #5b7fdb;
  border: 1px solid #c5d5f2;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.ai-suggested-tag:hover {
  background: linear-gradient(135deg, #e0eaff, #d4e2fc);
  border-color: #a0b8e8;
}

.ai-relevance {
  margin-left: 4px;
  font-size: 11px;
  opacity: 0.7;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .upload-page {
    padding: 10px;
  }
  
  .upload-container {
    padding: 16px;
  }
  
  .progress-info {
    flex-direction: column;
    gap: 4px;
  }
  
  .avatar-uploader {
    width: 150px;
    height: 84px;
  }
}
</style>
