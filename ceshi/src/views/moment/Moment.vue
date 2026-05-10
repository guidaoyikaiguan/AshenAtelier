<template>
  <div class="moment-page">
    <!-- 顶部导航栏 -->
    <Header />
    
    <!-- 左侧导航栏 -->
    <Sidebar />
    
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 动态标题 -->
      <div class="moment-header">
        <h2>旅人见闻</h2>
      </div>
      
      <!-- 关注用户列表 -->
      <div class="following-users">
        <div class="following-header">
          <el-button type="primary" plain @click="refreshFollowingList">
            <el-icon><Refresh /></el-icon>
            所有见闻
          </el-button>
          <div class="user-avatars">
            <div 
              v-for="(user, index) in followingUsers" 
              :key="user.userId || user.id"
              class="user-avatar"
              :title="user.nickName"
              @click="getUserMoments(user.userId || user.id, user.nickName)"
            >
              <el-avatar :size="40" :src="getAvatarUrl(user.avatar)">
                {{ user.nickName?.charAt(0) || '用' }}
              </el-avatar>
              <div class="user-name">{{ user.nickName }}</div>
            </div>
          </div>
        </div>
      </div>
      
      
      <!-- 动态列表 -->
      <div class="moment-list">

        
        <!-- 动态内容 -->
        <div class="moment-content">
          <div 
            v-for="(moment, index) in momentList" 
            :key="moment.momentId"
            class="moment-item"
            @click="goToMomentDetail(moment.momentId)"
          >
            <!-- 作者信息 -->
            <div class="author-info">
              <el-avatar :size="40" :src="getAvatarUrl(moment.avatar)">
                {{ (moment.userName || '用').charAt(0) }}
              </el-avatar>
              <div class="author-details">
                <div class="author-name">{{ moment.userName || '未知用户' }}</div>
                <div class="publish-time">{{ formatTime(moment.createTime) }}</div>
              </div>
            </div>
            
            <!-- 动态内容 -->
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
              <el-button type="text" @click.stop="likeMoment(moment)">
                <el-icon><Star /></el-icon>
                <span>{{ moment.likeCount || 0 }}</span>
              </el-button>
              <el-button type="text" @click="toggleComments(moment.momentId)">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ moment.commentCount || 0 }}</span>
              </el-button>
              <el-button type="text" @click="shareMoment(moment.momentId)">
                <el-icon><Share /></el-icon>
                <span>传递见闻</span>
              </el-button>
              <el-button v-if="currentUserId && moment.userId === currentUserId" type="text" @click.stop="deleteMoment(moment.momentId, moment.userId)">
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>
            </div>
            
            <!-- 评论输入框 -->
            <div v-if="commentMomentId === moment.momentId" class="comment-input-area">
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="2"
                placeholder="写下你的评论..."
                resize="none"
              ></el-input>
              <div class="comment-actions">
                <el-button @click="cancelComment">取消</el-button>
                <el-button type="primary" @click="submitComment">记录此刻</el-button>
              </div>
            </div>
            
            <!-- 评论列表 -->
            <div v-if="showCommentsMomentId === moment.momentId" class="comment-list-area">
              <div class="comment-list-header">
                <h4>评论 ({{ moment.commentCount || 0 }})</h4>
                <el-button type="text" @click="showCommentsMomentId = null">关闭</el-button>
              </div>
              
              <!-- 新增评论输入框 -->
              <div class="comment-input-section">
                <el-input
                  v-model="commentContent"
                  type="textarea"
                  :rows="2"
                  placeholder="写下你的评论..."
                  resize="none"
                  class="comment-textarea"
                ></el-input>
                <div class="comment-input-actions">
                  <el-button @click="cancelCommentInList(moment.momentId)" type="text">取消</el-button>
                  <el-button @click="submitCommentInList(moment.momentId)" type="primary" :disabled="!commentContent || !commentContent.trim()">记录此刻</el-button>
                </div>
              </div>
              
              <div class="comment-list">
                <el-empty v-if="!comments[moment.momentId] || comments[moment.momentId].length === 0" description="暂无评论"></el-empty>
                <div v-for="comment in comments[moment.momentId] || []" :key="comment.commentId" class="comment-item">
                  <div class="comment-user">
                    <el-avatar :size="32" :src="getAvatarUrl(comment.userAvatar)">{{ comment.nickName?.charAt(0) || '用' }}</el-avatar>
                    <div class="comment-user-info">
                      <div class="comment-user-name">{{ comment.nickName || '未知用户' }}</div>
                      <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
                    </div>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                  <div class="comment-actions">
                    <span class="comment-action" @click="likeComment(comment.commentId, moment.momentId)">
                      <el-icon><Star /></el-icon>
                      <span>{{ comment.likeCount || 0 }}</span>
                    </span>
                    <span class="comment-action" @click="replyComment(comment)">
                      <el-icon><ChatDotRound /></el-icon>
                      <span>回复</span>
                    </span>
                  </div>
                  
                  <!-- 回复输入框 -->
                  <div v-if="replyTargetId === comment.commentId" class="reply-input-section">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      :placeholder="`回复 @${comment.nickName}...`"
                      resize="none"
                      class="reply-textarea"
                    ></el-input>
                    <div class="reply-input-actions">
                      <el-button @click="cancelReply" type="text">取消</el-button>
                      <el-button @click="submitReply(comment)" type="primary" :disabled="!replyContent || !replyContent.trim()">记录此刻</el-button>
                    </div>
                  </div>
                  <!-- 子评论 -->
                  <div v-if="comment.subComments && comment.subComments.length > 0" class="sub-comment-list">
                    <!-- 显示第一条子评论 -->
                    <div v-for="(subComment, index) in comment.subComments.slice(0, comment.showAllSubComments ? comment.subComments.length : 1)" :key="subComment.commentId" class="sub-comment-item">
                      <div class="comment-user">
                        <el-avatar :size="24" :src="getAvatarUrl(subComment.userAvatar)">{{ subComment.nickName?.charAt(0) || '用' }}</el-avatar>
                        <div class="comment-user-info">
                          <div class="comment-user-name">{{ subComment.nickName || '未知用户' }}</div>
                          <div class="comment-time">{{ formatTime(subComment.createTime) }}</div>
                        </div>
                      </div>
                      <div class="comment-content">{{ subComment.content }}</div>
                      <div class="comment-actions">
                        <span class="comment-action" @click="likeComment(subComment.commentId, moment.momentId)">
                          <el-icon><Star /></el-icon>
                          <span>{{ subComment.likeCount || 0 }}</span>
                        </span>
                        <span class="comment-action" @click="replyComment(subComment)">
                          <el-icon><ChatDotRound /></el-icon>
                          <span>回复</span>
                        </span>
                      </div>
                    </div>
                    
                    <!-- 显示查看更多回复按钮 -->
                    <div v-if="comment.subComments.length > 1 && !comment.showAllSubComments" class="view-more-replies">
                      <el-button type="text" @click="toggleSubComments(comment)">
                        查看更多回复 ({{ comment.subComments.length - 1 }})
                      </el-button>
                    </div>
                    
                    <!-- 显示收起回复按钮 -->
                    <div v-if="comment.subComments.length > 1 && comment.showAllSubComments" class="view-more-replies">
                      <el-button type="text" @click="toggleSubComments(comment)">
                        收起回复
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 加载更多 -->
          <div class="load-more">
            <el-button type="primary" plain @click="loadMoreMoments">翻阅更多见闻</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Star, ChatDotRound, Share, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const Request = inject('Request')
