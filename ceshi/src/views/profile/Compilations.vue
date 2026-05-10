<template>
  <div class="compilations-page" :style="{ backgroundImage: 'url(/images/profile_bg.png)' }">
    <!-- 顶部导航栏 -->
    <Header />
    
    <!-- 主内容区 -->
    <div class="main-content">
      <div class="profile-container">
        <!-- 用户信息头 -->
        <div class="profile-header">
                    <div class="user-info">
            <div class="avatar-name">
                <el-avatar :size="100" :src="getAvatarUrl(userInfo.avatar)">
                  {{ userInfo.nickName?.charAt(0) || '用' }}
                </el-avatar>
              <div class="user-details">
                <h2 class="nickname">
                  {{ userInfo.nickName || '未设置昵称' }}
                  <span class="level">Lv.{{ userInfo.level || 1 }}</span>
                </h2>
                <p class="signature">{{ userInfo.signature || '未设置签名' }}</p>
                <div class="user-meta">
                  <el-tag v-if="userInfo.isVip" type="warning" size="small">大会员</el-tag>
                </div>
                <div class="user-stats">
                  <a href="#" class="stat-item" @click.prevent="goToFollowing">
                    <el-icon><User /></el-icon>
                    <span>关注数: {{ userInfo.followingCount || 0 }}</span>
                  </a>
                  <span class="stat-separator">•</span>
                  <a href="#" class="stat-item" @click.prevent="goToFollowers">
                    <el-icon><View /></el-icon>
                    <span>粉丝数: {{ userInfo.followerCount || 0 }}</span>
                  </a>
                  <span class="stat-separator">•</span>
                  <span class="stat-item">
                    <el-icon><Like /></el-icon>
                    <span>获赞数: {{ userInfo.likeCount || 0 }}</span>
                  </span>
                  <span class="stat-separator">•</span>
                  <span class="stat-item">
                    <el-icon><Star /></el-icon>
                    <span>播放数: {{ userInfo.playCount || 0 }}</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 内容导航标签 -->
        <div class="content-tabs">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="萤石印记" name="home"></el-tab-pane>
            <el-tab-pane label="旅人见闻" name="dynamic"></el-tab-pane>
            <el-tab-pane label="旅途绘卷" name="videos"></el-tab-pane>
            <el-tab-pane label="魔法集录" name="compilations"></el-tab-pane>
            <el-tab-pane label="旅途珍藏" name="collect"></el-tab-pane>
            <el-tab-pane v-if="!routeUserId" label="魔女工坊" name="settings"></el-tab-pane>
          </el-tabs>
        </div>
        
        <!-- 内容区域 -->
        <div class="content-area">
          <!-- 左侧内容 -->
          <div class="left-content">
            <!-- 合集管理区 -->
            <div class="compilations-management">
              <div class="section-header">
                <h3 class="section-title">{{ routeUserId ? 'TA的魔法集录' : '我的魔法集录' }}</h3>
                <el-button v-if="!routeUserId" type="primary" @click="openCreateCompilationDialog">
                  <el-icon><Plus /></el-icon> 编织新的魔法集录
                </el-button>
              </div>
              
              <!-- 合集列表 -->
              <div class="compilation-list">
                <el-empty v-if="compilationList.length === 0" description="暂无合集"></el-empty>
                <div v-for="compilation in compilationList" :key="compilation.compilationsId" class="compilation-item">
                  <div class="compilation-info">
                    <h4 class="compilation-name">{{ compilation.compilationsName }}</h4>
                    <p class="compilation-stats">收录 {{ compilation.videoCount }} 卷绘卷</p>
                  </div>
                  <div class="compilation-actions">
                    <el-button v-if="!routeUserId" type="primary" size="small" @click="openAddVideoDialog(compilation)">
                      <el-icon><Plus /></el-icon> 添加绘卷
                    </el-button>
                    <el-button type="default" size="small" @click="viewCompilationVideos(compilation)">
                      <el-icon><View /></el-icon> 阅览绘卷
                    </el-button>
                    <el-button v-if="!routeUserId" type="danger" size="small" @click="deleteCompilation(compilation)">
                      <el-icon><Delete /></el-icon> 解咒
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 合集中的视频列表 -->
            <div v-if="currentViewingCompilation" class="compilation-videos">
              <div class="section-header">
                <h3 class="section-title">{{ currentViewingCompilation.compilationsName }} <span class="video-count">({{ currentViewingCompilation.videoCount }}个视频)</span></h3>
                <el-button type="default" @click="closeCompilationVideos">
                  <el-icon><Close /></el-icon> 关闭
                </el-button>
              </div>
              
              <!-- 视频列表 -->
              <div class="video-list">
                <el-empty v-if="compilationVideos.length === 0" description="该合集中暂无视频"></el-empty>
                <div v-for="video in compilationVideos" :key="video.videoId" class="video-item" @click="goToVideoDetail(video)">
                  <div class="video-thumbnail">
                    <img :src="getVideoCoverUrl(video)" alt="视频封面" class="video-cover">
                    <span class="video-duration">{{ video.duration }}</span>
                  </div>
                  <div class="video-info-container">
                    <div class="video-title-info">
                      <h4 class="video-title">{{ video.videoName || video.title || '无标题' }}</h4>
                    </div>
                    <div class="video-stats-info">
                      <p class="video-stats">{{ video.playCount }}播放 · {{ video.likeCount }}点赞 · {{ formatDate(video.createTime) }}</p>
                    </div>
                  </div>
                  <div class="video-actions">
                    <el-button v-if="!routeUserId" type="danger" size="small" @click.stop="deleteVideoFromCompilation(video)">
                      <el-icon><Delete /></el-icon> 解咒
                    </el-button>
                  </div>
                </div>
              </div>
              
              <!-- 分页组件 -->
              <div v-if="compilationVideos.length > 0" class="pagination">
                <el-pagination
                  v-model:current-page="currentPage"
                  :page-size="pageSize"
                  layout="prev, pager, next"
                  :total="currentViewingCompilation?.videoCount || 0"
                  @current-change="handlePageChange"
                />
              </div>
            </div>
          </div>
          
          <!-- 右侧内容 -->
          <div class="right-content">
            <!-- 其他用户的公告 -->
            <div v-if="routeUserId" class="announcement">
              <div class="section-title">公告</div>
              <p class="announcement-text">{{ userInfo.my_Announcement || '暂无公告' }}</p>
            </div>
            
            <!-- 功能入口（仅自己可见） -->
            <div v-if="!routeUserId" class="function-links">
              <el-button type="primary" @click="handleCreatorCenter">
                <el-icon><Edit /></el-icon> 创作中心
              </el-button>
              <el-button type="default" @click="handleMyVideo">
                <el-icon><VideoCamera /></el-icon> 我的视频
              </el-button>
              <el-button type="default" @click="handleSecurity">
                <el-icon><Lock /></el-icon> 安全管理
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 编织新的魔法集录弹窗 -->
  <el-dialog
    v-model="createCompilationDialogVisible"
    title="编织新的魔法集录"
    width="400px"
  >
    <el-form label-position="top">
      <el-form-item label="合集名称">
        <el-input
          v-model="newCompilationName"
          placeholder="请输入合集名称"
          maxlength="50"
        ></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="createCompilationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createCompilation">确定</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 添加视频到合集弹窗 -->
  <el-dialog
    v-model="addVideoDialogVisible"
    :title="`添加视频到合集：${currentCompilation?.compilationsName}`"
    width="600px"
  >
    <div class="add-video-content">
      <!-- 不在合集中的视频列表 -->
      <h4>选择要添加的视频</h4>
      <div class="video-selection-list">
        <el-checkbox-group v-model="selectedVideos">
          <div v-for="video in videosNotInCompilation" :key="video.videoId" class="video-selection-item">
            <el-checkbox :label="video.videoId"></el-checkbox>
            <div class="video-info">
              <img :src="getVideoCoverUrl(video)" alt="视频封面" class="video-cover">
              <div class="video-details">
                <p class="video-title">{{ video.videoName }}</p>
                <p class="video-stats">{{ video.playCount }}播放 · {{ video.likeCount }}点赞 · {{ formatDate(video.createTime) }}</p>
              </div>
            </div>
          </div>
        </el-checkbox-group>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="addVideoDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddVideos">确定添加</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { inject } from 'vue'
