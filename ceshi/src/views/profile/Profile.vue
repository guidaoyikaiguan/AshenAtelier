<template>
  <div class="profile-page" :style="{ backgroundImage: 'url(/images/profile_bg.png)' }">
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
                
                <!-- 私信和关注按钮 - 仅在查看其他用户时显示 -->
                <div v-if="isViewingOtherUser" class="user-actions">
                  <el-button type="primary" size="small" @click="sendPrivateMessage">
                    <el-icon><Message /></el-icon> 魔法信笺
                  </el-button>
                  <el-button 
                    :type="isFollowing ? 'default' : 'primary'" 
                    size="small" 
                    @click="toggleFollow"
                  >
                    {{ isFollowing ? '已关注' : '关注' }}
                  </el-button>
                </div>
                
                <div class="user-stats">
                  <a href="#" class="stat-item" @click.prevent="goToFollowing">
                    <el-icon><User /></el-icon>
                    <span>旅伴: {{ userInfo.followingCount || 0 }}</span>
                  </a>
                  <span class="stat-separator">•</span>
                  <a href="#" class="stat-item" @click.prevent="goToFollowers">
                    <el-icon><View /></el-icon>
                    <span>仰慕者: {{ userInfo.followerCount || 0 }}</span>
                  </a>
                  <span class="stat-separator">•</span>
                  <span class="stat-item">
                    <el-icon><Like /></el-icon>
                    <span>祝福: {{ userInfo.likeCount || 0 }}</span>
                  </span>
                  <span class="stat-separator">•</span>
                  <span class="stat-item">
                    <el-icon><Star /></el-icon>
                    <span>阅览: {{ userInfo.playCount || 0 }}</span>
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
            <el-tab-pane v-if="!isViewingOtherUser" label="魔女工坊" name="settings"></el-tab-pane>
          </el-tabs>
        </div>
        
        <!-- 内容区域 -->
        <div class="content-area">
          <!-- 左侧内容 -->
          <div class="left-content">

            
            <!-- 作品区 -->
            <div class="my-videos" v-if="activeTab === 'home' || activeTab === 'videos'">
              <div class="section-title">{{ isViewingOtherUser ? 'TA的作品' : '我的作品' }}</div>
              <div class="video-list">
                <el-empty v-if="videoList.length === 0" description="暂无作品"></el-empty>
                <div v-for="video in videoList" :key="video.videoId" class="video-item" @click="goToVideoDetail(video)">
                  <div class="video-cover">
                    <img :src="getVideoCoverUrl(video)" alt="视频封面">
                    <span v-if="video.duration" class="video-duration">{{ video.duration }}</span>
                  </div>
                  <div class="video-info">
                    <p class="video-title">{{ video.videoName || video.title || '无标题' }}</p>
                    <p class="video-stats">{{ video.playCount }}播放 · {{ video.likeCount }}点赞</p>
                  </div>
                </div>
              </div>
              <!-- 分页组件 - 仅在投稿标签页显示 -->
              <div v-if="activeTab === 'videos'" class="pagination-container">
                <el-pagination
                  v-model:current-page="pagination.currentPage"
                  v-model:page-size="pagination.pageSize"
                  layout="total, prev, pager, next, jumper"
                  :total="pagination.total"
                  @current-change="handleCurrentChange"
                />
              </div>
              <!-- 查看更多按钮 - 仅在主页标签页显示 -->
              <el-button v-else-if="videoList.length > 0" type="text" class="view-more" @click="viewMoreVideos">展开旅途绘卷</el-button>
            </div>
            
            <!-- 动态区 -->
            <div class="my-moments" v-if="activeTab === 'dynamic'">
              <div class="section-title">{{ isViewingOtherUser ? 'TA的动态' : '我的动态' }}</div>
              <div class="moment-list">
                <el-empty v-if="momentList.length === 0" description="暂无动态"></el-empty>
                <div v-for="moment in momentList" :key="moment.momentId" class="moment-item" @click="goToMomentDetail(moment.momentId)">
                  <div class="moment-header">
                    <div class="moment-user">
                      <el-avatar :size="32" :src="getAvatarUrl(moment.avatar)">{{ moment.userName?.charAt(0) || '用' }}</el-avatar>
                      <span class="moment-username">{{ moment.userName || '未知用户' }}</span>
                    </div>
                    <span class="moment-time">{{ moment.createTime }}</span>
                  </div>
                  <div class="moment-content">{{ moment.content }}</div>
                  <div class="moment-images" v-if="moment.images && moment.images.length > 0">
                    <div class="image-list">
                      <img v-for="(image, index) in moment.images" :key="index" :src="image.imageUrl" alt="动态图片" class="moment-image">
                    </div>
                  </div>
                  <div class="moment-actions">
                    <span class="action-item">
                      <el-icon><Star /></el-icon>
                      <span>{{ moment.likeCount || 0 }} 点赞</span>
                    </span>
                    <span class="action-item">
                      <el-icon><ChatLineRound /></el-icon>
                      <span>{{ moment.commentCount || 0 }} 评论</span>
                    </span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 合集区 - 仅在主页标签页显示 -->
            <div v-if="activeTab === 'home'" class="my-compilations">
              <div class="section-title">{{ isViewingOtherUser ? 'TA的合集' : '我的合集' }}</div>
              <div class="compilation-list">
                <el-empty v-if="compilationList.length === 0" description="暂无合集"></el-empty>
                <div v-for="compilation in compilationList" :key="compilation.compilationsId" class="compilation-item">
                  <h4 class="compilation-name">{{ compilation.compilationsName }}</h4>
                  <p class="compilation-stats">包含 {{ compilation.videoCount }} 个视频</p>
                  
                  <!-- 合集中的视频 -->
                  <div v-if="compilation.videos && compilation.videos.length > 0" class="compilation-videos">
                    <div class="video-grid">
                      <div v-for="video in compilation.videos" :key="video.videoId" class="compilation-video-item" @click="goToVideoDetail(video)">
                        <div class="video-cover">
                          <img :src="getVideoCoverUrl(video)" alt="视频封面">
                          <span v-if="video.duration" class="video-duration">{{ video.duration }}</span>
                        </div>
                        <p class="video-title">{{ video.videoName || video.title || '无标题' }}</p>
                        <p class="video-stats">{{ video.playCount }}播放 · {{ video.likeCount }}点赞</p>
                      </div>
                    </div>
                  </div>
                  <el-empty v-else-if="compilation.videoCount === 0" description="暂无视频"></el-empty>
                  
                  <div class="compilation-actions">
                    <el-button type="default" size="small" @click="goToCompilations(compilation)">
                      <el-icon><View /></el-icon> 查看
                    </el-button>
                  </div>
                </div>
              </div>

            </div>
          </div>
          
          <!-- 右侧内容 -->
          <div class="right-content">
            <!-- 公告 -->
            <div class="announcement">
              <div class="section-title">旅人公告</div>
              <p class="announcement-text">{{ userInfo.my_Announcement || '暂无公告' }}</p>
              <el-button v-if="!isViewingOtherUser" type="text" class="edit-announcement" @click="openEditAnnouncementDialog">撰写旅人公告</el-button>
            </div>
            
            <!-- 个人资料 - 仅自己可见 -->
            <div v-if="!isViewingOtherUser" class="personal-info">
              <div class="section-title">旅人档案</div>
              <div class="info-item">
                <span class="label">魔导书编号:</span>
                <span class="value">{{ userInfo.userId || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">入会之日:</span>
                <span class="value">{{ formatDateToDay(userInfo.regTime) }}</span>
              </div>
              <el-button type="text" @click="handleEditProfile">修编档案</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 编辑公告弹窗 -->
  <el-dialog
    v-model="editAnnouncementDialogVisible"
    title="撰写旅人公告"
    width="500px"
    :show-close="true"
  >
    <el-form label-position="top">
      <el-form-item label="公告内容">
        <el-input
          v-model="editAnnouncementContent"
          type="textarea"
          :rows="4"
          placeholder="请输入公告内容"
        ></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="editAnnouncementDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAnnouncement">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
.user-actions {
  margin: 10px 0;
}

.user-actions .el-button {
  margin-right: 10px;
}
</style>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { inject } from 'vue'
import { useLoginStore } from '@/stores/loginStore'
import Header from '@/components/Header.vue'
import { User, View, Like, Star, Message, ChatLineRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loginStore = useLoginStore()
const Request = inject('Request')
const Api = inject('Api')

// 获取路由参数中的用户ID（同时检查路径参数和查询参数）
const routeUserId = computed(() => route.params.userId || route.query.userId)

// 判断是否是查看其他用户的个人中心
const isViewingOtherUser = computed(() => {
  return routeUserId.value && routeUserId.value !== loginStore.userInfo?.userId
})

// 监听路由参数变化，重新加载数据
watch(() => route.params.userId, (newUserId) => {
  if (newUserId) {
    loadUserInfo()
    loadMyVideos()
  }
})

// 监听路由中的tab参数变化
watch(() => route.query.tab, (newTab) => {
  if (newTab) {
    activeTab.value = newTab
  }
})

// 跳转到关注页面
const goToFollowing = () => {
  if (isViewingOtherUser.value) {
    router.push(`/following?userId=${routeUserId.value}`)
  } else {
    router.push('/following')
  }
}

// 跳转到动态详情页面
const goToMomentDetail = (momentId) => {
  router.push(`/moment/detail/${momentId}`)
}

// 跳转到粉丝页面
const goToFollowers = () => {
  if (isViewingOtherUser.value) {
    router.push(`/followers?userId=${routeUserId.value}`)
  } else {
    router.push('/followers')
  }
}

// 发送私信
const sendPrivateMessage = () => {
  if (isViewingOtherUser.value && routeUserId.value && userInfo.nickName) {
    router.push({
      path: '/chat',
      query: {
        otherUserId: routeUserId.value,
        otherUserName: userInfo.nickName
      }
    })
  }
}

// 跳转到视频详情页面
const goToVideoDetail = (video) => {
  if (video && video.videoId) {
    router.push(`/video/${video.videoId}`)
  }
}

// 当前激活的标签页
const activeTab = ref('home')

// 初始化时检查路由中的tab参数
onMounted(() => {
  // 检查路由中是否有tab参数
  if (route.query.tab) {
    activeTab.value = route.query.tab
  }
  
  // 初始化加载用户信息
  loadUserInfo()
  
  // 根据当前激活的标签页加载对应的数据
  if (activeTab.value === 'videos') {
    // 投稿标签页：加载视频总数和视频列表
    loadUserVideoCount()
    loadAllVideos(1)
  } else if (activeTab.value === 'home') {
    // 主页标签页：加载视频列表（最多6条）
    loadMyVideos()
    // 加载合集列表（无论是否是查看其他用户）
    loadCompilationList()
  }
})

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
  regTime: '',
  signature: '',
  my_Announcement: ''
})

// 关注状态
const isFollowing = ref(false)

// 编辑公告相关
const editAnnouncementDialogVisible = ref(false)
const editAnnouncementContent = ref('')

// 我的视频列表
const videoList = ref([])

// 分页相关
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 合集列表
const compilationList = ref([])

// 动态列表
const momentList = ref([])

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

// 格式化日期为只显示到日 (YYYY-M-D)
const formatDateToDay = (dateString) => {
  if (!dateString) return '未设置'
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = date.getMonth() + 1 // 月份从0开始，需要加1
  const day = date.getDate()
  return `${year}-${month}-${day}`
}

// 打开编辑公告弹窗
const openEditAnnouncementDialog = () => {
  editAnnouncementContent.value = userInfo.my_Announcement || ''
  editAnnouncementDialogVisible.value = true
}

// 保存公告
const saveAnnouncement = async () => {
  try {
    const result = await Request({
      url: Api.updateUserInfo,
      method: 'POST',
      data: {
        my_Announcement: editAnnouncementContent.value
      }
    })
    if (result.success) {
      userInfo.my_Announcement = editAnnouncementContent.value
      editAnnouncementDialogVisible.value = false
      ElMessage.success('公告保存成功')
    } else {
      ElMessage.error('公告保存失败')
    }
  } catch (error) {
    console.error('保存公告失败:', error)
    ElMessage.error('保存失败，请重试')
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    // 优先使用路由参数中的用户ID，如果没有或为"undefined"则使用当前登录用户的ID
    const targetUserId = (routeUserId.value && routeUserId.value !== 'undefined') ? routeUserId.value : loginStore.userInfo.userId
    
    // 如果是查看其他用户
    if (isViewingOtherUser.value) {
      // 先检查关注状态
      try {
        const followResult = await Request({
          url: Api.selectIfFollow,
          method: 'GET',
          params: {
            userId: targetUserId
          }
        })
        
        // 根据关注状态选择API接口
        const followingStatus = followResult && followResult.success && followResult.data
        isFollowing.value = followingStatus
        const apiUrl = followingStatus ? Api.getAttentionUserInfo : Api.getEXAttentionUserInfo
        
        const result = await Request({
          url: apiUrl,
          params: { userId: targetUserId }
        })
        
        if (result) {
          Object.assign(userInfo, result.data)
        }
      } catch (followError) {
        // 如果检查关注状态失败，默认使用getEXAttentionUserInfo
        console.error('检查关注状态失败:', followError)
        isFollowing.value = false
        const result = await Request({
          url: Api.getEXAttentionUserInfo,
          params: { userId: targetUserId }
        })
        if (result) {
          Object.assign(userInfo, result.data)
        }
      }
    } else {
      // 查看自己的个人中心
      const result = await Request({
        url: Api.getUserInfo,
        params: { userId: targetUserId }
      })
      if (result) {
        Object.assign(userInfo, result.data)
        // 更新本地存储的用户信息
        loginStore.saveUserInfo(result.data)
      }
    }
    
    // 调用接口获取总获赞数
    try {
      const likesResult = await Request({
        url: Api.getUserTotalLikes,
        params: { userId: targetUserId }
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
        params: { userId: targetUserId }
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

// 加载视频列表（主页标签使用）
const loadMyVideos = async () => {
  try {
    // 优先使用路由参数中的用户ID，如果没有或为"undefined"则使用当前登录用户的ID
    const targetUserId = (routeUserId.value && routeUserId.value !== 'undefined') ? routeUserId.value : loginStore.userInfo.userId
    const result = await Request({
      url: Api.getUserVideos,
      params: {
        userId: targetUserId,
        pageNo: 1,
        pageSize: 6
      }
    })
    if (result && result.success && result.data) {
      // 后端返回的直接是视频列表
      // 最多只显示6条视频
      videoList.value = result.data.slice(0, 6)
      // 调试：查看视频对象的结构
      if (videoList.value.length > 0) {
        console.log('视频对象结构:', videoList.value[0])
      }
    }
  } catch (error) {
    console.error('加载视频列表失败:', error)
    // 出错时设置为空数组，避免后续渲染错误
    videoList.value = []
  }
}

// 标签页切换处理
const handleTabChange = (tab) => {
  activeTab.value = tab
  // 根据不同的标签页加载不同的数据或跳转到不同的页面
  switch (tab) {
    case 'home':
      loadUserInfo()
      loadMyVideos()
      // 加载合集列表（无论是否是查看其他用户）
      loadCompilationList()
      break
    case 'dynamic':
      loadUserDynamics()
      break
    case 'videos':
      // 切换到投稿标签时，重置分页、获取视频总数并加载第一页数据
      pagination.currentPage = 1
      loadUserVideoCount()
      loadAllVideos(1)
      break
    case 'compilations':
      // 跳转到合集页面
      if (isViewingOtherUser.value) {
        // 查看其他用户时，跳转到该用户的合集页面，传递userId参数
        router.push(`/compilations?userId=${routeUserId.value}`)
      } else {
        // 查看自己时，跳转到自己的合集页面
        router.push('/compilations')
      }
      break
    case 'collect':
      // 跳转到收藏页面
      if (isViewingOtherUser.value) {
        // 查看其他用户时，跳转到该用户的收藏页面，传递userId参数
        router.push(`/collect?userId=${routeUserId.value}`)
      } else {
        // 查看自己时，跳转到自己的收藏页面
        router.push('/collect')
      }
      break
    case 'settings':
      // 跳转到设置页面
      if (!isViewingOtherUser.value) {
        router.push('/profile/setting')
      }
      break
  }
}

// 分页当前页变化处理
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  loadAllVideos(current)
}

// 视频上传处理
const handleVideoUpload = () => {
  router.push('/upload')
}

// 编辑资料处理
const handleEditProfile = () => {
  router.push('/profile/setting')
}

// 切换关注状态
const toggleFollow = async () => {
  try {
    const targetUserId = routeUserId.value
    console.log('开始切换关注状态:', targetUserId, '当前状态:', isFollowing.value)
    
    // 调用关注/取消关注接口
    const result = await Request({
      url: '/api/user/followUser',
      method: 'POST',
      params: {
        userId: targetUserId
      }
    })
    
    console.log('切换关注状态接口调用结果:', result)
    
    // 根据返回结果更新关注状态
    if (result && (result.success || result.status === 'success')) {
      console.log('切换关注状态成功')
      isFollowing.value = !isFollowing.value
      // 更新关注数
      if (isFollowing.value) {
        userInfo.followerCount = (userInfo.followerCount || 0) + 1
      } else {
        userInfo.followerCount = Math.max(0, (userInfo.followerCount || 0) - 1)
      }
    } else {
      console.log('切换关注状态失败:', result)
    }
  } catch (error) {
    console.error('切换关注状态失败:', error)
  }
}

// 查看更多视频处理
const viewMoreVideos = () => {
  // 直接切换到投稿标签页，而不是跳转到新页面
  activeTab.value = 'videos'
  // 触发投稿标签页的数据加载
  handleTabChange('videos')
}

// 加载用户动态
const loadUserDynamics = async () => {
  try {
    console.log('开始加载用户动态')
    // 优先使用路由参数中的用户ID，如果没有或为"undefined"则使用当前登录用户的ID
    const targetUserId = (routeUserId.value && routeUserId.value !== 'undefined') ? routeUserId.value : loginStore.userInfo.userId
    
    // 调用动态列表接口
    const result = await Request({
      url: '/api/moment/getMomentList',
      method: 'GET',
      params: {
        page: 1,
        size: 10,
        userId: targetUserId
      }
    })
    
    console.log('使用getMomentList方法的结果:', result)
    
    // 检查返回数据的结构
    let moments = []
    if (result && result.success && result.data) {
      console.log('动态列表数据:', result.data)
      moments = result.data
    } else {
      console.log('动态列表接口调用失败:', result)
    }
    
    console.log('提取后的动态列表:', moments)
    
    // 处理数据，添加缺少的字段
    const processedMoments = moments.map(moment => {
      console.log('原始moment数据:', moment)
      return {
        ...moment,
        // 确保点赞数和评论数存在
        likeCount: moment.likeCount || 0,
        commentCount: moment.commentCount || 0,
        // 确保用户名称和头像存在，优先使用后端返回的数据
        userName: moment.userName || moment.authorName || '未知用户',
        avatar: moment.avatar || moment.authorAvatar || '/default-avatar.png'
      }
    })
    
    console.log('处理后的动态列表:', processedMoments)
    
    // 直接替换动态列表
    momentList.value = processedMoments
    console.log('最终动态列表:', momentList.value)
    console.log('动态列表长度:', momentList.value.length)
  } catch (error) {
    console.error('加载用户动态失败:', error)
    // 发生错误时，设置为空数组
    momentList.value = []
  }
}

// 获取用户视频总数
const loadUserVideoCount = async () => {
  try {
    // 优先使用路由参数中的用户ID，如果没有或为"undefined"则使用当前登录用户的ID
    const targetUserId = (routeUserId.value && routeUserId.value !== 'undefined') ? routeUserId.value : loginStore.userInfo.userId
    const result = await Request({
      url: Api.getUserVideoCount,
      params: {
        userId: targetUserId
      }
    })
    if (result && result.success) {
      // 设置视频总数
      pagination.total = result.data
    }
  } catch (error) {
    console.error('获取用户视频总数失败:', error)
  }
}

// 加载所有视频（调用getUserVideos接口，支持分页）
const loadAllVideos = async (pageNo = 1) => {
  try {
    // 优先使用路由参数中的用户ID，如果没有或为"undefined"则使用当前登录用户的ID
    const targetUserId = (routeUserId.value && routeUserId.value !== 'undefined') ? routeUserId.value : loginStore.userInfo.userId
    const result = await Request({
      url: Api.getUserVideos,
      params: {
        userId: targetUserId,
        pageNo: pageNo
      }
    })
    if (result && result.success) {
      // 后端返回的直接是视频列表
      videoList.value = result.data
    }
  } catch (error) {
    console.error('加载用户投稿视频失败:', error)
    // 出错时设置为空数组，避免后续渲染错误
    videoList.value = []
  }
}

// 加载用户设置（模拟）
const loadUserSettings = async () => {
  // 实际项目中调用API加载用户设置
  console.log('加载用户设置')
}

// 加载合集中的视频数量
const loadCompilationCount = async (compilation) => {
  try {
    // 优先使用路由参数中的用户ID，如果没有或为"undefined"则使用当前登录用户的ID
    const targetUserId = (routeUserId.value && routeUserId.value !== 'undefined') ? routeUserId.value : loginStore.userInfo.userId
    // 调用后端API获取合集中的视频数量
    const result = await Request({
      url: Api.getCompilationsCount,
      method: 'GET',
      params: {
        userId: targetUserId,
        compilationsId: compilation.compilationsId
      }
    })
    
    if (result && result.success) {
      // 更新合集中的视频数量
      compilation.videoCount = result.data
    }
  } catch (error) {
    console.error(`加载合集中的视频数量失败:`, error)
  }
}

// 加载合集中的视频
const loadCompilationVideos = async (compilation) => {
  try {
    // 优先使用路由参数中的用户ID，如果没有或为"undefined"则使用当前登录用户的ID
    const targetUserId = (routeUserId.value && routeUserId.value !== 'undefined') ? routeUserId.value : loginStore.userInfo.userId
    // 调用后端API获取合集中的视频（只获取前3个）
    const result = await Request({
      url: Api.getCompilations,
      method: 'GET',
      params: {
        userId: targetUserId,
        compilationsId: compilation.compilationsId,
        pageNo: 1,
        pageSize: 3
      }
    })
    
    if (result && result.success) {
      // 将视频列表添加到合集中，最多只保留3个
      compilation.videos = (result.data || []).slice(0, 3)
    }
  } catch (error) {
    console.error(`加载合集中的视频失败:`, error)
    compilation.videos = []
  }
}

// 加载合集列表
const loadCompilationList = async () => {
  try {
    // 优先使用路由参数中的用户ID，如果没有或为"undefined"则使用当前登录用户的ID
    const targetUserId = (routeUserId.value && routeUserId.value !== 'undefined') ? routeUserId.value : loginStore.userInfo.userId
    // 调用后端API获取用户的合集列表
    const result = await Request({
      url: Api.getCompilationsList,
      method: 'GET',
      params: {
        userId: targetUserId
      }
    })
    
    if (result && result.success) {
      // 转换后端返回的数据格式
      compilationList.value = result.data.map(item => ({
        compilationsId: item.compilations,
        compilationsName: item.compilationsName,
        videoCount: item.count,
        videos: [] // 初始化视频列表
      }))
      
      // 为每个合集加载视频数量和视频
      for (const compilation of compilationList.value) {
        await loadCompilationCount(compilation)
        await loadCompilationVideos(compilation)
      }
    }
  } catch (error) {
    console.error('加载合集列表失败:', error)
  }
}

// 跳转到合集详情页
const goToCompilations = (compilation) => {
  // 只有在查看其他用户的合集时才传递userId参数
  if (isViewingOtherUser.value && routeUserId.value && routeUserId.value !== 'undefined') {
    router.push(`/compilations?userId=${routeUserId.value}`)
  } else {
    // 查看自己的合集时不传递userId参数
    router.push('/compilations')
  }
}

// 跳转到合集页面
const goToCompilationsPage = () => {
  // 只有在查看其他用户的合集时才传递userId参数
  if (isViewingOtherUser.value && routeUserId.value && routeUserId.value !== 'undefined') {
    router.push(`/compilations?userId=${routeUserId.value}`)
  } else {
    // 查看自己的合集时不传递userId参数
    router.push('/compilations')
  }
}


</script>

<style scoped>
.profile-page {
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

.level {
  font-size: 14px;
  color: var(--text-secondary);
  margin-right: 10px;
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

/* 公共区块样式 */
.fan-interaction, .my-videos, .announcement, .personal-info {
  background-color: transparent;
  border-radius: 5px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
  color: var(--text-primary);
  padding-bottom: 10px;
  border-bottom: 2px solid #e8e8e8;
}

/* 粉丝互动区 */
.fan-content {
  text-align: center;
}

.welcome-message {
  padding: 20px;
}

.welcome-message p {
  margin-bottom: 10px;
  color: var(--text-secondary);
}

/* 我的作品区 */
.video-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 15px;
}

.video-item {
  cursor: pointer;
  transition: transform 0.3s;
}

.video-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}

/* 动态样式 */
.my-moments {
  margin-bottom: 30px;
}

.moment-list {
  margin-top: 20px;
}

.moment-item {
  background-color: transparent;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.moment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.moment-user {
  display: flex;
  align-items: center;
}

.moment-username {
  margin-left: 8px;
  font-weight: 500;
  color: var(--text-primary);
}

.moment-time {
  font-size: 12px;
  color: var(--text-dim);
}

.moment-content {
  margin-bottom: 12px;
  line-height: 1.5;
  color: var(--text-primary);
}

.moment-images {
  margin-bottom: 12px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.moment-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.moment-actions {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: var(--text-secondary);
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.action-item:hover {
  color: var(--color-primary-light);
}

.video-cover {
  position: relative;
  height: 120px;
  background-color: #e9e9e9;
  margin-bottom: 10px;
  border-radius: 5px;
  overflow: hidden;
}

.video-duration {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 10px;
  padding: 2px 5px;
  border-radius: 2px;
}

.video-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-info {
}

.video-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 5px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-stats {
  font-size: 12px;
  color: var(--text-dim);
}

.view-more {
  width: 100%;
  text-align: center;
}

/* 合集区样式 */
.my-compilations {
  margin-top: 20px;
  background-color: transparent;
  border-radius: 5px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.compilation-list {
  margin-bottom: 15px;
}

.compilation-item {
  padding: 15px;
  border-bottom: 1px solid var(--glass-border);
  transition: all 0.3s;
}

.compilation-item:hover {
  background-color: var(--bg-secondary);
}

.compilation-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
  color: var(--text-primary);
}

.compilation-stats {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.compilation-actions {
  display: flex;
  justify-content: flex-end;
}

/* 合集中的视频样式 */
.compilation-videos {
  margin-top: 10px;
  margin-bottom: 10px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.compilation-video-item {
  transition: transform 0.3s;
}

.compilation-video-item:hover {
  transform: translateY(-2px);
}

.compilation-video-item .video-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* 16:9 比例 */
  overflow: hidden;
  border-radius: 4px;
  background-color: rgba(255,255,255,0.06);
}

.compilation-video-item .video-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.compilation-video-item .video-duration {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 10px;
  padding: 2px 5px;
  border-radius: 2px;
}

.compilation-video-item .video-title {
  font-size: 14px;
  font-weight: bold;
  margin-top: 5px;
  margin-bottom: 3px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.compilation-video-item .video-stats {
  font-size: 12px;
  color: var(--text-dim);
  margin: 0;
}

/* 合集中的视频样式 */
.compilation-videos {
  margin-top: 10px;
  margin-bottom: 10px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.compilation-video-item {
  transition: transform 0.3s;
}

.compilation-video-item:hover {
  transform: translateY(-2px);
}

.compilation-video-item .video-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* 16:9 比例 */
  overflow: hidden;
  border-radius: 4px;
  background-color: rgba(255,255,255,0.06);
}

.compilation-video-item .video-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.compilation-video-item .video-duration {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 10px;
  padding: 2px 5px;
  border-radius: 2px;
}

.compilation-video-item .video-title {
  font-size: 14px;
  font-weight: bold;
  margin-top: 5px;
  margin-bottom: 3px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.compilation-video-item .video-stats {
  font-size: 12px;
  color: var(--text-dim);
  margin: 0;
}

/* 分页组件样式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 公告 */
.announcement-text {
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.edit-announcement {
  width: 100%;
  text-align: center;
}

/* 个人资料 */
.info-item {
  display: flex;
  margin-bottom: 10px;
}

.info-item .label {
  width: 80px;
  color: var(--text-secondary);
  text-align: right;
  margin-right: 10px;
}

.info-item .value {
  color: var(--text-primary);
  flex: 1;
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
  
  .video-list {
    grid-template-columns: repeat(2, 1fr);
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
  
  .video-list {
    grid-template-columns: 1fr;
  }
}
</style>