const Api = inject('Api')

// 当前用户ID
const currentUserId = ref(getCurrentUserId())

function getCurrentUserId() {
  const info = localStorage.getItem('userInfo')
  if (info) {
    try {
      return JSON.parse(info).userId || null
    } catch (e) {}
  }
  return null
}

// 关注用户列表
const followingUsers = ref([])

// 动态列表
const momentList = ref([])



// 当前页码
const currentPage = ref(1)

// 每页数量
const pageSize = ref(10)

// 评论相关
const commentMomentId = ref(null)
const commentContent = ref('')
const showCommentsMomentId = ref(null)
const comments = ref({})

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

// 跳转到发布页面
const goToPublishPage = () => {
  router.push('/moment/publish')
}

// 选择图片
const selectImage = () => {
  // 实现图片选择功能
  console.log('选择图片')
}

// 选择用户
const selectUser = () => {
  // 实现用户选择功能
  console.log('选择用户')
}

// 选择音乐
const selectMusic = () => {
  // 实现音乐选择功能
  console.log('选择音乐')
}

// 开始直播
const startLive = () => {
  // 实现直播功能
  console.log('开始直播')
}

// 发布动态
const publishMoment = () => {
  // 实现发布动态功能
  console.log('发布动态')
}

// 删除动态
const deleteMoment = async (momentId, userId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条动态吗？', '确认删除', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const result = await Request({
      url: '/api/moment/deleteMoment',
      method: 'POST',
      params: { momentId, userId }
    })
    if (result && result.success) {
      ElMessage.success('删除成功')
      await loadMoments()
    } else {
      ElMessage.error(result?.msg || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error('删除动态失败:', e)
  }
}

