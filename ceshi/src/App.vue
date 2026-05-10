<template>
  <!-- 使用router-view显示当前路由对应的组件 -->
  <router-view />
  <!-- 登录弹窗组件，通过状态管理控制显示，管理端页面不显示 -->
  <Account v-if="!isAdminPage" />
  <MascotPet v-if="!isAdminPage" />
</template>

<script setup>
// 引入Account组件
import Account from './views/account/Account.vue'
import MascotPet from './components/MascotPet.vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 计算是否是管理端页面
const isAdminPage = computed(() => {
  return route.path.startsWith('/admin/')
})
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  width: 100%;
  height: 100%;
}

body {
  font-family: var(--font-body);
  color: var(--text-secondary);
  background: var(--bg-primary);
  background-attachment: fixed;
}

/* Element Plus 深色主题基础覆盖 */
:root {
  --el-color-primary: var(--color-primary);
  --el-color-primary-light-3: var(--color-primary-light);
  --el-color-primary-light-5: rgba(147, 112, 219, 0.5);
  --el-color-primary-light-7: rgba(147, 112, 219, 0.25);
  --el-color-primary-light-9: rgba(147, 112, 219, 0.1);
  --el-bg-color: var(--bg-primary);
  --el-bg-color-overlay: var(--bg-secondary);
  --el-fill-color-blank: var(--glass-bg);
  --el-border-color: var(--glass-border);
  --el-border-color-light: var(--glass-border);
  --el-text-color-primary: var(--text-primary);
  --el-text-color-regular: var(--text-secondary);
  --el-text-color-secondary: var(--text-muted);
  --el-text-color-placeholder: var(--text-dim);
}

.el-button--primary {
  --el-button-bg-color: var(--color-primary);
  --el-button-border-color: var(--color-primary);
  --el-button-hover-bg-color: var(--color-primary-light);
  --el-button-hover-border-color: var(--color-primary-light);
}

.el-button--default {
  --el-button-bg-color: var(--glass-bg);
  --el-button-border-color: var(--glass-border);
  --el-button-hover-bg-color: var(--glass-bg-hover);
  --el-button-hover-border-color: var(--glass-border-hover);
  --el-button-hover-text-color: var(--color-primary-light);
}

.el-input__wrapper {
  background: var(--glass-bg) !important;
  border-color: var(--glass-border) !important;
  box-shadow: none !important;
  border-radius: var(--radius-md) !important;
}
.el-input__inner {
  color: var(--text-primary) !important;
}

.el-carousel__arrow {
  background: var(--glass-bg) !important;
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: 50%;
}

.el-dialog {
  background: var(--glass-bg-dialog) !important;
  backdrop-filter: var(--glass-blur-heavy);
  -webkit-backdrop-filter: var(--glass-blur-heavy);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-lg);
}

.el-dropdown-menu {
  background: var(--glass-bg-dialog) !important;
  backdrop-filter: var(--glass-blur-heavy);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md) !important;
}

.el-dropdown-menu__item:hover {
  background: var(--glass-bg-hover) !important;
  color: var(--color-primary-light) !important;
}

.el-tabs__item {
  color: var(--text-muted) !important;
}
.el-tabs__item.is-active {
  color: var(--color-primary-light) !important;
}
.el-tabs__active-bar {
  background-color: var(--color-champagne) !important;
}

.el-tag {
  border-radius: var(--radius-xs);
}

.el-pagination .el-pager li.active {
  background-color: var(--color-primary) !important;
  color: #fff !important;
}

.el-message {
  background: var(--glass-bg-dialog) !important;
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md) !important;
}

.el-loading-spinner .circular circle {
  stroke: var(--color-primary);
}

.el-radio__input.is-checked .el-radio__inner {
  background-color: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
}

.el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
}

.el-switch.is-checked .el-switch__core {
  background-color: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
}

.el-popper.is-dark {
  background: var(--glass-bg-dialog) !important;
  border: 1px solid var(--glass-border);
}
</style>