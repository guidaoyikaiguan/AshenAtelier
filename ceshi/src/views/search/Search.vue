<template>
  <div class="search-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <input 
        v-model="searchInput" 
        type="text" 
        placeholder="水晶球占卜..." 
        class="search-input"
        @keyup.enter="handleSearch"
      >
      <button @click="handleSearch" class="search-button">占卜</button>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      魔女正在占卜中...
    </div>
    
    <!-- 错误信息 -->
    <div v-else-if="error" class="error">
      搜索失败: {{ error }}
    </div>
    
    <!-- 搜索结果 -->
    <div v-else-if="hasResults" class="search-results">
      <!-- 用户结果 -->
      <div v-if="users.length > 0" class="section">
        <h3 class="section-title">旅人</h3>
        <div class="user-results">
          <div v-for="user in users" :key="user.userId" class="user-card" @click="goToUserProfile(user.userId)">
            <div class="user-avatar">
              <img :src="getUserAvatar(user.avatar)" :alt="user.nickName">
            </div>
            <div class="user-info">
              <h4 class="user-name">{{ user.nickName }}</h4>
              <p class="user-signature">{{ user.mySignature || '这个人很懒，什么都没写' }}</p>
              <div class="user-stats">
                <span class="user-followers">{{ user.followerCount || 0 }} 粉丝</span>
              </div>
            </div>
            <div class="user-action" @click.stop="toggleFollow(user)">
              <button 
                class="follow-button" 
                :class="{ 'following': user.isFollowed }"
              >
                {{ user.isFollowed ? '已关注' : '关注' }}
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 视频结果 -->
      <div v-if="videos.length > 0" class="section">
        <h3 class="section-title">视频</h3>
        <div class="video-results">
          <div v-for="video in videos" :key="video.videoId" class="video-card" @click="goToVideoDetail(video.videoId)">
            <div class="video-cover">
              <img :src="video.coverUrl" :alt="video.title">
              <span class="video-duration">{{ video.duration }}</span>
            </div>
            <div class="video-info">
              <h3 class="video-title">{{ video.title }}</h3>
              <div class="video-stats">
                <span class="video-author">{{ video.nickName }}</span>
                <span class="video-play-count">{{ video.playCount }} 播放</span>
                <span class="video-time">{{ formatDate(video.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 无结果提示 -->
    <div v-else-if="keyword" class="no-results">
      水晶球中一片模糊，未寻得结果
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginStore } from '@/stores/loginStore'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const loginStore = useLoginStore()
const keyword = ref(route.query.keyword || '')
const searchInput = ref(keyword.value)
const videos = ref([])
const users = ref([])
const loading = ref(false)
const error = ref('')

// 计算是否有搜索结果
const hasResults = computed(() => videos.value.length > 0 || users.value.length > 0)

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 获取用户头像
const getUserAvatar = (avatar) => {
  if (!avatar) return '/default-avatar.png'
  return '/avatar/' + avatar
}

// 跳转到用户主页
const goToUserProfile = async (userId) => {
  try {
    // 检查当前用户是否登录
    if (loginStore.userInfo && loginStore.userInfo.userId) {
      // 检查是否关注该用户
      const response = await request({
        url: `/api/user/selectIfFollow`,
        method: 'get',
        params: { userId }
      })
      
      if (response && response.success) {
        const isFollowed = response.data
        console.log('用户关注状态:', isFollowed)
        
        // 根据关注状态调用不同的接口
        if (isFollowed) {
          console.log('已关注，调用getAttentionUserInfo')
          // 这里可以调用getAttentionUserInfo接口
        } else {
          console.log('未关注，调用getEXAttentionUserInfo')
          // 这里可以调用getEXAttentionUserInfo接口
        }
      }
    }
    
    // 跳转到用户主页
    router.push({ path: `/profile/${userId}` })
  } catch (err) {
    console.error('跳转到用户主页失败:', err)
    // 即使出错也跳转到用户主页
    router.push({ path: `/profile/${userId}` })
  }
}

// 跳转到视频详情页
const goToVideoDetail = (videoId) => {
  router.push({ path: `/video/${videoId}` })
}

// 切换关注状态
const toggleFollow = async (user) => {
  if (!loginStore.userInfo || Object.keys(loginStore.userInfo).length === 0) {
    // 如果未登录，跳转到登录页
    router.push('/login')
    return
  }
  
  try {
    // 临时更新状态，提升用户体验
    user.isFollowed = !user.isFollowed
    
    // 调用关注/取消关注接口
    const response = await request({
      url: `/api/user/followUser`,
      method: 'post',
      params: { userId: user.userId }
    })
    
    if (response && response.success) {
      // 更新粉丝数
      if (user.isFollowed) {
        user.followerCount = (user.followerCount || 0) + 1
      } else {
        user.followerCount = Math.max(0, (user.followerCount || 0) - 1)
      }
    } else {
      // 如果失败，恢复原来的状态
      user.isFollowed = !user.isFollowed
      throw new Error('操作失败')
    }
  } catch (err) {
    console.error('关注操作失败:', err)
    alert('操作失败，请稍后重试')
  }
}

// 加载用户关注状态
const loadUserFollowStatus = async () => {
  console.log('开始加载关注状态:', { userInfo: loginStore.userInfo, usersLength: users.value.length })
  if (!loginStore.userInfo || Object.keys(loginStore.userInfo).length === 0 || users.value.length === 0) {
    console.log('跳过加载关注状态，原因:', !loginStore.userInfo || Object.keys(loginStore.userInfo).length === 0 ? '未登录' : '无用户数据')
    return
  }
  
  try {
    const currentUserId = loginStore.userInfo.userId
    console.log('当前用户ID:', currentUserId)
    console.log('需要检查的用户列表:', users.value.map(user => ({ userId: user.userId, nickName: user.nickName })))
    
    // 为每个用户调用selectIfFollow方法
    for (const user of users.value) {
      console.log('检查用户关注状态:', user.userId, user.nickName)
      const response = await request({
        url: `/api/user/selectIfFollow`,
        method: 'get',
        params: { userId: user.userId }
      })
      
      console.log('关注状态响应:', user.userId, response)
      if (response && response.success) {
        user.isFollowed = response.data || false
        console.log('更新用户关注状态:', user.userId, user.isFollowed)
      }
    }
  } catch (err) {
    console.error('加载关注状态失败:', err)
  }
}

// 调用后端搜索接口
const fetchSearchResults = async () => {
  const tag = route.query.tag
  if (!keyword.value && !tag) return
  
  loading.value = true
  error.value = ''
  
  try {
    let response
    
    // 检查是否有tag参数
    if (tag) {
      // 使用标签搜索
      response = await request({
        url: `/api/video/search/byTags`,
        method: 'get',
        params: {
          tags: tag,
          page: 0,
          size: 20
        }
      })
    } else {
      // 使用关键词搜索
      response = await request({
        url: `/api/video/search/keyword`,
        method: 'get',
        params: {
          keyword: keyword.value,
          page: 0,
          size: 20
        }
      })
    }
    
    if (response && (response.success || response.status === 200)) {
      // 检查返回数据格式
      if (response.data && Array.isArray(response.data)) {
        // 后端直接返回视频数组的情况
        videos.value = response.data || []
        users.value = []
      } else if (response.data && response.data.videos) {
        // 后端返回包含videos和users字段的对象的情况
        videos.value = response.data.videos || []
        users.value = response.data.users || []
      } else {
        videos.value = []
        users.value = []
      }
      
      // 加载用户关注状态
      await loadUserFollowStatus()
    } else {
      throw new Error('搜索失败')
    }
  } catch (err) {
    error.value = err.message
    videos.value = []
    users.value = []
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  if (searchInput.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchInput.value.trim() } })
  }
}