// 点赞/取消点赞动态（后端自动toggle）
const likeMoment = async (moment) => {
  try {
    const result = await Request({
      url: '/api/moment/likeMoment',
      method: 'POST',
      params: { momentId: moment.momentId }
    })
    if (result && result.success && result.data) {
      // 直接用后端返回的准确 likeCount 更新本地
      if (result.data.likeCount !== undefined) {
        moment.likeCount = result.data.likeCount
      }
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

// 评论动态
const commentMoment = (momentId) => {
  console.log('开始评论动态:', momentId)
  // 显示评论输入框
  commentMomentId.value = momentId
  commentContent.value = ''
}

// 构建嵌套评论结构
const buildNestedComments = (commentList) => {
  if (!commentList || commentList.length === 0) {
    return []
  }
  
  const commentMap = new Map()
  const rootComments = []
  
  // 首先将所有评论放入map中
  commentList.forEach(comment => {
    comment.subComments = []
    comment.showAllSubComments = false
    commentMap.set(comment.commentId, comment)
  })
  
  // 然后构建嵌套结构
  commentList.forEach(comment => {
    if (!comment.parentId || comment.parentId === 0) {
      // 没有parentId的是主评论
      rootComments.push(comment)
    } else {
      // 有parentId的是回复
      const parentComment = commentMap.get(comment.parentId)
      if (parentComment) {
        parentComment.subComments.push(comment)
      }
    }
  })
  
  return rootComments
}

// 切换评论列表显示
const toggleComments = async (momentId) => {
  try {
    console.log('开始获取动态评论:', momentId)
    
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
    
    // 调用后端评论列表接口
    const result = await Request({
      url: '/api/moment/getMomentComments',
      method: 'GET',
      params: {
        momentId: momentId,
        sortord: 1, // 1按时间降序排序
        userId: userId
      }
    })
    
    console.log('评论列表接口调用结果:', result)
    
    // 检查返回数据的结构
    let commentList = []
    if (result && result.success && result.data) {
      console.log('评论列表数据:', result.data)
      commentList = result.data
    } else if (result && result.status === 'success' && result.data) {
      console.log('评论列表数据(不同结构):', result.data)
      commentList = result.data
    } else {
      console.log('评论列表接口调用失败:', result)
    }
    
    // 构建嵌套评论结构
    const nestedComments = buildNestedComments(commentList)
    console.log('嵌套评论结构:', nestedComments)
    
    // 存储评论列表
    comments.value[momentId] = nestedComments
    console.log('存储的评论列表:', comments.value)
    
    // 显示评论列表
    showCommentsMomentId.value = momentId
  } catch (error) {
    console.error('获取评论列表失败:', error)
  }
}

// 查找评论
const findCommentById = (commentId, comments) => {
  for (const comment of comments) {
    if (comment.commentId === commentId) {
      return comment
    }
    if (comment.subComments && comment.subComments.length > 0) {
      const found = findCommentById(commentId, comment.subComments)
      if (found) {
        return found
      }
    }
  }
  return null
}

// 点赞评论
const likeComment = async (commentId, momentId) => {
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
    const comment = findCommentById(commentId, comments.value[momentId] || [])
    if (!comment) {
      console.log('未找到评论:', commentId)
      console.log('当前评论列表:', comments.value[momentId])
      return
    }
    
    console.log('找到评论:', commentId, '当前点赞数:', comment.likeCount, '当前点赞状态:', comment.liked)
    
    // 调用后端点赞评论接口
    const result = await Request({
      url: '/api/moment/likeComment',
      method: 'POST',
      params: {
        commentId: commentId,
        momentId: momentId,
        userId: userId
      }
    })
    
    console.log('点赞评论接口调用结果:', result)
    
    // 根据返回结果更新点赞数
    if (result) {
      console.log('点赞评论接口返回成功')
      
      // 检查返回的消息
      const message = result.msg || result.message || ''
      console.log('返回消息:', message)
      
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
  // 设置回复内容
  replyContent.value = `@${comment.nickName} `
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
    
    console.log('开始提交回复:', replyContent.value, '到评论:', comment.commentId, '动态:', comment.momentId)
    
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
        momentId: comment.momentId,
        content: replyContent.value,
        userId: userId,
        parentId: replyCommentId.value || null // 对于主评论，传递null；对于回复，传递被回复评论的commentId
      }
    })
    
    console.log('回复接口调用结果:', result)
    
    // 根据返回结果更新前端的评论数
    if (result && result.status === 'success') {
      console.log('回复成功')
      // 找到对应的动态并更新评论数
      const moment = momentList.value.find(m => m.momentId === comment.momentId)
      if (moment) {
        moment.commentCount = (moment.commentCount || 0) + 1
        console.log('更新后的评论数:', moment.commentCount)
      }
      // 清空回复内容
      replyTargetId.value = null
      replyContent.value = ''
      replyCommentId.value = null
      // 重新加载评论列表
      await toggleComments(comment.momentId)
    } else if (result && result.success) {
      console.log('回复成功(不同结构)')
      // 找到对应的动态并更新评论数
      const moment = momentList.value.find(m => m.momentId === comment.momentId)
      if (moment) {
        moment.commentCount = (moment.commentCount || 0) + 1
        console.log('更新后的评论数:', moment.commentCount)
      }
      // 清空回复内容
      replyTargetId.value = null
      replyContent.value = ''
      replyCommentId.value = null
      // 重新加载评论列表
      await toggleComments(comment.momentId)
    } else {
      console.log('回复失败:', result)
    }
  } catch (error) {
    console.error('回复失败:', error)
  }
}

