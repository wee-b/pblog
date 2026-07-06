<template>
  <!-- 1. 用户头像（含下拉菜单）- 保持不变 -->
  <el-popover
      placement="bottom-end"
      trigger="hover"
      :width="220"
      popper-class="user-popover"
      :show-arrow="false"
      transition="el-zoom-in-top"
      @show="handlePopoverShow"
  >
    <template #reference>
      <div class="avatar-wrapper" @click="handleAvatarClick">
        <el-avatar :size="props.size" :src="avatarUrl" fit="cover" />
      </div>
    </template>

    <div class="user-menu-content">
      <div class="user-card">
        <p class="nickname">{{ isLogin ? (userInfo?.nickname || '默认用户') : '未登录' }}</p>
        <p class="username" v-if="isLogin">账号:{{ userInfo?.username }}</p>
      </div>
      <div class="menu-divider"></div>

      <template v-if="isLogin">
        <div class="menu-item" @click="navigateTo('/createCentre')">
          <el-icon><User /></el-icon> 个人中心
        </div>
        <div class="menu-divider"></div>
        <div class="menu-item logout" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon> 退出登录
        </div>
      </template>

      <template v-else>
        <div class="menu-item login-btn" @click="openLogin">
          <el-icon><User /></el-icon> 立即登录
        </div>
      </template>
    </div>
  </el-popover>

  <!-- 2. 登录/注册弹窗 -->
  <transition name="dialog-fade">
    <div class="login-dialog-mask" v-if="isVisible" @mousedown="handleMaskMousedown"
         @mouseup="handleMaskMouseup">
      <div class="login-dialog">
        <div class="login-dialog-header">
          <div
              class="login-tab"
              :class="{ active: loginType === 'account' }"
              @click="switchLoginType('account')"
          >
            账号登录
          </div>
          <div
              class="login-tab"
              :class="{ active: loginType === 'register' }"
              @click="switchLoginType('register')"
          >
            注册
          </div>
          <button class="close-btn" @click="hide">×</button>
        </div>

        <form class="login-form" @submit.prevent="handleSubmit">
          <!-- === 账号登录模块 === -->
          <template v-if="loginType === 'account'">
            <div class="form-item">
              <label>账号</label>
              <input
                  v-model.trim="passwordForm.username"
                  type="text"
                  placeholder="请输入账号"
                  required
                  :disabled="loading"
              />
            </div>

            <div class="form-item">
              <label>密码</label>
              <input
                  v-model.trim="passwordForm.password"
                  type="password"
                  placeholder="请输入密码"
                  required
                  :disabled="loading"
              />
            </div>
          </template>

          <!-- === 注册模块 (修改部分) === -->
          <template v-if="loginType === 'register'">

            <!-- 新增：昵称 -->
            <div class="form-item">
              <label>昵称</label>
              <input
                  v-model.trim="registerForm.nickname"
                  type="text"
                  placeholder="请输入昵称"
                  required
                  :disabled="loading"
              />
            </div>

            <!-- 新增：密码 -->
            <div class="form-item">
              <label>设置密码</label>
              <input
                  v-model.trim="registerForm.password"
                  type="password"
                  placeholder="请输入密码"
                  required
                  :disabled="loading"
              />
            </div>

            <!-- 新增：确认密码 -->
            <div class="form-item">
              <label>确认密码</label>
              <input
                  v-model.trim="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  required
                  :disabled="loading"
              />
            </div>
          </template>

          <!-- === 公共模块：图形验证码 (保持不变) === -->
          <div class="form-item">
            <label>图形验证码</label>
            <div class="verify-code-container">
              <input
                  v-model.trim="captchaCodeValue"
                  type="text"
                  placeholder="请输入图形验证码"
                  required
                  :disabled="loading"
              />
              <el-image
                  :src="codeBlobUrl"
                  class="captcha-img"
                  alt="图形验证码"
                  @click="handleRefreshCaptcha"
                  :preview="false"
                  fit="cover"
              >
                <template #error>
                  <div class="captcha-placeholder" @click="handleRefreshCaptcha">
                    加载失败
                  </div>
                </template>
                <template #loading>
                  <div class="captcha-placeholder">...</div>
                </template>
              </el-image>
            </div>
          </div>

          <!-- === 按钮组 === -->
          <div class="btn-group" v-if="loginType === 'account'">
            <button
                type="button"
                class="register-btn"
                :disabled="loading"
                @click="switchLoginType('register')"
            >
              去注册
            </button>
            <button
                type="submit"
                class="login-btn"
                :disabled="loading || !getCurrentForm().captchaUuid"
            >
              登录
            </button>
          </div>

          <div class="btn-group" v-if="loginType === 'register'">
            <button
                type="submit"
                class="login-btn solo-btn"
                :disabled="loading  || !registerForm.captchaUuid"
            >
              立即注册
            </button>
          </div>
        </form>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, onUnmounted, onDeactivated, computed, getCurrentInstance, onMounted } from 'vue';
