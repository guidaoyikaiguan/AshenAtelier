<template>
  <div class="followers-page">
    <!-- 顶部导航栏 -->
    <Header />
    
    <!-- 主内容区 -->
    <div class="main-content">
      <div class="followers-container">
        <div class="page-title">仰慕的旅人们</div>
        
        <div class="followers-content">
          <!-- 左侧导航栏 -->
          <div class="left-nav">
            <div class="nav-section">
              <div class="section-title">我的关注</div>
              <div class="nav-item" @click="$router.push('/following')">
                <el-icon><UserFilled /></el-icon>
                <span>全部关注</span>
                <span class="count">{{ userInfo.followingCount || 0 }}</span>
              </div>
            </div>
            
            <div class="nav-section">
              <div class="section-title">仰慕的旅人们</div>
              <div class="nav-item active">
                <el-icon><Star /></el-icon>
                <span>仰慕的旅人们</span>
                <span class="count">{{ userInfo.followerCount || 0 }}</span>
              </div>
            </div>
          </div>
          
          <!-- 右侧粉丝列表 -->
          <div class="right-content">
            <!-- 顶部操作栏 -->
            <div class="top-bar">
              <div class="tab-buttons">
                <el-button :type="activeTab === 'recent' ? 'primary' : 'default'" @click="activeTab = 'recent'">最近关注</el-button>
                <el-button :type="activeTab === 'visited' ? 'primary' : 'default'" @click="activeTab = 'visited'">最近访问</el-button>
              </div>
              <div class="search-box">
                <el-input v-model="searchKeyword" placeholder="输入关键词" clearable style="width: 200px;">
                  <template #append>
                    <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
                  </template>
                </el-input>
              </div>
            </div>
            
            <!-- 粉丝列表 -->
            <div class="followers-list">
              <div v-for="(item, index) in followersList" :key="index" class="follower-item" @click="goToUserProfile(item.id)">
                <div class="user-avatar">
                  <el-avatar :size="50" :src="item.avatar ? '' + item.avatar : '/default-avatar.png'">
                    {{ item.nickName?.charAt(0) || '用' }}
                  </el-avatar>
                </div>
                <div class="user-info">
                  <div class="user-name">{{ item.nickName }}</div>
                  <div class="user-desc">{{ item.description || '这个人没有填写简介' }}</div>
                </div>
                <div class="action-button">
                  <el-button 
                    :type="item.isFollow ? 'default' : 'primary'" 
                    size="small"
                    :disabled="item.isFollow"
                  >
                    {{ item.isFollow ? '已关注' : '回关' }}
                  </el-button>
                </div>
              </div>
              
              <!-- 无数据提示 -->
              <el-empty v-if="followersList.length === 0" description="暂无粉丝" />
            </div>
            
            <!-- 分页 -->
            <div class="pagination" v-if="followersList.length > 0">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                layout="prev, pager, next, jumper, ->, total"
                :total="total"
                :page-count="Math.ceil(total / pageSize)"
                :pager-count="8"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { inject } from 'vue'
import { useLoginStore } from '@/stores/loginStore'
import Header from '@/components/Header.vue'
import { UserFilled, Star, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loginStore = useLoginStore()
const Request = inject('Request')
const Api = inject('Api')

// 获取URL参数中的用户ID
const targetUserId = ref((route.query.userId && route.query.userId !== 'undefined') ? route.query.userId : loginStore.userInfo.userId)

// 当前激活的标签
const activeTab = ref('recent')
// 搜索关键词
const searchKeyword = ref('')
// 分页参数
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 用户信息
const userInfo = reactive({
  followingCount: 0,
  followerCount: 0
})

// 粉丝列表
const followersList = ref([])

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const result = await Request({
      url: Api.getUserInfo,
      params: { userId: targetUserId.value }
    })
    if (result) {
      userInfo.followingCount = result.data.followingCount || 0
      userInfo.followerCount = result.data.followerCount || 0
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 加载粉丝列表
const loadFollowersList = async () => {
  try {
    const fans = {
      userId: targetUserId.value,
      pageNo: currentPage.value,
      pageSize: pageSize.value
    }
    const result = await Request({
      url: Api.getFollowerList,
      method: 'post',
      data: fans
    })
    console.log('粉丝列表返回数据:', result)
    if (result && result.data) {
      console.log('粉丝列表数据:', result.data)
      followersList.value = result.data
      total.value = result.data.length
    }
  } catch (error) {
    console.error('加载粉丝列表失败:', error)
    ElMessage.error('加载粉丝列表失败，请重试')
  }
}

// 搜索粉丝
const handleSearch = () => {
  // 实现搜索功能
  console.log('搜索:', searchKeyword.value)
}

// 跳转到用户个人中心
const goToUserProfile = (userId) => {
  router.push(`/profile/${userId}`)
}

// 监听路由参数变化，重新加载数据
watch(() => route.query.userId, (newUserId) => {
  if (newUserId && newUserId !== 'undefined') {
    targetUserId.value = newUserId
    loadUserInfo()
    loadFollowersList()
  }
})

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  loadFollowersList()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadFollowersList()
}

onMounted(() => {
  loadUserInfo()
  loadFollowersList()
})
</script>

<style scoped>
.followers-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

.main-content {
  margin-top: 80px; /* 60px header + 20px padding */
  padding: 0 20px;
}

.followers-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: var(--text-primary);
}

