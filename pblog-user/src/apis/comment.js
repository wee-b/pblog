import http from '@/utils/http.js'; // 导入全局 http 实例


export const getArticleAllComments = async (id)=>{
    const integerId = Number.isInteger(id) ? id : parseInt(id, 10);
    const response = await http({
        url:`/comment/all/${integerId}`,
        method:"GET",
    })
    return response;
}

export const getCommentsFromMe = async ()=>{
    const response = await http({
        url:'/comment/fromMe',
        method:"GET",
    })
    return response;
}

export const getCommentsForMe = async ()=>{
    const response = await http({
        url:'/comment/forMe',
        method:"GET",
    })
    return response;
}

export const insertComment = async (data)=>{
    const response = await http({
        url:'/comment/insert',
        method:"POST",
        data:data
    })
    return response;
}

export const deleteComment = async (id)=>{
    const integerId = Number.isInteger(id) ? id : parseInt(id, 10);
    const response = await http({
        url:`/comment/delete/${integerId}`,
        method:"DELETE",
    })
    return response;
}


export const insertRemark = async (data)=>{
    const response = await http({
        url:'/comment/insertRemark',
        method:"POST",
        data:data
    })
    return response;
}