import AdminApi from '@/apis/admin.js';
import { refreshCaptcha } from '@/apis/code.js';
import {
  registerLoginDialog,
  handleLoginSuccess,
  handleLoginFail,
  saveToken,
  saveUserInfoJson
} from '@/utils/loginManager';
import { ElMessage } from 'element-plus';
import { SwitchButton, User } from "@element-plus/icons-vue";
import { useRouter, useRoute } from "vue-router";

const props = defineProps({
  size: { type: [Number, String], default: 36 },
  defaultAvatar: { type: String, default: () => new URL('@/assets/imgs/default-avatar.png', import.meta.url).href },
  clickToLogin: { type: Boolean, default: false },
});

// 路由与状态
const router = useRouter();
const route = useRoute();
const { proxy } = getCurrentInstance();
const $loginManager = proxy?.$loginManager;
const isLogin = ref(false);
const userInfo = ref({});

// 头像计算
const avatarUrl = computed(() => {
  return (isLogin.value && userInfo.value?.avatarUrl) ? userInfo.value.avatarUrl : props.defaultAvatar;
});

// 基础方法
const navigateTo = (path) => router.push(path);
const openLogin = () => $loginManager?.showLogin();
const handleLogout = () => $loginManager?.logout();

// 初始化登录状态
const initLoginState = () => {
  if (!$loginManager) return;
  isLogin.value = $loginManager.checkLogin();
  userInfo.value = $loginManager.getUserInfo() || {};

  const onLoginSuccess = (data) => {
    isLogin.value = true;
    userInfo.value = typeof data === 'string' ? JSON.parse(data) : data;
    ElMessage.success('欢迎回来');
  };
  const onLogoutSuccess = () => {
    isLogin.value = false;
    userInfo.value = {};
    ElMessage.info('已退出登录');
    if (route.meta?.requiresAuth) router.push('/');
  };

  $loginManager.on?.('loginSuccess', onLoginSuccess);
  $loginManager.on?.('logoutSuccess', onLogoutSuccess);

  return () => {
    $loginManager.off?.('loginSuccess', onLoginSuccess);
    $loginManager.off?.('logoutSuccess', onLogoutSuccess);
  };
};

const handleAvatarClick = () => {
  if (props.clickToLogin && !isLogin.value) openLogin();
};

const handlePopoverShow = () => {
  if ($loginManager) {
    isLogin.value = $loginManager.checkLogin();
    userInfo.value = $loginManager.getUserInfo() || {};
  }
};

let cleanupLoginListeners = null;
onMounted(() => {
  cleanupLoginListeners = initLoginState();
  if (isVisible.value && !loading.value) handleRefreshCaptcha();
});

onUnmounted(() => {
  if (cleanupLoginListeners) cleanupLoginListeners();
  if (codeBlobUrl.value?.startsWith('blob:')) URL.revokeObjectURL(codeBlobUrl.value);
});

// === 弹窗表单状态 ===
const isVisible = ref(false);
const loading = ref(false);
const loginType = ref('account'); // 'account' | 'register'
const passwordForm = ref({ username: '', password: '', captchaUuid: '', captchaCode: '' });

// 🔧 修改：注册表单结构
const registerForm = ref({
  nickname: '',       // 新增
  password: '',       // 新增
  confirmPassword: '',// 新增
  captchaUuid: '',
  captchaCode: ''
});

