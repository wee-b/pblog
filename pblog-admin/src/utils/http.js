// @/apis/http.js
import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router'; // 导入路由实例
import { getToken , logout, showLogin} from '@/utils/loginManager';

const http = axios.create({
    baseURL: "http://localhost:8081",
    timeout: 5000,
});

// 🔴 新增：白名单 - 无需携带 Token 的接口路径（精准匹配，支持完整路径或后缀）
const NO_TOKEN_WHITE_LIST = [
    "/api/error",
    "/api/hello",
    "/api/code/picture/generate",
    "/api/admin/login",
    "/api/admin/addPerson" ,

    "/api/article/pageQuery",
    "/api/article/queryById/**",
    "/api/article/getFeaturedArticles",
    "/api/category/all",
    "/api/comment/all/{id}",
    "/api/comment/insertRemark"
];


// 🔴 修改后：请求拦截器（保留原有打印日志，新增 Token 注入）
http.interceptors.request.use(
    (config) => {
        console.log('发送请求：', config.method.toUpperCase(), config.baseURL + config.url);

        // 核心逻辑：1. 判断是否在白名单 → 2. 不在则注入 Token
        const isWhiteList = NO_TOKEN_WHITE_LIST.some(whitePath => {
            // 匹配规则：接口路径是否以白名单路径结尾（适配带参数的场景，例：/auth/captcha?uuid=xxx）
            return config.url.endsWith(whitePath);
        });

        if (!isWhiteList) { // 不在白名单中，才添加 Token
            const token = getToken();
            if (token) {
                config.headers.token = token; // 后端要求的 "token" 字段
            }
        }

        return config;
    },
    (e) => {
        ElMessage.error('请求发送失败，请检查网络');
        return Promise.reject(e);
    }
);

// 🔴 修改后：带 401 处理的响应拦截器
// 🔴 修改 http.js 中的响应拦截器 401 部分
http.interceptors.response.use(
    (response) => {
        console.log('发送请求：', response.config.method.toUpperCase(), response.config.baseURL + response.config.url);
        return response;
    },
    (error) => {
        console.error('响应错误：', error.message);
        console.error('错误详情：', error.response || '无响应体');

        // 🔴 适配弹窗的 401 处理逻辑
        if (error.response?.status === 401) {
            // 1. 清除本地无效 Token 和用户信息
            logout();

            // 2. 获取当前页面路径（用于登录成功后重定向）
            const currentPath = router.currentRoute.value.fullPath;

            // 3. 存储重定向路径（登录成功后跳回）
            localStorage.setItem('redirectPath', currentPath);

            // 4. 提示用户登录失效（避免重复提示）
            ElMessage.warning('登录已失效，请重新登录');

            // 5. 唤起登录弹窗（loginManager 的 showLogin 已绑定弹窗实例，无需额外处理）
            showLogin();
        }

        // 保留原有其他错误处理逻辑
        if (error.code === 'ECONNABORTED') {
            ElMessage.error('请求超时，请重试');
        } else if (error.response?.status === 404) {
            ElMessage.error('接口地址错误，后端未找到该接口');
        } else if (error.response?.status === 500) {
            ElMessage.error('后端服务错误，请联系管理员');
        } else if (error.response?.status !== 401) { // 排除 401 已单独提示
            ElMessage.error('请求失败：' + (error.message || '未知错误'));
        }

        return Promise.reject(error);
    }
);

export default http;