// 切换子评论显示状态
const toggleSubComments = (comment) => {
  console.log('切换子评论显示状态:', comment)
  // 切换showAllSubComments属性
  comment.showAllSubComments = !comment.showAllSubComments
}

// 回复的评论ID，用于设置parentId
const replyCommentId = ref(null)
// 回复目标的评论ID，用于控制回复输入框的显示
const replyTargetId = ref(null)
// 回复内容
const replyContent = ref('')

// 提交评论
const submitComment = async () => {
  try {
    if (!commentContent.value || !commentContent.value.trim()) {
      console.log('评论内容不能为空')
      return
    }
    
    console.log('开始提交评论:', commentContent.value, '到动态:', commentMomentId.value, '回复评论:', replyCommentId.value)
    
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
        momentId: commentMomentId.value,
        content: commentContent.value,
        userId: userId,
        parentId: replyCommentId.value || null // 对于主评论，传递null；对于回复，传递被回复评论的commentId
      }
    })
    
    console.log('评论接口调用结果:', result)
    
    // 根据返回结果更新前端的评论数
    if (result && result.status === 'success') {
      console.log('评论成功')
      // 找到对应的动态并更新评论数
      const moment = momentList.value.find(m => m.momentId === commentMomentId.value)
      if (moment) {
        moment.commentCount = (moment.commentCount || 0) + 1
        console.log('更新后的评论数:', moment.commentCount)
      }
      // 隐藏评论输入框
      commentMomentId.value = null
      commentContent.value = ''
      replyCommentId.value = null
    } else if (result && result.success) {
      console.log('评论成功(不同结构)')
      // 找到对应的动态并更新评论数
      const moment = momentList.value.find(m => m.momentId === commentMomentId.value)
      if (moment) {
        moment.commentCount = (moment.commentCount || 0) + 1
        console.log('更新后的评论数:', moment.commentCount)
      }
      // 隐藏评论输入框
      commentMomentId.value = null
      commentContent.value = ''
      replyCommentId.value = null
    } else {
      console.log('评论失败:', result)
    }
  } catch (error) {
    console.error('评论失败:', error)
  }
}

