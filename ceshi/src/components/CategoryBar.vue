<template>
  <div class="category-bar">
    <div class="category-container">
      <!-- 全部分类按钮 -->
      <el-dropdown @command="handleCategoryCommand">
        <span class="category-item all-category">
          <el-icon><Menu /></el-icon>
          <span>全部分类</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="all">全部</el-dropdown-item>
            <el-dropdown-item command="hot">热门</el-dropdown-item>
            <el-dropdown-item command="anime">动画</el-dropdown-item>
            <el-dropdown-item command="music">音乐</el-dropdown-item>
            <el-dropdown-item command="dance">舞蹈</el-dropdown-item>
            <el-dropdown-item command="game">游戏</el-dropdown-item>
            <el-dropdown-item command="tech">科技</el-dropdown-item>
            <el-dropdown-item command="food">美食</el-dropdown-item>
            <el-dropdown-item command="sports">体育</el-dropdown-item>
            <el-dropdown-item command="car">汽车</el-dropdown-item>
            <el-dropdown-item command="documentary">纪录片</el-dropdown-item>
            <el-dropdown-item command="movie">电影</el-dropdown-item>
            <el-dropdown-item command="tv">电视剧</el-dropdown-item>
            <el-dropdown-item command="knowledge">知识</el-dropdown-item>
            <el-dropdown-item command="digital">数码</el-dropdown-item>
            <el-dropdown-item command="vlog">VLOG</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      
      <!-- 主要分类 -->
      <div class="main-categories">
        <router-link 
          v-for="category in categoryList" 
          :key="category.id" 
          :to="`/category/${category.id}`"
          class="category-item"
          :class="{ active: currentCategoryId === category.id }"
        >
          {{ category.name }}
        </router-link>
      </div>
      
      <!-- 右侧更多分类 -->
      <div class="more-categories">
        <el-dropdown @command="handleCategoryCommand">
          <span class="category-item">
            <span>更多</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item 
                v-for="category in moreCategoryList" 
                :key="category.id" 
                :command="`category_${category.id}`"
              >
                {{ category.name }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { inject } from 'vue'
import { useCategoryStore } from '@/stores/categoryStore'
import { Menu, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const categoryStore = useCategoryStore()
const Request = inject('Request')
const Api = inject('Api')

// 当前选中的分类ID
const currentCategoryId = ref('all')

// 主要分类列表
const categoryList = ref([
  { id: 'hot', name: '热门' },
  { id: 'anime', name: '动画' },
  { id: 'music', name: '音乐' },
  { id: 'dance', name: '舞蹈' },
  { id: 'game', name: '游戏' },
  { id: 'tech', name: '科技' },
  { id: 'food', name: '美食' },
  { id: 'sports', name: '体育' }
])

// 更多分类列表
const moreCategoryList = ref([
  { id: 'car', name: '汽车' },
  { id: 'documentary', name: '纪录片' },
  { id: 'movie', name: '电影' },
  { id: 'tv', name: '电视剧' },
  { id: 'knowledge', name: '知识' },
  { id: 'digital', name: '数码' },
  { id: 'vlog', name: 'VLOG' }
])

// 处理分类命令
const handleCategoryCommand = (command) => {
  if (command === 'all') {
    router.push('/')
  } else if (command.startsWith('category_')) {
    const categoryId = command.replace('category_', '')
    router.push(`/category/${categoryId}`)
  }
}

// 加载分类数据
const loadCategories = async () => {
  try {
    const result = await Request({
      url: Api.loadCategories
    })
    if (result) {
      // 处理后端返回的分类数据
      const allCategories = result.data || []
      // 根据需要将分类分为主要分类和更多分类
      // 这里暂时使用默认分类，实际项目中可以根据后端数据调整
      categoryStore.saveCategoryList(allCategories)
    }
  } catch (error) {
    console.error('加载分类数据失败:', error)
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-bar {
  background: var(--glass-bg-nav);
  backdrop-filter: var(--glass-blur-heavy);
  -webkit-backdrop-filter: var(--glass-blur-heavy);
  border-bottom: 1px solid var(--glass-border);
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  z-index: 90;
}

.category-container {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 40px;
  overflow-x: auto;
  white-space: nowrap;
}

.category-item {
  display: inline-flex;
  align-items: center;
  margin-right: 20px;
  height: 100%;
  padding: 0 10px;
  cursor: pointer;
  color: var(--text-primary);
  font-size: 14px;
  transition: color 0.3s, border-bottom-color 0.3s;
  text-decoration: none;
  border-bottom: 2px solid transparent;
}

.category-item:hover {
  color: var(--color-primary-light);
}

.category-item.active {
  color: var(--color-champagne);
  border-bottom-color: var(--color-champagne);
  font-weight: bold;
}

.all-category {
  margin-right: 10px;
}

.main-categories {
  display: flex;
  align-items: center;
}

.more-categories {
  margin-left: auto;
}

/* 自定义滚动条 */
.category-container::-webkit-scrollbar {
  height: 6px;
}

.category-container::-webkit-scrollbar-track {
  background: transparent;
}

.category-container::-webkit-scrollbar-thumb {
  background: rgba(147, 112, 219, 0.4);
  border-radius: 3px;
}

.category-container::-webkit-scrollbar-thumb:hover {
  background: rgba(147, 112, 219, 0.6);
}
</style>
