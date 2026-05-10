<template>
  <div class="setting-page">
    <!-- 顶部导航栏 -->
    <Header />
    
    <!-- 主内容区 -->
    <div class="main-content">
      <div class="setting-container">
        <div class="page-title">魔女工坊</div>
        
        <div class="setting-content">
          <!-- 左侧导航栏 -->
          <div class="left-nav">
            <div class="nav-item active" @click="activeTab = 'myInfo'">
              <el-icon><User /></el-icon>
              <span>旅人档案</span>
            </div>
            <div class="nav-item" @click="activeTab = 'myAvatar'">
              <el-icon><Picture /></el-icon>
              <span>旅人画像</span>
            </div>
            <div class="nav-item" @click="activeTab = 'myCoin'">
              <el-icon><Document /></el-icon>
              <span>魔法币囊</span>
            </div>
          </div>
          
          <!-- 右侧设置内容 -->
          <div class="right-content">
            <!-- 我的信息 -->
            <div v-if="activeTab === 'myInfo'" class="setting-section">
              <div class="section-title">旅人档案</div>
              
              <div class="form-item">
                <label class="form-label">旅人之名:</label>
                <div class="form-control">
                  <el-input v-model="userInfo.nickName" placeholder="请输入昵称"></el-input>
                  <span class="form-hint">书写新名需消耗6枚魔法币</span>
                </div>
              </div>
              
              <div class="form-item">
                <label class="form-label">我的印记:</label>
                <div class="form-control">
                  <el-input v-model="userInfo.signature" type="textarea" rows="3" placeholder="请输入签名"></el-input>
                </div>
              </div>
              
              <div class="form-item">
                <label class="form-label">旅途装束:</label>
                <div class="form-control">
                  <el-radio-group v-model="userInfo.gender">
                    <el-radio label="male">男</el-radio>
                    <el-radio label="female">女</el-radio>
                    <el-radio label="secret">保密</el-radio>
                  </el-radio-group>
                </div>
              </div>
              
              <div class="form-item">
                <label class="form-label">旅程之始:</label>
                <div class="form-control">
                  <span class="form-disabled">{{ userInfo.createTime || '未设置' }}</span>
                </div>
              </div>
              
              <div class="form-item">
                <label class="form-label">旅伴:</label>
                <div class="form-control">
                  <span class="form-link" @click="goToFollowing">{{ userInfo.followingCount || 0 }}</span>
                </div>
              </div>
              
              <div class="form-item">
                <label class="form-label">仰慕者:</label>
                <div class="form-control">
                  <span class="form-link" @click="goToFollowers">{{ userInfo.followerCount || 0 }}</span>
                </div>
              </div>
              
              <div class="form-item form-actions">
                <el-button type="primary" @click="saveUserInfo">封印·保存</el-button>
              </div>
            </div>
            
            <!-- 我的头像 -->
            <div v-if="activeTab === 'myAvatar'" class="setting-section">
              <div class="section-title">旅人画像</div>
              <div class="avatar-upload">
                <el-avatar :size="150" :src="getAvatarUrl(userInfo.avatar)">
                  {{ userInfo.nickName?.charAt(0) || '用' }}
                </el-avatar>
                <div class="avatar-hint">
                  <p>支持魔法画卷格式</p>
                  <p>画卷魔力不超过2MB</p>
                </div>
                <el-upload
                  action=""
                  :show-file-list="false"
                  :before-upload="handleAvatarUpload"
                >
                  <el-button type="primary">更新旅人画像</el-button>
                </el-upload>
              </div>
            </div>
            
            <!-- 我的硬币 -->
            <div v-if="activeTab === 'myCoin'" class="setting-section">
              <div class="section-title">魔法币囊</div>
              <div class="coin-info">
                <div class="coin-amount">
                  <span class="coin-label">魔法币:</span>
                  <span class="coin-number">{{ userInfo.myCoin || 0 }}</span>
                </div>
                <div class="coin-hint">
                  <p>硬币可以用于打赏视频、修改昵称等操作</p>
                  <p>通过参与活动或充值可以获取更多硬币</p>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { inject } from 'vue'
import { useLoginStore } from '@/stores/loginStore'
import Header from '@/components/Header.vue'
import { User, Picture, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loginStore = useLoginStore()
const Request = inject('Request')
const Api = inject('Api')

// 跳转到关注页面
const goToFollowing = () => {
  router.push('/following')
}

// 跳转到粉丝页面
const goToFollowers = () => {
  router.push('/followers')
}

// 当前激活的标签页
const activeTab = ref('myInfo')

// 用户信息
const userInfo = reactive({
  userId: '',
  nickName: '',
  userName: '',
  signature: '',
  gender: 'secret',
  avatar: '',
  myCoin: 0,
  createTime: '',
  followingCount: 0,
  followerCount: 0
})

// 日期格式化函数，将时间戳或日期字符串格式化为YYYY-MM-DD格式
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  
  // 处理日期字符串，去除可能的时区信息
  if (typeof dateStr === 'string') {
    // 处理包含时区的日期字符串，如"2026-01-14T12:18:43.000+00:00"
    dateStr = dateStr.replace('T', ' ').split('.')[0]
  }
  
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return ''
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  
  return `${year}-${month}-${day}`
}