// 取消评论
const cancelComment = () => {
  commentMomentId.value = null
  commentContent.value = ''
}

// 取消列表中的评论
const cancelCommentInList = (momentId) => {
  commentContent.value = ''
}

// 提交列表中的评论
const submitCommentInList = async (momentId) => {
  try {
    if (!commentContent.value.trim()) {
      console.log('评论内容不能为空')
      return
    }
    
    console.log('开始提交评论:', commentContent.value, '到动态:', momentId, '回复评论:', replyCommentId.value)
    
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
        momentId: momentId,
        content: commentContent.value,
        userId: userId,
        parentId: replyCommentId.value || null // 对于主评论，传递null；对于回复，传递被回复评论的commentId
      }
    })
    
    console.log('评论接口调用结果:', result)
    
    // 根据返回结果更新前端的评论数
    if (result && result.status === 'success') {
      console.log('评论成功')
      // 找到对应的动态并更新评论数
      const moment = momentList.value.find(m => m.momentId === momentId)
      if (moment) {
        moment.commentCount = (moment.commentCount || 0) + 1
        console.log('更新后的评论数:', moment.commentCount)
      }
      // 清空评论内容和回复评论ID
      commentContent.value = ''
      replyCommentId.value = null
      // 重新加载评论列表
      await toggleComments(momentId)
    } else if (result && result.success) {
      console.log('评论成功(不同结构)')
      // 找到对应的动态并更新评论数
      const moment = momentList.value.find(m => m.momentId === momentId)
      if (moment) {
        moment.commentCount = (moment.commentCount || 0) + 1
        console.log('更新后的评论数:', moment.commentCount)
      }
      // 清空评论内容和回复评论ID
      commentContent.value = ''
      replyCommentId.value = null
      // 重新加载评论列表
      await toggleComments(momentId)
    } else {
      console.log('评论失败:', result)
    }
  } catch (error) {
    console.error('评论失败:', error)
  }
}

// 获取当前用户头像
const getCurrentUserAvatar = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      if (userInfo.avatar) {
        return `/avatar/${userInfo.avatar}`
      }
    } catch (e) {
      console.error('解析userInfo失败:', e)
    }
  }
  return '/default-avatar.png'
}

// 获取当前用户昵称
const getCurrentUserNickName = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      return userInfo.nickName || userInfo.userName
    } catch (e) {
      console.error('解析userInfo失败:', e)
    }
  }
  return '我'
}

// 分享动态
const shareMoment = (momentId) => {
  // 实现分享功能
  console.log('分享动态:', momentId)
}

// 加载更多动态
const loadMoreMoments = () => {
  currentPage.value++
  loadMoments()
}

// 跳转到动态详情页面
const goToMomentDetail = (momentId) => {
  router.push(`/moment/detail/${momentId}`)
}

// 刷新关注用户列表
const refreshFollowingList = () => {
  loadFollowingUsers()
}

