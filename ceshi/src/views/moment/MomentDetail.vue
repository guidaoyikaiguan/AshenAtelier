<template>
  <div class="moment-detail-page">
    <!-- 顶部导航栏 -->
    <Header />
    
    <!-- 左侧导航栏 -->
    <Sidebar />
    
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 动态详情 -->
      <div class="moment-detail">
        <!-- 动态内容 -->
        <div class="moment-content">
          <!-- 作者信息 -->
          <div class="author-info">
            <el-avatar :size="50" :src="getAvatarUrl(moment.avatar)">
              {{ moment.userName?.charAt(0) || '用' }}
            </el-avatar>
            <div class="author-details">
              <div class="author-name">{{ moment.userName || '未知用户' }}</div>
              <div class="publish-time">{{ formatTime(moment.createTime) }}</div>
            </div>
          </div>
          
          <!-- 动态正文 -->
          <div class="moment-body">
            <div class="moment-text">{{ moment.content }}</div>
            
            <!-- 动态图片 -->
            <div v-if="moment.images && moment.images.length > 0" class="moment-images">
              <div 
                v-for="(image, imgIndex) in moment.images" 
                :key="imgIndex"
                class="moment-image"
              >
                <img :src="image.imageUrl" :alt="'动态图片 ' + (imgIndex + 1)" />
              </div>
            </div>
          </div>
          
          <!-- 互动区 -->
          <div class="interaction-area">
            <el-button type="text" @click="likeMoment(moment.momentId)">
              <el-icon><Star /></el-icon>
              <span>{{ moment.likeCount || 0 }}</span>
            </el-button>
            <el-button type="text">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ moment.commentCount || 0 }}</span>
            </el-button>
            <el-button type="text" @click="shareMoment(moment.momentId)">
              <el-icon><Share /></el-icon>
              <span>传递见闻</span>
            </el-button>
          </div>
        </div>
        
        <!-- 评论区 -->
        <div class="comment-section">
          <div class="comment-section-header">
            <h3>旅人讨论 ({{ moment.commentCount || 0 }})</h3>
          </div>
          
          <!-- 评论输入框 -->
          <div class="comment-input-area">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="留下你的足迹..."
              resize="none"
              class="comment-textarea"
            ></el-input>
            <div class="comment-actions">
              <el-button @click="cancelComment">取消</el-button>
              <el-button type="primary" @click="submitComment">发布</el-button>
            </div>
          </div>
          
          <!-- 评论列表 -->
          <div class="comment-list">
            <el-empty v-if="comments.length === 0" description="暂无评论"></el-empty>
            <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
              <div class="comment-user">
                <el-avatar :size="40" :src="getAvatarUrl(comment.userAvatar)">
                  {{ (comment.nickName || comment.username)?.charAt(0) || '用' }}
                </el-avatar>
                <div class="comment-user-info">
                  <div class="comment-user-name">{{ comment.nickName || comment.username || '未知用户' }}</div>
                  <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
                </div>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-actions">
                <span class="comment-action" @click="likeComment(comment.commentId)">
                  <el-icon><Star /></el-icon>
                  <span>{{ comment.likeCount || 0 }}</span>
                </span>
                <span class="comment-action" @click="replyComment(comment)">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>回应</span>
                </span>
              </div>
              
              <!-- 查看更多回复按钮 -->
              <div v-if="comment.subComments && comment.subComments.length > 0" class="view-more-replies">
                <el-button 
                  type="text" 
                  @click="toggleSubComments(comment)"
                  class="view-more-button"
                >
                  {{ comment.showSubComments ? '收起回应' : `展开更多回应 (${comment.subComments.length})` }}
                </el-button>
              </div>
              
              <!-- 子评论列表 -->
              <div v-if="comment.showSubComments && comment.subComments && comment.subComments.length > 0" class="sub-comment-list">
                <div 
                  v-for="subComment in comment.subComments" 
                  :key="subComment.commentId"
                  class="sub-comment-item"
                >
                  <div class="comment-user">
                    <el-avatar :size="32" :src="getAvatarUrl(subComment.userAvatar)">
                      {{ (subComment.nickName || subComment.username)?.charAt(0) || '用' }}
                    </el-avatar>
                    <div class="comment-user-info">
                      <div class="comment-user-name">{{ subComment.nickName || subComment.username || '未知用户' }}</div>
                      <div class="comment-time">{{ formatTime(subComment.createTime) }}</div>
                    </div>
                  </div>
                  <div class="comment-content">{{ subComment.content }}</div>
                  <div class="comment-actions">
                    <span class="comment-action" @click="likeComment(subComment.commentId)">
                      <el-icon><Star /></el-icon>
                      <span>{{ subComment.likeCount || 0 }}</span>
                    </span>
                    <span class="comment-action" @click="replyComment(subComment)">
                      <el-icon><ChatDotRound /></el-icon>
                      <span>回应</span>
                    </span>
                  </div>
                </div>
              </div>
              
              <!-- 回复输入框 -->
              <div v-if="replyTargetId === comment.commentId" class="reply-input-section">
                <el-input
                  v-model="replyContent"
                  type="textarea"
                  :rows="2"
                  :placeholder="`回复 @${comment.nickName || comment.username}...`"
                  resize="none"
                  class="reply-textarea"
                ></el-input>
                <div class="reply-input-actions">
                  <el-button @click="cancelReply" type="text">取消</el-button>
                  <el-button @click="submitReply(comment)" type="primary" :disabled="!replyContent || !replyContent.trim()">发布</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import { Star, ChatDotRound, Share } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const Request = inject('Request')

