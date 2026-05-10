<template>
  <div>
    <el-dialog
      v-model="loginStore.showLogin"
      width="1000px"
      :show-close="false"
      @close="closeDialog"
      :custom-class="'login-dialog'"
    >
      <div class="dialog-panel">
        <div class="bg">
          <img :src="Utils.getLocalImage('login_bg.png')" />
        </div>
        <el-form
          class="login-register"
          :model="formData"
          :rules="rules"
          ref="formDataRef"
        >
          <div class="tab-panel">
            <!-- ✅ 修复：tab选中态样式绑定写反的BUG -->
            <div :class="[opType == 1 ? 'active' : '']" @click="showPanel(1)">
              开启旅程
            </div>
            <el-divider direction="vertical" />
            <div :class="[opType == 0 ? 'active' : '']" @click="showPanel(0)">
              加入旅程
            </div>
          </div>
          <!--input输入-->
          <el-form-item prop="email">
            <el-input
              size="large"
              clearable
              placeholder="请输入魔法信使"
              v-model="formData.email"
              maxLength="150"
            >
              <template #prefix>
                <span class="iconfont icon-account"></span>
              </template>
            </el-input>
          </el-form-item>
          <!--登录密码-->
          <el-form-item prop="password" v-if="opType == 1">
            <el-input
              show-password
              size="large"
              placeholder="请输入旅人密语"
              v-model="formData.password"
            >
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>
          <!--注册-->
          <div v-if="opType == 0">
            <el-form-item prop="nickName" v-if="opType == 0">
              <el-input
                size="large"
                clearable
                placeholder="请赐予旅人之名"
                v-model="formData.nickName"
                maxLength="20"
              >
                <template #prefix>
                  <span class="iconfont icon-account"></span>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="registerPassword">
              <el-input
                show-password
                type="password"
                size="large"
                placeholder="请输入旅人密语"
                v-model="formData.registerPassword"
              >
                <template #prefix>
                  <span class="iconfont icon-password"></span>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="reRegisterPassword">
              <el-input
                show-password
                type="password"
                size="large"
                placeholder="再次确认旅人密语"
                v-model="formData.reRegisterPassword"
              >
                <template #prefix>
                  <span class="iconfont icon-password"></span>
                </template>
              </el-input>
            </el-form-item>
          </div>
          <el-form-item prop="checkCode">
            <div class="check-code-panel">
              <el-input
                size="large"
                placeholder="解读魔法符文"
                v-model="formData.checkCode"
                @keyup.enter="doSubmit"
              >
                <template #prefix>
                  <span class="iconfont icon-checkcode"></span>
                </template>
              </el-input>
              <img
                :src="checkCodeInfo.checkCode"
                class="check-code"
                @click="changeCheckCode()"
              />
            </div>
          </el-form-item>
          <el-form-item class="bottom-btn">
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              @click="doSubmit"
            >
              <span v-if="opType == 0">加入旅程</span>
              <span v-if="opType == 1">开启旅程</span>
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  nextTick,
  onMounted,
  onUpdated,
  inject // ✅ 新增注入方法 核心修复
} from "vue";
import { useRouter, useRoute } from "vue-router";
import md5 from "js-md5";

// ✅ 核心修复 全部替换掉原有的 proxy 方式 注入所有全局工具
const Request = inject('Request')
const Api = inject('Api')
const Verify = inject('Verify')
const Utils = inject('Utils')
const Message = inject('Message')

const router = useRouter();
const route = useRoute();

import { useLoginStore } from "@/stores/loginStore.js";
const loginStore = useLoginStore();

//验证码
const checkCodeInfo = ref({});
const changeCheckCode = async () => {
  let result = await Request({
    url: Api.checkCode,
  });
  if (!result) {
    return;
  }
  checkCodeInfo.value = result.data;
};

//登录，注册 弹出配置
const dialogConfig = ref({
  show: true,
});

const checkRePassword = (rule, value, callback) => {
  if (value !== formData.value.registerPassword) {
    callback(new Error(rule.message));
  } else {
    callback();
  }
};

// 0:注册 1:登录
const opType = ref(1);
const formData = ref({});
const formDataRef = ref();
const rules = {
  email: [
    { required: true, message: "请输入邮箱" },
    { validator: Verify.email, message: "请输入正确的邮箱" },
  ],
  password: [{ required: true, message: "请输入密码" }],
  nickName: [{ required: true, message: "请输入昵称" }],
  registerPassword: [
    { required: true, message: "请输入密码" },
    {
      validator: Verify.password,
      message: "密码只能是数字，字母，特殊字符 8-18位",
    },
  ],
  reRegisterPassword: [
    { required: true, message: "请再次输入密码" },
    {
      validator: checkRePassword,
      message: "两次输入的密码不一致",
    },
  ],
  checkCode: [{ required: true, message: "请输入图片验证码" }],
};

//重置表单
const resetForm = () => {
  changeCheckCode();
  nextTick(() => {
    formDataRef.value.resetFields();
    formData.value = {};
  });
};

// 登录、注册、重置密码  提交表单
const doSubmit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);
    params.checkCodeKey = checkCodeInfo.value.checkCodeKey;
    // 登录密码加密已移至后端处理，前端不再加密
    // if (opType.value == 1) {
    //   params.password = md5(params.password);
    // }
    let result = await Request({
      url: opType.value == 0 ? Api.register : Api.login,
      params: params,
      errorCallback: () => {
        changeCheckCode();
      },
    });
    if (!result) {
      return;
    }
    //注册返回
    if (opType.value == 0) {
      Message.success("注册成功,请登录");
      showPanel(1);
    } else if (opType.value == 1) {
      Message.success("登录成功");
      loginStore.setLogin(false);
      loginStore.saveUserInfo(result.data);
      // 登录成功后跳转到Index页面
      router.push('/');
    }
  });
};

const closeDialog = () => {
  dialogConfig.value.show = false;
  loginStore.setLogin(false);
};

const showPanel = (type) => {
  opType.value = type;
  if (loginStore.showLogin) {
    resetForm();
  }
};

onUpdated(() => {
  showPanel(1);
});

onMounted(() => {
  showPanel(1);
});
</script>

<style lang="scss">
.dialog-panel {
  display: flex;
  align-items: center;
  justify-content: space-around;
  .bg {
    width: 450px;
    height: 580px;
    overflow: hidden;
    img {
      width: 100%;
    }
  }
  .login-register {
    width: 350px;
    .tab-panel {
      margin: 10px auto;
      display: flex;
      width: 130px;
      font-size: 18px;
      align-items: center;
      justify-content: space-around;
      cursor: pointer;
      .active {
        color: var(--blue2);
      }
    }
    .no-account {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }
    .login-btn {
      width: 100%;
    }
    .bottom-btn {
      margin-bottom: 0px;
    }
  }
}

.check-code-panel {
  display: flex;
  .check-code {
    margin-left: 5px;
    cursor: pointer;
  }
}

/* 自定义对话框样式 */
.login-dialog {
  .el-dialog__body {
    padding: 0;
  }
  .el-dialog__header {
    display: none;
  }
}
</style>