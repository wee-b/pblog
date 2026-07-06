import request from '@/utils/http.js'


export function getCollectionArticles() {
    return request({
        url: '/article/getFeaturedArticles',
        method: 'get'
    })
}

export function pageQueryArticles(data) {
    return request({
        url: '/article/pageQuery',
        method: 'post',
        data:data
    })
}

export function getArticleDetail(id) {
    const integerId = Number.isInteger(id) ? id : parseInt(id, 10);
    return request({
        url: `/article/queryById/${integerId}`,
        method: 'get',
    })
}

// 删除文章
export function deleteArticleById(id) {
    const integerId = Number.isInteger(id) ? id : parseInt(id, 10);
    return request({
        url: `/article/delete/${integerId}`,
        method: 'delete',
    })
}

// 上架/下架文章，（0待审核 1已发布 2草稿）
export function enableDisableArticle(id) {
    const integerId = Number.isInteger(id) ? id : parseInt(id, 10);
    return request({
        url: `/article/status/${integerId}`,
        method: 'post',
    })
}

// 审核通过文章
export function statusPass(id) {
    const integerId = Number.isInteger(id) ? id : parseInt(id, 10);
    return request({
        url: `/article/statusPass/${integerId}`,
        method: 'post',
    })
}


// 置顶/取消置顶文章
export function setArticleSticky(id) {
    const integerId = Number.isInteger(id) ? id : parseInt(id, 10);
    return request({
        url: `/article/setSticky/${integerId}`,
        method: 'post',
    })
}

// 新增文章
export function insertArticle(data) {
    return request({
        url: `/article/insert`,
        method: 'post',
        data:data
    })
}

// 新增为草稿
export function insertDraft(data) {
    return request({
        url: `/article/insertDraft`,
        method: 'post',
        data:data
    })
}

// 修改文章
export function updateArticle(data) {
    return request({
        url: `/article/update`,
        method: 'put',
        data:data
    })
}