// 字段映射函数，将数据库返回的字段名转换为前端使用的字段名
const mapDatabaseFields = (data) => {
  // 性别已经由后端转换为字符串，直接使用即可
  let genderStr = data.gender || 'secret'
  
  return {
    userId: data.userId || data.user_id || '',
    nickName: data.nickName || data.nick_name || '',
    userName: data.userName || data.user_id || 'bili',
    signature: data.signature || data.my_signature || '',
    gender: genderStr,
    avatar: data.avatar || '',
    myCoin: data.myCoin || data.my_coin || 0,
    createTime: formatDate(data.createTime || data.create_time) || '',
    followingCount: data.followingCount || data.following_count || 0,
    followerCount: data.followerCount || data.follower_count || 0
  }
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const result = await Request({
      url: Api.getUserInfo,
      params: { userId: loginStore.userInfo.userId }
    })
    if (result) {
      // 将数据库返回的数据进行字段映射后保存到loginStore中
      const mappedData = mapDatabaseFields(result.data)
      Object.assign(userInfo, mappedData)
      loginStore.saveUserInfo(mappedData)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 保存用户信息
const saveUserInfo = async () => {
  try {
    // 性别转换：male-1，female-2，secret-0
    let genderNum = 0
    if (userInfo.gender === 'male') {
      genderNum = 1
    } else if (userInfo.gender === 'female') {
      genderNum = 2
    }
    
    // 转换数据字段名，确保与后端匹配
    const updateData = {
      nickName: userInfo.nickName,
      mySignature: userInfo.signature,
      gender: genderNum
    }
    
    const result = await Request({
      url: Api.updateUserInfo,
      method: 'POST',
      data: updateData
    })
    if (result) {
      ElMessage.success('保存成功')
      // 使用后端返回的最新用户信息更新本地状态
      const mappedData = mapDatabaseFields(result.data)
      Object.assign(userInfo, mappedData)
      // 更新登录状态中的用户信息
      loginStore.saveUserInfo(mappedData)
    }
  } catch (error) {
    console.error('保存用户信息失败:', error)
    ElMessage.error(error.response?.data?.msg || '保存失败，请稍后重试')
  }
}

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

// 上传头像
const handleAvatarUpload = async (file) => {
  try {
    const formData = new FormData()
    formData.append('avatar', file)
    formData.append('userId', loginStore.userInfo.userId)
    
    const result = await Request({
      url: Api.uploadAvatar,
      method: 'POST',
      data: formData
    })
    
    if (result) {
      userInfo.avatar = result.data.avatarUrl
      ElMessage.success('头像上传成功')
      // 更新登录状态中的用户信息
      const loginUserInfo = loginStore.userInfo
      loginUserInfo.avatar = result.data.avatarUrl
      loginStore.saveUserInfo(loginUserInfo)
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请稍后重试')
  }
  
  return false // 阻止默认上传行为
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.setting-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

.main-content {
  margin-top: 60px; /* 60px header */
  padding: 0 20px;
}

.setting-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: var(--text-primary);
}

.setting-content {
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

.nav-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  margin-bottom: 10px;
  cursor: pointer;
  border-radius: 5px;
  transition: all 0.3s;
}

.nav-item:hover {
  background-color: var(--bg-secondary);
}

.nav-item.active {
  background-color: var(--color-primary-light);
  color: #fff;
}

.nav-item .el-icon {
  margin-right: 10px;
  font-size: 18px;
}

/* 右侧设置内容 */
.right-content {
  flex: 1;
  padding-left: 30px;
}

.setting-section {
  width: 100%;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  color: var(--text-primary);
  padding-bottom: 10px;
  border-bottom: 2px solid #e8e8e8;
}

/* 表单样式 */
.form-item {
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
}

.form-label {
  width: 100px;
  text-align: right;
  margin-right: 20px;
  font-weight: bold;
  color: var(--text-primary);
  padding-top: 8px;
}

.form-control {
  flex: 1;
}

.form-hint {
  font-size: 12px;
  color: var(--text-dim);
  margin-top: 5px;
}

.form-suffix {
  color: var(--text-dim);
  margin-left: 10px;
}

.form-disabled {
  color: var(--text-dim);
}

.form-link {
  color: var(--color-primary-light);
  cursor: pointer;
  text-decoration: none;
}

.form-link:hover {
  text-decoration: underline;
}

.form-actions {
  margin-left: 120px;
}

/* 头像上传 */
.avatar-upload {
  text-align: center;
  padding: 30px;
}

.avatar-upload .el-avatar {
  margin-bottom: 20px;
}

/* 硬币信息 */
.coin-info {
  padding: 30px;
  text-align: center;
}

.coin-amount {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
}

.coin-label {
  color: var(--text-secondary);
  margin-right: 10px;
}

.coin-number {
  color: #ff9500;
  font-size: 36px;
}

.coin-hint {
  color: var(--text-dim);
  font-size: 14px;
}

.coin-hint p {
  margin: 5px 0;
}

.avatar-hint {
  margin-bottom: 20px;
  color: var(--text-dim);
  font-size: 14px;
}

.avatar-hint p {
  margin: 5px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .setting-content {
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
  
  .form-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .form-label {
    text-align: left;
    margin-bottom: 10px;
  }
  
  .form-actions {
    margin-left: 0;
  }
}
</style>
