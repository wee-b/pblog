import http from '@/utils/http.js'; // 导入全局 http 实例


export const getAllCategorys = async ()=>{
    const response = await http({
        url:'/category/all',
        method:"GET",
    })
    return response;
}