<template>
  <div class="admin-dashboard">
    <!-- 管理员头部导航 -->
    <header class="admin-header">
      <div class="header-left">
        <h1>视频平台管理系统</h1>
      </div>
      <div class="header-right">
        <span class="admin-name">{{ adminInfo?.username || '管理员' }}</span>
        <el-button type="text" @click="handleLogout">退出登录</el-button>
      </div>
    </header>
    
    <!-- 侧边导航栏 -->
    <div class="admin-body">
      <aside class="admin-sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="video-audit">
            <el-icon><VideoCamera /></el-icon>
            <span>视频审核</span>
          </el-menu-item>
          <el-menu-item index="user-management">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="category-management">
            <el-icon><Grid /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="carousel-management">
            <el-icon><Picture /></el-icon>
            <span>轮播图管理</span>
          </el-menu-item>
        </el-menu>
      </aside>
      
      <!-- 主内容区域 -->
      <main class="admin-content">

        
        <!-- 视频审核 -->
        <div v-if="activeMenu === 'video-audit'" class="content-section">
          <h2>视频审核</h2>
          <el-table :data="videoList" style="width: 100%">
            <el-table-column prop="videoId" label="视频ID" width="100" />
            <el-table-column prop="title" label="视频标题" min-width="200">
              <template #default="scope">
                <span class="video-title" @click="viewVideo(scope.row)">{{ scope.row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="uploader" label="上传者" width="120" />
            <el-table-column prop="uploadTime" label="上传时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === '0' ? 'warning' : scope.row.status === '1' ? 'success' : 'danger'">
                  {{ scope.row.status === '0' ? '待审核' : scope.row.status === '1' ? '已通过' : '已拒绝' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleApprove(scope.row)">通过</el-button>
                <el-button type="danger" size="small" @click="handleReject(scope.row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination" style="margin-top: 20px">
            <el-pagination
              layout="prev, pager, next, jumper, ->, total"
              :total="total"
              :page-size="pageSize"
              v-model:current-page="currentPage"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
        
        <!-- 用户管理 -->
        <div v-if="activeMenu === 'user-management'" class="content-section">
          <h2>用户管理</h2>
          
          <!-- 用户列表 -->
          <el-table :data="userList" style="width: 100%">
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column label="用户头像" width="100">
              <template #default="scope">
                <el-avatar :src="scope.row.avatar" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="nickName" label="用户名称" min-width="150" />
            <el-table-column prop="state" label="用户状态" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.state === 1 ? 'success' : 'danger'">
                  {{ scope.row.state === 1 ? '正常' : '封禁' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column prop="lastLoginTime" label="最后登录时间" width="180" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button 
                  :type="scope.row.state === 1 ? 'danger' : 'primary'" 
                  size="small" 
                  @click="handleUserStateChange(scope.row)"
                >
                  {{ scope.row.state === 1 ? '封禁' : '解禁' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination" style="margin-top: 20px">
            <el-pagination
              layout="prev, pager, next, jumper, ->, total"
              :total="userTotal"
              :page-size="userPageSize"
              v-model:current-page="userCurrentPage"
              @current-change="handleUserCurrentChange"
            />
          </div>
        </div>
        
        <!-- 分类管理 -->
        <div v-if="activeMenu === 'category-management'" class="content-section">
          <h2>分类管理</h2>
          
          <!-- 操作按钮 -->
          <div class="action-buttons" style="margin-bottom: 20px;">
            <el-button type="primary" @click="handleAddCategory">
              <el-icon><Plus /></el-icon>
              新增分类
            </el-button>
          </div>
          
          <!-- 分类列表 -->
          <el-table :data="categoryList" style="width: 100%">
            <el-table-column prop="categoryId" label="分类ID" width="80" />
            <el-table-column prop="categoryName" label="分类名称" min-width="150" />
            <el-table-column prop="description" label="分类描述" min-width="200" />
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                  {{ scope.row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEditCategory(scope.row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteCategory(scope.row)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="pagination" style="margin-top: 20px;">
            <el-pagination
              layout="prev, pager, next, jumper, ->, total"
              :total="categoryTotal"
              :page-size="categoryPageSize"
              v-model:current-page="categoryCurrentPage"
              @current-change="handleCategoryCurrentChange"
            />
          </div>
          
          <!-- 新增/编辑分类对话框 -->
          <el-dialog
            v-model="categoryDialogVisible"
            :title="isEditCategory ? '编辑分类' : '新增分类'"
            width="500px"
          >
            <el-form :model="categoryForm" :rules="categoryRules" ref="categoryFormRef" label-width="100px">
              <el-form-item label="分类名称" prop="categoryName">
                <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" maxlength="50" show-word-limit />
              </el-form-item>
              <el-form-item label="分类描述" prop="description">
                <el-input type="textarea" v-model="categoryForm.description" placeholder="请输入分类描述" :rows="3" maxlength="200" show-word-limit />
              </el-form-item>
              <el-form-item label="排序" prop="sortOrder">
                <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="999" placeholder="请输入排序权重" />
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-switch v-model="categoryForm.status" active-value="1" inactive-value="0" />
              </el-form-item>
            </el-form>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="categoryDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSaveCategory">保存</el-button>
              </span>
            </template>
          </el-dialog>
        </div>
        

        
        <!-- 轮播图管理 -->
        <div v-if="activeMenu === 'carousel-management'" class="content-section">
          <h2>轮播图管理</h2>
          <CarouselManager />
        </div>
        
        <!-- 视频查看弹窗 -->
        <el-dialog
          v-model="videoDialogVisible"
          title="视频查看"
          width="800px"
          destroy-on-close
        >
          <div v-if="currentVideo" class="video-viewer">
            <div class="video-player">
              <video
                v-if="currentVideo.videoUrl"
                :src="getFullVideoUrl(currentVideo.videoUrl)"
                controls
                width="100%"
                height="450px"
              >
                您的浏览器不支持视频播放
              </video>
              <div v-else class="no-video">
                <el-icon class="no-video-icon"><VideoCamera /></el-icon>
                <p>视频文件不存在或无法访问</p>
                <p>视频ID: {{ currentVideo.videoId }}</p>
              </div>
            </div>
            <div class="video-info">
              <h3>{{ currentVideo.title }}</h3>
              <p><strong>上传者:</strong> {{ currentVideo.uploader }}</p>
              <p><strong>上传时间:</strong> {{ currentVideo.uploadTime }}</p>
              <p><strong>状态:</strong> {{ currentVideo.status === '0' ? '待审核' : currentVideo.status === '1' ? '已通过' : '已拒绝' }}</p>
              <p><strong>视频ID:</strong> {{ currentVideo.videoId }}</p>
              <p v-if="currentVideo.videoUrl"><strong>视频URL:</strong> {{ getFullVideoUrl(currentVideo.videoUrl) }}</p>
            </div>
          </div>
          <div v-else class="loading">
            加载中...
          </div>
        </el-dialog>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { VideoCamera, User, Grid, Plus, Edit, Delete, Picture } from '@element-plus/icons-vue'
import { inject } from 'vue'
import CarouselManager from './CarouselManager.vue'

const router = useRouter()
const Request = inject('Request')
const Api = inject('Api')

// 管理员信息
const adminInfo = ref(JSON.parse(localStorage.getItem('adminInfo') || '{"username": "管理员"}'))

// 当前激活的菜单
const activeMenu = ref('video-audit')

// 视频列表数据
const videoList = ref([])
// 视频查看弹窗
const videoDialogVisible = ref(false)
const currentVideo = ref(null)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(3)

// 分类管理相关数据
const categoryList = ref([])
const categoryCurrentPage = ref(1)
const categoryPageSize = ref(10)
const categoryTotal = ref(0)
const categoryDialogVisible = ref(false)
const isEditCategory = ref(false)
const categoryFormRef = ref(null)
const categoryForm = reactive({
  categoryId: null,
  categoryName: '',
  description: '',
  sortOrder: 0,
  status: 1
})
const categoryRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// 用户管理相关数据
const userList = ref([])
const userCurrentPage = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)

// 检查登录状态
const checkLoginStatus = () => {
  const adminInfo = localStorage.getItem('adminInfo')
  if (!adminInfo) {
    ElMessage.error('请先登录')
    router.push('/admin/login')
    return false
  }
  return true
}

// 处理菜单选择
const handleMenuSelect = (key) => {
  activeMenu.value = key
  if (key === 'category-management') {
    loadCategoryList()
  } else if (key === 'video-audit') {
    loadVideoList()
  } else if (key === 'user-management') {
    loadUserList()
  }
}

// 加载视频列表
const loadVideoList = async () => {
  try {
    const result = await Request({
      url: '/api/admin/getAllAdmin',
      method: 'GET',
      params: {
        pageNo: currentPage.value
      }
    })
    if (result && result.success && result.data) {
      // 对视频列表进行处理，添加上传者和上传时间字段
      videoList.value = result.data.list.map(video => {
        return {
          ...video,
          uploader: video.nickName || `用户${video.userId}`, // 使用nickName作为上传者名称，如果没有则使用userId
          uploadTime: video.createTime // 使用createTime作为上传时间
        }
      })
      total.value = result.data.totalCount
    }
  } catch (error) {
    console.error('加载视频列表失败:', error)
    ElMessage.error('加载视频列表失败')
  }
}

// 加载分类列表
const loadCategoryList = async () => {
  try {
    const result = await Request({
      url: '/api/category/admin/list'
    })
    if (result && result.success) {
      categoryList.value = result.data
      categoryTotal.value = result.data.length
    }
  } catch (error) {
    console.error('加载分类列表失败:', error)
    ElMessage.error('加载分类列表失败')
  }
}

// 加载用户列表
const loadUserList = async () => {
  try {
    const result = await Request({
      url: '/api/admin/getAllUser',
      method: 'GET',
      params: {
        pageNo: userCurrentPage.value
      }
    })
    if (result && result.success && result.data) {
      userList.value = result.data.list
      userTotal.value = result.data.totalCount
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  }
}

// 处理新增分类
const handleAddCategory = () => {
  isEditCategory.value = false
  Object.assign(categoryForm, {
    categoryId: null,
    categoryName: '',
    description: '',
    sortOrder: 0,
    status: 1
  })
  categoryDialogVisible.value = true
}

// 处理编辑分类
const handleEditCategory = (category) => {
  isEditCategory.value = true
  Object.assign(categoryForm, category)
  categoryDialogVisible.value = true
}

// 处理删除分类
const handleDeleteCategory = async (category) => {
  try {
    const result = await Request({
      url: '/api/category/admin/delete',
      params: { categoryId: category.categoryId }
    })
    if (result && result.success) {
      ElMessage.success('删除分类成功')
      loadCategoryList()
    } else {
      ElMessage.error('删除分类失败')
    }
  } catch (error) {
    console.error('删除分类失败:', error)
    ElMessage.error('删除分类失败')
  }
}

// 处理保存分类
const handleSaveCategory = async () => {
  if (!categoryFormRef.value) return
  
  try {
    await categoryFormRef.value.validate()
    
    const url = isEditCategory.value ? '/api/category/admin/update' : '/api/category/admin/add'
    const result = await Request({
      url: url,
      method: 'post',
      data: categoryForm
    })
    
    if (result && result.success) {
      ElMessage.success(isEditCategory.value ? '修改分类成功' : '新增分类成功')
      categoryDialogVisible.value = false
      loadCategoryList()
    } else {
      ElMessage.error(isEditCategory.value ? '修改分类失败' : '新增分类失败')
    }
  } catch (error) {
    console.error('保存分类失败:', error)
    ElMessage.error('保存分类失败')
  }
}

// 分类分页处理
const handleCategoryCurrentChange = (val) => {
  categoryCurrentPage.value = val
  loadCategoryList()
}

// 处理登出
const handleLogout = async () => {
  try {
    // 调用后端退出登录接口
    const result = await Request({
      url: Api.adminLogout,
      method: 'post'
    })
    
    // 清除本地存储中的管理员信息
    localStorage.removeItem('adminInfo')
    ElMessage.success('退出登录成功')
    router.push('/admin/login')
  } catch (error) {
    console.error('退出登录失败:', error)
    // 即使后端调用失败，也要清除本地存储并跳转到登录页面
    localStorage.removeItem('adminInfo')
    ElMessage.success('退出登录成功')
    router.push('/admin/login')
  }
}

// 处理视频审核通过
const handleApprove = async (video) => {
  try {
    const result = await Request({
      url: '/api/video/updateVideoStatus',
      method: 'post',
      data: {
        videoId: video.videoId,
        status: '1' // 1：已通过
      }
    })
    if (result && result.success) {
      ElMessage.success(`视频 "${video.title}" 审核通过`)
      video.status = '1'
    }
  } catch (error) {
    console.error('审核视频失败:', error)
    ElMessage.error('审核视频失败')
  }
}

// 处理视频审核拒绝
const handleReject = async (video) => {
  try {
    const result = await Request({
      url: '/api/video/updateVideoStatus',
      method: 'post',
      data: {
        videoId: video.videoId,
        status: '2' // 2：已拒绝
      }
    })
    if (result && result.success) {
      ElMessage.error(`视频 "${video.title}" 审核拒绝`)
      video.status = '2'
    }
  } catch (error) {
    console.error('审核视频失败:', error)
    ElMessage.error('审核视频失败')
  }
}

// 分页处理
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadVideoList()
}

// 用户管理分页处理
const handleUserCurrentChange = (val) => {
  userCurrentPage.value = val
  loadUserList()
}

// 获取完整的视频URL
const getFullVideoUrl = (videoUrl) => {
  if (!videoUrl) return ''
  
  // 如果是相对路径，添加完整的服务器地址
  if (videoUrl.startsWith('/')) {
    return `${videoUrl}`
  }
  
  // 如果是完整的URL，直接返回
  if (videoUrl.startsWith('http://') || videoUrl.startsWith('https://')) {
    return videoUrl
  }
  
  // 其他情况，返回原始URL
  return videoUrl
}

// 查看视频
const viewVideo = (video) => {
  if (video.videoId) {
    // 在管理员端内部打开视频查看弹窗
    currentVideo.value = video
    videoDialogVisible.value = true
  }
}

// 处理用户状态变更（封禁/解禁）
const handleUserStateChange = async (user) => {
  try {
    const url = user.state === 1 ? '/api/admin/banUser' : '/api/admin/unbanUser'
    const result = await Request({
      url: url,
      method: 'GET',
      params: {
        userId: user.userId
      }
    })
    if (result && result.success) {
      ElMessage.success(result.message || (user.state === 1 ? '封禁成功' : '解禁成功'))
      // 重新加载用户列表
      loadUserList()
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 页面加载时检查登录状态
onMounted(() => {
  if (checkLoginStatus()) {
    // 加载视频审核数据
    loadVideoList()
  }
})
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.admin-header {
  background-color: var(--color-primary-light);
  color: white;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.header-left h1 {
  font-size: 20px;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-name {
  font-size: 14px;
}

.admin-body {
  display: flex;
  min-height: calc(100vh - 64px);
}

.admin-sidebar {
  width: 200px;
  background-color: rgba(255,255,255,0.04);
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.content-section {
  background-color: rgba(255,255,255,0.04);
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.content-section h2 {
  margin-bottom: 24px;
  color: #303133;
  font-size: 18px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: var(--color-primary-light);
  margin: 10px 0;
}

.stat-desc {
  font-size: 14px;
  color: #909399;
}

.video-title {
  color: var(--color-primary-light);
  cursor: pointer;
  text-decoration: underline;
  &:hover {
    color: #40a9ff;
  }
}

.video-viewer {
  .video-player {
    margin-bottom: 20px;
    border: 1px solid var(--glass-border);
    border-radius: 4px;
    overflow: hidden;
  }
  
  .no-video {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 450px;
    background-color: var(--bg-secondary);
    color: #909399;
    
    .no-video-icon {
      font-size: 60px;
      margin-bottom: 20px;
    }
    
    p {
      margin: 5px 0;
    }
  }
  
  .video-info {
    padding: 10px;
    background-color: var(--bg-secondary);
    border-radius: 4px;
    
    h3 {
      margin-top: 0;
      margin-bottom: 10px;
      color: #303133;
    }
    
    p {
      margin: 5px 0;
      color: #606266;
      word-break: break-all;
    }
  }
}

.loading {
  text-align: center;
  padding: 50px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 180px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .admin-sidebar {
    width: 100%;
    height: auto;
  }
  
  .admin-body {
    flex-direction: column;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>