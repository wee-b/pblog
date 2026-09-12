/**
 * 新增文章参数类型
 */
export interface InsertArticleDTO {
    title: string;
    content: string;
    summary: string;
    coverFileId: number | null;
    tagIds: number[];
}

export interface SeriesDTO {
    seriesName: string;
    description: string;
    coverFileId: number | null;
    articleIds: number[];
}



