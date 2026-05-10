<template>
  <div class="collect-page">
    <!-- 顶部导航栏 -->
    <Header />
    
    <!-- 主内容区 -->
    <div class="main-content">
      <div class="collect-container">
        <div class="page-title">{{ isViewingOtherUser ? 'TA的旅途珍藏' : '旅途珍藏' }}</div>
        
        <div class="collect-content">
          <!-- 左侧导航栏 - 收藏夹列表 -->
          <div class="left-nav">
            <div class="nav-section">
              <div class="section-title">{{ isViewingOtherUser ? 'TA创建的珍藏夹' : '我创建的珍藏夹' }}</div>
              <div v-if="!isViewingOtherUser" class="nav-item create-folder" @click="createNewFolder">
                <el-icon><Plus /></el-icon>
                <span>编织新珍藏夹</span>
              </div>
              <div 
                v-for="folder in folders" 
                :key="folder.id" 
                class="nav-item"
                :class="{ active: activeFolderId === folder.id }"
              >
                <div class="folder-info" @click="selectFolder(folder.id)">
                  <el-icon><Folder /></el-icon>
                  <span>{{ folder.name }}</span>
                  <span class="count">{{ folder.count }}</span>
                </div>
                <div v-if="!isViewingOtherUser" class="folder-actions">
                  <el-dropdown @command="(command) => handleFolderAction(command, folder)">
                    <el-button type="text" size="small" class="action-btn">
                      <el-icon><More /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="delete">删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>
            
            <div class="nav-section">
              <div class="section-title">其他收藏</div>
              <div class="nav-item">
                <el-icon><Clock /></el-icon>
                <span>待览绘卷</span>
                <span class="count">{{ laterWatchCount }}</span>
              </div>
              <div class="nav-item">
                <el-icon><Document /></el-icon>
                <span>我的笔记</span>
                <span class="count">{{ noteCount }}</span>
              </div>
            </div>
          </div>
          
          <!-- 右侧内容 - 收藏的视频 -->
          <div class="right-content">
            <!-- 收藏夹信息 -->
            <div class="folder-info" v-if="activeFolder">
              <h3>{{ activeFolder.name }}</h3>
              <div class="folder-meta">
                <span class="status">{{ activeFolder.isPublic ? '公开' : '私有' }}</span>
                <span class="count">视频数: {{ activeFolder.count }}</span>
              </div>
              <div v-if="!isViewingOtherUser" class="folder-actions">
                <el-button type="primary" size="small">播放全部</el-button>
                <el-button type="default" size="small">批量操作</el-button>
              </div>
            </div>
            
            <!-- 视频筛选标签 -->
            <div class="filter-tabs">
              <el-button 
                v-for="filter in filters" 
                :key="filter.key"
                :type="activeFilter === filter.key ? 'primary' : 'default'"
                @click="handleFilterChange(filter.key)"
              >
                {{ filter.name }}
              </el-button>
            </div>
            
            <!-- 视频列表 -->
            <div class="video-list">
              <el-empty v-if="videos.length === 0" description="暂无收藏视频"></el-empty>
              <div v-for="video in videos" :key="video.videoId" class="video-item" @click="goToVideoDetail(video)">
                <div class="video-cover">
                  <img :src="video.coverUrl" alt="视频封面">
                  <span class="video-duration">{{ formatDuration(video.duration) }}</span>
                </div>
                <div class="video-info">
                  <p class="video-title">{{ video.title }}</p>
                  <p class="video-author">{{ video.author }}</p>
                  <p class="video-stats">{{ video.playCount }}播放 · {{ video.collectTime }}</p>
                </div>
                <div v-if="!isViewingOtherUser" class="video-actions">
                  <el-dropdown @command="(command) => handleVideoAction(command, video)">
                    <el-button type="text" size="small" class="action-btn" @click.stop>
                      <el-icon><More /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="move">移动到收藏夹</el-dropdown-item>
                        <el-dropdown-item command="remove">从收藏中移除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>
            
            <!-- 分页 -->
            <div class="pagination" v-if="videos.length > 0">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                layout="prev, pager, next, jumper, ->, total"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- 新建收藏夹弹窗 -->
  <el-dialog
    v-if="!isViewingOtherUser"
    v-model="createFolderDialogVisible"
    title="编织新珍藏夹"
    width="400px"
  >
    <el-form>
      <el-form-item label="珍藏夹名称">
        <el-input v-model="newFolderName" placeholder="请输入珍藏夹名称"></el-input>
      </el-form-item>
      <el-form-item label="隐私设置">
        <el-radio-group v-model="newFolderIsPublic">
          <el-radio :label="true">公开</el-radio>
          <el-radio :label="false">私有</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="createFolderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNewFolder">确定</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 编辑收藏夹弹窗 -->
  <el-dialog
    v-if="!isViewingOtherUser"
    v-model="editFolderDialogVisible"
    title="编辑珍藏夹"
    width="400px"
  >
    <el-form>
      <el-form-item label="珍藏夹名称">
        <el-input v-model="editFolderName" placeholder="请输入珍藏夹名称"></el-input>
      </el-form-item>
      <el-form-item label="隐私设置">
        <el-radio-group v-model="editFolderIsPublic">
          <el-radio :label="true">公开</el-radio>
          <el-radio :label="false">私有</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="editFolderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditFolder">确定</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 移动视频到收藏夹弹窗 -->
  <el-dialog
    v-if="!isViewingOtherUser"
    v-model="moveVideoDialogVisible"
    title="移动视频到珍藏夹"
    width="400px"
  >
    <el-form>
      <el-form-item label="选择珍藏夹">
        <el-select v-model="targetFolderId" placeholder="请选择要移动到的珍藏夹">
          <el-option
            v-for="folder in availableFolders"
            :key="folder.favorite"
            :label="folder.favoriteName"
            :value="folder.favorite"
          >
            {{ folder.favoriteName }}
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="moveVideoDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleMoveVideo">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLoginStore } from '@/stores/loginStore'
import Header from '@/components/Header.vue'
import { Plus, Folder, Clock, Document, Delete, More } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const Request = inject('Request')
const Api = inject('Api')
const loginStore = useLoginStore()
const route = useRoute()
const router = useRouter()

