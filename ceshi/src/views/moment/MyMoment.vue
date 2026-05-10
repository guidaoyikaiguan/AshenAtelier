<template>
  <div class="my-moment-page">
    <Header />
    <Sidebar />

    <div class="main-content">
      <div class="my-moment-header">
        <h2>我的见闻</h2>
        <el-button type="primary" @click="goToPublish">记录新的见闻</el-button>
      </div>

      <el-empty v-if="!loading && momentList.length === 0" description="还没有发布过见闻">
        <el-button type="primary" @click="goToPublish">去发布第一条见闻</el-button>
      </el-empty>

      <div class="moment-list">
        <div v-for="moment in momentList" :key="moment.momentId" class="moment-item">
          <div class="moment-body">
            <div class="moment-meta">
              <span class="moment-time">{{ formatTime(moment.createTime) }}</span>
              <span class="moment-stats">
                <el-icon><Star /></el-icon> {{ moment.likeCount || 0 }}
                <el-icon><ChatDotRound /></el-icon> {{ moment.commentCount || 0 }}
              </span>
            </div>

            <!-- 编辑模式 -->
            <div v-if="editingId === moment.momentId" class="edit-area">
              <el-input
                v-model="editContent"
                type="textarea"
                :rows="3"
                placeholder="编辑见闻内容..."
              />
              <div class="edit-actions">
                <el-button size="small" @click="cancelEdit">取消</el-button>
                <el-button size="small" type="primary" @click="saveEdit(moment)">保存</el-button>
              </div>
            </div>

            <!-- 展示模式 -->
            <div v-else class="moment-content">{{ moment.content }}</div>

            <div v-if="moment.images && moment.images.length > 0" class="moment-images">
              <div v-for="(image, idx) in moment.images" :key="idx" class="moment-image">
                <img :src="image.imageUrl" :alt="'图片 ' + (idx + 1)" />
              </div>
            </div>
          </div>

          <div class="moment-actions">
            <el-button size="small" type="primary" plain @click="startEdit(moment)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button size="small" type="danger" plain @click="deleteMoment(moment.momentId, moment.userId)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/Header.vue'
import Sidebar from '@/components/Sidebar.vue'
import { Star, ChatDotRound, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const Request = inject('Request')

const momentList = ref([])
const loading = ref(false)
const editingId = ref(null)
const editContent = ref('')

const currentUserId = ref(null)
const userInfoStr = localStorage.getItem('userInfo')
if (userInfoStr) {
  try {
    currentUserId.value = JSON.parse(userInfoStr).userId
  } catch (e) {}
}

const formatTime = (time) => {
  if (!time) return ''
  const now = new Date()
  const createTime = new Date(time)
  const diff = now - createTime
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return createTime.toISOString().split('T')[0]
}

const goToPublish = () => router.push('/moment/publish')

const loadMyMoments = async () => {
  loading.value = true
  try {
    const result = await Request({
      url: '/api/moment/getMomentList',
      params: { userId: currentUserId.value }
    })
    if (result && result.success && result.data) {
      momentList.value = result.data
    }
  } catch (e) {
    console.error('加载我的动态失败:', e)
  } finally {
    loading.value = false
  }
}

const startEdit = (moment) => {
  editingId.value = moment.momentId
  editContent.value = moment.content
}

const cancelEdit = () => {
  editingId.value = null
  editContent.value = ''
}

const saveEdit = async (moment) => {
  if (!editContent.value.trim()) {
    ElMessage.warning('内容不能为空')
    return
  }
  try {
    const result = await Request({
      url: '/api/moment/updateMoment',
      method: 'POST',
      params: {
        momentId: moment.momentId,
        content: editContent.value.trim(),
        userId: currentUserId.value
      }
    })
    if (result && result.success) {
      ElMessage.success('修改成功')
      moment.content = editContent.value.trim()
      cancelEdit()
    } else {
      ElMessage.error(result?.msg || '修改失败')
    }
  } catch (e) {
    console.error('修改动态失败:', e)
  }
}

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
      await loadMyMoments()
    } else {
      ElMessage.error(result?.msg || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error('删除动态失败:', e)
  }
}

onMounted(() => {
  if (currentUserId.value) {
    loadMyMoments()
  }
})
</script>

<style scoped>
.my-moment-page {
  background: var(--bg-primary, #0a0a1a);
  min-height: 100vh;
}

.main-content {
  margin-left: 200px;
  margin-top: 60px;
  padding: 24px 40px;
  max-width: 800px;
}

.my-moment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.my-moment-header h2 {
  color: var(--text-primary);
  font-size: 24px;
  margin: 0;
}

.moment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.moment-item {
  background: rgba(22, 20, 48, 0.6);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(255,255,255,0.06);
}

.moment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  color: var(--text-dim, #888);
  font-size: 13px;
}

.moment-stats {
  display: flex;
  align-items: center;
  gap: 4px;
}

.moment-stats .el-icon {
  margin-left: 12px;
}

.moment-content {
  color: var(--text-primary, #ddd);
  line-height: 1.7;
  white-space: pre-wrap;
  margin-bottom: 12px;
}

.edit-area {
  margin-bottom: 12px;
}

.edit-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  justify-content: flex-end;
}

.moment-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.moment-image img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}

.moment-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid rgba(255,255,255,0.06);
}

.loading {
  text-align: center;
  color: var(--text-dim);
  padding: 40px;
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 60px;
    padding: 16px;
  }
}
</style>
