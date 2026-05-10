import { defineStore } from 'pinia';

// 你的代码中用到的登录状态仓库，1:1匹配调用方法
export const useLoginStore = defineStore('login', {
  state: () => ({
    showLogin: !localStorage.getItem('token'), // 如果有token，不显示登录弹窗
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || {}, // 从localStorage中恢复用户信息
    token: localStorage.getItem('token') || '' // 从localStorage中恢复token
  }),
  getters: {
    // 判断是否已登录
    isLoggedIn: (state) => {
      return !!state.token && Object.keys(state.userInfo).length > 0;
    }
  },
  actions: {
    // 设置弹窗显示状态
    setLogin(val) {
      this.showLogin = val;
    },
    // 保存用户信息
    saveUserInfo(info) {
      this.userInfo = info;
      localStorage.setItem('userInfo', JSON.stringify(info)); // 持久化存储用户信息
      
      // 保存token
      if (info.token) {
        this.token = info.token;
        localStorage.setItem('token', info.token); // 持久化存储token
      }
    },
    // 退出登录(可选)
    logout() {
      this.userInfo = {};
      this.token = '';
      localStorage.removeItem('userInfo');
      localStorage.removeItem('token');
    },
    // 设置token
    setToken(token) {
      this.token = token;
      localStorage.setItem('token', token);
    },
    // 获取token
    getToken() {
      return this.token || localStorage.getItem('token');
    }
  }
});