// 动态数据
const moment = ref({})
// 评论列表
const comments = ref([])
// 评论内容
const commentContent = ref('')
// 回复目标评论ID
const replyTargetId = ref(null)
// 回复内容
const replyContent = ref('')
// 回复的评论ID
const replyCommentId = ref(null)

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

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  
  const now = new Date()
  const createTime = new Date(time)
  const diff = now - createTime
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 1) {
    return '刚刚'
  } else if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return createTime.toISOString().split('T')[0].replace(/-/g, '-')
  }
}

// 加载动态详情
const loadMomentDetail = async () => {
  try {
    const momentId = route.params.momentId
    console.log('开始加载动态详情:', momentId)
    
    // 由于getMomentById接口不存在，我们使用getMomentList接口获取所有动态，然后在前端找到对应的动态
    // 这里假设当前用户是动态的发布者，或者我们可以从评论中获取发布者的userId
    const result = await Request({
      url: '/api/moment/getMomentList',
      method: 'GET',
      params: {
        page: 1,
        size: 20
      }
    })
    
    console.log('动态列表接口调用结果:', result)
    
    // 检查返回数据的结构
    if (result && result.success && result.data) {
      console.log('动态列表数据:', result.data)
      // 在动态列表中找到对应的动态
      const foundMoment = result.data.find(m => m.momentId === parseInt(momentId))
      if (foundMoment) {
        console.log('找到动态详情:', foundMoment)
        moment.value = foundMoment
      } else {
        console.log('未找到动态详情:', momentId)
      }
    } else {
      console.log('动态列表接口调用失败:', result)
    }
  } catch (error) {
    console.error('加载动态详情失败:', error)
  }
}

// 加载评论列表
const loadComments = async () => {
  try {
    const momentId = route.params.momentId
    console.log('开始加载评论列表:', momentId)
    
    // 调用评论列表接口
    const result = await Request({
      url: '/api/moment/getMomentComments',
      method: 'GET',
      params: {
        momentId: momentId,
        sortord: 1 // 1按时间降序排序
      }
    })
    
    console.log('评论列表接口调用结果:', result)
    
    // 检查返回数据的结构
    if (result && result.success && result.data) {
      console.log('评论列表数据:', result.data)
      // 处理评论数据，添加showSubComments属性
      const processedComments = result.data.map(comment => {
        return {
          ...comment,
          showSubComments: false // 默认不显示子评论
        }
      })
      // 过滤出主评论（parentId为null的评论）
      comments.value = processedComments.filter(comment => comment.parentId === null)
    } else {
      console.log('评论列表接口调用失败:', result)
    }
  } catch (error) {
    console.error('加载评论列表失败:', error)
  }
}

