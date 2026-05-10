<template>
  <div class="history-page">
    <!-- 导航栏和搜索框 -->
    <div class="history-nav">
      <div class="nav-right">
        <el-input v-model="searchText" placeholder="" class="search-input" clearable>
          <template #append>
            <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        <el-button @click="clearVideoHistory" size="small" class="clear-button">清空历史</el-button>
        <template v-if="!isBatchMode">
          <el-button @click="batchManage" size="small" class="batch-button">批量管理</el-button>
        </template>
        <template v-else>
          <el-button @click="deleteBatchVideoHistory" size="small" class="batch-delete-button" :disabled="selectedHistoryIds.length === 0">批量删除</el-button>
          <el-button @click="batchManage" size="small" class="cancel-batch-button">取消</el-button>
        </template>
      </div>
    </div>

    <!-- 页面头部 -->
    <div class="history-header">
      <h2>走过的风景</h2>
    </div>

    <!-- 历史记录列表 -->
    <div v-if="Object.keys(groupedHistory).length > 0" class="history-list">
      <div v-for="(group, date) in groupedHistory" :key="date" class="history-group">
        <div class="group-date">
          <span class="date-dot"></span>
          {{ date }}
        </div>
        <div class="history-items">
          <div class="history-item" v-for="history in group" :key="history.videoId">
            <div class="item-left" @click="goToVideoDetail(history.videoId)">
              <el-checkbox v-if="isBatchMode" v-model="selectedHistoryIds" :value="history.videoId" class="history-checkbox">
                <template #label></template>
              </el-checkbox>
              <div class="video-thumbnail">
                <img :src="history.cover_url || '/placeholder.jpg'" alt="视频封面" />
                <div class="video-duration">{{ typeof history.duration === 'string' ? history.duration : formatTime(history.duration || 0) }}</div>
              </div>
              <div class="video-info">
                <div class="video-title">{{ history.title || '未知视频' }}</div>
                <div class="video-meta">
                  <span class="up-name">{{ history.nick_name || '未知UP主' }}</span>
                  <span class="view-time">{{ formatDate(history.lastWatchTime) }}</span>
                </div>
              </div>
            </div>
            <div class="item-right">
              <el-button type="text" @click.stop="deleteVideoHistory(history.videoId)" size="small">
                删除
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 加载更多提示 -->
        <div v-if="isLoadingMore" class="loading-more">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        
        <!-- 无更多数据提示 -->
        <div v-else-if="!hasMore" class="no-more">
          没有更多历史记录了
        </div>
      </div>
      

    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-history">
      <el-empty description="暂无观看历史" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Timer, Setting, Search, Loading } from '@element-plus/icons-vue'
import VideoItem from '@/components/VideoItem.vue'
import { useLoginStore } from '@/stores/loginStore'
import api from '@/api'
import Request from '@/utils/request'

const router = useRouter()
const loginStore = useLoginStore()

// 响应式数据
const historyList = ref([])
const isLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const recordHistory = ref(true)
const searchText = ref('')
const isBatchMode = ref(false)
const selectedHistoryIds = ref([])
const activeTab = ref('综合')

// 滚动加载相关
const isLoadingMore = ref(false) // 标记是否正在加载更多数据
const hasMore = ref(true) // 标记是否还有更多数据

// 计算属性
const isLoggedIn = computed(() => loginStore.isLoggedIn)