// 获取路由参数中的用户ID，用于判断是否查看其他用户的收藏
const routeUserId = computed(() => route.query.userId)
// 是否查看其他用户的收藏
const isViewingOtherUser = computed(() => !!routeUserId.value)

// 收藏夹列表（从后端动态获取）
const folders = ref([])

// 其他收藏计数
const laterWatchCount = ref(0)
const noteCount = ref(0)

// 当前激活的收藏夹
const activeFolderId = ref(null)
const activeFolder = ref(null)

// 视频筛选标签
const filters = ref([
  { key: 'recent', name: '最近收藏' },
  { key: 'mostPlayed', name: '最多播放' },
  { key: 'mostCollected', name: '最近投稿' }
])
const activeFilter = ref('recent')

// 处理筛选标签点击
const handleFilterChange = async (filterKey) => {
  console.log('开始处理筛选标签点击:', filterKey)
  activeFilter.value = filterKey
  console.log('筛选标签已更新为:', activeFilter.value)
  console.log('当前激活的收藏夹ID:', activeFolderId.value)
  
  let sortord = 1
  if (filterKey === 'mostPlayed') {
    sortord = 2
  } else if (filterKey === 'mostCollected') {
    sortord = 3
  }
  
  if (activeFolderId.value !== null) {
    console.log('准备重新加载视频列表，参数:', {
      folderId: activeFolderId.value,
      page: currentPage.value,
      size: pageSize.value,
      sortord: sortord
    })
    await loadCollectList(activeFolderId.value, currentPage.value, pageSize.value, sortord)
    console.log('视频列表加载完成')
  } else {
    console.log('activeFolderId为null，无法加载视频列表')
  }
}

// 收藏的视频列表（从后端动态获取）
const videos = ref([])

// 分页数据
const currentPage = ref(1)
const pageSize = ref(15)
const total = ref(0)

