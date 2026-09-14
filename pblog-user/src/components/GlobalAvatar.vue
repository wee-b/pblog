<template>
  <!-- 1. 用户头像（含下拉菜单）- 支持自定义尺寸 -->
  <el-popover
      v-model:visible="popoverVisible"
      placement="bottom-end"
      trigger="hover"
      :width="240"
      popper-class="user-popover"
      :show-arrow="false"
      transition="el-zoom-in-top"
      @show="handlePopoverShow"
  >
  <template #reference>
    <div class="avatar-wrapper" @click="handleAvatarClick"> <!-- 🔧 新增：点击头像事件（支持直接登录） -->
      <el-avatar :size="props.size" :src="avatarUrl" fit="cover" /> <!-- 🔧 修改：尺寸从props获取，支持自定义 -->
    </div>
  </template>

  <!-- 下拉菜单内容（原有逻辑不变） -->
  <div class="user-menu-content">
    <div class="user-card">
      <span class="user-card-mark" aria-hidden="true">
        <span></span>
      </span>
      <div class="user-card-copy">
        <p class="nickname">{{ isLogin ? (userInfo?.nickname || '默认用户') : '访客模式' }}</p>
        <p class="username" v-if="isLogin">账号：{{ userInfo?.username }}</p>
        <p class="username" v-else>登录后管理你的内容</p>
      </div>
    </div>
    <div class="menu-divider"></div>

    <template v-if="isLogin">
      <div class="menu-item" @click="navigateTo('/createCentre')">
        <el-icon><User /></el-icon> 个人中心
      </div>
<!--      <div class="menu-item" @click="navigateTo('/favorites')">-->
<!--        <el-icon><Star /></el-icon> 我的收藏-->
<!--      </div>-->
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

  <!-- 2. 登录弹窗（原有逻辑不变） -->
  <transition name="dialog-fade">
    <div class="login-dialog-mask" v-if="isVisible" @mousedown="handleMaskMousedown"
         @mouseup="handleMaskMouseup">
      <div class="login-dialog">
        <div class="dialog-kicker">
          <span aria-hidden="true"></span>
          PBLOG ACCESS
        </div>
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
              :class="{ active: loginType === 'email' }"
              @click="switchLoginType('email')"
          >
            邮箱登录
          </div>
          <button class="close-btn" @click="hide">×</button>
        </div>

        <form class="login-form" @submit.prevent="handleSubmit">
          <div class="form-item" v-if="loginType === 'account'">
            <label>账号或邮箱</label>
            <input
                v-model.trim="passwordForm.username"
                type="text"
                placeholder="请输入账号或邮箱"
                required
                :disabled="loading"
            />
          </div>

          <div class="form-item" v-if="loginType === 'account'">
            <label>密码</label>
            <input
                v-model.trim="passwordForm.password"
                type="password"
                placeholder="请输入密码"
                required
                :disabled="loading"
            />
          </div>

          <div class="form-item" v-if="loginType === 'email'">
            <label>邮箱</label>
            <input
                v-model.trim="emailForm.email"
                type="text"
                placeholder="请输入邮箱"
                required
                :disabled="loading"
            />
          </div>

          <!-- 邮箱验证码模块 -->
          <div class="form-item" v-if="loginType === 'email' && emailVerificationEnabled">
            <label>邮箱验证码</label>
            <div class="verify-code-container">
              <input
                  v-model.trim="emailForm.emailCode"
                  type="text"
                  placeholder="请输入邮箱验证码"
                  required
                  :disabled="loading "
              />
              <button
                  type="button"
                  class="send-code-btn"
                  :disabled="loading || isCountingDown || !isEmailValid"
                  @click="handleSendEmailCode"
              >
                {{ isCountingDown ? `${countDownSeconds}秒后重发` : '发送验证码' }}
              </button>
            </div>
          </div>

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
                    加载失败，点击重试
                  </div>
                </template>
                <template #loading>
                  <div class="captcha-placeholder">加载中...</div>
                </template>
              </el-image>
            </div>
          </div>

          <div class="btn-group" v-if="loginType === 'account'">
            <button
                type="button"
                class="register-btn"
                :disabled="loading"
                @click="switchLoginType('email')"
            >
              注册
            </button>
            <button
                type="submit"
                class="login-btn"
                :disabled="loading || !getCurrentForm().captchaUuid"
            >
              登录
            </button>
          </div>

          <div class="btn-group" v-if="loginType === 'email'">
            <button
                type="submit"
                class="login-btn solo-btn"
                :disabled="loading || (emailVerificationEnabled && !emailForm.emailCode) || !emailForm.captchaUuid"
            >
              登录/注册
            </button>
          </div>
        </form>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, onUnmounted, onDeactivated, computed, getCurrentInstance, onMounted } from 'vue'; // 🔧 修改：新增onMounted