// 加载关注用户列表
const loadFollowingUsers = async () => {
  try {
    console.log('开始加载关注用户列表')
    
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
    
    // 调用用户关注列表接口
    const result = await Request({
      url: '/api/user/getFollowingList',
      method: 'POST',
      data: {
        userId: userId, // 从登录状态中获取用户ID
        // 添加其他可能需要的Fans对象字段
        isCreateTime: true
      },
      params: {
        type: 1 // 1按创建时间排序，2按点击量排序
      }
    })
    
    console.log('关注用户列表接口调用结果:', result)
    
    // 检查返回数据的结构
    let userList = []
    if (result && result.success && result.data) {
      console.log('关注用户列表数据:', result.data)
      userList = result.data
    } else if (result && result.data && Array.isArray(result.data)) {
      // 处理不同的数据结构
      console.log('关注用户列表数据(不同结构):', result.data)
      userList = result.data
    } else {
      console.log('关注用户列表接口调用失败:', result)
    }
    
    // 当用户谁也不关注时，不添加默认数据
    if (userList.length === 0) {
      console.log('用户未关注任何人')
      userList = []
    }
    
    followingUsers.value = userList
    console.log('最终关注用户列表:', followingUsers.value)
  } catch (error) {
    console.error('加载关注用户列表失败:', error)
    // 发生错误时也不添加默认数据
    followingUsers.value = []
  }
}

