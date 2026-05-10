<template>
  <div class="sidebar">
    <div class="sidebar-content">
      <!-- 首页按钮 -->
      <div class="nav-section">
        <div 
          class="nav-item" 
          :class="{ active: currentRoute === '/' }"
          @click="goToHome"
        >
          <el-icon><HomeFilled /></el-icon>
          <span>萤石驿站</span>
        </div>
      </div>
      
      <!-- 分类导航 -->
      <div class="nav-section">
        <div class="nav-section-title">魔导书库</div>
        <div 
          v-for="category in categories" 
          :key="category.categoryId" 
          class="nav-item" 
          :class="{ active: currentRoute.includes(`/category/${category.categoryId}`) }"
          @click="goToCategory(category.categoryId, category.categoryName)"
        >
          <el-icon><Collection /></el-icon>
          <span>{{ category.categoryName }}</span>
        </div>
      </div>

      <!-- 旅人见闻 -->
      <div class="nav-section">
        <div class="nav-section-title">旅人见闻</div>
        <div
          class="nav-item"
          :class="{ active: currentRoute === '/moment' }"
          @click="goToMoment"
        >
          <el-icon><Notebook /></el-icon>
          <span>所有见闻</span>
        </div>
        <div
          class="nav-item"
          :class="{ active: currentRoute === '/moment/my' }"
          @click="goToMyMoment"
        >
          <el-icon><User /></el-icon>
          <span>我的见闻</span>
        </div>
      </div>

      <!-- 呼唤魔女 -->
      <div class="nav-section mascot-section">
        <div class="nav-item mascot-toggle" @click="toggleMascot">
          <el-icon><MagicStick /></el-icon>
          <span>呼唤魔女</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Collection, HomeFilled, MagicStick, Notebook, User } from '@element-plus/icons-vue'
import { mitter } from '@/eventbus/eventBus'

const route = useRoute()
const router = useRouter()

// 注入全局工具
const Request = inject('Request')
const Api = inject('Api')

// 当前路由路径
const currentRoute = computed(() => route.path)

// 分类数据
const categories = ref([])

// 从后端加载分类数据
const loadCategories = async () => {
  try {
    const result = await Request({
      url: Api.loadCategories,
    })
    if (result) {
      categories.value = result.data
    }
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

// 跳转到分类页面
const goToCategory = (categoryId, categoryName) => {
  router.push(`/category/${categoryId}?name=${encodeURIComponent(categoryName)}`)
  console.log('Go to category:', categoryId, categoryName)
}

// 跳转到首页
const goToHome = () => {
  router.push('/')
  console.log('Go to home page')
}

// 跳转到旅人见闻
const goToMoment = () => {
  router.push('/moment')
}

// 跳转到我的见闻
const goToMyMoment = () => {
  router.push('/moment/my')
}

// 呼唤魔女
const toggleMascot = () => {
  mitter.emit('toggle-mascot')
}

// 组件挂载时加载分类数据
onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.sidebar {
  width: 200px;
  min-height: calc(100vh - 60px);
  background: rgba(22, 20, 48, 0.9);
  backdrop-filter: blur(16px);
  border-right: 1px solid rgba(255,255,255,0.06);
  position: fixed;
  left: 0;
  top: 60px;
  bottom: 0;
  overflow-y: auto;
  z-index: 80;
}

.sidebar-content {
  padding: 16px 0;
  min-height: 100%;
  position: relative;
}

.nav-section {
  margin-bottom: 16px;
}

.nav-section-title {
  padding: 0 20px 10px;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-dim);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  margin: 0 8px;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.nav-item:hover {
  background: rgba(147, 112, 219, 0.1);
  color: var(--color-primary-light);
}

.nav-item.active {
  background: rgba(147, 112, 219, 0.15);
  color: var(--color-primary-light);
  font-weight: 600;
}

.nav-item .el-icon {
  margin-right: 10px;
  font-size: 18px;
}

/* 响应式设计 */
.mascot-section {
  position: absolute;
  bottom: 16px;
  left: 0;
  right: 0;
  margin-bottom: 0;
}

.mascot-toggle {
  color: var(--color-champagne, #D3AF36) !important;
}

.mascot-toggle:hover {
  background: rgba(211, 175, 54, 0.12) !important;
  color: var(--color-champagne, #D3AF36) !important;
}

@media (max-width: 992px) {
  .sidebar {
    width: 60px;
  }

  .nav-item span {
    display: none;
  }

  .nav-section-title {
    display: none;
  }

  .nav-item .el-icon {
    margin-right: 0;
  }
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style>