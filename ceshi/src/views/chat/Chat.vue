<template>
  <div class="chat-container">
    <!-- 顶部导航栏 -->
    <div class="chat-header">
      <h2>消息中心</h2>
      <div class="header-right">
        <el-badge :value="totalUnreadCount" :hidden="totalUnreadCount === 0">
          <el-button type="text" icon="el-icon-bell"></el-button>
        </el-badge>
        <el-button type="text" icon="el-icon-setting"></el-button>
      </div>
    </div>
    
    <!-- 聊天内容区 -->
    <div class="chat-content">
      <!-- 左侧消息列表 -->
      <div class="chat-sidebar">
        <div class="message-list">
          <div v-if="isLoadingSessions" class="loading-sessions">
            <el-skeleton :rows="3" animated></el-skeleton>
          </div>
          <div v-else>
            <!-- AI 智能助手入口 -->
            <div
              :class="[
                'message-item', 'ai-assistant-item',
                currentChatUserId === 0 ? 'active' : ''
              ]"
              @click="selectAiAssistant"
            >
              <div class="message-avatar">
                <div class="ai-avatar">AI</div>
              </div>
              <div class="message-info">
                <div class="message-top">
                  <span class="message-name">AI 智能助手</span>
                </div>
                <div class="message-bottom">
                  <span class="last-message">有问题随时问我</span>
                </div>
              </div>
            </div>
            <div
              v-for="session in sessions"
              :key="session.otherUserId"
              :class="[
                'message-item',
                currentChatUserId === session.otherUserId ? 'active' : ''
              ]"
              @click="selectChat(session.otherUserId, session.otherUserName)"
            >
              <div class="message-avatar">
                <img :src="session.otherUserAvatar || '/default-avatar.png'" alt="头像" />
                <span v-if="session.unreadCount > 0" class="unread-badge">{{ session.unreadCount }}</span>
              </div>
              <div class="message-info">
                <div class="message-top">
                  <span class="message-name">{{ session.otherUserName }}</span>
                  <span class="message-time">{{ session.updateTime }}</span>
                </div>
                <div class="message-bottom">
                  <span class="last-message">{{ session.lastMessage }}</span>
                </div>
              </div>
            </div>
            <div v-if="sessions.length === 0" class="no-messages">
              暂无消息
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧聊天窗口 -->
      <div class="chat-main">
        <div v-if="currentChatUserId == null" class="empty-chat">
          <el-empty description="选择一个聊天开始对话" />
        </div>
        <div v-else class="chat-window">
          <!-- 聊天头部 -->
          <div class="chat-window-header">
            <div class="chat-user-info">
              <div v-if="currentChatUserId === 0" class="ai-avatar ai-avatar-large">AI</div>
              <el-avatar v-else :src="currentChatUserAvatar" size="large"></el-avatar>
              <span class="chat-user-name">{{ currentChatUserName }}</span>
            </div>
            <div class="chat-header-actions">
              <el-button type="text" icon="el-icon-more"></el-button>
            </div>
          </div>
          
          <!-- 聊天消息区 -->
          <div class="chat-messages" ref="messagesContainer">
            <div v-if="isLoadingMessages" class="loading-messages">
              <el-skeleton :rows="5" animated></el-skeleton>
            </div>
            <div v-else>
              <div
                v-for="(message, index) in messages"
                :key="index"
                :class="[
                  'message-item',
                  isMessageSentByCurrentUser(message) ? 'message-sent' : 'message-received'
                ]"
              >
                <div class="message-avatar">
                  <div
                    v-if="!isMessageSentByCurrentUser(message) && currentChatUserId === 0"
                    class="ai-avatar"
                    style="width: 36px; height: 36px; font-size: 12px; flex-shrink: 0;"
                  >AI</div>
                  <img
                    v-else
                    :src="
                      isMessageSentByCurrentUser(message)
                        ? currentUserAvatar
                        : currentChatUserAvatar
                    "
                    alt="头像"
                    @error="e => {
                      e.target.src = 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 40 40%22%3E%3Crect fill=%22%23ddd%22 width=%2240%22 height=%2240%22/%3E%3Ctext x=%2220%22 y=%2226%22 text-anchor=%22middle%22 font-size=%2216%22 fill=%22%23999%22%3E?%3C/text%3E%3C/svg%3E'
                    }"
                  />
                </div>
                <div class="message-content">
                  <div class="message-bubble">
                    {{ message.content }}
                  </div>
                  <div class="message-time">{{ formatTime(message.sendTime) }}</div>
                </div>
              </div>
              <div v-if="messages.length === 0" class="no-chat-messages">
                开始聊天吧！
              </div>
            </div>
          </div>
          
          <!-- 聊天输入区 -->
          <div class="chat-input-area">
            <el-input
              v-model="inputMessage"
              placeholder="输入消息..."
              type="textarea"
              :rows="1"
              resize="none"
              @keyup.enter.exact="sendMessage"
              :disabled="isSendingMessage"
            />
            <el-button type="primary" @click="sendMessage" :loading="isSendingMessage">发送</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useLoginStore } from '@/stores/loginStore'