const codeBlobUrl = ref('');


// 图形验证码绑定
const captchaCodeValue = computed({
  get() {
    return loginType.value === 'account' ? passwordForm.value.captchaCode : registerForm.value.captchaCode;
  },
  set(val) {
    loginType.value === 'account'
        ? passwordForm.value.captchaCode = val
        : registerForm.value.captchaCode = val;
  }
});

const getCurrentForm = () => {
  return loginType.value === 'account' ? passwordForm.value : registerForm.value;
};

// 遮罩层点击逻辑
const isMouseDownOnMask = ref(false);
const handleMaskMousedown = (e) => { isMouseDownOnMask.value = e.target === e.currentTarget; };
const handleMaskMouseup = (e) => {
  if (isMouseDownOnMask.value && e.target === e.currentTarget) hide();
  isMouseDownOnMask.value = false;
};

const hide = () => {
  isVisible.value = false;
  // 清空表单
  passwordForm.value.password = '';
  passwordForm.value.captchaCode = '';
  registerForm.value.password = '';
  registerForm.value.confirmPassword = '';
  registerForm.value.captchaCode = '';
};

registerLoginDialog({ isVisible, hide });
defineExpose({ isVisible, openLogin });

// 切换登录/注册模式
const switchLoginType = (type) => {
  loginType.value = type;
  getCurrentForm().captchaCode = '';
  registerForm.value.emailCode = '';
  !loading.value && handleRefreshCaptcha();
};

watch(isVisible, async (newVisible) => {
  if (newVisible && !loading.value) await handleRefreshCaptcha();
});



// 刷新图形验证码
const handleRefreshCaptcha = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    if (codeBlobUrl.value?.startsWith('blob:')) URL.revokeObjectURL(codeBlobUrl.value);
    const { blobUrl, captchaUuid } = await refreshCaptcha();
    codeBlobUrl.value = blobUrl;
    getCurrentForm().captchaUuid = captchaUuid;
  } catch (error) {
    console.error('图形验证码刷新失败:', error);
    getCurrentForm().captchaUuid = '';
  } finally {
    loading.value = false;
  }
};