.followers-content {
  display: flex;
  gap: 20px;
  background-color: rgba(255,255,255,0.04);
  border-radius: 5px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 左侧导航栏 */
.left-nav {
  width: 200px;
  border-right: 1px solid var(--glass-border);
}

.nav-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: var(--text-dim);
  margin-bottom: 10px;
  padding-left: 20px;
}

.nav-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  margin-bottom: 5px;
  cursor: pointer;
  border-radius: 5px;
  transition: all 0.3s;
}

.nav-item:hover {
  background-color: var(--bg-secondary);
}

.nav-item.active {
  background-color: rgba(147, 112, 219, 0.15);
  color: var(--color-primary-light);
}

.nav-item .el-icon {
  margin-right: 10px;
  font-size: 16px;
}

.count {
  font-size: 12px;
  color: var(--text-dim);
  background-color: var(--bg-secondary);
  padding: 2px 8px;
  border-radius: 10px;
}

.nav-item.active .count {
  background-color: #bae7ff;
  color: var(--color-primary-light);
}

/* 右侧内容区 */
.right-content {
  flex: 1;
  padding-left: 30px;
}

/* 顶部操作栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--glass-border);
}

.tab-buttons {
  display: flex;
  gap: 10px;
}

.search-box {
  display: flex;
  align-items: center;
}

/* 粉丝列表 */
.followers-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.follower-item {
  display: flex;
  align-items: center;
  padding: 15px;
  background-color: var(--bg-secondary);
  border-radius: 8px;
  transition: all 0.3s;
}

.follower-item:hover {
  background-color: rgba(255,255,255,0.06);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.user-avatar {
  margin-right: 15px;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-desc {
  font-size: 12px;
  color: var(--text-dim);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.action-button {
  margin-left: 15px;
}

/* 分页 */
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.pagination :deep(.el-pagination__total) {
  order: 3;
  margin-left: 10px;
  font-size: 14px;
  color: var(--text-secondary);
}

.pagination :deep(.el-pagination__jump) {
  order: 2;
  margin-left: 10px;
  font-size: 14px;
  color: var(--text-secondary);
}

.pagination :deep(.el-pagination__prev),
.pagination :deep(.el-pagination__next),
.pagination :deep(.el-pager li) {
  margin: 0 5px;
  padding: 0 10px;
  height: 32px;
  line-height: 32px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: rgba(255,255,255,0.04);
  cursor: pointer;
}

.pagination :deep(.el-pager li:hover) {
  color: #409eff;
  border-color: #c6e2ff;
}

.pagination :deep(.el-pager li.active) {
  color: #fff;
  background-color: #409eff;
  border-color: #409eff;
}

.pagination :deep(.el-pagination__prev:hover),
.pagination :deep(.el-pagination__next:hover) {
  color: #409eff;
  border-color: #c6e2ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .followers-content {
    flex-direction: column;
  }
  
  .left-nav {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--glass-border);
    margin-bottom: 20px;
  }
  
  .right-content {
    padding-left: 0;
  }
  
  .top-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .followers-list {
    grid-template-columns: 1fr;
  }
}
</style>
