<template>
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
              :class="{ active: loginType === 'email' }"
              @click="switchLoginType('email')"
          >
            邮箱登录
          </div>
          <button class="close-btn" @click="hide">×</button>
        </div>

        <form class="login-form" @submit.prevent="handleSubmit">
          <div class="form-item" v-if="loginType === 'account'">
            <label>账号</label>
            <input
                v-model.trim="passwordForm.username"
                type="text"
                placeholder="请输入账号"
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
          <div class="form-item" v-if="loginType === 'email'">
            <label>邮箱验证码</label>
            <div class="verify-code-container">
              <input
                  v-model.trim="emailForm.emailCode"
                  type="text"
                  placeholder="请输入邮箱验证码"
                  required
                  :disabled="loading "
              />
              <!-- 关键：绑定基础状态变量，无复杂逻辑 -->
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
                :disabled="loading || !emailForm.emailCode || !emailForm.captchaUuid"
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
import { ref, watch, onUnmounted, onDeactivated, computed } from 'vue';
import UserApi from '@/apis/user/user.js';
import { refreshCaptcha, sendEmailCode } from '@/apis/code.js';
import { registerLoginDialog, handleLoginSuccess, handleLoginFail, saveLoginInfo } from '@/utils/loginManager';
import { ElMessage } from 'element-plus';

// 邮箱正则
const emailReg = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;

// 基础状态（简化响应式，避免复杂依赖）
const isVisible = ref(false);
const loading = ref(false);
const loginType = ref('account');
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
defineExpose({ isVisible });

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
    await handleRefreshCaptcha();
  }
});

// 组件生命周期清理
onDeactivated(() => clearCountDownTimer());
onUnmounted(() => {
  if (codeBlobUrl.value?.startsWith('blob:')) {
    URL.revokeObjectURL(codeBlobUrl.value);
  }
  clearCountDownTimer(); // 卸载必清定时器
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
    if (!currentForm.username) { ElMessage.warning('请输入账号'); valid = false; }
    else if (!currentForm.password) { ElMessage.warning('请输入密码'); valid = false; }
  } else {
    if (!isEmailValid.value) { ElMessage.warning('请输入有效邮箱'); valid = false; }
    else if (!emailForm.value.emailCode) { ElMessage.warning('请输入邮箱验证码'); valid = false; }
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
        : await UserApi.emailCodeLogin({
          email: emailForm.value.email,
          code: emailForm.value.emailCode,
          captchaCode: currentForm.captchaCode,
          captchaUuid: currentForm.captchaUuid
        });

    const data = res.data;
    if (data.code === 200 && data.data.token && data.data.userInfoJson) {
      saveLoginInfo(data.data.token, data.data.userInfoJson);
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
/* 原有样式不变，确保按钮状态清晰 */
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
  width: 350px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
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
  font-size: 14px;
  cursor: pointer;
}
.send-code-btn:disabled {
  background: #a0cfff;
  cursor: not-allowed;
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

/* 新增：倒计时按钮禁用状态强化（避免视觉混淆） */
.send-code-btn:disabled {
  background: #e6f4ff !important;
  color: #8cc5ff !important;
  opacity: 0.9;
}

/* 弹窗过渡动画（确保弹窗显示隐藏流畅，不影响状态更新） */
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

/* 修复输入框禁用状态样式（避免与倒计时状态冲突） */
.login-form input:disabled {
  background: #f9f9f9;
  color: #999;
  border-color: #e0e0e0;
}
</style>

<!-- 额外添加：调试用的console日志说明（可选删除） -->
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