import UserApi from '@/apis/user/user.js';
import { getEmailVerificationEnabled, refreshCaptcha, sendEmailCode } from '@/apis/code.js';
import {
  registerLoginDialog,
  handleLoginSuccess,
  handleLoginFail,
  saveToken,
  saveUserInfoJson
} from '@/utils/loginManager';
import { ElMessage } from 'element-plus';
import { Star, SwitchButton, User } from "@element-plus/icons-vue";
import { useRouter, useRoute } from "vue-router"; // 🔧 新增：导入useRoute

// 🔧 新增：组件Props（支持自定义配置，实现全局组件灵活性）
const props = defineProps({
  size: {
    type: [Number, String],
    default: 36, // 默认尺寸，外部可覆盖
  },
  defaultAvatar: {
    type: String,
    default: () => new URL('@/assets/imgs/default-avatar.png', import.meta.url).href, // 默认头像，支持自定义
  },
  clickToLogin: {
    type: Boolean,
    default: false, // 未登录时点击头像是否直接打开登录弹窗（默认不打开）
  },
});

// 登录相关（原有逻辑优化）
const router = useRouter();
const route = useRoute(); // 🔧 新增：路由实例（用于退出登录时判断是否需要跳转）
const popoverVisible = ref(false);
const { proxy } = getCurrentInstance();
const $loginManager = proxy?.$loginManager;
const isLogin = ref(false);
const userInfo = ref({});

// 🔧 修改：头像地址计算（支持自定义默认头像）
const avatarUrl = computed(() => {
  return (isLogin.value && userInfo.value?.avatarUrl) ? userInfo.value.avatarUrl : props.defaultAvatar;
});

const navigateTo = (path) => {
  popoverVisible.value = false;
  router.push(path);
};

const openLogin = () => {
  popoverVisible.value = false;
  $loginManager?.showLogin();
};

const handleLogout = () => {
  popoverVisible.value = false;
  $loginManager?.logout();
};

// --- 登录管理器集成（原有逻辑优化）---
const initLoginState = () => {
  if (!$loginManager) return;

  // 初始化读取
  isLogin.value = $loginManager.checkLogin();
  userInfo.value = $loginManager.getUserInfo() || {};

  // 事件监听回调
  const onLoginSuccess = (data) => {
    isLogin.value = true;
    userInfo.value = typeof data === 'string' ? JSON.parse(data) : data;
    ElMessage.success('欢迎回来');
  };

  const onLogoutSuccess = () => {
    isLogin.value = false;
    userInfo.value = {};
    ElMessage.info('已退出登录');
    // 如果在需要登录的页面，踢回首页
    if (route.meta?.requiresAuth) router.push('/');
  };

  // 绑定事件
  $loginManager.on?.('loginSuccess', onLoginSuccess);
  $loginManager.on?.('logoutSuccess', onLogoutSuccess);

  // 返回清理函数
  return () => {
    $loginManager.off?.('loginSuccess', onLoginSuccess);
    $loginManager.off?.('logoutSuccess', onLogoutSuccess);
  };
};

// 🔧 新增：点击头像事件（未登录时可直接打开登录弹窗）
const handleAvatarClick = () => {
  if (props.clickToLogin && !isLogin.value) {
    openLogin();
  }
};

