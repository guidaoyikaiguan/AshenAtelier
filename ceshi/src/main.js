import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
// 全局主题
import './styles/theme.css'
// 引入Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入封装的工具
import Request from './utils/request'
import Api from './api'
import Verify from './utils/verify'
import Utils from './utils/utils'
// 引入Element的Message消息提示
import { ElMessage } from 'element-plus'
// 引入登录状态管理
import { useLoginStore } from './stores/loginStore'

const app = createApp(App)
app.provide('Request', Request)
app.provide('Api', Api)
app.provide('Verify', Verify)
app.provide('Utils', Utils)
app.provide('Message', ElMessage)

// 挂载应用
const pinia = createPinia()
app.use(pinia).use(router).use(ElementPlus).mount('#app')

// 应用初始化后验证token有效性
const loginStore = useLoginStore(pinia)
if (loginStore.token && loginStore.userInfo && loginStore.userInfo.userId) {
  // 如果有token和有效的userId，尝试获取用户信息以验证token有效性
  Request({
    url: Api.getUserInfo,
    params: { userId: loginStore.userInfo.userId },
    errorCallback: () => {
      // token无效，清除本地存储
      loginStore.logout()
      ElMessage.warning('登录已过期，请重新登录')
    }
  })
} else if (loginStore.token) {
  // 只有token但没有有效userId，清除token
  loginStore.logout()
}