// 点赞动态
const likeMoment = async (momentId) => {
  try {
    console.log('开始点赞动态:', momentId)
    
    // 调用后端点赞接口
    const result = await Request({
      url: '/api/moment/likeMoment',
      method: 'POST',
      params: {
        momentId: momentId
      }
    })
    
    console.log('点赞接口调用结果:', result)
    
    // 更新点赞数
    if (result && result.success && result.data) {
      if (result.data.likeCount !== undefined) {
        moment.value.likeCount = result.data.likeCount
      }
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

// 点赞评论
const likeComment = async (commentId) => {
  try {
    console.log('开始点赞评论:', commentId)
    
    // 从sessionStorage中获取用户信息
    const userInfoStr = localStorage.getItem('userInfo')
    let userId = 1 // 默认值
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr)
        userId = userInfo.userId || 1
        console.log('从sessionStorage获取到的userId:', userId)
      } catch (e) {
        console.error('解析userInfo失败:', e)
      }
    } else {
      console.log('sessionStorage中没有userInfo，使用默认userId:', userId)
    }
    
    // 查找对应的评论
    let comment = null
    // 先在主评论中查找
    comment = comments.value.find(c => c.commentId === commentId)
    // 如果主评论中没有，在子评论中查找
    if (!comment) {
      for (const mainComment of comments.value) {
        if (mainComment.subComments) {
          comment = mainComment.subComments.find(sc => sc.commentId === commentId)
          if (comment) break
        }
      }
    }
    
    if (!comment) {
      console.log('未找到评论:', commentId)
      return
    }
    
    console.log('找到评论:', commentId, '当前点赞数:', comment.likeCount, '当前点赞状态:', comment.liked)
    
    // 调用后端点赞评论接口
    const result = await Request({
      url: '/api/moment/likeComment',
      method: 'POST',
      params: {
        commentId: commentId,
        momentId: route.params.momentId,
        userId: userId
      }
    })
    
    console.log('点赞评论接口调用结果:', result)
    
    // 根据返回结果更新点赞数和点赞状态
    if (result) {
      console.log('点赞评论接口返回成功')
      
      // 直接根据当前点赞状态更新
      if (comment.liked) {
        // 当前是已点赞状态，点击后应该取消点赞
        console.log('当前是已点赞状态，执行取消点赞操作，点赞数减1')
        comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
        comment.liked = false
        console.log('更新后点赞数:', comment.likeCount, '更新后点赞状态:', comment.liked)
      } else {
        // 当前是未点赞状态，点击后应该点赞
        console.log('当前是未点赞状态，执行点赞操作，点赞数加1')
        comment.likeCount = (comment.likeCount || 0) + 1
        comment.liked = true
        console.log('更新后点赞数:', comment.likeCount, '更新后点赞状态:', comment.liked)
      }
    } else {
      console.log('点赞评论失败:', result)
    }
  } catch (error) {
    console.error('点赞评论失败:', error)
  }
}

// 回复评论
const replyComment = (comment) => {
  console.log('开始回复评论:', comment)
  // 隐藏之前的回复输入框
  replyTargetId.value = null
  // 显示当前评论的回复输入框
  replyTargetId.value = comment.commentId
  // 设置回复内容，使用nickName或username字段
  replyContent.value = `@${comment.nickName || comment.username} `
  // 设置回复的评论ID作为parentId
  replyCommentId.value = comment.commentId
}

// 取消回复
const cancelReply = () => {
  replyTargetId.value = null
  replyContent.value = ''
  replyCommentId.value = null
}

// 提交回复
const submitReply = async (comment) => {
  try {
    if (!replyContent.value.trim()) {
      console.log('回复内容不能为空')
      return
    }
    
    console.log('开始提交回复:', replyContent.value, '到评论:', comment.commentId, '动态:', route.params.momentId)
    
    // 从sessionStorage中获取用户信息
    const userInfoStr = localStorage.getItem('userInfo')
    let userId = 1 // 默认值
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr)
        userId = userInfo.userId || 1
        console.log('从sessionStorage获取到的userId:', userId)
      } catch (e) {
        console.error('解析userInfo失败:', e)
      }
    } else {
      console.log('sessionStorage中没有userInfo，使用默认userId:', userId)
    }
    
    // 调用后端评论接口
    const result = await Request({
      url: '/api/moment/addComment',
      method: 'POST',
      params: {
        momentId: route.params.momentId,
        content: replyContent.value,
        userId: userId,
        parentId: replyCommentId.value || null // 对于主评论，传递null；对于回复，传递被回复评论的commentId
      }
    })
    
    console.log('回复接口调用结果:', result)
    
    // 根据返回结果更新前端的评论数
    if (result && (result.status === 'success' || result.success)) {
      console.log('回复成功')
      // 更新评论数
      moment.value.commentCount = (moment.value.commentCount || 0) + 1
      // 清空回复内容
      replyTargetId.value = null
      replyContent.value = ''
      replyCommentId.value = null
      // 重新加载评论列表
      await loadComments()
    } else {
      console.log('回复失败:', result)
    }
  } catch (error) {
    console.error('回复失败:', error)
  }
}

