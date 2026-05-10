<template>
  <div class="header">
    <!-- Logo -->
    <div class="logo">
      <router-link to="/" class="logo-link">
        <span class="logo-main">魔女之旅</span>
        <span class="logo-sub">THE JOURNEY OF ELAINA</span>
      </router-link>
    </div>

    <!-- 搜索栏 -->
    <div class="search-box">
      <div class="search-wrapper">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="searchText"
          class="search-input"
          placeholder="水晶球占卜中... 输入你想探寻的内容"
          @keyup.enter="handleSearch"
        />
      </div>
    </div>

    <!-- 右侧功能区 -->
    <div class="user-menu">
      <!-- 消息通知 -->
      <el-dropdown @command="handleCommand" popper-class="header-dropdown">
        <span class="user-menu-item">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="header-badge">
            <el-icon class="icon-btn"><Bell /></el-icon>
          </el-badge>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="notification">魔法信笺</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 大会员 -->
      
      <!-- 动态 -->
      <el-dropdown @command="handleCommand" popper-class="header-dropdown">
        <span class="user-menu-item">
          <el-icon class="icon-btn"><ChatLineSquare /></el-icon>
          <span class="menu-label">旅人见闻</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="moment">所有见闻</el-dropdown-item>
            <el-dropdown-item command="publishMoment">记录见闻</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 创作中心 -->
      <el-dropdown @command="handleCommand" popper-class="header-dropdown">
        <span class="user-menu-item">
          <el-icon class="icon-btn"><Edit /></el-icon>
          <span class="menu-label">绘写卷轴</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="upload">提交绘卷</el-dropdown-item>
            <el-dropdown-item command="creator">绘卷工坊</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 用户头像 -->
      <el-dropdown @command="handleCommand" popper-class="header-dropdown">
        <span class="user-menu-item user-avatar-item">
          <el-avatar :size="36" :src="getAvatarUrl(loginStore.userInfo.avatar)" class="header-avatar">
            {{ loginStore.userInfo.nickName?.charAt(0) || '用' }}
          </el-avatar>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">魔女行囊</el-dropdown-item>
            <el-dropdown-item command="history">走过的风景</el-dropdown-item>
            <el-dropdown-item command="collect">旅途珍藏</el-dropdown-item>
            <el-dropdown-item command="logout">结束旅程</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { inject } from 'vue'
import { useLoginStore } from '@/stores/loginStore'
import { Bell, Edit, Search, ChatLineSquare } from '@element-plus/icons-vue'

const router = useRouter()
const loginStore = useLoginStore()
const Request = inject('Request')
const Api = inject('Api')

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

// 搜索文本
const searchText = ref('')

// 未读消息数
const unreadCount = ref(0)

// 搜索处理
const handleSearch = () => {
  if (searchText.value.trim()) {
    // 跳转到搜索结果页面
    router.push({
      path: '/search',
      query: { keyword: searchText.value }
    })
  }
}

// 菜单命令处理
const handleCommand = (command) => {
  switch (command) {
    case 'notification':
      router.push('/chat')
      // 进入消息中心后重置未读消息数
      unreadCount.value = 0
      break
    case 'upload':
      router.push('/upload')
      break
    case 'creator':
      router.push('/creator')
      break
    case 'moment':
      router.push('/moment')
      break
    case 'publishMoment':
      router.push('/moment/publish')
      break
    case 'profile':
      router.push('/profile')
      break
    case 'history':
      router.push('/history')
      break
    case 'collect':
      router.push('/collect')
      break
    case 'logout':
      loginStore.logout()
      router.push('/login')
      break
    default:
      console.log('未知命令:', command)
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  if (loginStore.userInfo.userId) {
    try {
      const result = await Request({
        url: Api.getUserInfo,
        params: { userId: loginStore.userInfo.userId }
      })
      if (result) {
        loginStore.saveUserInfo(result.data)
      }
    } catch (error) {
      console.error('加载用户信息失败:', error)
    }
  }
}

// 加载未读消息数
const loadUnreadCount = async () => {
  if (loginStore.userInfo.userId) {
    try {
      // 这里使用获取会话列表的API，因为它会返回每个会话的未读消息数
      // 我们可以计算总和作为总未读消息数
      const result = await Request({
        url: Api.getSessions,
        params: { userId: loginStore.userInfo.userId }
      })
      if (result && result.success && result.data) {
        // 计算总未读消息数
        unreadCount.value = result.data.reduce((sum, session) => sum + (session.unreadCount || 0), 0)
        console.log('未读消息数:', unreadCount.value)
      }
    } catch (error) {
      console.error('加载未读消息数失败:', error)
    }
  }
}

onMounted(() => {
  loadUserInfo()
  loadUnreadCount()
})
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
  background: rgba(22, 20, 48, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

/* === Logo === */
.logo-link {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  text-decoration: none;
  gap: 0;
  line-height: 1;
}

.logo-main {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-accent);
  letter-spacing: 2px;
  text-shadow: 0 0 20px rgba(212, 175, 55, 0.3);
}

.logo-sub {
  font-size: 9px;
  font-weight: 500;
  color: var(--text-muted);
  letter-spacing: 2.5px;
  text-transform: uppercase;
}

/* === 搜索框 === */
.search-box {
  flex: 1;
  max-width: 460px;
  margin: 0 40px;
}

.search-wrapper {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 0 14px;
  height: 38px;
  transition: all 0.3s ease;
}

.search-wrapper:hover,
.search-wrapper:focus-within {
  border-color: rgba(147, 112, 219, 0.4);
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 0 16px rgba(147, 112, 219, 0.15);
}

.search-icon {
  color: var(--text-muted);
  font-size: 16px;
  margin-right: 8px;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text-primary);
  font-size: 13px;
  font-family: var(--font-body);
}

.search-input::placeholder {
  color: var(--text-dim);
}

/* === 右侧菜单 === */
.user-menu {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-menu-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.3s ease;
  font-size: 13px;
}

.user-menu-item:hover {
  color: var(--color-primary-light);
  background: rgba(147, 112, 219, 0.12);
}

.icon-btn {
  font-size: 19px;
}

.menu-label {
  font-size: 12px;
}

.header-badge :deep(.el-badge__content) {
  background: var(--color-primary);
}

.user-avatar-item {
  padding: 2px 6px;
  margin-left: 4px;
}

.header-avatar {
  border: 2px solid rgba(147, 112, 219, 0.3);
  transition: border-color 0.3s ease;
}

.user-avatar-item:hover .header-avatar {
  border-color: var(--color-accent);
}
</style>