// 监听路由变化，更新关键词和标签
watch(() => [route.query.keyword, route.query.tag], ([newKeyword, newTag]) => {
  keyword.value = newKeyword || ''
  searchInput.value = keyword.value
  if (keyword.value || newTag) {
    fetchSearchResults()
  }
}, { immediate: true })

onMounted(() => {
  const tag = route.query.tag
  console.log('Search page mounted with keyword:', keyword.value, 'tag:', tag)
  if (keyword.value || tag) {
    fetchSearchResults()
  }
})
</script>

<style scoped>
.search-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 搜索栏样式 */
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
  max-width: 600px;
}

.search-input {
  flex: 1;
  padding: 10px 16px;
  font-size: 14px;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  outline: none;
  transition: border-color 0.3s;
}

.search-input:focus {
  border-color: #409eff;
}

.search-button {
  padding: 0 20px;
  font-size: 14px;
  color: white;
  background-color: #409eff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-button:hover {
  background-color: #66b1ff;
}

.search-button:active {
  background-color: #3a8ee6;
}

.loading, .error, .no-results {
  padding: 60px 0;
  text-align: center;
  color: var(--text-secondary);
}

.error {
  color: #ff4d4f;
}

/* 搜索结果区域 */
.search-results {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.section {
  width: 100%;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}

/* 用户结果样式 */
.user-results {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  background-color: var(--bg-secondary);
  cursor: pointer;
  transition: background-color 0.3s, transform 0.2s;
}

.user-card:hover {
  background-color: rgba(255,255,255,0.06);
  transform: translateY(-2px);
}

.user-avatar {
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-signature {
  font-size: 13px;
  color: var(--text-dim);
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-stats {
  font-size: 12px;
  color: var(--text-secondary);
}

.user-followers {
  display: inline-block;
}

.user-action {
  flex-shrink: 0;
}

.follow-button {
  padding: 6px 16px;
  font-size: 13px;
  border: 1px solid #409eff;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: rgba(255,255,255,0.04);
  color: #409eff;
}

.follow-button:hover {
  background-color: #ecf5ff;
}

.follow-button.following {
  background-color: rgba(255,255,255,0.06);
  color: var(--text-secondary);
  border-color: #dcdfe6;
}

.follow-button.following:hover {
  background-color: rgba(255,255,255,0.08);
}

/* 视频结果样式 */
.video-results {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.video-card {
  transition: transform 0.2s;
  cursor: pointer;
}

.video-card:hover {
  transform: translateY(-2px);
}

.video-cover {
  position: relative;
  margin-bottom: 10px;
  border-radius: 4px;
  overflow: hidden;
}

.video-cover img {
  width: 100%;
  aspect-ratio: 16/9;
  object-fit: cover;
  border-radius: 4px;
}

.video-duration {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 2px;
}

.video-info {
  min-height: 100px;
}

.video-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
  margin: 0 0 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-stats {
  font-size: 12px;
  color: var(--text-dim);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.video-author {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.video-play-count {
  display: block;
}

.video-time {
  display: block;
}

/* 响应式布局 */
@media (min-width: 768px) {
  .video-results {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 24px;
  }
  
  .user-results {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 24px;
  }
}

@media (min-width: 1024px) {
  .video-results {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 28px;
  }
  
  .user-results {
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
    gap: 28px;
  }
}
</style>