// 按日期分组的历史记录
const groupedHistory = computed(() => {
  const groups = {}
  
  historyList.value.forEach(history => {
    if (!history.lastWatchTime) return
    
    const date = new Date(history.lastWatchTime)
    const dateKey = `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
    
    if (!groups[dateKey]) {
      groups[dateKey] = []
    }
    groups[dateKey].push(history)
  })
  
  return groups
})

// 格式化时间
const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载历史记录
const loadHistoryList = async (loadMore = false) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (loadMore && (isLoadingMore.value || !hasMore.value)) {
    return
  }
  
  try {
    const userId = loginStore.userInfo?.userId
    if (!userId) {
      ElMessage.error('获取用户信息失败')
      return
    }
    
    const params = { 
      userId,
      pageNo: loadMore ? currentPage.value + 1 : 1,
      pageSize: pageSize.value
    }
    
    if (searchText.value) {
      params.keyword = searchText.value
    }
    
    if (activeTab.value !== '综合') {
      params.type = activeTab.value
    }
    
    if (loadMore) {
      isLoadingMore.value = true
    } else {
      isLoading.value = true
    }
    
    const res = await Request({
      url: api.getVideoHistory,
      params: params,
      method: 'get'
    })
    
    if (res && res.success) {
      const data = res.data || []
      
      if (loadMore) {
        // 追加数据
        historyList.value = [...historyList.value, ...data]
        currentPage.value++
      } else {
        // 替换数据
        historyList.value = data
        currentPage.value = 1
      }
      
      total.value = res.total || data.length
      hasMore.value = historyList.value.length < total.value
      
      // 打印返回的数据，方便调试
      console.log('历史记录数据:', data)
    } else {
      ElMessage.error('获取历史记录失败')
    }
  } catch (error) {
    console.error('获取历史记录失败:', error)
    ElMessage.error('获取历史记录失败')
  } finally {
    isLoading.value = false
    isLoadingMore.value = false
  }
}

// 删除单个历史记录
const deleteVideoHistory = async (videoId) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const userId = loginStore.userInfo?.userId
    if (!userId) {
      ElMessage.error('获取用户信息失败')
      return
    }
    
    const res = await Request({
      url: api.deleteVideoHistory,
      params: { userId, videoId },
      method: 'get'
    })
    
    if (res && (res.success || res.code === 200)) {
      ElMessage.success('删除成功')
      loadHistoryList()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    console.error('删除历史记录失败:', error)
    ElMessage.error('删除历史记录失败')
  }
}

// 清空历史记录
const clearVideoHistory = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const userId = loginStore.userInfo?.userId
    if (!userId) {
      ElMessage.error('获取用户信息失败')
      return
    }
    
    const res = await Request({
      url: api.clearVideoHistory,
      params: { userId },
      method: 'get'
    })
    
    if (res && (res.success || res.code === 200)) {
      ElMessage.success('清空成功')
      loadHistoryList()
    } else {
      ElMessage.error('清空失败')
    }
  } catch (error) {
    console.error('清空历史记录失败:', error)
    ElMessage.error('清空历史记录失败')
  }
}

// 跳转到视频详情页
const goToVideoDetail = (videoId) => {
  router.push(`/video/${videoId}`)
}

// 搜索历史记录
const handleSearch = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const userId = loginStore.userInfo?.userId
    if (!userId) {
      ElMessage.error('获取用户信息失败')
      return
    }
    
    const res = await Request({
      url: api.searchVideoHistory,
      params: {
        userId: userId,
        keyword: searchText.value
      },
      method: 'get'
    })
    
    if (res && res.success) {
      // 检查返回的数据格式，如果是直接返回的数组，就直接使用
      // 如果是包装在对象中的数据，就提取出来
      historyList.value = res.data || []
      total.value = res.data?.length || 0
      
      // 打印返回的数据，方便调试
      console.log('搜索历史记录数据:', res.data)
    } else {
      ElMessage.error('搜索历史记录失败')
    }
  } catch (error) {
    console.error('搜索历史记录失败:', error)
    ElMessage.error('搜索历史记录失败')
  }
}

// 批量管理
const batchManage = () => {
  isBatchMode.value = !isBatchMode.value
  selectedHistoryIds.value = []
}

// 切换选中状态
const toggleHistorySelection = (historyId) => {
  const index = selectedHistoryIds.value.indexOf(historyId)
  if (index > -1) {
    selectedHistoryIds.value.splice(index, 1)
  } else {
    selectedHistoryIds.value.push(historyId)
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  if (selectedHistoryIds.value.length === getAllHistoryIds().length) {
    selectedHistoryIds.value = []
  } else {
    selectedHistoryIds.value = getAllHistoryIds()
  }
}

// 获取所有历史记录ID
const getAllHistoryIds = () => {
  const allIds = []
  Object.values(groupedHistory.value).forEach(dayHistories => {
    dayHistories.forEach(history => {
      if (history.videoId) {
        allIds.push(history.videoId)
      }
    })
  })
  return allIds
}

// 批量删除历史记录
const deleteBatchVideoHistory = async () => {
  if (selectedHistoryIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的历史记录')
    return
  }
  
  try {
    const userId = loginStore.userInfo?.userId
    if (!userId) {
      ElMessage.error('获取用户信息失败')
      return
    }
    
    const result = await Request({
      url: api.deleteBatchVideoHistory,
      method: 'GET',
      params: {
        videoIds: selectedHistoryIds.value.join(','),
        userId: userId
      }
    })
    
    if (result && (result.success || result.code === 200)) {
      ElMessage.success('批量删除历史记录成功')
      // 重新加载历史记录
      await loadHistoryList()
      // 退出批量管理模式
      isBatchMode.value = false
      selectedHistoryIds.value = []
    } else {
      ElMessage.error('批量删除历史记录失败')
    }
  } catch (error) {
    console.error('批量删除历史记录失败:', error)
    ElMessage.error('批量删除历史记录失败，请重试')
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  loadHistoryList()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadHistoryList()
}

// 监听滚动事件
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight
  const clientHeight = document.documentElement.clientHeight || window.innerHeight
  
  // 当滚动到距离底部100px时触发加载
  if (scrollTop + clientHeight >= scrollHeight - 100 && hasMore.value && !isLoadingMore.value) {
    loadHistoryList(true)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadHistoryList()
  // 添加滚动事件监听
  window.addEventListener('scroll', handleScroll)
})

// 组件卸载时移除滚动事件监听
onUnmounted(() => {
  // 移除滚动事件监听
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.history-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: var(--bg-secondary);
  min-height: 100vh;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--glass-border);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 24px;
  color: var(--text-primary);
}

.history-header h2 {
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.menu-button {
  width: 36px;
  height: 36px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.history-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background-color: rgba(255,255,255,0.04);
  padding: 10px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-item {
  padding: 8px 16px;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-item:hover {
  background-color: rgba(255,255,255,0.06);
}

.nav-item.active {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.more-filter {
  display: flex;
  align-items: center;
  gap: 5px;
}

.dropdown-icon {
  font-size: 12px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  width: 300px;
}

.clear-button,
.batch-button {
  font-size: 14px;
}

.history-list {
  margin-bottom: 30px;
}

.history-group {
  margin-bottom: 30px;
}

.group-date {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

.date-dot {
  width: 8px;
  height: 8px;
  background-color: #409eff;
  border-radius: 50%;
}

.history-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.history-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.item-left {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
  cursor: pointer;
}

.history-checkbox {
  margin-right: 0;
  flex-shrink: 0;
}

.history-checkbox .el-checkbox__label {
  display: none;
}

.video-thumbnail {
  position: relative;
  width: 160px;
  height: 90px;
  border-radius: 4px;
  overflow: hidden;
}

.video-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-duration {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background-color: rgba(0, 0, 0, 0.7);
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 2px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.watch-duration {
  color: #fff;
}

.duration-separator {
  color: #fff;
}

.total-duration {
  color: #fff;
}

.video-info {
  flex: 1;
  min-width: 0;
}

.video-title {
  font-size: 16px;
  color: var(--text-primary);
  margin-bottom: 8px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
  color: var(--text-dim);
}

.up-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.view-time {
  font-size: 12px;
}

.item-right {
  display: flex;
  align-items: center;
}

.history-pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding: 20px;
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.empty-history {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.loading-more,
.no-more {
  text-align: center;
  padding: 20px;
  color: var(--text-dim);
  font-size: 14px;
}

.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .history-page {
    padding: 10px;
  }
  
  .history-nav {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .nav-left {
    justify-content: space-around;
  }
  
  .nav-item {
    padding: 6px 12px;
    font-size: 12px;
  }
  
  .search-input {
    width: 100%;
  }
  
  .history-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .item-left {
    width: 100%;
  }
  
  .video-thumbnail {
    width: 120px;
    height: 67.5px;
  }
  
  .video-title {
    font-size: 14px;
  }
  
  .video-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .item-right {
    align-self: flex-end;
  }
  
  .loading-more,
  .no-more {
    padding: 15px;
    font-size: 12px;
  }
}
</style>
