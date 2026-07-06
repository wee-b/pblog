<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="login-bg"></div>

    <!-- 登录卡片 -->
    <el-card class="login-card">
      <div class="login-header">
        <h2 class="login-title">博客管理系统</h2>
        <p class="login-desc">请登录您的账号</p>
      </div>

      <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
      >
        <el-form-item prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              autocomplete="off"
          ></el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              :show-password="showPassword"
              @input="handlePasswordInput"
          ></el-input>
        </el-form-item>

        <el-form-item class="form-actions">
          <el-checkbox v-model="rememberMe" size="small">记住我</el-checkbox>
          <el-button type="text" size="small" class="forgot-password">忘记密码?</el-button>
        </el-form-item>

        <el-form-item>
          <div class="buttons-container">
            <el-button
                type="primary"
                class="login-btn"
                @click="handleLogin"
                :loading="loginLoading"
            >
              登录
            </el-button>

            <el-button
                type="primary"
                class="register-btn"
                @click="navigateToRegister"
                :loading="loginLoading"
            >
              注册
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 页脚信息 -->
    <div class="login-footer">
      <p>© 2023 博客管理系统 版权所有</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { adminLogin } from '@/apis/auth';

// 路由实例
const router = useRouter();

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
});

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
};

// 表单引用
const loginFormRef = ref(null);
loginFormRef.value = undefined;

// 状态管理
const showPassword = ref(false);
const rememberMe = ref(false);
const loginLoading = ref(false);
const passwordStrength = ref(0);

// 处理密码输入，简单的密码强度检测
const handlePasswordInput = (value) => {
  if (value.length < 6) {
    passwordStrength.value = 0;
  } else if (value.length < 10) {
    passwordStrength.value = 1;
  } else if (/[A-Z]/.test(value) && /[0-9]/.test(value)) {
    passwordStrength.value = 3;
  } else {
    passwordStrength.value = 2;
  }
};

// 处理登录
const handleLogin = async () => {
  try {
    // 表单验证
    await loginFormRef.value.validate();
    
    // 设置加载状态
    loginLoading.value = true;
    
    // 调用登录API
    const response = await adminLogin(loginForm.value.username, loginForm.value.password);
    
    // 处理登录成功
    if (response.status === 200 && response.code === 200) {
      // 保存token和用户信息
      localStorage.setItem('blogAdminToken', response.data.token);
      localStorage.setItem('blogAdminUser', JSON.stringify(response.data));
      
      // 记住密码功能 - 修复为使用rememberMe
      if (rememberMe.value) {
        localStorage.setItem('rememberUsername', loginForm.value.username);
        localStorage.setItem('rememberPassword', loginForm.value.password);
      } else {
        localStorage.removeItem('rememberUsername');
        localStorage.removeItem('rememberPassword');
      }
      
      // 显示成功消息
      ElMessage.success(response.message || '登录成功');
      
      // 跳转到首页
      router.push('/');
    } else {
      // 显示错误消息
      ElMessage.error(response.message || '登录失败');
    }
  } catch (error) {
    // 处理表单验证失败
    if (error.name === 'Error') {
      ElMessage.error('请检查您的输入');
    } else {
      // 处理API错误
      ElMessage.error('登录失败，请稍后重试');
      console.error('登录错误:', error);
    }
  } finally {
    // 重置加载状态
    loginLoading.value = false;
  }
};

// 页面加载时检查是否有记住的用户
const initForm = () => {
  // 优先检查是否有记住的用户名密码
  const rememberedUsername = localStorage.getItem('rememberUsername');
  const rememberedPassword = localStorage.getItem('rememberPassword');
  if (rememberedUsername && rememberedPassword) {
    loginForm.username = rememberedUsername;
    loginForm.password = rememberedPassword;
    rememberMe.value = true;
  } else {
    // 其次检查是否有保存的用户信息
    const savedUser = localStorage.getItem('blogAdminUser');
    if (savedUser) {
      try {
        const user = JSON.parse(savedUser);
        loginForm.username = user.username || '';
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }
  }
};

// 初始化表单
initForm();

// 跳转到注册页面
const navigateToRegister = () => {
  router.push('/register');
};
</script>


<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #F5F7FA;
  position: relative;
  padding: 20px;
}

/* 背景装饰效果优化 */
.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
      radial-gradient(circle at 20% 20%, rgba(64, 158, 255, 0.05) 0%, transparent 20%),
      radial-gradient(circle at 80% 80%, rgba(64, 158, 255, 0.05) 0%, transparent 20%);
  background-size: 100% 100%;
  z-index: 1;
}

.login-card {
  width: 420px;
  padding: 36px 40px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  background-color: #fff;
  z-index: 10;
  transition: transform 0.3s ease;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #1D2129;
  margin-bottom: 8px;
  position: relative;
  display: inline-block;
}

.login-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background-color: #409EFF;
  border-radius: 3px;
}

.login-desc {
  font-size: 14px;
  color: #86909C;
}

.login-form {
  margin-top: 20px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-input {
  height: 44px;
  border-radius: 6px;
  transition: border-color 0.3s;
}

.el-input:focus-within {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.forgot-password {
  color: #409EFF;
  transition: color 0.2s;
  text-decoration: none;
}

.forgot-password:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.buttons-container {
  width: 100%;
  display: flex;
  gap: 12px;
  justify-content: center; /* 关键：让按钮组水平居中 */
}

.login-btn, .register-btn {
  width: 120px;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.login-btn::before, .register-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.6s ease-in-out;
}

.login-btn:hover::before, .register-btn:hover::before {
  left: 100%;
}

.login-btn:hover, .register-btn:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
}

.login-btn:active, .register-btn:active {
  transform: translateY(0) scale(0.98);
}

/* 为登录和注册按钮添加细微的颜色区分 */
.register-btn {
  background-color: #67c23a;
  border-color: #67c23a;
}

.register-btn:hover {
  background-color: #85ce61;
  border-color: #85ce61;
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.3);
}

.login-footer {
  margin-top: 30px;
  text-align: center;
  font-size: 12px;
  color: #86909C;
  z-index: 10;
}

/* 密码强度指示器样式 */
.password-strength {
  height: 6px;
  margin-top: 8px;
  border-radius: 3px;
  background-color: #f5f5f5;
  overflow: hidden;
}

.strength-bar {
  height: 100%;
  transition: width 0.3s ease;
}

.strength-weak {
  width: 33%;
  background-color: #ff4d4f;
}

.strength-medium {
  width: 66%;
  background-color: #faad14;
}

.strength-strong {
  width: 100%;
  background-color: #52c41a;
}

.strength-text {
  font-size: 12px;
  margin-top: 4px;
  color: #86909C;
}

/* 响应式调整 */
@media (max-width: 576px) {
  .login-card {
    width: 100%;
    padding: 24px 20px;
  }

  .login-title {
    font-size: 20px;
  }
}
</style>