import { useLoginStore } from '@/stores/loginStore'
import Header from '@/components/Header.vue'
import { User, View, Like, Star, Edit, VideoCamera, Lock, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loginStore = useLoginStore()
const Request = inject('Request')
const Api = inject('Api')

// 获取路由参数中的userId
const routeUserId = computed(() => route.query.userId)

// 当前激活的标签页
const activeTab = ref('compilations')

// 用户信息
const userInfo = reactive({
  userId: '',
  nickName: '',
  avatar: '',
  coverImage: '',
  level: 1,
  isVip: false,
  followingCount: 0,
  followerCount: 0,
  likeCount: 0,
  playCount: 0,
  signature: ''
})

// 合集列表
const compilationList = ref([])

// 编织新的魔法集录弹窗
const createCompilationDialogVisible = ref(false)
const newCompilationName = ref('')

// 添加视频到合集弹窗
const addVideoDialogVisible = ref(false)
const currentCompilation = ref(null)
const videosNotInCompilation = ref([])
const selectedVideos = ref([])
const currentViewingCompilation = ref(null)
const compilationVideos = ref([])

// 分页相关数据
const currentPage = ref(1)
const pageSize = ref(6)

// 获取完整的头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  
  // 如果已经是完整的URL，则直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  
  // 添加完整的域名和端口
  const baseUrl = ''
  return `${baseUrl}${avatar}`
}

