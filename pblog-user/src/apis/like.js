import http from '@/utils/http.js'; // 导入全局 http 实例

/**
 operateType： 1-点赞，0-取消点赞
 targetType： 0-其他，1-文章，2-评论
 */


// 点赞/取消点赞
export const likeUnlike = async (params)=>{
    return await http({
        url:"/like/operate",
        method:"post",
        data:params,
    })
}

// 获取用户是否点赞（如果登录）
export const isLiked = async (targetId,targetType)=>{
    const tId = Number.isInteger(targetId) ? targetId : parseInt(targetId, 10);
    return await http({
        url:"/like/isLiked",
        method:"get",
        params:{
            'targetId':tId,
            'targetType':targetType
        }
    })
}

// 获取评论/文章点赞数量
export const getLikeCount = async (targetId,targetType)=>{
    const tId = Number.isInteger(targetId) ? targetId : parseInt(targetId, 10);
    return await http({
        url:"/like/count",
        method:"get",
        params:{
            'targetId':tId,
            'targetType':targetType
        }
    })
}