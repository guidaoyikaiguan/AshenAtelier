import { createRouter, createWebHistory } from 'vue-router'
// 引入你的Account组件
import Account from '@/views/account/Account.vue'
import Index from '@/views/index/Index.vue'

const routes = [
  {
    path: '/',
    name: 'Index',
    component: Index
  },
  {
    path: '/login',
    name: 'Account',
    component: Account
  },
  // 分类页面
  {
    path: '/category/:categoryId',
    name: 'Category',
    component: () => import('@/views/category/Category.vue')
  },
  // 视频详情页
  {
    path: '/video/:videoId',
    name: 'VideoDetail',
    component: () => import('@/views/video/VideoDetail.vue')
  },
  // 搜索结果页
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/search/Search.vue')
  },
  // 用户相关页面
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/Profile.vue')
  },
  {
    path: '/profile/:userId',
    name: 'UserProfile',
    component: () => import('@/views/profile/Profile.vue')
  },
  {
     path: '/history' ,
     name: 'History' ,
     component : () => import ( '@/views/profile/History.vue' )
  },
  {
    path: '/collect',
    name: 'Collect',
    component: () => import('@/views/profile/Collect.vue')
  },
  {
    path: '/compilations',
    name: 'Compilations',
    component: () => import('@/views/profile/Compilations.vue')
  },
  {
    path: '/profile/setting',
    name: 'Setting',
    component: () => import('@/views/profile/Setting.vue')
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/chat/Chat.vue')
  },
  // 关注和粉丝页面
  {
    path: '/following',
    name: 'Following',
    component: () => import('@/views/profile/Following.vue')
  },
  {
    path: '/followers',
    name: 'Followers',
    component: () => import('@/views/profile/Followers.vue')
  },
  // 功能页面
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('@/views/upload/Upload.vue')
  },
  {
    path: '/creator',
    name: 'Creator',
    component: () => import('@/views/upload/Creator.vue')
  },
  {
    path: '/vip',
    name: 'Vip',
    component: () => import('@/views/vip/Vip.vue')
  },
  { path: '/notification', name: 'Notification', component: () => import('@/views/notification/Notification.vue') },
  // 动态相关页面
  { path: '/moment', name: 'Moment', component: () => import('@/views/moment/Moment.vue') },
  {
    path: '/moment/my',
    name: 'MyMoment',
    component: () => import('@/views/moment/MyMoment.vue')
  },
  {
    path: '/moment/publish',
    name: 'PublishMoment',
    component: () => import('@/views/moment/PublishMoment.vue')
  },
  {
    path: '/moment/detail/:momentId',
    name: 'MomentDetail',
    component: () => import('@/views/moment/MomentDetail.vue')
  },
  // 管理员相关页面
  { path: '/admin/login', name: 'AdminLogin', component: () => import('@/views/admin/AdminLogin.vue') },
  { 
    path: '/admin/dashboard', 
    name: 'AdminDashboard', 
    component: () => import('@/views/admin/AdminDashboard.vue'),
    children: [
      { 
        path: 'carousel', 
        name: 'CarouselManager', 
        component: () => import('@/views/admin/CarouselManager.vue'),
        meta: { title: '轮播图管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：管理员登录状态检查
router.beforeEach((to, from, next) => {
  // 检查是否访问管理员相关页面
  if (to.path.startsWith('/admin/')) {
    // 检查管理员登录状态
    const adminInfo = localStorage.getItem('adminInfo')
    if (adminInfo) {
      // 已登录，放行
      next()
    } else {
      // 未登录，重定向到管理员登录页面
      if (to.path !== '/admin/login') {
        next('/admin/login')
      } else {
        next()
      }
    }
  } else {
    // 非管理员页面，正常放行
    next()
  }
})

export default router