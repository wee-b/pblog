

export interface PublishFormData {
    title: string;
    content: string;
    summary: string;
    cover?: string; // 子组件的预览URL（可选）
    coverFile: File | null; // 原始文件对象（可为null）
    tagIds: number[];
}