// 加载动态列表
const loadMoments = async (isLoadMore = false) => {
  try {
    console.log('开始加载动态列表')
    
    if (!isLoadMore) {
      currentPage.value = 1
      momentList.value = []
    }
    
    // 调用动态列表接口
    const result = await Request({
      url: '/api/moment/getFollowedMomentList',
      method: 'GET',
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    
    console.log('使用getFollowedMomentList方法的结果:', result)
    
    // 检查返回数据的结构
    let moments = []
    if (result && result.success && result.data) {
      console.log('动态列表数据:', result.data)
      // 处理嵌套的列表结构，将其扁平化为单个列表
      if (Array.isArray(result.data)) {
        // 遍历嵌套列表，将每个子列表中的元素添加到moments中
        result.data.forEach((subList, index) => {
          console.log('子列表', index, ':', subList)
          if (Array.isArray(subList)) {
            subList.forEach(moment => {
              console.log('单个moment数据:', moment)
              moments.push(moment)
            })
          }
        })
      }
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
    console.error('加载动态列表失败:', error)
    // 发生错误时，添加默认数据以便测试
    momentList.value = [
      {
        momentId: 1,
        content: '测试动态1',
        likeCount: 0,
        commentCount: 0,
        createTime: new Date().toISOString(),
        userName: '测试用户1',
        avatar: '/default-avatar.png'
      },
      {
        momentId: 2,
        content: '测试动态2',
        likeCount: 0,
        commentCount: 0,
        createTime: new Date().toISOString(),
        userName: '测试用户2',
        avatar: '/default-avatar.png'
      }
    ]
    console.log('添加默认数据后的动态列表:', momentList.value)
  }
}

// 获取指定用户的动态列表
const getUserMoments = async (userId, userName) => {
  try {
    console.log('开始获取用户动态:', userId, userName)
    
    // 检查userId是否存在
    if (!userId) {
      console.error('用户ID不存在:', userId)
      return
    }
    
    // 重置页码和动态列表
    currentPage.value = 1
    momentList.value = []
    
    // 调用用户动态列表接口
    const result = await Request({
      url: '/api/moment/getMomentList',
      method: 'GET',
      params: {
        userId: userId,
        page: currentPage.value,
        size: pageSize.value
      }
    })
    
    console.log('用户动态列表接口调用结果:', result)
    console.log('请求URL:', `/api/moment/getMomentList?userId=${userId}&page=${currentPage.value}&size=${pageSize.value}`)
    
    // 检查返回数据的结构
    let moments = []
    if (result && result.success && result.data) {
      console.log('用户动态列表数据:', result.data)
      moments = result.data
    } else if (result && result.status === 'success' && result.data) {
      console.log('用户动态列表数据(不同结构):', result.data)
      moments = result.data
    } else {
      console.log('用户动态列表接口调用失败:', result)
    }
    
    console.log('最终用户动态列表:', moments)
    
    // 处理数据，添加缺少的字段
    const processedMoments = moments.map(moment => {
      console.log('原始moment数据:', moment)
      return {
        ...moment,
        // 确保点赞数和评论数存在
        likeCount: moment.likeCount || 0,
        commentCount: moment.commentCount || 0,
        // 确保用户名称和头像存在，优先使用后端返回的数据
        userName: moment.userName || userName || '未知用户',
        avatar: moment.avatar || '/default-avatar.png'
      }
    })
    
    console.log('处理后的用户动态列表:', processedMoments)
    
    // 直接替换动态列表
    momentList.value = processedMoments
    console.log('最终动态列表:', momentList.value)
    console.log('动态列表长度:', momentList.value.length)
  } catch (error) {
    console.error('获取用户动态列表失败:', error)
  }
}

onMounted(() => {
  // 加载关注用户列表
  loadFollowingUsers()
  
  // 加载动态列表
  loadMoments()
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

/* 发布区 */
.publish-area {
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.publish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.publish-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

/* 话题选择 */
.topic-selector {
  margin-bottom: 15px;
}

.topic-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

/* 内容输入 */
.content-input {
  margin-bottom: 15px;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tools {
  display: flex;
  gap: 15px;
}

/* 关注用户列表 */
.following-users {
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.following-header {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.user-avatars {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.user-avatar {
  position: relative;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  padding: 5px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.user-avatar:hover {
  background-color: rgba(255,255,255,0.06);
}

.user-name {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

/* 动态列表 */
.moment-list {
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 标签筛选 */
.moment-tabs {
  margin-bottom: 20px;
}

/* 动态内容 */
.moment-content {
  min-height: 400px;
}

.moment-item {
  border-bottom: 1px solid #e4e7ed;
  padding: 20px 0;
}

.moment-item:last-child {
  border-bottom: none;
}

/* 作者信息 */
.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  font-size: 14px;
}

.publish-time {
  font-size: 12px;
  color: #909399;
}

/* 动态内容 */
.moment-body {
  margin-bottom: 15px;
}

.moment-text {
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 15px;
}

/* 动态图片 */
.moment-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
}

.moment-image {
  border-radius: 4px;
  overflow: hidden;
}

.moment-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 互动区 */
.interaction-area {
  display: flex;
  gap: 20px;
}

/* 评论输入区域 */
.comment-input-area {
  margin-top: 15px;
  padding: 15px;
  background-color: var(--bg-secondary);
  border-radius: 8px;
}

.comment-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 评论列表区域 */
.comment-list-area {
  margin-top: 15px;
  padding: 15px;
  background-color: var(--bg-secondary);
  border-radius: 8px;
}

.comment-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

/* 评论输入区域 */
.comment-input-section {
  margin-bottom: 20px;
  padding: 15px;
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.comment-input-header {
  margin-bottom: 10px;
}

.comment-textarea {
  margin-bottom: 10px;
}

.comment-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 回复输入区域 */
.reply-input-section {
  margin-top: 10px;
  margin-left: 42px;
  padding: 10px;
  background-color: var(--bg-secondary);
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.reply-textarea {
  margin-bottom: 10px;
}

.reply-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.comment-list-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.comment-list {
  margin-top: 10px;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #e4e7ed;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-user-info {
  display: flex;
  flex-direction: column;
}

.comment-user-name {
  font-weight: 500;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  margin-bottom: 10px;
  line-height: 1.5;
}

.comment-actions {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #606266;
}

.comment-action {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.comment-action:hover {
  color: var(--color-primary-light);
}

/* 子评论 */
.sub-comment-list {
  margin-left: 42px;
  margin-top: 10px;
}

.sub-comment-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--glass-border);
}

.sub-comment-item:last-child {
  border-bottom: none;
}

/* 加载更多 */
.load-more {
  text-align: center;
  margin-top: 20px;
}
</style>