// 粉圈：获取收藏夹列表（调用getFavorite方法或getOtherUserFavorite方法）
const loadFavoriteList = async () => {
  try {
    console.log('开始加载收藏夹列表，isViewingOtherUser:', isViewingOtherUser.value, 'routeUserId:', routeUserId.value)
    let result
    
    if (isViewingOtherUser.value) {
      // 查看其他用户的收藏夹列表
      console.log('调用getOtherUserFavorite方法')
      result = await Request({
        url: Api.getOtherUserFavorite,
        method: 'GET',
        params: {
          userId: routeUserId.value
        }
      })
    } else {
      // 查看自己的收藏夹列表
      console.log('调用getFavorite方法')
      result = await Request({
        url: Api.getFavorite,
        method: 'GET'
      })
    }
    
    console.log('收藏夹列表返回结果:', result)
    
    if (result && result.data) {
      const favoriteList = result.data
      console.log('原始收藏夹数据:', favoriteList)
      
      // 将后端返回的数据转换为前端需要的格式
      const folderMap = new Map()
      favoriteList.forEach(item => {
        const key = item.favorite
        if (!folderMap.has(key)) {
          folderMap.set(key, {
            id: item.favorite,
            name: item.favoriteName,
            count: item.count || 0,
            isPublic: item.isPublic
          })
        }
      })
      
      folders.value = Array.from(folderMap.values())
      console.log('处理后的收藏夹列表:', folders.value)
      
      // 默认选中第一个收藏夹
      if (folders.value.length > 0) {
        activeFolderId.value = folders.value[0].id
        activeFolder.value = folders.value[0]
        
        // 加载第一个收藏夹的视频数量和视频列表
        await loadCollectCount(activeFolderId.value)
        await loadCollectList(activeFolderId.value, currentPage.value, pageSize.value, 1)
      }
    }
  } catch (error) {
    console.error('加载收藏夹列表失败:', error)
    ElMessage.error('加载收藏夹列表失败')
  }
}

// 蓝圈：获取收藏夹视频数量（调用getCollectCount方法或getOtherUserCollectCount方法）
const loadCollectCount = async (folderId) => {
  try {
    console.log('开始加载收藏夹数量:', folderId, 'isViewingOtherUser:', isViewingOtherUser.value)
    let result
    
    if (isViewingOtherUser.value) {
      // 查看其他用户的收藏夹数量
      console.log('调用getOtherUserCollectCount方法')
      result = await Request({
        url: Api.getOtherUserCollectCount,
        method: 'GET',
        params: {
          userId: routeUserId.value,
          favoriteNO: folderId
        }
      })
    } else {
      // 查看自己的收藏夹数量
      console.log('调用getCollectCount方法')
      result = await Request({
        url: Api.getCollectCount,
        method: 'GET',
        params: {
          favoriteNO: folderId
        }
      })
    }
    
    console.log('收藏夹数量返回结果:', result)
    
    if (result && result.data !== undefined) {
      const count = result.data
      console.log('收藏夹数量:', count)
      
      // 更新对应收藏夹的数量
      const folder = folders.value.find(f => f.id === folderId)
      if (folder) {
        folder.count = count
        console.log('更新收藏夹数量:', folder.name, count)
      }
      
      // 更新当前激活收藏夹的数量
      if (activeFolder.value && activeFolder.value.id === folderId) {
        activeFolder.value.count = count
        console.log('更新激活收藏夹数量:', activeFolder.value.name, count)
      }
      
      // 更新总数用于分页
      total.value = count
      console.log('更新分页总数:', total.value)
    }
  } catch (error) {
    console.error('加载收藏数量失败:', error)
    ElMessage.error('加载收藏数量失败')
  }
}

// 黑圈：分页查询收藏的视频列表（调用getCollectList方法或getOtherUserCollect方法）
const loadCollectList = async (folderId, page, size, sortord = 1) => {
  try {
    console.log('开始加载收藏视频列表:', { folderId, page, size, sortord }, 'isViewingOtherUser:', isViewingOtherUser.value)
    let result
    
    if (isViewingOtherUser.value) {
      // 查看其他用户的收藏视频列表
      console.log('调用getOtherUserCollect方法')
      result = await Request({
        url: Api.getOtherUserCollect,
        method: 'GET',
        params: {
          userId: routeUserId.value,
          favoriteNO: folderId,
          pageNo: page,
          pageSize: size,
          sortord: sortord
        }
      })
    } else {
      // 查看自己的收藏视频列表
      console.log('调用getCollectList方法')
      result = await Request({
        url: Api.getCollectList,
        method: 'GET',
        params: {
          favoriteNO: folderId,
          pageNo: page,
          pageSize: size,
          sortord: sortord
        }
      })
    }
    
    console.log('收藏视频列表返回结果:', result)
    
    if (result && result.data) {
      videos.value = result.data
      console.log('视频列表更新成功，数量:', videos.value.length)
    } else {
      videos.value = []
      console.log('未返回视频数据，设置为空数组')
    }
  } catch (error) {
    console.error('加载收藏视频列表失败:', error)
    ElMessage.error('加载收藏视频列表失败')
  }
}

