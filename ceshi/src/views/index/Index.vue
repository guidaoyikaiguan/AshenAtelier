<template>
  <Header />

  <!-- Hero Banner 区 -->
  <div class="hero-banner">
    <div class="hero-overlay"></div>
    <div class="hero-content">
      <div class="hero-left">
        <div class="hero-label">魔女之旅 · THE JOURNEY OF ELAINA</div>
        <h1 class="hero-title">邂逅旅途中的魔法</h1>
        <p class="hero-slogan">一位魔女、一次旅行、一个故事。行走在广阔的世界，邂逅不一样的风景与人们。</p>
        <button class="hero-btn" @click="goToIndex">
          <el-icon><VideoPlay /></el-icon>
          <span>踏上旅途</span>
        </button>
      </div>
      <div class="hero-right">
        <div class="hero-banner-carousel">
          <el-carousel :interval="5000" height="340px" arrow="never" indicator-position="none">
            <el-carousel-item v-for="(item, index) in carouselList" :key="index">
              <div class="hero-carousel-item" @click="goToBannerTarget(item)">
                <img :src="item.cover" :alt="item.title" />
                <div class="hero-carousel-caption">{{ item.title }}</div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
      </div>
    </div>
    <!-- 轮播指示器 -->
    <div class="hero-indicators" v-if="carouselList.length > 1">
      <span v-for="(item, i) in carouselList" :key="i" class="hero-dot"></span>
    </div>
  </div>

  <!-- 横向分类栏 -->
  <div class="category-bar">
    <div class="category-bar-inner">
      <span
        :class="['category-tab', currentCategoryId === null ? 'active' : '']"
        @click="goToIndex"
      >萤石驿站</span>
      <span
        v-for="cat in categoryList"
        :key="cat.categoryId"
        :class="['category-tab', currentCategoryId === cat.categoryId ? 'active' : '']"
        @click="goToCategory(cat.categoryId, cat.categoryName)"
      >{{ cat.categoryName }}</span>
    </div>
  </div>

  <!-- 主内容区 -->
  <div class="main-content">
    <!-- 星之指引 + 星空榜单 -->
    <div class="content-row">
      <div class="video-section">
        <div class="section-header">
          <h3 class="section-title">
            <span class="title-icon">✦</span>
            {{ currentCategoryName || '星之指引' }}
          </h3>
          <span class="section-refresh" @click="refreshVideos">
            <el-icon><RefreshRight /></el-icon> 刷新占卜
          </span>
        </div>
        <div class="video-grid">
          <div
            v-for="item in displayVideos"
            :key="item.id"
            class="video-card"
            @click="goToVideoDetail(item)"
          >
            <div class="video-cover-wrapper">
              <img :src="item.cover" :alt="item.title" />
              <div v-if="item.duration" class="video-dur">{{ item.duration }}</div>
              <div class="video-play-info">
                <el-icon><VideoPlay /></el-icon>
                <span>{{ formatPlayCount(item.playCount) }}</span>
              </div>
            </div>
            <div class="video-meta">
              <div class="video-card-title" :title="item.title">{{ item.title }}</div>
              <div class="video-card-author">{{ item.author }}</div>
            </div>
          </div>
        </div>
        <div v-if="loading" class="loading-more">
          <span class="loading-spinner"></span> 魔女吟唱中...
        </div>
        <div v-else-if="!hasMore && displayVideos.length > 0" class="no-more">✦ 已达旅途终点 ✦</div>
      </div>

      <!-- 星空榜单 -->
      <div class="hot-sidebar">
        <div class="hot-title">
          <span class="hot-title-icon">★</span> 星空榜单
        </div>
        <div
          v-for="(item, idx) in hotVideos"
          :key="item.id"
          class="hot-item"
          @click="goToVideoDetail(item)"
        >
          <span class="hot-rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
          <div class="hot-info">
            <div class="hot-video-title">{{ item.title }}</div>
            <div class="hot-video-author">旅人: {{ item.author }}</div>
            <div class="hot-video-stats">{{ formatPlayCount(item.playCount) }} 播放</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue'