// 🔧 新增：下拉框显示时刷新登录状态（避免状态过期）
const handlePopoverShow = () => {
  if ($loginManager) {
    isLogin.value = $loginManager.checkLogin();
    userInfo.value = $loginManager.getUserInfo() || {};
  }
};

// 🔧 新增：生命周期相关（初始化登录状态+清理）
let cleanupLoginListeners = null;
onMounted(() => {
  cleanupLoginListeners = initLoginState(); // 初始化登录状态
  // 初始化图形验证码（避免首次打开弹窗时无验证码）
  if (isVisible.value && !loading.value) {
    handleRefreshCaptcha();
  }
});

onUnmounted(() => {
  // 清理登录状态监听
  if (cleanupLoginListeners) cleanupLoginListeners();
  // 清理验证码blob URL，避免内存泄漏
  if (codeBlobUrl.value?.startsWith('blob:')) {
    URL.revokeObjectURL(codeBlobUrl.value);
  }
  clearCountDownTimer();
});

// 基础状态（简化响应式，避免复杂依赖）
const isVisible = ref(false);
const loading = ref(false);
const loginType = ref('account');
const emailVerificationEnabled = ref(true);
const passwordForm = ref({ username: '', password: '', captchaUuid: '', captchaCode: '' });
const emailForm = ref({ email: '', emailCode: '', captchaUuid: '', captchaCode: '' });
const codeBlobUrl = ref('');

// 倒计时核心状态（重点：无复杂封装，直接控制）
const isCountingDown = ref(false);
const countDownSeconds = ref(120);
let countDownTimer = null; // 普通变量存定时器，避免响应式干扰

// 计算属性：简化邮箱校验逻辑
const isEmailValid = computed(() => {
  const valid = emailForm.value.email && emailReg.test(emailForm.value.email);
  console.log('邮箱校验状态:', emailForm.value.email, '→', valid);
  return valid;
});

// 图形验证码计算属性
const captchaCodeValue = computed({
  get() {
    return loginType.value === 'account' ? passwordForm.value.captchaCode : emailForm.value.captchaCode;
  },
  set(val) {
    loginType.value === 'account'
        ? passwordForm.value.captchaCode = val
        : emailForm.value.captchaCode = val;
  }
});

// 邮箱正则
const emailReg = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;

const getCurrentForm = () => {
  return loginType.value === 'account' ? passwordForm.value : emailForm.value;
};

// 新增：记录鼠标按下时是否在遮罩层上
const isMouseDownOnMask = ref(false);

// 鼠标按下时判断：如果点击的是遮罩层（不是弹窗内容），标记为true
const handleMaskMousedown = (e) => {
  // e.target 是遮罩层本身（不是子元素login-dialog）时，才标记
  isMouseDownOnMask.value = e.target === e.currentTarget;
};

// 鼠标松开时判断：只有按下时在遮罩层，且松开时也在遮罩层，才关闭弹窗
const handleMaskMouseup = (e) => {
  if (isMouseDownOnMask.value && e.target === e.currentTarget) {
    hide();
  }
  // 重置标记（避免后续点击受影响）
  isMouseDownOnMask.value = false;
};

const hide = () => {
  isVisible.value = false;
  passwordForm.value.captchaCode = '';
  emailForm.value.captchaCode = '';
  emailForm.value.emailCode = '';
  clearCountDownTimer(); // 关闭时强制清除
};

// 注册全局控制
registerLoginDialog({ isVisible, hide });
defineExpose({ isVisible, openLogin }); // 🔧 修改：暴露openLogin方法，支持外部主动唤起登录弹窗

// 切换登录类型
const switchLoginType = (type) => {
  loginType.value = type;
  getCurrentForm().captchaCode = '';
  emailForm.value.emailCode = '';
  clearCountDownTimer(); // 切换时清除倒计时
  !loading.value && handleRefreshCaptcha();
};