import { Message } from '@element-plus/icons-vue'
import { inject } from 'vue'

const route = useRoute()
const loginStore = useLoginStore()
const Request = inject('Request')
const Api = inject('Api')

// 当前登录用户信息
const currentUserId = computed(() => loginStore.userInfo?.userId)
const currentUserAvatar = computed(() => {
  const avatar = getAvatarUrl(loginStore.userInfo?.avatar)
  console.log('当前用户头像:', avatar)
  return avatar
})
console.log('当前用户ID:', currentUserId.value)

// 获取完整的头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) {
    console.log('头像为空，使用默认头像')
	    return 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 40 40%22%3E%3Crect fill=%22%23ddd%22 width=%2240%22 height=%2240%22/%3E%3Ctext x=%2220%22 y=%2226%22 text-anchor=%22middle%22 font-size=%2216%22 fill=%22%23999%22%3E?%3C/text%3E%3C/svg%3E'
  }
  
  // 如果已经是完整的URL，则直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    console.log('使用完整URL头像:', avatar)
    return avatar
  }
  
  // 添加完整的域名和端口
  const baseUrl = ''
  // 确保baseUrl和avatar之间有斜杠
  let avatarUrl = avatar
  if (!avatarUrl.startsWith('/')) {
    avatarUrl = `/${avatarUrl}`
  }
  // 确保头像路径包含 /avatar/ 前缀
  if (!avatarUrl.startsWith('/avatar/')) {
    avatarUrl = `/avatar${avatarUrl}`
  }
  const fullUrl = `${baseUrl}${avatarUrl}`
  console.log('构建的头像URL:', fullUrl)
  return fullUrl
}

// 调试函数：检查头像URL
const debugAvatarUrls = () => {
  console.log('=== 头像URL调试 ===')
  console.log('当前用户ID:', currentUserId.value)
  console.log('当前用户头像:', currentUserAvatar.value)
  console.log('当前聊天用户ID:', currentChatUserId.value)
  console.log('当前聊天用户头像:', currentChatUserAvatar.value)
  console.log('会话列表:', sessions.value)
  console.log('=== 调试结束 ===')
}

// 辅助函数：判断消息是否由当前用户发送
const isMessageSentByCurrentUser = (message) => {
  if (!message || !message.senderId || !currentUserId.value) return false;
  return parseInt(message.senderId) === parseInt(currentUserId.value);
};

// 调试函数：检查消息
const debugMessages = () => {
  console.log('=== 消息调试 ===')
  console.log('当前用户ID:', currentUserId.value)
  console.log('消息列表:', messages.value)
  messages.value.forEach((message, index) => {
    const isSentByCurrentUser = isMessageSentByCurrentUser(message);
    console.log(`消息${index}:`, {
      senderId: message.senderId,
      receiverId: message.receiverId,
      content: message.content,
      isSentByCurrentUser: isSentByCurrentUser,
      senderIdType: typeof message.senderId,
      currentUserIdType: typeof currentUserId.value,
      parsedSenderId: parseInt(message.senderId),
      parsedCurrentUserId: parseInt(currentUserId.value)
    })
  })
  console.log('=== 调试结束 ===')
}

// 激活的标签页
const activeTab = ref('myMessages')

// 会话列表
const sessions = ref([])

// 总未读消息数
const totalUnreadCount = ref(0)

// 当前聊天用户
const currentChatUserId = ref(null)
const currentChatUserName = ref('')
const currentChatUserAvatar = ref('/default-avatar.png')
console.log('初始化currentChatUserAvatar:', currentChatUserAvatar.value)

// 聊天消息
const messages = ref([])

// 输入消息
const inputMessage = ref('')

// 消息容器引用
const messagesContainer = ref(null)

// 加载状态
const isLoadingSessions = ref(false)
const isLoadingMessages = ref(false)
const isSendingMessage = ref(false)

// WebSocket连接
let websocket = null