// 获取完整的视频封面URL
const getVideoCoverUrl = (video) => {
  if (!video) return '/default-cover.jpg'
  
  // 尝试获取封面路径，支持多种字段名
  let coverUrl = video.coverUrl || video.cover || video.videoCover || ''
  
  if (!coverUrl) return '/default-cover.jpg'
  
  // 如果已经是完整的URL，则直接返回
  if (coverUrl.startsWith('http://') || coverUrl.startsWith('https://')) {
    return coverUrl
  }
  
  // 添加完整的域名和端口
  const baseUrl = ''
  return `${baseUrl}${coverUrl}`
}

// 跳转到关注页面
const goToFollowing = () => {
  // 优先使用路由参数中的用户ID，如果没有则使用当前登录用户的ID
  const userId = routeUserId.value
  if (userId) {
    router.push(`/following?userId=${userId}`)
  } else {
    router.push('/following')
  }
}

// 跳转到粉丝页面
const goToFollowers = () => {
  // 优先使用路由参数中的用户ID，如果没有则使用当前登录用户的ID
  const userId = routeUserId.value
  if (userId) {
    router.push(`/followers?userId=${userId}`)
  } else {
    router.push('/followers')
  }
}

// 跳转到视频详情页面
const goToVideoDetail = (video) => {
  if (video && video.videoId) {
    router.push(`/video/${video.videoId}`)
  }
}