// 监听弹窗显示刷新验证码
watch(isVisible, async (newVisible) => {
  console.log('弹窗显示状态:', newVisible);
  if (newVisible && !loading.value) {
    try {
      emailVerificationEnabled.value = await getEmailVerificationEnabled();
      if (!emailVerificationEnabled.value) {
        emailForm.value.emailCode = '';
        clearCountDownTimer();
      }
    } catch (error) {
      console.error('读取邮箱验证码配置失败:', error);
      // 配置读取失败时保持需要验证码，避免意外绕过校验。
      emailVerificationEnabled.value = true;
    }
    await handleRefreshCaptcha();
  }
});

// 组件生命周期清理
onDeactivated(() => clearCountDownTimer());

// 刷新图形验证码
const handleRefreshCaptcha = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    if (codeBlobUrl.value?.startsWith('blob:')) URL.revokeObjectURL(codeBlobUrl.value);
    const { blobUrl, captchaUuid } = await refreshCaptcha();
    codeBlobUrl.value = blobUrl;
    getCurrentForm().captchaUuid = captchaUuid;
    console.log('图形验证码刷新成功,UUID:', captchaUuid);
  } catch (error) {
    console.error('图形验证码刷新失败:', error);
    ElMessage.error('验证码加载失败，请重试');
    getCurrentForm().captchaUuid = '';
    codeBlobUrl.value = '';
  } finally {
    loading.value = false;
  }
};

