package com.pblog.user.module.business.article.service;

import com.pblog.common.domain.dto.Article.ArticlePageQueryDTO;
import com.pblog.common.domain.result.PageResult;
import com.pblog.user.module.business.article.esEntity.EsArticle;

public interface EsArticleService {
    PageResult pageQuery(ArticlePageQueryDTO pageQueryDTO);

    void syncArticle(EsArticle article);

    void deleteArticle(Integer id);

    // 新增一个更通用的方法，供 ArticleCategoryRelationHandler 使用
    void updateArticleTags(Integer articleId, String[] tagIds, boolean isAddOperation);
}
