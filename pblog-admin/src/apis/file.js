import http from '@/utils/http.js';

// 上传文章图片
export const uploadImage = async (file) => {
    if (!file) {
        throw new Error('请选择要上传的图片文件');
    }
    const formData = new FormData();
    formData.append('file', file);
    const response = await http({
        url: '/file/uploadImage',
        method: 'POST',
        data: formData,
        // headers: {
        //     'Content-Type': 'multipart/form-data'
        // }
    });

    return response.data;
};

// 上传头像（同理）
export const uploadAvatar = async (file) => {
    if (!file) {
        throw new Error('请选择要上传的头像文件');
    }
    const formData = new FormData();
    formData.append('file', file);
    const response = await http({
        url: '/file/uploadAvatar',
        method: 'POST',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
    // 提取URL
    return response.data;
};