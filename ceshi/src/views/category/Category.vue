<template>
  <Header />

  <!-- 横向分类栏 -->
  <div class="category-bar">
    <div class="category-bar-inner">
      <span
        :class="['category-tab', !isCategoryPage ? 'active' : '']"
        @click="goToIndex"
      >萤石驿站</span>
      <span
        v-for="cat in categoryList"
        :key="cat.categoryId"
        :class="['category-tab', categoryId == cat.categoryId ? 'active' : '']"
        @click="goToCategory(cat.categoryId, cat.categoryName)"
      >{{ cat.categoryName }}</span>
    </div>
  </div>

  <!-- 主内容区 -->
  <div class="main-content">
    <!-- Banner + 星空榜单 -->
    <div class="content-row">
      <div class="banner-carousel">
        <el-carousel :interval="5000" height="400px" arrow="always">
          <el-carousel-item v-for="(item, index) in carouselList" :key="index">
            <div class="carousel-item" @click="goToBannerTarget(item)">
              <img :src="item.cover" :alt="item.title" />
              <div class="carousel-caption">{{ item.title }}</div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      <div class="hot-sidebar">
        <div class="hot-title">
          <span class="hot-title-icon">★</span> 星空榜单 · {{ categoryName }}
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

    <!-- 分类视频区域 -->
    <div class="video-section">
      <div class="section-header">
        <h3 class="section-title">
          <span class="title-icon">✦</span> {{ categoryName }}
        </h3>
        <span class="section-count">魔导书库共收录 {{ totalCount }} 卷</span>
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
  </div>
</template>

<script setup>
import Header from '@/components/Header.vue'
import { ref, onMounted, onUnmounted, inject, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { computeHotScore } from '@/utils/utils'
import { VideoPlay } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const Request = inject('Request')
const Api = inject('Api')

// 分类列表（横向分类栏用）
const categoryList = ref([])
const isCategoryPage = ref(true)

// 轮播图数据
const carouselList = ref([])

// 热门视频（右侧排行）
const hotVideos = ref([])

// 展示视频
const displayVideos = ref([])
const allVideos = ref([])

// 当前分类
const categoryId = ref(route.params.categoryId)
const categoryName = ref(route.query.name || '分类')

// 分页状态
const pageNo = ref(1)
const pageSize = ref(12)
const loading = ref(false)
const hasMore = ref(true)
const totalCount = ref(0)

// 格式化播放量
const formatPlayCount = (count) => {
  const n = parseInt(count) || 0
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toString()
}

// 构建封面 URL
const buildCoverUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '' + (url.startsWith('/') ? '' : '/') + url
}

// 加载横向分类列表
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

// 加载轮播图（按分类，优先API，fallback到主页轮播图）
const loadCarousel = async () => {
  try {
    const result = await Request({
      url: Api.getCarouselByCategory,
      params: { categoryId: categoryId.value }
    })
    if (result && result.success && result.data && result.data.length > 0) {
      carouselList.value = result.data.map(v => ({
        cover: buildCoverUrl(v.coverUrl || v.cover),
        title: v.title,
        videoId: v.videoId || v.id
      }))
      return
    }
  } catch (e) {
    console.error('Failed to load carousel:', e)
  }
  // fallback: 尝试主页轮播图
  try {
    const result = await Request({ url: Api.getCarousel })
    if (result && result.success && result.data && result.data.length > 0) {
      carouselList.value = result.data.map(v => ({
        cover: buildCoverUrl(v.coverUrl || v.cover),
        title: v.title,
        videoId: v.videoId || v.id
      }))
    }
  } catch (e) {
    console.error('Failed to load fallback carousel:', e)
  }
}