// 标签页切换处理
const handleTabChange = (tab) => {
  activeTab.value = tab
  // 根据不同的标签页加载不同的数据或跳转到不同的页面
  switch (tab) {
    case 'home':
      // 优先使用路由参数中的用户ID，如果没有则使用当前登录用户的ID
      const userId = routeUserId.value
      if (userId) {
        router.push(`/profile?userId=${userId}`)
      } else {
        router.push('/profile')
      }
      break
    case 'dynamic':
      // 优先使用路由参数中的用户ID，如果没有则使用当前登录用户的ID
      const userIdForDynamic = routeUserId.value
      if (userIdForDynamic) {
        router.push(`/profile?userId=${userIdForDynamic}&tab=dynamic`)
      } else {
        router.push('/profile?tab=dynamic')
      }
      break
    case 'videos':
      // 优先使用路由参数中的用户ID，如果没有则使用当前登录用户的ID
      const userIdForVideos = routeUserId.value
      if (userIdForVideos) {
        router.push(`/profile?userId=${userIdForVideos}&tab=videos`)
      } else {
        router.push('/profile?tab=videos')
      }
      break
    case 'collect':
      // 跳转到收藏页面
      if (routeUserId.value) {
        // 查看其他用户时，跳转到该用户的收藏页面，传递userId参数
        router.push(`/collect?userId=${routeUserId.value}`)
      } else {
        // 查看自己时，跳转到自己的收藏页面
        router.push('/collect')
      }
      break
    case 'settings':
      router.push('/profile/setting')
      break
  }
}

// 创作中心处理
const handleCreatorCenter = () => {
  router.push('/creator')
}

// 我的视频处理
const handleMyVideo = () => {
  router.push('/profile')
}

// 安全管理处理
const handleSecurity = () => {
  router.push('/profile/setting')
}

// 打开编织新的魔法集录弹窗
const openCreateCompilationDialog = () => {
  newCompilationName.value = ''
  createCompilationDialogVisible.value = true
}

// 编织新的魔法集录
const createCompilation = async () => {
  if (!newCompilationName.value.trim()) {
    ElMessage.warning('请输入合集名称')
    return
  }
  
  // 只有查看自己时才能创建合集
  if (routeUserId.value) {
    ElMessage.warning('只能创建自己的合集')
    return
  }
  
  try {
    // 调用后端API创建合集
    const result = await Request({
      url: Api.addCompilations,
      method: 'POST',
      data: {
        compilationsName: newCompilationName.value
      }
    })
    
    if (result && result.success) {
      createCompilationDialogVisible.value = false
      ElMessage.success('合集创建成功')
      // 重新加载合集列表
      loadCompilationList()
    } else {
      ElMessage.error(result.message || '创建合集失败')
    }
  } catch (error) {
    console.error('创建合集失败:', error)
    ElMessage.error('创建合集失败，请重试')
  }
}

// 打开添加视频到合集弹窗
const openAddVideoDialog = async (compilation) => {
  currentCompilation.value = compilation
  selectedVideos.value = []
  
  // 只有查看自己时才能添加视频到合集
  if (routeUserId.value) {
    ElMessage.warning('只能添加视频到自己的合集')
    return
  }
  
  try {
    // 调用后端API获取不在合集中的视频
    const result = await Request({
      url: Api.getExCompilationsVideo,
      method: 'GET',
      params: {
        compilationsId: compilation.compilationsId
      }
    })
    
    if (result && result.success) {
      videosNotInCompilation.value = result.data
      addVideoDialogVisible.value = true
    } else {
      ElMessage.error('获取视频列表失败')
    }
  } catch (error) {
    console.error('获取视频列表失败:', error)
    ElMessage.error('获取视频列表失败，请重试')
  }
}

// 确认添加视频到合集
const confirmAddVideos = async () => {
  if (selectedVideos.value.length === 0) {
    ElMessage.warning('请选择要添加的视频')
    return
  }
  
  // 只有查看自己时才能添加视频到合集
  if (routeUserId.value) {
    ElMessage.warning('只能添加视频到自己的合集')
    return
  }
  
  try {
    // 调用后端API添加视频到合集
    // 由于需要一个一个添加，这里使用循环
    let successCount = 0
    for (const videoId of selectedVideos.value) {
      const result = await Request({
        url: Api.addVideo2CompilationsId,
        method: 'POST',
        params: {
          compilationsId: currentCompilation.value.compilationsId,
          videoId: videoId
        }
      })
      
      if (result && result.success) {
        successCount++
      }
    }
    
    ElMessage.success(`成功添加 ${successCount} 个视频到合集`)
    addVideoDialogVisible.value = false
    
    // 重新加载合集列表，更新视频数量
    loadCompilationList()
  } catch (error) {
    console.error('添加视频到合集失败:', error)
    ElMessage.error('添加视频失败，请重试')
  }
}

