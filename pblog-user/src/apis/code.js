// @/apis/code.js
import http from '@/utils/http.js'; // 导入全局 http 实例


/**
 * 获取图形验证码（路径不变，代理自动转发）
 */
export const getCaptcha = async () => {
    const response = await http({
        url: '/code/picture/generate',
        method: 'GET',
        responseType: 'blob', // 接收二进制图片流
    });

    // 提取响应头的 Captcha-Uuid（Axios 转为小写）
    const captchaUuid = response.headers['captcha-uuid'];
    if (!captchaUuid) {
        throw new Error('后端未返回验证码 UUID');
    }

    const blobUrl = URL.createObjectURL(response.data);
    return { blobUrl, captchaUuid };
};

export const refreshCaptcha = async () => getCaptcha();

// 邮箱验证码函数（移除所有 ElMessage 提示，仅保留错误抛出）
export const sendEmailCode = async (receiveEmail) => {
    if (!/^[\w.-]+@[a-zA-Z0-9-]+\.[a-zA-Z]+$/.test(receiveEmail)) {
        throw new Error('邮箱格式错误');
    }

    const response = await http({
        url: '/code/email/sendEmail',
        method: 'GET',
        params: { receiveEmail },
    });
    return response.data;
};

export default { getCaptcha, refreshCaptcha, sendEmailCode };