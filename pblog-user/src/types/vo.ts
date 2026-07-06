
export interface ApiResponse<T = any> {
    code: number;
    message: string;
    data: T;
    timestamp: string;
}


// --- Props 定义 ---
export interface CategoryVO {
    id: number;
    categoryName: string;
    parentId: number;
    orderNum: number;
    articleCount?: any;
    description?: any;
}


export interface UserInfoVO {
    username: string,
    nickname:string,
    avatarUrl:string,
    bio:string | null,
}

export interface ArticleDetailVO {
    id: number;
    title: string;
    summary: string;
    coverImage: string;
    authorUsername: string;
    authorNickname: string;
    viewCount: number;
    likeCount: number;
    commentCount: number;
    sticky: string;
    featured: string;
    publishedAt: string| null;
    categories: {
        id: number;
        categoryName: string;
        parentId: number;
        orderNum: number;
        articleCount: number | null;
        description: string | null;
    }[];
    status: string;
    content: string;
}


export interface ArticleVO {
    id: number;
    title: string;
    summary: string;
    coverImage: string;
    authorUsername: string;
    authorNickname: string;
    viewCount: number;
    likeCount: number;
    commentCount: number;
    sticky: string;
    featured: string;
    publishedAt: string| null;
    categories: {
        id: number;
        categoryName: string;
        parentId: number;
        orderNum: number;
        articleCount: number | null;
        description: string | null;
    }[];
    status: string;
}




export interface CommentVO {
    id:number,
    article_id:number,
    root_id:number,
    parent_id:number,
    to_reply_username:string,
    content:string,
    like_count:number,
    create_time:string,
    userInfoVO:UserInfoVO;
}