// 新建收藏夹弹窗
const createFolderDialogVisible = ref(false)
const newFolderName = ref('')
const newFolderIsPublic = ref(true)

// 选择收藏夹
const selectFolder = async (folderId) => {
  activeFolderId.value = folderId
  activeFolder.value = folders.value.find(folder => folder.id === folderId)
  currentPage.value = 1
  
  await loadCollectCount(folderId)
  
  let sortord = 1
  if (activeFilter.value === 'mostPlayed') {
    sortord = 2
  } else if (activeFilter.value === 'mostCollected') {
    sortord = 3
  }
  
  await loadCollectList(folderId, currentPage.value, pageSize.value, sortord)
}

// 新建收藏夹
const createNewFolder = () => {
  createFolderDialogVisible.value = true
  newFolderName.value = ''
  newFolderIsPublic.value = true
}

// 保存新建收藏夹
const saveNewFolder = async () => {
  if (!newFolderName.value.trim()) {
    ElMessage.warning('请输入珍藏夹名称')
    return
  }
  
  try {
    const result = await Request({
      url: Api.addCollect,
      method: 'POST',
      params: {
        favoriteName: newFolderName.value.trim(),
        isPublic: newFolderIsPublic.value ? 1 : 0
      }
    })
    
    if (result && result.success) {
      ElMessage.success('收藏夹创建成功')
      createFolderDialogVisible.value = false
      // 重新加载收藏夹列表
      await loadFavoriteList()
    } else {
      ElMessage.error('收藏夹创建失败')
    }
  } catch (error) {
    console.error('创建收藏夹失败:', error)
    ElMessage.error('收藏夹创建失败')
  }
}

// 移动视频到其他收藏夹相关
const moveVideoDialogVisible = ref(false)
const selectedVideoId = ref(null)
const availableFolders = ref([])
const targetFolderId = ref(null)

// 处理视频操作
const handleVideoAction = (command, video) => {
  // 只有查看自己的收藏时才允许编辑操作
  if (!isViewingOtherUser.value) {
    if (command === 'move') {
      // 移动视频到其他收藏夹
      selectedVideoId.value = video.videoId
      // 获取用户的所有收藏夹
      loadAvailableFolders()
      moveVideoDialogVisible.value = true
    } else if (command === 'remove') {
      // 从收藏中移除视频
      removeFromCollection(video.videoId)
    }
  }
}

// 从收藏中移除视频
const removeFromCollection = async (videoId) => {
  try {
    // 这里可以调用后端API来移除收藏
    // 假设API为 /api/collect/removeCollect
    const result = await Request({
      url: '/api/collect/removeCollect',
      method: 'POST',
      params: {
        videoId: videoId
      }
    })
    
    if (result && result.success) {
      const index = videos.value.findIndex(video => video.videoId === videoId)
      if (index > -1) {
        videos.value.splice(index, 1)
        if (activeFolder.value) {
          activeFolder.value.count--
        }
        ElMessage.success('已从收藏中移除')
      }
    } else {
      ElMessage.error('移除视频失败')
    }
  } catch (error) {
    console.error('移除视频失败:', error)
    ElMessage.error('移除视频失败')
  }
}

// 获取用户的所有收藏夹（用于移动视频）
const loadAvailableFolders = async () => {
  try {
    const result = await Request({
      url: Api.getFavorite,
      method: 'GET'
    })
    
    if (result && result.success) {
      // 对收藏夹列表进行去重处理，确保每个收藏夹只显示一次
      const folderMap = new Map()
      result.data.forEach(collect => {
        const folderKey = collect.favorite
        if (!folderMap.has(folderKey)) {
          folderMap.set(folderKey, collect)
        }
      })
      // 将去重后的收藏夹转换为数组
      availableFolders.value = Array.from(folderMap.values())
    }
  } catch (error) {
    console.error('加载收藏夹列表失败:', error)
    ElMessage.error('加载收藏夹列表失败')
  }
}

