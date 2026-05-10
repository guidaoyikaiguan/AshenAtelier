<template>
  <div class="admin-login">
    <div class="login-container">
      <div class="login-header">
        <h2>管理员登录</h2>
        <p>请输入管理员账号和密码</p>
      </div>
      <div class="login-form">
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-position="top">
          <el-form-item label="用户名" prop="adminName">
            <el-input v-model="loginForm.adminName" placeholder="请输入管理员用户名" prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入管理员密码" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { inject } from 'vue'

const router = useRouter()
const Request = inject('Request')
const Api = inject('Api')

const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  adminName: '',
  password: ''
})

const rules = {
  adminName: [{ required: true, message: '请输入管理员用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入管理员密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    // 调用管理员登录API
    const result = await Request({
      url: Api.adminLogin,
      method: 'post',
      data: loginForm
    })
    
    if (result && result.success) {
      // 登录成功，保存管理员信息到本地存储
      localStorage.setItem('adminInfo', JSON.stringify(result.data))
      ElMessage.success('登录成功')
      // 跳转到管理员主页面
      router.push('/admin/dashboard')
    } else {
      ElMessage.error('登录失败，请检查账号密码')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  background-color: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  background-color: rgba(255,255,255,0.04);
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  padding: 30px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #303133;
  margin-bottom: 10px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>