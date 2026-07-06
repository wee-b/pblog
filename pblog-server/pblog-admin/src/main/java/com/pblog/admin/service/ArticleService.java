package com.pblog.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pblog.common.domain.dto.Article.ArticleDTO;
import com.pblog.common.domain.dto.Article.ArticlePageQueryDTO;
import com.pblog.common.domain.dto.Article.updateArticleDTO;
import com.pblog.common.domain.entity.Article;
import com.pblog.common.domain.result.PageResult;
import com.pblog.common.domain.vo.ArticleDetailVO;
import com.pblog.common.domain.vo.ArticleVO;

import java.util.List;

/**
 * 博客文章表(Article)表服务接口
 */
public interface ArticleService extends IService<Article> {

    PageResult pageQuery(ArticlePageQueryDTO pageQueryDTO);

    Integer insert(ArticleDTO articledto,String url);

    boolean update(updateArticleDTO articledto);

    boolean statusPass(Integer id);

    boolean status(Integer id);

    boolean setSticky(Integer id);

    boolean setFeatured(Integer id);

    List<ArticleVO> getFeaturedArticles();

    ArticleDetailVO queryById(Integer id);
}


