package com.pblog.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pblog.common.domain.dto.Article.ArticlePageQueryDTO;
import com.pblog.common.domain.entity.Article;
import com.pblog.common.domain.vo.ArticleDetailVO;
import com.pblog.common.domain.vo.ArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;



@Mapper
public interface ArticleMapper extends BaseMapper<Article> {


     IPage<ArticleVO> selectArticlePage(
             @Param("page") Page<ArticleVO> page,
             @Param("query") ArticlePageQueryDTO queryDTO
     );

     List<ArticleVO> selectFeaturedArticle();

     ArticleDetailVO getArticleDetail(Integer articleId);
}