// 处理移动视频
const handleMoveVideo = async () => {
  if (!targetFolderId.value) {
    ElMessage.warning('请选择目标收藏夹')
    return
  }
  
  try {
    const result = await Request({
      url: Api.moveCollect,
      method: 'POST',
      params: {
        videoId: selectedVideoId.value,
        newFavorite: targetFolderId.value
      }
    })
    
    if (result && result.success) {
      ElMessage.success('视频移动成功')
      moveVideoDialogVisible.value = false
      // 重新加载当前收藏夹的视频列表
      if (activeFolderId.value) {
        await loadCollectCount(activeFolderId.value)
        let sortord = 1
        if (activeFilter.value === 'mostPlayed') {
          sortord = 2
        } else if (activeFilter.value === 'mostCollected') {
          sortord = 3
        }
        await loadCollectList(activeFolderId.value, currentPage.value, pageSize.value, sortord)
      }
    } else {
      ElMessage.error('视频移动失败')
    }
  } catch (error) {
    console.error('移动视频失败:', error)
    ElMessage.error('移动视频失败')
  }
}

// 格式化视频时长
const formatDuration = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 跳转到视频详情页面
const goToVideoDetail = (video) => {
  if (video && video.videoId) {
    router.push(`/video/${video.videoId}`)
  }
}

// 处理收藏夹操作（编辑、删除）
const handleFolderAction = async (command, folder) => {
  if (command === 'edit') {
    // 编辑收藏夹
    editFolderDialogVisible.value = true
    editFolderId.value = folder.id
    editFolderName.value = folder.name
    editFolderIsPublic.value = folder.isPublic
  } else if (command === 'delete') {
    // 删除收藏夹
    ElMessageBox.confirm(
      `确定要删除收藏夹 "${folder.name}" 吗？`,
      '删除收藏夹',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      try {
        // 这里需要调用删除收藏夹的API
        // 假设API为 /api/collect/deleteCollect
        const result = await Request({
          url: '/api/collect/deleteCollect',
          method: 'POST',
          params: {
            favoriteNO: folder.id
          }
        })
        
        if (result && result.success) {
          ElMessage.success('删除收藏夹成功')
          // 重新加载收藏夹列表
          await loadFavoriteList()
          // 如果删除的是当前激活的收藏夹，重置激活状态
          if (activeFolderId.value === folder.id) {
            activeFolderId.value = null
            activeFolder.value = null
            videos.value = []
            total.value = 0
          }
        } else {
          ElMessage.error('删除收藏夹失败')
        }
      } catch (error) {
        console.error('删除收藏夹失败:', error)
        ElMessage.error('删除收藏夹失败')
      }
    }).catch(() => {
      // 取消删除
    })
  }
}

// 编辑收藏夹相关
const editFolderDialogVisible = ref(false)
const editFolderId = ref(null)
const editFolderName = ref('')
const editFolderIsPublic = ref(true)

const handleEditFolder = async () => {
  if (!editFolderName.value.trim()) {
    ElMessage.warning('珍藏夹名称不能为空')
    return
  }
  
  try {
    const result = await Request({
      url: Api.updateCollectStatus,
      method: 'POST',
      params: {
        favorite: editFolderId.value,
        favoriteName: editFolderName.value,
        isPublic: editFolderIsPublic.value ? 1 : 0
      }
    })
    
    if (result && result.success) {
      ElMessage.success('编辑收藏夹成功')
      editFolderDialogVisible.value = false
      // 重新加载收藏夹列表
      await loadFavoriteList()
    } else {
      ElMessage.error('编辑收藏夹失败')
    }
  } catch (error) {
    console.error('编辑收藏夹失败:', error)
    ElMessage.error('编辑收藏夹失败')
  }
}

// 分页处理
const handleSizeChange = async (size) => {
  pageSize.value = size
  currentPage.value = 1
  // 重新加载数据
  if (activeFolderId.value !== null) {
    // 根据activeFilter确定排序方式
    let sortord = 1 // 默认按收藏时间排序
    if (activeFilter.value === 'mostPlayed') {
      sortord = 2 // 按播放量排序
    } else if (activeFilter.value === 'mostCollected') {
      sortord = 3 // 按视频创建时间排序
    }
    await loadCollectList(activeFolderId.value, currentPage.value, pageSize.value, sortord)
  }
}