// 查看合集中的视频
const viewCompilationVideos = async (compilation, pageNo = 1) => {
  try {
    // 优先使用路由参数中的用户ID，如果没有则使用当前登录用户的ID
    const userId = routeUserId.value || loginStore.userInfo.userId
    // 调用后端API获取合集中的视频（支持分页）
    const result = await Request({
      url: Api.getCompilations,
      method: 'GET',
      params: {
        userId: userId,
        compilationsId: compilation.compilationsId,
        pageNo: pageNo,
        pageSize: 6 // 每页最多显示6条数据
      }
    })
    
    if (result && result.success) {
      const videos = result.data
      // 设置当前查看的合集和视频列表
      currentViewingCompilation.value = compilation
      compilationVideos.value = videos || []
      currentPage.value = pageNo
      
      // 调用后端API获取合集中的视频数量
      await loadCompilationCount(compilation)
    } else {
      ElMessage.error('获取合集中的视频失败')
    }
  } catch (error) {
    console.error('获取合集中的视频失败:', error)
    ElMessage.error('获取合集中的视频失败，请重试')
  }
}

// 加载合集中的视频数量
const loadCompilationCount = async (compilation) => {
  try {
    // 优先使用路由参数中的用户ID，如果没有则使用当前登录用户的ID
    const userId = routeUserId.value || loginStore.userInfo.userId
    // 调用后端API获取合集中的视频数量
    const result = await Request({
      url: Api.getCompilationsCount,
      method: 'GET',
      params: {
        userId: userId,
        compilationsId: compilation.compilationsId
      }
    })
    
    if (result && result.success) {
      // 更新合集中的视频数量
      compilation.videoCount = result.data
    }
  } catch (error) {
    console.error('获取合集中的视频数量失败:', error)
  }
}

// 分页变化处理
const handlePageChange = (pageNo) => {
  if (currentViewingCompilation.value) {
    viewCompilationVideos(currentViewingCompilation.value, pageNo)
  }
}