// 初始化WebSocket连接
const initWebSocket = () => {
  if (!currentUserId.value) {
    console.log('用户未登录，无法建立WebSocket连接')
    return
  }
  
  // 构建WebSocket连接地址
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${wsProtocol}//${window.location.host}/ws/chat/${currentUserId.value}`
  console.log('建立WebSocket连接:', wsUrl)
  
  try {
    websocket = new WebSocket(wsUrl)
    
    websocket.onopen = () => {
      console.log('WebSocket连接成功')
    }
    
    websocket.onmessage = (event) => {
      console.log('收到WebSocket消息:', event.data)
      try {
        const message = JSON.parse(event.data)
        // 检查消息是否与当前聊天用户相关
        if (message.senderId === parseInt(currentChatUserId.value) || 
            message.receiverId === parseInt(currentChatUserId.value)) {
          // 添加到消息列表
          messages.value.push(message)
          scrollToBottom()
        }
        // 刷新会话列表以更新最后一条消息和未读数
        loadSessions()
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }
    
    websocket.onerror = (error) => {
      console.error('WebSocket错误:', error)
    }
    
    websocket.onclose = () => {
      console.log('WebSocket连接关闭')
    }
  } catch (error) {
    console.error('建立WebSocket连接失败:', error)
  }
}

// 关闭WebSocket连接
const closeWebSocket = () => {
  if (websocket) {
    websocket.close()
    websocket = null
    console.log('WebSocket连接已关闭')
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 选择 AI 助手
const selectAiAssistant = async () => {
  currentChatUserId.value = 0
  currentChatUserName.value = 'AI 智能助手'
  currentChatUserAvatar.value = ''
  // 加载 AI 助手的历史消息
  loadMessages()
}

// 选择聊天用户
const selectChat = async (userId, userName) => {
  console.log('选择聊天用户:', userId, userName)
  // 确保currentChatUserId是字符串类型，用于前端显示
  currentChatUserId.value = userId
  currentChatUserName.value = userName
  
  // 查找用户头像
  // 注意：这里需要考虑userId的类型转换，可能是字符串或数字
  const session = sessions.value.find(s => {
    return String(s.otherUserId) === String(userId)
  })
  if (session) {
    currentChatUserAvatar.value = session.otherUserAvatar
    console.log('找到用户头像:', session.otherUserAvatar)
  } else {
    console.log('未找到用户会话，使用默认头像')
    currentChatUserAvatar.value = 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 40 40%22%3E%3Crect fill=%22%23ddd%22 width=%2240%22 height=%2240%22/%3E%3Ctext x=%2220%22 y=%2226%22 text-anchor=%22middle%22 font-size=%2216%22 fill=%22%23999%22%3E?%3C/text%3E%3C/svg%3E'
  }
  
  // 检查会话是否存在，不存在则创建
  if (!session && currentUserId.value) {
    console.log('会话不存在，创建会话')
    await addChatSession(userId)
  } else {
    // 会话存在，直接加载消息
    loadMessages()
  }
}

// 添加聊天会话
const addChatSession = async (otherUserId) => {
  if (!currentUserId.value) return
  
  try {
    // 确保参数是整数类型
    const userId = parseInt(currentUserId.value)
    const otherId = parseInt(otherUserId)
    
    console.log('添加聊天会话:', userId, otherId)
    const result = await Request({
      url: Api.addChatSession,
      params: {
        userId: userId,
        otherUserId: otherId
      },
      method: 'get'
    })
    
    console.log('添加聊天会话结果:', result)
    if (result && result.success) {
      // 重新加载会话列表
      await loadSessions()
      console.log('会话列表已更新')
      // 加载消息
      loadMessages()
    }
  } catch (error) {
    console.error('添加聊天会话失败:', error)
    // 失败后仍然继续，不影响聊天功能
    loadMessages()
  }
}

// 加载会话列表
const loadSessions = async () => {
  if (!currentUserId.value) return
  
  try {
    isLoadingSessions.value = true
    console.log('开始加载会话列表，用户ID:', currentUserId.value)
    const result = await Request({
      url: Api.getSessions,
      params: {
        userId: currentUserId.value
      },
      method: 'get'
    })
    
    console.log('加载会话列表结果:', result)
    if (result && result.success) {
      // 处理会话列表，确保头像URL正确
      sessions.value = (result.data || []).map(session => {
        const processedSession = {
          ...session,
          otherUserAvatar: getAvatarUrl(session.otherUserAvatar)
        }
        console.log('处理会话:', session.otherUserId, '原头像:', session.otherUserAvatar, '处理后:', processedSession.otherUserAvatar)
        return processedSession
      })
      console.log('会话列表加载完成:', sessions.value)
      
      // 计算总未读消息数
      totalUnreadCount.value = sessions.value.reduce((sum, session) => sum + (session.unreadCount || 0), 0)
      
      // 如果当前有聊天用户，更新其头像
      if (currentChatUserId.value) {
        const session = sessions.value.find(s => {
          return String(s.otherUserId) === String(currentChatUserId.value)
        })
        if (session) {
          currentChatUserAvatar.value = session.otherUserAvatar
          console.log('更新当前聊天用户头像:', currentChatUserAvatar.value)
        }
      }
    } else {
      ElMessage.error('加载会话列表失败')
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败')
  } finally {
    isLoadingSessions.value = false
  }
}

// 加载聊天消息
const loadMessages = async () => {
  if (!currentUserId.value || currentChatUserId.value == null) return
  
  try {
    isLoadingMessages.value = true
    const result = await Request({
      url: Api.getMessages,
      params: {
        userId: currentUserId.value,
        otherUserId: currentChatUserId.value
      },
      method: 'get'
    })
    
    if (result && result.success) {
    messages.value = result.data || []
    // 调用消息调试函数
    debugMessages()
    await scrollToBottom()
    
    // 消息加载成功后，刷新会话列表以更新未读消息数
    await loadSessions()
  } else {
    ElMessage.error('加载聊天消息失败')
  }
  } catch (error) {
    console.error('加载聊天消息失败:', error)
    ElMessage.error('加载聊天消息失败')
  } finally {
    isLoadingMessages.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  if (!currentUserId.value || currentChatUserId.value == null) return
  
  const content = inputMessage.value.trim()
  
  try {
    isSendingMessage.value = true
    const result = await Request({
      url: Api.sendMessage,
      data: {
        senderId: currentUserId.value,
        receiverId: currentChatUserId.value,
        content
      },
      method: 'post'
    })
    
    if (result && result.success) {
      inputMessage.value = ''
      // 重新加载消息列表（AI 回复已同步入库）
      await loadMessages()
      // 刷新会话列表
      loadSessions()
    } else {
      ElMessage.error('发送消息失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  } finally {
    isSendingMessage.value = false
  }
}

// 监听路由参数变化
watch(() => route.query.otherUserId, async (newUserId) => {
  if (newUserId) {
    await selectChat(newUserId, route.query.otherUserName)
  }
})

onMounted(async () => {
  console.log('组件挂载，初始化聊天页面')
  await loadSessions()
  
  // 初始化WebSocket连接
  initWebSocket()
  
  // 检查路由参数，自动选择聊天用户
  const otherUserId = route.query.otherUserId
  const otherUserName = route.query.otherUserName
  if (otherUserId) {
    console.log('路由参数中有其他用户ID:', otherUserId, '用户名:', otherUserName)
    await selectChat(otherUserId, otherUserName)
  }
  
  // 调用调试函数
  setTimeout(debugAvatarUrls, 1000)
})
</script>

<style scoped>
.chat-page {
  background: var(--bg-primary);
}

.chat-container {
  min-height: 100vh;
  background-color: var(--bg-secondary);
  display: flex;
  flex-direction: column;
}

/* 聊天头部 */
.chat-header {
  height: 60px;
  background-color: rgba(255,255,255,0.04);
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.chat-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 聊天内容区 */
.chat-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧消息列表 */
.chat-sidebar {
  width: 300px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-right: 1px solid rgba(255, 255, 255, 0.12);
  display: flex;
  flex-direction: column;
}



.message-list {
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.message-item {
  display: flex;
  align-items: center;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 1px solid var(--glass-border);
}

.message-item:hover {
  background-color: var(--bg-secondary);
}

.message-item.active {
  background-color: #f0f9ff;
  border-right: 3px solid var(--color-primary-light);
}

.message-avatar {
  position: relative;
  margin-right: 12px;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.ai-avatar-large {
  width: 44px;
  height: 44px;
  font-size: 16px;
  flex-shrink: 0;
}

.message-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  background-color: #ff4d4f;
  color: #fff;
  border-radius: 9px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-info {
  flex: 1;
  min-width: 0;
}

.message-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.message-name {
  font-weight: 500;
  font-size: 14px;
  color: var(--text-primary);
}

.message-time {
  font-size: 12px;
  color: var(--text-dim);
}

.message-bottom {
  display: flex;
  align-items: center;
}

.last-message {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

/* 无消息状态 */
.no-messages {
  text-align: center;
  padding: 40px 0;
  color: var(--text-dim);
  font-size: 14px;
}

.no-chat-messages {
  text-align: center;
  padding: 60px 0;
  color: var(--text-dim);
  font-size: 14px;
}

.no-content {
  text-align: center;
  padding: 60px 0;
  color: var(--text-dim);
  font-size: 14px;
}

/* 加载状态 */
.loading-sessions,
.loading-messages {
  padding: 20px;
}

.loading-sessions .el-skeleton,
.loading-messages .el-skeleton {
  width: 100%;
}

/* 右侧聊天窗口 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-secondary);
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 聊天窗口头部 */
.chat-window-header {
  height: 60px;
  background-color: rgba(255,255,255,0.04);
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-user-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary);
}

.chat-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 聊天消息区 */
.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.chat-messages .message-item {
  border-bottom: none;
  padding: 0;
  display: flex;
  align-items: flex-end;
  gap: 10px;
  margin-bottom: 10px;
  width: fit-content;
  max-width: 100%;
}

/* 自己发送的消息（右侧） */
.chat-messages .message-item.message-sent {
  justify-content: flex-end !important;
  flex-direction: row-reverse !important;
  margin-left: 20%; /* 为发送的消息添加左侧边距，增加与接收消息的距离 */
}

.chat-messages .message-item.message-received {
  justify-content: flex-start !important;
  flex-direction: row !important;
  margin-right: 20%; /* 为接收的消息添加右侧边距，增加与发送消息的距离 */
}

/* 对方发送的消息（左侧） */
.chat-messages .message-item.message-received {
  justify-content: flex-start !important;
}

/* 确保消息内容的宽度和对齐 */
.chat-messages .message-item.message-sent .message-content {
  align-items: flex-end !important;
}

.chat-messages .message-item.message-received .message-content {
  align-items: flex-start !important;
}

.chat-messages .message-avatar img {
  width: 36px;
  height: 36px;
}

.chat-messages .message-avatar {
  position: relative;
}

.chat-messages .message-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

/* 确保头像加载失败时显示默认头像 */
.chat-messages .message-avatar img[src=""] {
  src: '/default-avatar.png';
}

.chat-messages .message-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

.chat-messages .message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.4;
  margin: 4px 0;
}

.chat-messages .message-item.message-sent .message-bubble {
  background-color: var(--color-primary-light) !important;
  color: #fff !important;
  border-top-left-radius: 18px !important;
  border-top-right-radius: 4px !important;
  border-bottom-left-radius: 18px !important;
  border-bottom-right-radius: 18px !important;
  box-shadow: 0 1px 2px rgba(24, 144, 255, 0.3) !important;
}

.chat-messages .message-item.message-received .message-bubble {
  background-color: rgba(255,255,255,0.08) !important;
  color: var(--text-primary) !important;
  border-top-left-radius: 4px !important;
  border-top-right-radius: 18px !important;
  border-bottom-left-radius: 18px !important;
  border-bottom-right-radius: 18px !important;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1) !important;
}

.chat-messages .message-item.message-sent .message-time {
  font-size: 11px !important;
  color: var(--text-dim) !important;
  margin-top: 4px !important;
  text-align: right !important;
}

.chat-messages .message-item.message-received .message-time {
  font-size: 11px !important;
  color: var(--text-dim) !important;
  margin-top: 4px !important;
  text-align: left !important;
}

/* 聊天输入区 */
.chat-input-area {
  padding: 15px 20px;
  background-color: rgba(255,255,255,0.04);
  border-top: 1px solid #e0e0e0;
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.chat-input-area .el-input {
  flex: 1;
}

.chat-input-area .el-textarea__inner {
  border-radius: 20px;
  resize: none;
  min-height: 40px;
  max-height: 120px;
}

.chat-input-area .el-button {
  height: 40px;
  border-radius: 20px;
  flex-shrink: 0;
}

/* 滚动条样式 */
.message-list::-webkit-scrollbar,
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track,
.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb,
.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover,
.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-sidebar {
    width: 260px;
  }
  
  .chat-messages .message-content {
    max-width: 80%;
  }
}

@media (max-width: 576px) {
  .chat-content {
    flex-direction: column;
  }
  
  .chat-sidebar {
    width: 100%;
    height: 300px;
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .message-list {
    height: calc(300px - 120px);
  }
}
</style>