import { useNavAction } from '@/stores/navActionStore'
import { ref, onMounted, onUnmounted, inject, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { computeHotScore } from '@/utils/utils'
import { RefreshRight, VideoPlay } from '@element-plus/icons-vue'

const navActionStore = useNavAction()
const router = useRouter()
const route = useRoute()

const Request = inject('Request')
const Api = inject('Api')

// 分类列表
const categoryList = ref([])

// 轮播图数据
const carouselList = ref([])

// 热门视频（右侧排行）
const hotVideos = ref([])

// 视频数据
const displayVideos = ref([])
const allVideos = ref([])

// 分页状态
const pageNo = ref(1)
const pageSize = ref(24)
const loading = ref(false)
const hasMore = ref(true)

// 当前分类
const currentCategoryId = ref(null)
const currentCategoryName = ref('推荐')

// 格式化播放量
const formatPlayCount = (count) => {
  const n = parseInt(count) || 0
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toString()
}

// 加载分类
const loadCategories = async () => {
  try {
    const result = await Request({ url: Api.loadCategories })
    if (result && result.success && result.data) {
      categoryList.value = result.data
    }
  } catch (e) {
    console.error('Failed to load categories:', e)
  }
}

// 加载轮播图（本地静态图片）
const loadCarousel = () => {
  const images = []
  for (let i = 1; i <= 8; i++) {
    const num = String(i).padStart(2, '0')
    images.push({
      cover: `/carousel/carousel${num}.jpg`,
      title: '',
      videoId: null
    })
  }
  carouselList.value = images
}

// 构建封面 URL
const buildCoverUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '' + (url.startsWith('/') ? '' : '/') + url
}

// 加载推荐视频
const loadRecommendVideo = async (isLoadMore = false) => {
  if (loading.value || (!isLoadMore && !hasMore.value)) return
  loading.value = true

  try {
    let userId = localStorage.getItem('userId')
    if (!userId) {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        try { const parsed = JSON.parse(userInfo); userId = parsed.userId || parsed.id } catch (e) {}
      }
    }

    let result
    try {
      result = await Request({
        url: '/api/recommend/videos',
        params: { userId: userId || '', limit: pageSize.value, offset: isLoadMore ? (pageNo.value - 1) * pageSize.value : 0 }
      })
    } catch (e) {
      result = await Request({
        url: Api.loadRecommendVideo,
        params: { pageNo: pageNo.value, pageSize: pageSize.value }
      })
    }

    if (result && (result.success || result.code === 200)) {
      let newVideos = result.data || []
      let totalCount = 0
      if (result.data && result.data.list) {
        newVideos = result.data.list
        totalCount = result.data.totalCount || 0
      }

      const processed = newVideos.map(v => processVideoItem(v))

      if (isLoadMore) {
        allVideos.value = [...allVideos.value, ...processed]
      } else {
        allVideos.value = processed
        hotVideos.value = [...processed].sort((a, b) => b.hotScore - a.hotScore).slice(0, 6)
        displayVideos.value = processed
      }

      pageNo.value++
      // 优先用 API 返回的 totalCount，否则用已加载数量估算
      if (totalCount > 0) {
        hasMore.value = allVideos.value.length < totalCount
      } else {
        hasMore.value = processed.length >= pageSize.value
      }
    }
  } catch (e) {
    console.error('Failed to load videos:', e)
  } finally {
    loading.value = false
  }
}

// 处理单个视频
const processVideoItem = (video) => {
  const item = {
    title: video.title,
    cover: buildCoverUrl(video.coverUrl || video.cover),
    author: video.nickName || video.nickname || video.author || video.username || '未知作者',
    playCount: video.playCount || video.viewCount || 0,
    likeCount: video.likeCount || 0,
    followCount: video.followCount || 0,
    commentCount: video.commentCount || 0,
    coinsInserted: video.coinsInserted || 0,
    duration: video.duration,
    id: video.videoId || video.id,
    videoId: video.videoId || video.id
  }
  item.hotScore = computeHotScore(item)
  return item
}

// 按分类加载
const loadVideosByCategory = async (categoryId, categoryName) => {
  try {
    loading.value = true
    const result = await Request({
      url: '/api/video/loadVideoByCategory',
      params: { categoryId }
    })
    if (result && result.data) {
      let list = result.data.list || result.data
      if (Array.isArray(list)) {
        const processed = list.map(v => processVideoItem(v))
        displayVideos.value = processed
        hotVideos.value = [...processed].sort((a, b) => b.hotScore - a.hotScore).slice(0, 6)
        currentCategoryId.value = categoryId
        currentCategoryName.value = categoryName
        hasMore.value = false
      }
    }
  } catch (e) {
    console.error('Failed to load by category:', e)
  } finally {
    loading.value = false
  }
}

// 跳转到视频详情
const goToVideoDetail = (video) => {
  if (video.videoId) router.push(`/video/${video.videoId}`)
}

// 跳转 Banner
const goToBannerTarget = (item) => {
  if (item.videoId) router.push(`/video/${item.videoId}`)
}