// === 核心提交逻辑 ===
const handleSubmit = async () => {
  const currentForm = getCurrentForm();
  let valid = true;

  // 1. 账号登录校验
  if (loginType.value === 'account') {
    if (!currentForm.username) { ElMessage.warning('请输入账号'); valid = false; }
    else if (!currentForm.password) { ElMessage.warning('请输入密码'); valid = false; }
  }
  // 2. 注册校验
  else {
    if (!registerForm.value.nickname) { ElMessage.warning('请输入昵称'); valid = false; }
    else if (!registerForm.value.password) { ElMessage.warning('请输入密码'); valid = false; }
    else if (registerForm.value.password !== registerForm.value.confirmPassword) {
      ElMessage.warning('两次输入密码不一致'); valid = false;
    }
  }

  // 公共校验
  if (!currentForm.captchaCode) { ElMessage.warning('请输入图形验证码'); valid = false; }
  if (!currentForm.captchaUuid) { ElMessage.warning('请刷新图形验证码'); valid = false; }

  if (!valid) return;

  try {
    loading.value = true;
    let res;

    if (loginType.value === 'account') {
      // 登录接口
      res = await AdminApi.passwordLogin({
        username: currentForm.username,
        password: currentForm.password,
        captchaCode: currentForm.captchaCode,
        captchaUuid: currentForm.captchaUuid
      });
    } else {
      // 注册接口 (假设后端有 register 方法)
      // 注意：只传一个 password
      res = await AdminApi.register({
        nickname: registerForm.value.nickname,
        password: registerForm.value.password, // 直传一个密码
        captchaCode: currentForm.captchaCode,
        captchaUuid: currentForm.captchaUuid
      });
    }

    const data = res.data;
    if (data.code === 200) {

      // 登录成功处理
      if (data.data.token && data.data.userInfoJson) {
        saveToken(data.data.token);
        saveUserInfoJson(data.data.userInfoJson);
        handleLoginSuccess(data.data.userInfoJson);
        hide();
      }
      if (loginType.value === 'account') {
        ElMessage.success('登录成功');
      } else {
        // 注册成功处理
        ElMessage.success('注册成功');
      }
    } else {
      handleLoginFail(data.message || (loginType.value === 'account' ? '登录失败' : '注册失败'));
      ElMessage.error(data.message);
      await handleRefreshCaptcha();
    }
  } catch (err) {
    handleLoginFail(err.message || '操作异常');
    ElMessage.error(err.message || '请求失败');
    await handleRefreshCaptcha();
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* 保持原有 CSS 样式，无需大幅变动，
   可以根据需要微调 .login-dialog 的最大高度或增加滚动条，
   因为注册表单变长了。 */

.avatar-wrapper {
  cursor: pointer;
  transition: transform 0.2s;
  display: flex;
  align-items: center;
}
.avatar-wrapper:hover {
  transform: scale(1.1);
}
.user-menu-content {
  padding: 10px 0;
}
.user-card {
  padding: 10px 20px;
  background-color: #f9fafe;
}
.user-card .nickname {
  font-weight: bold;
  font-size: 15px;
  margin: 0;
  color: #333;
}
.user-card .username {
  font-size: 12px;
  color: #999;
  margin: 4px 0 0 0;
}
.menu-divider {
  height: 1px;
  background-color: #eee;
  margin: 5px 0;
}
.menu-item {
  padding: 10px 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #555;
  transition: background 0.2s;
}
.menu-item:hover {
  background-color: #f0f9eb;
  color: #67c23a;
}
.menu-item .el-icon {
  margin-right: 8px;
  font-size: 16px;
}
.menu-item.logout:hover {
  background-color: #fef0f0;
  color: #f56c6c;
}

.login-dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.login-dialog {
  width: 380px; /* 稍微加宽一点适应更多字段 */
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  /* 防止小屏幕下高度溢出 */
  max-height: 90vh;
  overflow-y: auto;
}
.login-dialog-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  position: relative;
  height: 40px;
}
.login-tab {
  width: 50%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
}
.login-tab.active {
  color: #409eff;
  border-bottom: 2px solid #409eff;
}
.close-btn {
  position: absolute;
  right: 0;
  top: 0;
  background: transparent;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #999;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.form-item {
  margin-bottom: 16px;
}
.form-item label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #333;
}
.form-item input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box; /* 确保 padding 不撑大宽度 */
}
.form-item input:focus {
  outline: none;
  border-color: #409eff;
}
.verify-code-container {
  display: flex;
  gap: 10px;
}
.verify-code-container input {
  flex: 1;
}
.captcha-img {
  width: 110px;
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #e5e7eb;
}
.captcha-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f9fafb;
  color: #666;
  font-size: 12px;
  cursor: pointer;
}
.send-code-btn {
  width: 110px;
  height: 40px;
  border: none;
  border-radius: 4px;
  background: #409eff;
  color: #fff;
  font-size: 12px; /* 字体稍微调小 */
  cursor: pointer;
}
.send-code-btn:disabled {
  background: #e6f4ff !important;
  color: #8cc5ff !important;
  cursor: not-allowed;
  opacity: 0.9;
}
.btn-group {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}
.btn-group button {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}
.login-btn {
  background: #409eff;
  color: #fff;
}
.login-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}
.register-btn {
  background: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}
.register-btn:disabled {
  background: #fafafa;
  color: #999;
  cursor: not-allowed;
}
.register-btn:hover:not(:disabled) {
  background: #eee;
}
.login-btn.solo-btn {
  width: 100%;
  flex: none;
}
.login-form input:disabled {
  background: #f9f9f9;
  color: #999;
  border-color: #e0e0e0;
}
.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.2s ease;
}
.login-dialog {
  transition: transform 0.2s ease;
}
.dialog-fade-enter-from .login-dialog,
.dialog-fade-leave-to .login-dialog {
  transform: translateY(-10px);
}
</style>

<style>
.user-popover {
  padding: 0 !important;
  border-radius: 8px !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1) !important;
}
</style>