const handleCurrentChange = async (current) => {
  currentPage.value = current
  // 重新加载数据
  if (activeFolderId.value !== null) {
    // 根据activeFilter确定排序方式
    let sortord = 1 // 默认按收藏时间排序
    if (activeFilter.value === 'mostPlayed') {
      sortord = 2 // 按播放量排序
    } else if (activeFilter.value === 'mostCollected') {
      sortord = 3 // 按视频创建时间排序
    }
    await loadCollectList(activeFolderId.value, currentPage.value, pageSize.value, sortord)
  }
}

// 组件挂载时的初始化
onMounted(async () => {
  console.log('Collect page mounted')
  // 加载用户的收藏夹和收藏的视频
  await loadFavoriteList()
})
</script>

<style scoped>
.collect-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

.main-content {
  margin: 0 auto;
  padding: 20px;
  max-width: 1200px;
}

.collect-container {
  background-color: rgba(255,255,255,0.04);
  border-radius: 5px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  padding: 20px;
  border-bottom: 1px solid var(--glass-border);
  background-color: var(--bg-secondary);
}

.collect-content {
  display: flex;
  min-height: 600px;
}

/* 左侧导航栏 */
.left-nav {
  width: 200px;
  border-right: 1px solid var(--glass-border);
  background-color: var(--bg-secondary);
  padding: 20px 0;
}

.nav-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: var(--text-secondary);
  padding: 0 20px 10px;
}

.nav-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.nav-item:hover {
  background-color: rgba(255,255,255,0.06);
}

.folder-info {
  display: flex;
  align-items: center;
  flex: 1;
  cursor: pointer;
}

.folder-info .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.folder-info .count {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-dim);
}

.folder-actions {
  opacity: 0;
  transition: opacity 0.3s;
}

.nav-item:hover .folder-actions {
  opacity: 1;
}

.action-btn {
  padding: 4px;
  min-width: auto;
}

.nav-item.active {
  background-color: rgba(147, 112, 219, 0.15);
  border-left-color: var(--color-primary-light);
  color: var(--color-primary-light);
}

.nav-item.create-folder {
  color: var(--color-primary-light);
  font-weight: 500;
}

/* 右侧内容 */
.right-content {
  flex: 1;
  padding: 20px;
}

.folder-info {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--glass-border);
}

.folder-info h3 {
  margin: 0 0 10px;
  font-size: 18px;
}

.folder-meta {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 15px;
}

.folder-meta .status {
  margin-right: 15px;
}

.folder-actions {
  display: flex;
  gap: 10px;
}

/* 视频列表 */
.video-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.video-item {
  display: flex;
  flex-direction: column;
  background-color: var(--bg-secondary);
  border-radius: 5px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
}

.video-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.video-cover {
  position: relative;
  padding-top: 66.67%; /* 3:2 比例 */
  overflow: hidden;
}

.video-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.video-item:hover .video-cover img {
  transform: scale(1.05);
}

.video-duration {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background-color: rgba(0, 0, 0, 0.7);
  color: #fff;
  font-size: 12px;
  padding: 2px 5px;
  border-radius: 3px;
}

.video-info {
  padding: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.video-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 5px;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.video-author {
  font-size: 12px;
  color: var(--text-dim);
  margin: 0 0 5px;
}

.video-stats {
  font-size: 12px;
  color: var(--text-dim);
  margin: 0;
  margin-top: auto;
}

.video-actions {
  position: absolute;
  top: 10px;
  right: 10px;
  opacity: 0;
  transition: opacity 0.3s;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 4px;
  padding: 5px;
}

.video-item:hover .video-actions {
  opacity: 1;
}

.video-actions .action-btn {
  color: white;
  padding: 4px;
  min-width: auto;
}

.video-actions .action-btn:hover {
  color: var(--color-primary-light);
}

/* 筛选标签 */
.filter-tabs {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

/* 分页 */
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .collect-content {
    flex-direction: column;
  }
  
  .left-nav {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--glass-border);
  }
  
  .nav-section {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 10px;
  }
  
  .section-title {
    width: 100%;
  }
  
  .nav-item {
    width: calc(50% - 20px);
    margin: 0 10px 10px;
    border-radius: 4px;
    border-left: none;
  }
  
  .video-list {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
  }
}
</style>
