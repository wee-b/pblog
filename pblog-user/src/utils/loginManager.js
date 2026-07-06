// src/utils/loginManager.js
import { ref, watch } from 'vue';
import loginApi from '@/apis/user/user.js';

let appInstance = null;
let loginDialogInstance = null;

// 使用 Vue 3 的 ref 来创建响应式事件系统
const eventListeners = ref({});
// 🔴 存储弹窗显示状态（全局唯一）
let isLoginDialogShow = false;

const STORAGE_KEYS = {
  TOKEN: 'token',
  USER_INFO: 'userInfoJson',
  SAVED_USER: 'savedUser'
};

const LOGIN_EVENTS = {
  SUCCESS: 'loginSuccess',
  FAIL: 'loginFail',
  LOGOUT: 'logoutSuccess'
};

/**
 * 初始化登录管理器
 */
export const initLoginManager = (app) => {
  appInstance = app;

  // 挂载全局方法
  app.config.globalProperties.$loginManager = {
    showLogin,
    checkLogin,
    logout,
    on: onEvent,
    off: offEvent,
    getToken,
    getUserInfo
  };

  // 开发环境下暴露到 window
  if (import.meta.env.DEV && import.meta.env.CLIENT) {
    window.$loginManager = {
      showLogin,
      checkLogin,
      logout,
      on: onEvent,
      off: offEvent,
      getToken,
      getUserInfo
    };
  }
};

/**
 * 注册登录弹窗组件实例
 */
export const registerLoginDialog = (instance) => {
  loginDialogInstance = instance;

  // 🔧 监听弹窗显示状态，保持 isLoginDialogShow 与 isVisible 同步
  watch(
      () => loginDialogInstance.isVisible.value,
      (visible) => {
        isLoginDialogShow = !!visible;
      }
  );
};

/**
 * 唤起登录弹窗
 */
export const showLogin = () => {
  if (!loginDialogInstance) {
    console.error('登录弹窗组件未注册，请确保 <LoginDialog> 已全局注册');
    return;
  }

  // 弹窗已经显示，则直接返回
  if (loginDialogInstance.isVisible.value) return;

  loginDialogInstance.isVisible.value = true;
};

/**
 * 判断是否已登录
 */
export const checkLogin = () => {
  try {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    return !!token && token !== 'undefined' && token !== 'null';
  } catch (error) {
    console.error('检查登录状态失败:', error);
    return false;
  }
};

/**
 * 获取用户信息
 */
export const getUserInfo = () => {
  try {
    const userInfo = localStorage.getItem(STORAGE_KEYS.USER_INFO);
    if (!userInfo) return null;

    const parsed = JSON.parse(userInfo);

    // 确保 username 是字符串
    if (parsed.username !== undefined && parsed.username !== null) {
      parsed.username = String(parsed.username);
    }

    return parsed;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    return null;
  }
};

/**
 * 获取 token
 */
export const getToken = () => {
  return localStorage.getItem(STORAGE_KEYS.TOKEN);
};

/**
 * 保存登录信息
 */
export const saveToken = (token) => {
  try {
    localStorage.setItem(STORAGE_KEYS.TOKEN, token);
  } catch (error) {
    console.error('保存登录信息失败:', error);
    throw new Error('保存登录信息失败');
  }
};

export const saveUserInfoJson = (userInfoJson) => {
  try {
    localStorage.setItem(STORAGE_KEYS.USER_INFO, userInfoJson);
  } catch (error) {
    console.error('保存登录信息失败:', error);
    throw new Error('保存登录信息失败');
  }
};

/**
 * 退出登录
 */
export const logout = () => {
  // 清除本地存储
  localStorage.removeItem(STORAGE_KEYS.TOKEN);
  localStorage.removeItem(STORAGE_KEYS.USER_INFO);
  localStorage.removeItem(STORAGE_KEYS.SAVED_USER);

  // 触发退出事件
  emitEvent(LOGIN_EVENTS.LOGOUT);
};

/**
 * 登录成功处理
 */
export const handleLoginSuccess = (user) => {
  emitEvent(LOGIN_EVENTS.SUCCESS, user);
};

/**
 * 登录失败处理
 */
export const handleLoginFail = (msg) => {
  emitEvent(LOGIN_EVENTS.FAIL, msg);
};

/**
 * 触发事件
 */
const emitEvent = (eventName, ...args) => {
  const listeners = eventListeners.value[eventName];
  if (listeners) {
    listeners.forEach(callback => {
      try {
        callback(...args);
      } catch (error) {
        console.error(`执行事件 ${eventName} 的回调时出错:`, error);
      }
    });
  }
};

/**
 * 监听事件
 */
const onEvent = (eventName, callback) => {
  if (!eventListeners.value[eventName]) {
    eventListeners.value[eventName] = [];
  }
  eventListeners.value[eventName].push(callback);
};

/**
 * 移除事件监听
 */
const offEvent = (eventName, callback) => {
  const listeners = eventListeners.value[eventName];
  if (listeners) {
    if (callback) {
      const index = listeners.indexOf(callback);
      if (index > -1) {
        listeners.splice(index, 1);
      }
    } else {
      // 如果没有指定 callback，移除所有该事件的监听器
      eventListeners.value[eventName] = [];
    }
  }
};