// 提交评论
const submitComment = async () => {
  try {
    if (!commentContent.value.trim()) {
      console.log('评论内容不能为空')
      return
    }
    
    console.log('开始提交评论:', commentContent.value, '到动态:', route.params.momentId)
    
    // 从sessionStorage中获取用户信息
    const userInfoStr = localStorage.getItem('userInfo')
    let userId = 1 // 默认值
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr)
        userId = userInfo.userId || 1
        console.log('从sessionStorage获取到的userId:', userId)
      } catch (e) {
        console.error('解析userInfo失败:', e)
      }
    } else {
      console.log('sessionStorage中没有userInfo，使用默认userId:', userId)
    }
    
    // 调用后端评论接口
    const result = await Request({
      url: '/api/moment/addComment',
      method: 'POST',
      params: {
        momentId: route.params.momentId,
        content: commentContent.value,
        userId: userId,
        parentId: null // 主评论，parentId为null
      }
    })
    
    console.log('评论接口调用结果:', result)
    
    // 根据返回结果更新前端的评论数
    if (result && (result.status === 'success' || result.success)) {
      console.log('评论成功')
      // 更新评论数
      moment.value.commentCount = (moment.value.commentCount || 0) + 1
      // 清空评论内容
      commentContent.value = ''
      // 重新加载评论列表
      await loadComments()
    } else {
      console.log('评论失败:', result)
    }
  } catch (error) {
    console.error('评论失败:', error)
  }
}

// 取消评论
const cancelComment = () => {
  commentContent.value = ''
}

// 切换子评论显示状态
const toggleSubComments = (comment) => {
  comment.showSubComments = !comment.showSubComments
}

// 分享动态
const shareMoment = (momentId) => {
  // 实现分享功能
  console.log('分享动态:', momentId)
}

// 页面加载时调用
onMounted(() => {
  // 加载动态详情
  loadMomentDetail()
  // 加载评论列表
  loadComments()
})
</script>

<style lang="scss" scoped>
/* 主内容区 */
.main-content {
  margin-top: 60px; /* 60px header */
  margin-left: 200px; /* 200px sidebar */
  padding: 0 20px;
  min-height: calc(100vh - 60px);
}

/* 动态详情 */
.moment-detail {
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 动态内容 */
.moment-content {
  margin-bottom: 30px;
}

/* 作者信息 */
.author-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.author-details {
  margin-left: 15px;
}

.author-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 5px;
}

.publish-time {
  font-size: 14px;
  color: #909399;
}

/* 动态正文 */
.moment-body {
  margin-bottom: 20px;
}

.moment-text {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 20px;
}

/* 动态图片 */
.moment-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.moment-image {
  flex: 1 1 300px;
  max-width: 80%;
  margin: 0 auto;
}

.moment-image img {
  width: 100%;
  height: auto;
  border-radius: 8px;
  max-height: 500px;
  object-fit: contain;
}

/* 互动区 */
.interaction-area {
  display: flex;
  align-items: center;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
  padding: 15px 0;
}

.interaction-area el-button {
  margin-right: 30px;
}

/* 评论区 */
.comment-section {
  margin-top: 30px;
}

.comment-section-header {
  margin-bottom: 20px;
}

.comment-section-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

/* 评论输入框 */
.comment-input-area {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.comment-textarea {
  margin-bottom: 15px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 评论列表 */
.comment-list {
  margin-top: 20px;
}

.comment-item {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.view-more-replies {
  margin-top: 10px;
  text-align: left;
}

.view-more-button {
  color: #409eff;
  font-size: 14px;
}

.sub-comment-list {
  margin-top: 15px;
  margin-left: 50px;
}

.sub-comment-item {
  margin-bottom: 15px;
  padding: 15px;
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.comment-user {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.comment-user-info {
  margin-left: 15px;
}

.comment-user-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 5px;
}

.comment-time {
  font-size: 14px;
  color: #909399;
}

.comment-content {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 15px;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.comment-action {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.comment-action:hover {
  color: #409eff;
}

/* 回复输入框 */
.reply-input-section {
  margin-top: 15px;
  padding: 15px;
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.reply-textarea {
  margin-bottom: 10px;
}

.reply-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>