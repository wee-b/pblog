/**
 * 新增文章参数类型
 */
export interface InsertArticleDTO {
    title: string;
    content: string;
    summary: string;
    coverImage: string;
    tagIds: number[];
}