// 发送验证码（核心优化）
const handleSendEmailCode = async () => {
  if (!emailVerificationEnabled.value) {
    ElMessage.info('邮箱验证码功能已关闭');
    return;
  }

  // 双重防重复点击
  if (loading.value || isCountingDown.value) {
    console.log('阻止重复发送:', { loading: loading.value, counting: isCountingDown.value });
    return;
  }

  try {
    loading.value = true;
    console.log('发送验证码请求:', emailForm.value.email);
    // 调用接口
    const res = await sendEmailCode(emailForm.value.email);
    ElMessage.success(res.message || '验证码已发送，请注意查收');

    // 接口成功后立即启动倒计时（无多余操作）
    startCountDown();
  } catch (error) {
    console.error('发送验证码失败:', error.message || error);
    ElMessage.error('发送失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 启动倒计时（极简写法，确保生效）
const startCountDown = () => {
  console.log('启动倒计时');
  isCountingDown.value = true;
  countDownSeconds.value = 120;

  // 清除旧定时器（关键：防止多个定时器同时运行）
  if (countDownTimer) clearInterval(countDownTimer);

  // 基础定时器逻辑，直接操作状态
  countDownTimer = setInterval(() => {
    console.log('倒计时:', countDownSeconds.value);
    countDownSeconds.value--;

    // 倒计时结束
    if (countDownSeconds.value <= 0) {
      console.log('倒计时结束');
      clearCountDownTimer();
    }
  }, 1000);
};

// 清除倒计时（彻底重置状态）
const clearCountDownTimer = () => {
  if (countDownTimer) {
    clearInterval(countDownTimer);
    countDownTimer = null;
  }
  // 直接重置响应式状态，无需nextTick
  isCountingDown.value = false;
  countDownSeconds.value = 120;
  console.log('倒计时已清除');
};

// 表单提交逻辑不变
const handleSubmit = async () => {
  const currentForm = getCurrentForm();
  let valid = true;

  // 简单校验
  if (loginType.value === 'account') {
    if (!currentForm.username) { ElMessage.warning('请输入账号或邮箱'); valid = false; }
    else if (!currentForm.password) { ElMessage.warning('请输入密码'); valid = false; }
  } else {
    if (!isEmailValid.value) { ElMessage.warning('请输入有效邮箱'); valid = false; }
    else if (emailVerificationEnabled.value && !emailForm.value.emailCode) {
      ElMessage.warning('请输入邮箱验证码');
      valid = false;
    }
  }

  if (!currentForm.captchaCode) { ElMessage.warning('请输入图形验证码'); valid = false; }
  if (!currentForm.captchaUuid) { ElMessage.warning('请刷新图形验证码'); valid = false; }
  if (!valid) return;

  try {
    loading.value = true;
    const res = loginType.value === 'account'
        ? await UserApi.passwordLogin({
          username: currentForm.username,
          password: currentForm.password,
          captchaCode: currentForm.captchaCode,
          captchaUuid: currentForm.captchaUuid
        })
        : emailVerificationEnabled.value
          ? await UserApi.emailCodeLogin({
            email: emailForm.value.email,
            code: emailForm.value.emailCode,
            captchaCode: currentForm.captchaCode,
            captchaUuid: currentForm.captchaUuid
          })
          : await UserApi.emailLoginWithoutCode({
            email: emailForm.value.email,
            captchaCode: currentForm.captchaCode,
            captchaUuid: currentForm.captchaUuid
          });
    const data = res.data
    if (data.code === 200 && data.data.token && data.data.userInfoJson) {
      saveToken(data.data.token)
      saveUserInfoJson(data.data.userInfoJson)
      handleLoginSuccess(data.data.userInfoJson);
      hide();
    } else {
      handleLoginFail(data.message || '登录失败');
      ElMessage.error(data.message)
      await handleRefreshCaptcha();
    }
  } catch (err) {
    handleLoginFail(err.message || '登录异常');
    await handleRefreshCaptcha();
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.avatar-wrapper {
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: transform 0.18s ease;
}

.avatar-wrapper:hover {
  transform: translate(-1px, -1px);
}

.user-menu-content {
  padding: 12px;
  color: var(--geo-navy);
  background: #fff;
}

.user-card {
  display: flex;
  align-items: center;
  min-height: 64px;
  padding: 12px;
  gap: 12px;
  background: var(--geo-gold-light);
  border: 2px solid var(--geo-navy);
  box-shadow: 4px 4px 0 var(--geo-gold);
}

.user-card-mark {
  position: relative;
  display: grid;
  flex: none;
  width: 34px;
  height: 34px;
  place-items: center;
  background: var(--geo-coral);
  border: 2px solid var(--geo-navy);
}

.user-card-mark::before,
.user-card-mark::after,
.user-card-mark span {
  position: absolute;
  content: '';
  background: var(--geo-navy);
}

.user-card-mark::before {
  top: 7px;
  width: 9px;
  height: 9px;
  border-radius: 50%;
}

.user-card-mark::after {
  bottom: 6px;
  width: 18px;
  height: 9px;
  border-radius: 10px 10px 2px 2px;
}

.user-card-copy {
  min-width: 0;
}

.user-card .nickname {
  margin: 0;
  overflow: hidden;
  color: var(--geo-navy);
  font-size: 15px;
  font-weight: 850;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-card .username {
  margin: 4px 0 0 0;
  overflow: hidden;
  color: var(--color-text-secondary);
  font-size: 11px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.menu-divider {
  height: 2px;
  margin: 12px 0;
  background: var(--geo-navy);
  opacity: 0.12;
}

.menu-item {
  display: flex;
  align-items: center;
  min-height: 42px;
  padding: 0 12px;
  color: var(--geo-navy);
  font-size: 13px;
  font-weight: 750;
  background: #fff;
  border: 2px solid var(--geo-navy);
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background-color 0.16s ease;
}

.menu-item:hover {
  color: var(--geo-navy);
  background: var(--geo-sky-light);
  box-shadow: 3px 3px 0 var(--geo-navy);
  transform: translate(-2px, -2px);
}

.menu-item .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

.menu-item.logout:hover {
  color: var(--geo-navy);
  background: var(--geo-coral-light);
}

.user-menu-content > .login-btn {
  color: var(--geo-navy);
  background: var(--geo-gold);
  box-shadow: 3px 3px 0 var(--geo-navy);
}

.login-dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
  background-color: rgba(26, 26, 46, 0.66);
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.045) 1px, transparent 1px);
  background-size: 36px 36px;
  backdrop-filter: blur(4px);
  z-index: 9999;
}

.login-dialog {
  position: relative;
  width: min(420px, calc(100vw - 48px));
  max-height: calc(100vh - 48px);
  padding: 26px 28px 28px;
  overflow-y: auto;
  box-sizing: border-box;
  color: var(--geo-navy);
  background: #fff;
  border: 3px solid var(--geo-navy);
  border-radius: 0;
  box-shadow: 10px 10px 0 var(--geo-navy);
}

.login-dialog::before,
.login-dialog::after {
  position: absolute;
  content: '';
  pointer-events: none;
}

.login-dialog::before {
  top: 0;
  left: 0;
  width: 72px;
  height: 7px;
  background: var(--geo-coral);
}

.login-dialog::after {
  right: 0;
  bottom: 0;
  width: 7px;
  height: 72px;
  background: var(--geo-sky);
}

.dialog-kicker {
  display: flex;
  align-items: center;
  margin: 2px 0 20px;
  gap: 9px;
  color: var(--color-text-secondary);
  font-size: 11px;
  font-weight: 850;
  letter-spacing: 0.16em;
}

.dialog-kicker span {
  width: 24px;
  height: 6px;
  background: linear-gradient(90deg, var(--geo-coral) 0 55%, var(--geo-gold) 55%);
}

.login-dialog-header {
  position: relative;
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  gap: 10px;
}

.login-tab {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  min-height: 46px;
  box-sizing: border-box;
  color: var(--geo-navy);
  font-size: 15px;
  font-weight: 800;
  background: #fff;
  border: 2px solid var(--geo-navy);
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background-color 0.16s ease;
}

.login-tab:hover {
  background: var(--geo-sky-light);
}

.login-tab.active {
  color: var(--geo-navy);
  background: var(--geo-gold);
  box-shadow: 4px 4px 0 var(--geo-navy);
  transform: translate(-2px, -2px);
}

.close-btn {
  position: absolute;
  top: -50px;
  right: -15px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  padding: 0;
  color: var(--geo-navy);
  font-size: 22px;
  font-weight: 800;
  line-height: 1;
  background: var(--geo-coral-light);
  border: 2px solid var(--geo-navy);
  box-shadow: 3px 3px 0 var(--geo-navy);
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}

.close-btn:hover {
  transform: translate(-2px, -2px);
  box-shadow: 5px 5px 0 var(--geo-navy);
}

.form-item {
  margin-bottom: 18px;
}

.form-item label {
  display: block;
  margin-bottom: 7px;
  color: var(--geo-navy);
  font-size: 13px;
  font-weight: 800;
}

.form-item input {
  width: 100%;
  min-width: 0;
  height: 44px;
  padding: 0 12px;
  box-sizing: border-box;
  color: var(--geo-navy);
  font-size: 14px;
  font-weight: 600;
  background: #fff;
  border: 2px solid var(--geo-navy);
  border-radius: 0;
  outline: none;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background-color 0.16s ease;
}

.form-item input::placeholder {
  color: var(--color-text-tertiary);
  font-weight: 500;
}

.form-item input:focus {
  background: var(--geo-gold-light);
  box-shadow: 4px 4px 0 var(--geo-coral);
  transform: translate(-2px, -2px);
}

.verify-code-container {
  display: flex;
  align-items: stretch;
  gap: 12px;
}

.verify-code-container input {
  flex: 1;
}

.captcha-img {
  flex: none;
  width: 112px;
  height: 44px;
  box-sizing: border-box;
  background: var(--geo-sky-light);
  border: 2px solid var(--geo-navy);
  border-radius: 0;
  box-shadow: 3px 3px 0 var(--geo-sky);
  cursor: pointer;
}

.captcha-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  box-sizing: border-box;
  color: var(--geo-navy);
  font-size: 12px;
  font-weight: 700;
  text-align: center;
  background: var(--geo-sky-light);
  cursor: pointer;
}

.send-code-btn {
  flex: none;
  width: 112px;
  min-height: 44px;
  color: var(--geo-navy);
  font-size: 13px;
  font-weight: 800;
  background: var(--geo-sky);
  border: 2px solid var(--geo-navy);
  box-shadow: 3px 3px 0 var(--geo-navy);
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}

.send-code-btn:hover:not(:disabled) {
  box-shadow: 5px 5px 0 var(--geo-navy);
  transform: translate(-2px, -2px);
}

.send-code-btn:disabled {
  color: var(--color-text-tertiary) !important;
  background: var(--color-bg-tertiary) !important;
  box-shadow: none;
  opacity: 1;
  cursor: not-allowed;
}

.btn-group {
  display: flex;
  margin-top: 26px;
  gap: 14px;
}

.btn-group button {
  flex: 1;
  min-height: 48px;
  padding: 0 16px;
  color: var(--geo-navy);
  font-size: 15px;
  font-weight: 850;
  border: 2px solid var(--geo-navy);
  border-radius: 0;
  cursor: pointer;
  transition: transform 0.16s ease, box-shadow 0.16s ease, background-color 0.16s ease;
}

.btn-group .login-btn {
  color: var(--geo-navy);
  background: var(--geo-gold);
  box-shadow: 5px 5px 0 var(--geo-navy);
}

.btn-group button:hover:not(:disabled) {
  box-shadow: 7px 7px 0 var(--geo-navy);
  transform: translate(-2px, -2px);
}

.btn-group button:disabled {
  color: var(--color-text-tertiary);
  background: var(--color-bg-tertiary);
  box-shadow: none;
  cursor: not-allowed;
}

.register-btn {
  color: var(--geo-navy);
  background: #fff;
  box-shadow: 5px 5px 0 var(--geo-coral);
}

.register-btn:hover:not(:disabled) {
  background: var(--geo-coral-light);
}

.login-btn.solo-btn {
  width: 100%;
  flex: none;
}

/* 弹窗过渡动画（确保弹窗显示隐藏流畅，不影响状态更新） */
.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.18s ease;
}
.login-dialog {
  transition: transform 0.2s ease, opacity 0.18s ease;
}
.dialog-fade-enter-from .login-dialog,
.dialog-fade-leave-to .login-dialog {
  opacity: 0;
  transform: translate(-8px, -8px);
}

.login-form input:disabled {
  color: var(--color-text-tertiary);
  background: var(--color-bg-tertiary);
  border-color: var(--color-border-dark);
  cursor: not-allowed;
}

@media (max-width: 520px) {
  .login-dialog-mask {
    align-items: flex-start;
    padding: 26px 16px;
    overflow-y: auto;
  }

  .login-dialog {
    width: 100%;
    max-height: none;
    padding: 24px 18px 22px;
    box-shadow: 7px 7px 0 var(--geo-navy);
  }

  .dialog-kicker {
    margin-bottom: 18px;
  }

  .close-btn {
    right: -8px;
  }

  .login-dialog-header,
  .verify-code-container,
  .btn-group {
    gap: 9px;
  }

  .send-code-btn,
  .captcha-img {
    width: 104px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .avatar-wrapper,
  .menu-item,
  .login-tab,
  .close-btn,
  .form-item input,
  .send-code-btn,
  .btn-group button,
  .dialog-fade-enter-active,
  .dialog-fade-leave-active,
  .login-dialog {
    transition: none;
  }
}
</style>

<!-- 🔧 新增：全局样式（下拉框弹窗，需全局生效，不能加scoped） -->
<style>
.user-popover {
  padding: 0 !important;
  overflow: visible !important;
  background: #fff !important;
  border: 2px solid var(--geo-navy) !important;
  border-radius: 0 !important;
  box-shadow: 6px 6px 0 var(--geo-navy) !important;
}

.user-popover .el-popper__arrow {
  display: none !important;
}
</style>

<!-- 调试用的console日志说明（可选删除） -->
<!--
  若仍不生效，请打开浏览器控制台（F12），查看以下日志：
  1. 点击发送验证码时，是否输出 "发送验证码请求: 你的邮箱"
  2. 接口成功后，是否输出 "启动倒计时"
  3. 倒计时过程中，是否每秒输出 "倒计时: X"（X从120递减）
  4. 倒计时结束后，是否输出 "倒计时结束" 和 "倒计时已清除"

  若没有上述日志，说明：
  - 未触发handleSendEmailCode → 检查按钮disabled条件（isEmailValid是否为true）
  - 接口调用失败 → 检查sendEmailCode接口是否返回成功
  - 定时器未启动 → 检查是否有其他代码清除了定时器
-->