// 分类导航
const goToCategory = (categoryId, categoryName) => {
  router.push({ path: `/category/${categoryId}`, query: { name: categoryName } })
}

// 回到首页
const goToIndex = () => {
  currentCategoryId.value = null
  currentCategoryName.value = '推荐'
  router.push('/')
}

// 换一换
const refreshVideos = () => {
  pageNo.value = 1
  hasMore.value = true
  allVideos.value = []
  loadRecommendVideo()
}

// 无限滚动
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight
  const clientHeight = document.documentElement.clientHeight || window.innerHeight
  if (scrollTop + clientHeight >= scrollHeight - 200 && !loading.value && hasMore.value) {
    loadRecommendVideo(true).then(() => {
      displayVideos.value = [...allVideos.value]
    })
  }
}

onMounted(() => {
  navActionStore.setShowHeader(true)
  navActionStore.setFixedHeader(true)
  navActionStore.setFixedCategory(false)
  navActionStore.setShowCategory(false)
  navActionStore.setForceFixedHeader(false)

  loadCategories()
  loadCarousel()
  loadRecommendVideo()

  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 监听路由变化
watch(() => route.path, (newPath) => {
  if (newPath.startsWith('/category/')) {
    const categoryId = parseInt(newPath.split('/')[2])
    const categoryName = route.query.name || '分类'
    loadVideosByCategory(categoryId, categoryName)
  } else if (newPath === '/') {
    currentCategoryId.value = null
    currentCategoryName.value = '推荐'
    pageNo.value = 1
    hasMore.value = true
    allVideos.value = []
    loadRecommendVideo()
  }
}, { immediate: true })
</script>

<style lang="scss" scoped>
/* ===== Hero Banner ===== */
.hero-banner {
  position: relative;
  margin-top: 60px;
  height: 420px;
  background: linear-gradient(135deg, #1a1638 0%, #2a2452 30%, #1e1a3a 60%, #161430 100%);
  overflow: hidden;
  display: flex;
  align-items: center;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 30% 50%, rgba(147, 112, 219, 0.12) 0%, transparent 60%),
    radial-gradient(ellipse at 70% 30%, rgba(212, 175, 55, 0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(72, 61, 139, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
  width: 100%;
  gap: 60px;
}

.hero-left {
  flex: 0 0 420px;
}

.hero-label {
  font-size: 12px;
  color: var(--color-accent);
  letter-spacing: 3px;
  margin-bottom: 12px;
  text-transform: uppercase;
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 16px;
  line-height: 1.2;
  text-shadow: 0 0 40px rgba(147, 112, 219, 0.3);
}

.hero-slogan {
  font-size: 15px;
  color: var(--text-muted);
  line-height: 1.7;
  margin: 0 0 28px;
  max-width: 380px;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background: linear-gradient(135deg, rgba(147, 112, 219, 0.4), rgba(72, 61, 139, 0.6));
  border: 1px solid rgba(147, 112, 219, 0.4);
  border-radius: 24px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(8px);

  &:hover {
    transform: scale(1.05);
    background: linear-gradient(135deg, rgba(147, 112, 219, 0.6), rgba(72, 61, 139, 0.8));
    box-shadow: 0 0 24px rgba(147, 112, 219, 0.3);
  }
}

.hero-right {
  flex: 1;
  min-width: 0;
}

.hero-banner-carousel {
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg), 0 0 40px rgba(147, 112, 219, 0.15);
}

.hero-carousel-item {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: pointer;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.hero-carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30px 20px 16px;
  background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
  color: #fff;
  font-size: 14px;
}

.hero-indicators {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 3;
}

.hero-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255,255,255,0.3);
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover, &.active {
    background: var(--color-accent);
    box-shadow: 0 0 8px rgba(212, 175, 55, 0.4);
  }
}

/* ===== 横向分类栏 ===== */
.category-bar {
  position: sticky;
  top: 60px;
  left: 0;
  right: 0;
  height: 44px;
  background: rgba(22, 20, 48, 0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255,255,255,0.06);
  z-index: 99;
  display: flex;
  align-items: center;
}

.category-bar-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  gap: 4px;
  overflow-x: auto;
  white-space: nowrap;
  scrollbar-width: none;
  &::-webkit-scrollbar { display: none; }
}

.category-tab {
  display: inline-flex;
  align-items: center;
  padding: 6px 14px;
  font-size: 13px;
  color: var(--text-muted);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
  position: relative;

  &:hover { color: var(--color-primary-light); background: rgba(147, 112, 219, 0.1); }

  &.active {
    color: var(--color-primary-light);
    font-weight: 600;
    background: rgba(147, 112, 219, 0.15);

    &::after {
      content: '';
      position: absolute;
      bottom: 2px;
      left: 50%;
      transform: translateX(-50%);
      width: 20px;
      height: 2px;
      border-radius: 1px;
      background: var(--color-primary);
    }
  }
}

/* ===== 主内容区 ===== */
.main-content {
  padding: 0 24px 60px;
  max-width: 1400px;
  margin-left: auto;
  margin-right: auto;
  min-height: calc(100vh - 104px);
}

.content-row {
  display: flex;
  gap: 24px;
  margin-top: 24px;
}

/* ===== 视频区域 ===== */
.video-section {
  flex: 1;
  min-width: 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  color: var(--color-accent);
  font-size: 16px;
}

.section-refresh {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
  cursor: pointer;
  padding: 6px 14px;
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 20px;
  transition: all 0.3s ease;

  &:hover {
    color: var(--color-primary-light);
    border-color: rgba(147, 112, 219, 0.4);
    background: rgba(147, 112, 219, 0.1);
  }
}

/* ===== 视频卡片网格 ===== */
.video-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.video-card {
  cursor: pointer;
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: all 0.3s ease;
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.04);

  &:hover {
    transform: translateY(-6px);
    background: rgba(147, 112, 219, 0.08);
    border-color: rgba(147, 112, 219, 0.2);
    box-shadow: 0 12px 32px rgba(0,0,0,0.4);
  }
}