// 关闭合集中的视频列表
const closeCompilationVideos = () => {
  currentViewingCompilation.value = null
  compilationVideos.value = []
  currentPage.value = 1
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 删除合集
const deleteCompilation = async (compilation) => {
  // 只有查看自己时才能删除合集
  if (routeUserId.value) {
    ElMessage.warning('只能删除自己的合集')
    return
  }
  
  try {
    // 调用后端API删除合集
    const result = await Request({
      url: Api.deleteCompilations,
      method: 'DELETE',
      params: {
        compilationsId: compilation.compilationsId
      }
    })
    
    if (result && result.success) {
      // 从列表中移除
      compilationList.value = compilationList.value.filter(item => item.compilationsId !== compilation.compilationsId)
      ElMessage.success('合集删除成功')
    } else {
      ElMessage.error(result.message || '删除合集失败')
    }
  } catch (error) {
    console.error('删除合集失败:', error)
    ElMessage.error('删除合集失败，请重试')
  }
}

// 从合集中删除视频
const deleteVideoFromCompilation = async (video) => {
  // 只有查看自己时才能从合集中删除视频
  if (routeUserId.value) {
    ElMessage.warning('只能从自己的合集中删除视频')
    return
  }
  
  try {
    // 调用后端API从合集中删除视频
    const result = await Request({
      url: Api.deleteCompilationsVideo,
      method: 'DELETE',
      params: {
        compilationsId: currentViewingCompilation.value.compilationsId,
        videoId: video.videoId
      }
    })
    
    if (result && result.success) {
      // 从列表中移除视频
      compilationVideos.value = compilationVideos.value.filter(item => item.videoId !== video.videoId)
      // 更新合集中的视频数量
      currentViewingCompilation.value.videoCount--
      // 重新加载合集列表，更新视频数量
      await loadCompilationList()
      ElMessage.success('视频从合集中删除成功')
    } else {
      ElMessage.error(result.message || '删除视频失败')
    }
  } catch (error) {
    console.error('从合集中删除视频失败:', error)
    ElMessage.error('删除视频失败，请重试')
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    // 优先使用路由参数中的用户ID，如果没有则使用当前登录用户的ID
    const userId = routeUserId.value || loginStore.userInfo.userId
    // 调用接口获取用户基本信息
    const result = await Request({
      url: Api.getUserInfo,
      params: { userId: userId }
    })
    if (result) {
      Object.assign(userInfo, result.data)
    }
    
    // 调用接口获取总获赞数
    try {
      const likesResult = await Request({
        url: Api.getUserTotalLikes,
        params: { userId: userId }
      })
      if (likesResult && likesResult.success) {
        userInfo.likeCount = likesResult.data
      }
    } catch (likesError) {
      console.error('获取总获赞数失败:', likesError)
    }
    
    // 调用接口获取总播放数
    try {
      const playsResult = await Request({
        url: Api.getUserTotalPlays,
        params: { userId: userId }
      })
      if (playsResult && playsResult.success) {
        userInfo.playCount = playsResult.data
      }
    } catch (playsError) {
      console.error('获取总播放数失败:', playsError)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 加载合集列表
const loadCompilationList = async () => {
  try {
    // 优先使用路由参数中的用户ID，如果没有则使用当前登录用户的ID
    const userId = routeUserId.value || loginStore.userInfo.userId
    // 调用后端API获取用户的合集列表
    const result = await Request({
      url: Api.getCompilationsList,
      method: 'GET',
      params: {
        userId: userId
      }
    })
    
    if (result && result.success) {
      // 转换后端返回的数据格式
      compilationList.value = result.data.map(item => ({
        compilationsId: item.compilations,
        compilationsName: item.compilationsName,
        videoCount: item.count
      }))
      
      // 对于每个合集，调用getCompilationsCount获取真实的视频数量
      for (const compilation of compilationList.value) {
        await loadCompilationCount(compilation)
      }
    }
  } catch (error) {
    console.error('加载合集列表失败:', error)
    ElMessage.error('加载合集列表失败，请重试')
  }
}

onMounted(() => {
  // 初始化加载用户信息和合集列表
  loadUserInfo()
  loadCompilationList()
})
</script>

<style scoped>
.compilations-page {
  min-height: 100vh;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

.main-content {
  margin-top: 60px;
  padding: 20px;
}

.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 用户信息头 */
.profile-header {
  margin-bottom: 16px;
}

.user-info {
  padding: 20px 20px 0;
}

.avatar-name {
  display: flex;
  align-items: flex-end;
}

.user-details {
  margin-left: 20px;
  flex: 1;
}

.nickname {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
}

.signature {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nickname .level {
  font-size: 14px;
  color: var(--text-dim);
  font-weight: normal;
}

.user-meta {
  margin-bottom: 10px;
}

.user-stats {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-top: 10px;
}

.stat-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.3s;
  text-decoration: none;
}

.stat-item:hover {
  color: var(--color-primary-light);
}

.stat-item .el-icon {
  margin-right: 5px;
  font-size: 14px;
}

.stat-separator {
  margin: 0 10px;
  color: var(--text-dim);
}

/* 内容导航标签 */
.content-tabs {
  background-color: transparent;
  border-radius: 5px;
  padding: 0 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.content-tabs .el-tabs__header {
  margin-bottom: 0;
  border-bottom: 1px solid var(--glass-border);
}

.content-tabs .el-tabs__nav {
  height: 50px;
}

.content-tabs .el-tabs__item {
  height: 50px;
  line-height: 50px;
  font-size: 16px;
}

/* 内容区域 */
.content-area {
  display: flex;
  gap: 20px;
}

/* 左侧内容 */
.left-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 右侧内容 */
.right-content {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 合集管理区 */
.compilations-management {
  background-color: transparent;
  border-radius: 5px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0;
}

/* 合集列表 */
.compilation-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.compilation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border: 1px solid var(--glass-border);
  border-radius: 5px;
  transition: all 0.3s;
}

.compilation-item:hover {
  box-shadow: var(--shadow-sm);
}

.compilation-name {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 5px 0;
  color: var(--text-primary);
}

.compilation-stats {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.compilation-actions {
    display: flex;
    gap: 10px;
  }
  
  /* 合集中的视频列表 */
  .compilation-videos {
    margin-top: 30px;
    background-color: transparent;
    border-radius: 5px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
  
  .video-count {
    font-size: 14px;
    font-weight: normal;
    color: var(--text-secondary);
  }
  
  .video-list {
    margin-top: 20px;
  }
  
  .video-item {
    display: flex;
    gap: 15px;
    padding: 15px;
    border-bottom: 1px solid var(--glass-border);
    transition: all 0.3s;
    align-items: flex-start;
  }
  
  .video-info-container {
    display: flex;
    flex-direction: column;
    flex: 1;
  }
  
  .video-main-info {
    display: flex;
    align-items: center;
    margin-bottom: 5px;
  }
  
  .video-title-info {
    flex: 1;
  }
  
  .video-item:hover {
    background-color: var(--bg-secondary);
  }
  
  .video-actions {
    margin-left: auto;
  }
  
  .video-thumbnail {
    position: relative;
    width: 180px;
    height: 101px;
    flex-shrink: 0;
  }
  
  .video-cover {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 4px;
  }
  
  .video-duration {
    position: absolute;
    bottom: 5px;
    right: 5px;
    background-color: rgba(0, 0, 0, 0.7);
    color: #fff;
    font-size: 12px;
    padding: 2px 5px;
    border-radius: 2px;
  }
  
  .video-info-container {
    flex: 1;
    min-width: 0;
  }
  
  .video-title-info {
    margin-bottom: 8px;
  }
  
  .video-stats-info {
    margin-top: 0;
  }
  
  .video-title {
    font-size: 16px;
    font-weight: bold;
    margin: 0;
    color: var(--text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    text-align: left;
  }
  
  .video-stats {
    font-size: 14px;
    color: var(--text-secondary);
    margin: 0;
    text-align: left;
  }
  
  .video-uploader {
    font-size: 14px;
    color: var(--text-dim);
    margin: 0;
  }
  
  /* 分页组件样式 */
  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }

/* 功能入口 */
.function-links {
  background-color: transparent;
  border-radius: 5px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.announcement {
  background-color: transparent;
  border-radius: 5px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
  color: var(--text-primary);
  padding-bottom: 10px;
  border-bottom: 2px solid #e8e8e8;
}

.announcement-text {
  font-size: 14px;
  line-height: 1.5;
  color: var(--text-secondary);
}

/* 添加视频到合集弹窗内容 */
.add-video-content {
  max-height: 400px;
  overflow-y: auto;
}

.video-selection-list {
  margin-top: 20px;
}

.video-selection-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border: 1px solid var(--glass-border);
  border-radius: 5px;
  margin-bottom: 10px;
  transition: all 0.3s;
}

.video-selection-item:hover {
  box-shadow: var(--shadow-sm);
}

.video-selection-item .el-checkbox {
  margin-right: 10px;
}

.video-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.video-info .video-cover {
  width: 80px;
  height: 45px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 10px;
}

.video-details {
  flex: 1;
}

.video-title {
  font-size: 14px;
  font-weight: bold;
  margin: 0 0 5px 0;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-stats {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .content-area {
    flex-direction: column;
  }
  
  .right-content {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .user-stats {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .stat-item {
    margin-right: 0;
  }
  
  .compilation-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .compilation-actions {
    align-self: flex-end;
  }
}

@media (max-width: 576px) {
  .avatar-name {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .user-details {
    margin-left: 0;
    margin-top: 10px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .compilation-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>