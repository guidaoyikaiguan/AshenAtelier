<template>
  <div class="carousel-manager">
    <h2>轮播图管理</h2>
    
    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <el-select v-model="searchForm.categoryId" placeholder="选择分类">
        <el-option label="全部" value="" />
        <el-option 
          v-for="category in categories" 
          :key="category.categoryId" 
          :label="category.categoryName" 
          :value="category.categoryId"
        />
      </el-select>
      <el-button type="primary" @click="loadCarouselList">查询</el-button>
      <el-button type="success" @click="openAddDialog">添加轮播图</el-button>
    </div>
    
    <!-- 轮播图列表 -->
    <el-table :data="carouselList" style="width: 100%">
      <el-table-column prop="carouselId" label="ID" width="80" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column label="图片" width="200">
        <template #default="scope">
          <el-image 
            :src="scope.row.cover" 
            fit="cover" 
            style="width: 100px; height: 50px"
          />
        </template>
      </el-table-column>
      <el-table-column prop="link" label="跳转链接" min-width="150" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button 
            size="small" 
            :type="scope.row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(scope.row)"
          >
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteCarousel(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        layout="prev, pager, next, jumper, ->, total"
        :total="carouselCount"
        :page-size="pageSize"
        v-model:current-page="currentPage"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="分类">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" required>
            <el-option 
              v-for="category in categories" 
              :key="category.categoryId" 
              :label="category.categoryName" 
              :value="category.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="formData.title" placeholder="请输入标题" required />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="avatar-uploader"
            :action="'https://upload-z2.qiniup.com'"
            :data="{ token: qiniuToken }"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :show-file-list="false"
          >
            <img v-if="formData.cover" :src="formData.cover" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCarousel">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, inject } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 注入全局工具（添加错误处理）
const Request = inject('Request', null)
const Api = inject('Api', null)

// 数据
const carouselList = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogTitle = ref('添加轮播图')
const formData = reactive({
  carouselId: null,
  categoryId: null,
  title: '',
  cover: '',
  sortOrder: 0,
  status: 1
})
const qiniuToken = ref('')
const searchForm = reactive({
  categoryId: null
})
// 轮播图总数
const carouselCount = ref(0)

// 加载分类列表
const loadCategories = async () => {
  if (!Request) {
    console.error('Request工具未注入')
    ElMessage.error('系统初始化失败')
    return
  }
  
  try {
    const result = await Request({
      url: '/api/category/admin/list'
    })
    if (result && result.success && result.data) {
      // 确保分类数据结构正确
      categories.value = result.data.map(category => {
        return {
          categoryId: category.categoryId || category.id || category.CategoryId,
          categoryName: category.categoryName || category.name || category.CategoryName
        }
      })
      console.log('加载的分类数据:', categories.value)
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  }
}

// 加载轮播图总数
const loadCarouselCount = async () => {
  if (!Request) {
    console.error('Request工具未注入')
    return
  }
  
  try {
    let url = '/api/admin/carousel/countCarouselByCategoryId'
    let params = {
      categoryId: searchForm.categoryId
    }
    
    // 如果选择了"全部"，调用countCarousel方法
    if (!searchForm.categoryId) {
      url = '/api/admin/carousel/countCarousel'
      params = {}
    }
    
    const result = await Request({
      url,
      params
    })
    if (result && result.success && result.data) {
      carouselCount.value = result.data
    }
  } catch (error) {
    console.error('加载轮播图总数失败:', error)
  }
}

// 加载轮播图列表
const loadCarouselList = async () => {
  if (!Request) {
    console.error('Request工具未注入')
    ElMessage.error('系统初始化失败')
    return
  }
  
  try {
    let url = '/api/admin/carousel/getCarouselByCategoryId'
    let params = {
      pageNo: currentPage.value,
      categoryId: searchForm.categoryId
    }
    
    // 如果选择了"全部"，调用getCarouselList方法
    if (!searchForm.categoryId) {
      url = '/api/admin/carousel/list'
      params = {
        pageNo: currentPage.value
      }
    }
    
    const result = await Request({
      url,
      params
    })
    if (result && result.success && result.data) {
      carouselList.value = result.data.list || []
    }
    
    // 加载轮播图总数
    await loadCarouselCount()
  } catch (error) {
    console.error('加载轮播图失败:', error)
    ElMessage.error('加载轮播图失败')
  }
}

// 获取七牛云令牌
const getQiniuToken = async () => {
  if (!Request) {
    console.error('Request工具未注入')
    ElMessage.error('系统初始化失败')
    return
  }
  
  try {
    const result = await Request({
      url: '/api/admin/carousel/getQiniuToken'
    })
    if (result && result.success && result.data) {
      qiniuToken.value = result.data
    }
  } catch (error) {
    console.error('获取七牛云令牌失败:', error)
    ElMessage.error('获取上传令牌失败')
  }
}

// 打开添加对话框
const openAddDialog = () => {
  dialogTitle.value = '添加轮播图'
  Object.assign(formData, {
    carouselId: null,
    categoryId: null,
    title: '',
    cover: '',
    link: '',
    sortOrder: 0,
    status: 1
  })
  getQiniuToken()
  dialogVisible.value = true
  console.log('打开添加对话框，初始formData:', formData)
}

// 打开编辑对话框
const openEditDialog = (row) => {
  dialogTitle.value = '编辑轮播图'
  Object.assign(formData, row)
  getQiniuToken()
  dialogVisible.value = true
}

// 保存轮播图
const saveCarousel = async () => {
  if (!Request) {
    console.error('Request工具未注入')
    ElMessage.error('系统初始化失败')
    return
  }
  
  // 检查分类ID是否为空
  if (!formData.categoryId) {
    ElMessage.error('请选择分类')
    return
  }
  
  try {
    const url = formData.carouselId 
      ? '/api/admin/carousel/update' 
      : '/api/admin/carousel/add'
    
    console.log('保存轮播图数据:', formData)
    
    // 确保传递正确的参数名
    const requestData = {
      ...formData,
      categoryId: formData.categoryId // 明确指定categoryId参数
    }
    
    console.log('发送的请求数据:', requestData)
    
    const result = await Request({
      url,
      method: 'POST',
      data: requestData
    })
    
    if (result && result.success) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadCarouselList()
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (error) {
    console.error('保存轮播图失败:', error)
    ElMessage.error('保存失败')
  }
}

// 切换状态
const toggleStatus = async (row) => {
  if (!Request) {
    console.error('Request工具未注入')
    ElMessage.error('系统初始化失败')
    return
  }
  
  try {
    const result = await Request({
      url: '/api/admin/carousel/toggleStatus',
      params: {
        carouselId: row.carouselId,
        status: row.status === 1 ? 0 : 1
      }
    })
    
    if (result && result.success) {
      ElMessage.success('操作成功')
      loadCarouselList()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除轮播图
const deleteCarousel = async (row) => {
  if (!Request) {
    console.error('Request工具未注入')
    ElMessage.error('系统初始化失败')
    return
  }
  
  try {
    const result = await Request({
      url: '/api/admin/carousel/delete',
      params: {
        carouselId: row.carouselId
      }
    })
    
    if (result && result.success) {
      ElMessage.success('删除成功')
      loadCarouselList()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    console.error('删除轮播图失败:', error)
    ElMessage.error('删除失败')
  }
}

// 上传图片
        const handleUploadSuccess = async (response) => {
          if (response && response.key) {
            try {
              // 获取带签名的URL
              const result = await Request({
                url: '/api/admin/carousel/getSignedUrl',
                params: {
                  key: response.key
                }
              })
              
              if (result && result.success && result.data) {
                // 直接使用后端返回的完整签名URL
                formData.cover = result.data;
                console.log('获取到的签名URL:', result.data);
              } else {
                // 如果获取签名失败，使用原始URL作为备份
                formData.cover = `${process.env.VUE_APP_QINIU_DOMAIN || 'http://tet31ocbs.hn-bkt.clouddn.com'}/${response.key}`;
                console.log('使用原始URL:', formData.cover);
              }
            } catch (error) {
              console.error('获取签名URL失败:', error);
              // 失败时使用原始URL
              formData.cover = `${process.env.VUE_APP_QINIU_DOMAIN || 'http://tet31ocbs.hn-bkt.clouddn.com'}/${response.key}`;
              console.log('使用原始URL:', formData.cover);
            }
          }
        }

const handleUploadError = (error) => {
  ElMessage.error('上传失败')
}

const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isJPG) {
    ElMessage.error('只能上传JPG/PNG图片')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
  }
  
  return isJPG && isLt2M
}

// 分页
const handleCurrentChange = (page) => {
  currentPage.value = page
  loadCarouselList()
}

// 初始化
onMounted(() => {
  if (Request) {
    loadCategories()
    loadCarouselList()
  } else {
    console.error('Request工具未注入，轮播图管理功能无法使用')
    ElMessage.error('系统初始化失败，请检查全局配置')
  }
})
</script>

<style scoped>
.carousel-manager {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 50px;
  line-height: 50px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 50px;
  display: block;
}
</style>