// 按分类加载视频
const loadVideosByCategory = async (isLoadMore = false) => {
  if (loading.value || (!isLoadMore && !hasMore.value)) return
  loading.value = true

  try {
    const result = await Request({
      url: Api.loadVideoByCategory,
      params: {
        categoryId: categoryId.value,
        pageNo: pageNo.value,
        pageSize: pageSize.value
      }
    })

    if (result && result.data) {
      const newVideos = result.data.list || []
      totalCount.value = result.data.totalCount || 0

      const processed = newVideos.map(v => processVideoItem(v))

      if (isLoadMore) {
        allVideos.value = [...allVideos.value, ...processed]
      } else {
        allVideos.value = processed
        hotVideos.value = [...processed].sort((a, b) => b.hotScore - a.hotScore).slice(0, 6)
        displayVideos.value = processed
      }

      pageNo.value++
      hasMore.value = (pageNo.value - 1) * pageSize.value < totalCount.value
    }
  } catch (e) {
    console.error('Failed to load videos by category:', e)
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

// 跳转
const goToVideoDetail = (video) => {
  if (video.videoId) router.push(`/video/${video.videoId}`)
}

const goToBannerTarget = (item) => {
  if (item.videoId) router.push(`/video/${item.videoId}`)
}

const goToCategory = (catId, catName) => {
  router.push({ path: `/category/${catId}`, query: { name: catName } })
}

const goToIndex = () => {
  router.push('/')
}

// 无限滚动（加载更多追加到 allVideos）
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight
  const clientHeight = document.documentElement.clientHeight || window.innerHeight
  if (scrollTop + clientHeight >= scrollHeight - 200 && !loading.value && hasMore.value) {
    loadVideosByCategory(true).then(() => {
      displayVideos.value = allVideos.value
    })
  }
}

onMounted(() => {
  loadCategories()
  loadCarousel()
  loadVideosByCategory()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 监听路由参数变化（切换分类）
watch(() => [route.params.categoryId, route.query.name], ([newCatId, newName]) => {
  if (newCatId && newCatId !== categoryId.value) {
    categoryId.value = newCatId
    categoryName.value = newName || '分类'
    pageNo.value = 1
    hasMore.value = true
    allVideos.value = []
    displayVideos.value = []
    loadCarousel()
    loadVideosByCategory()
  }
}, { immediate: true })
</script>

<style lang="scss" scoped>
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

  &:hover { color: var(--color-primary-light); background: rgba(147, 112, 219, 0.1); }

  &.active {
    color: var(--color-primary-light);
    font-weight: 600;
    background: rgba(147, 112, 219, 0.15);
  }
}

/* ===== 主内容区 ===== */
.main-content {
  padding: 24px 24px 60px;
  max-width: 1400px;
  margin-left: auto;
  margin-right: auto;
  min-height: calc(100vh - 104px);
}

.content-row {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
}

/* ===== Banner 轮播 ===== */
.banner-carousel {
  flex: 1;
  min-width: 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg), 0 0 30px rgba(147, 112, 219, 0.1);
}

.carousel-item {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: pointer;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    background: rgba(0,0,0,0.3);
  }
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24px 16px 14px;
  background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
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

  &:hover { background: rgba(147, 112, 219, 0.08); }
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

/* ===== 视频区域 ===== */
.video-section {
  margin-top: 8px;
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

.section-count {
  font-size: 13px;
  color: var(--text-dim);
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

/* ===== 加载状态 ===== */
.loading-more, .no-more {
  text-align: center;
  padding: 32px;
  color: var(--text-dim);
  font-size: 13px;
}

.no-more { color: var(--text-muted); }

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

@keyframes spin { to { transform: rotate(360deg); } }

/* ===== 响应式 ===== */
@media (max-width: 1400px) {
  .video-grid { grid-template-columns: repeat(4, 1fr); }
}

@media (max-width: 1100px) {
  .video-grid { grid-template-columns: repeat(3, 1fr); }
  .hot-sidebar { width: 260px; }
}

@media (max-width: 900px) {
  .content-row { flex-direction: column; }
  .hot-sidebar { width: 100%; position: static; }
  .video-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 640px) {
  .main-content { padding: 0 12px 40px; }
  .video-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
}
</style>