.video-cover-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 56.25%;
  overflow: hidden;
  background: var(--bg-tertiary);

  img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s ease;
  }

  .video-card:hover & img { transform: scale(1.06); }
}

.video-dur {
  position: absolute;
  bottom: 6px;
  right: 6px;
  background: rgba(0,0,0,0.8);
  color: #fff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
}

.video-play-info {
  position: absolute;
  bottom: 6px;
  left: 6px;
  color: #fff;
  font-size: 11px;
  background: rgba(0,0,0,0.6);
  padding: 2px 6px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 3px;
}

.video-meta {
  padding: 10px 10px 12px;
}

.video-card-title {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 6px;
  min-height: 36px;
}

.video-card-author {
  font-size: 11px;
  color: var(--text-dim);
}

/* ===== 星空榜单侧栏 ===== */
.hot-sidebar {
  width: 300px;
  flex-shrink: 0;
  background: rgba(255,255,255,0.03);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.06);
  border-radius: var(--radius-lg);
  padding: 20px;
  align-self: flex-start;
  position: sticky;
  top: 124px;
}

.hot-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  display: flex;
  align-items: center;
  gap: 6px;
}

.hot-title-icon {
  color: var(--color-accent);
}

.hot-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 8px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(147, 112, 219, 0.08);
  }
}

.hot-rank {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  background: rgba(255,255,255,0.06);
  color: var(--text-dim);

  &.rank-1 { background: var(--color-accent); color: #1a1638; }
  &.rank-2 { background: rgba(212, 175, 55, 0.7); color: #fff; }
  &.rank-3 { background: rgba(184, 162, 217, 0.5); color: #fff; }
}

.hot-info {
  flex: 1;
  min-width: 0;
}

.hot-video-title {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.hot-video-author,
.hot-video-stats {
  font-size: 11px;
  color: var(--text-dim);
}

/* ===== 加载状态 ===== */
.loading-more, .no-more {
  text-align: center;
  padding: 32px;
  color: var(--text-dim);
  font-size: 13px;
}

.no-more {
  color: var(--text-muted);
}

.loading-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(147,112,219,0.2);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-right: 6px;
  vertical-align: middle;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== 响应式 ===== */
@media (max-width: 1400px) {
  .video-grid { grid-template-columns: repeat(4, 1fr); }
}

@media (max-width: 1100px) {
  .video-grid { grid-template-columns: repeat(3, 1fr); }
  .hot-sidebar { width: 260px; }
  .hero-left { flex: 0 0 340px; }
  .hero-title { font-size: 32px; }
}

@media (max-width: 900px) {
  .hero-banner { height: auto; padding: 40px 0; }
  .hero-content { flex-direction: column; gap: 24px; padding: 0 20px; }
  .hero-left { flex: none; text-align: center; }
  .hero-slogan { max-width: none; }
  .content-row { flex-direction: column; }
  .hot-sidebar { width: 100%; position: static; }
  .video-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 640px) {
  .hero-title { font-size: 26px; }
  .hero-btn { padding: 10px 24px; font-size: 14px; }

  .main-content { padding: 0 12px 40px; }
  .video-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
}
</style>
