package com.pblog.user.module.business.article.controller;

import com.pblog.common.domain.dto.Article.ArticlePageQueryDTO;
import com.pblog.common.domain.result.PageResult;
import com.pblog.common.domain.result.ResponseResult;
import com.pblog.user.module.business.article.service.EsArticleService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @since 2026-3-11
 */
@Slf4j
@RestController
public class EsArticleController {

    @Resource
    private EsArticleService esArticleService;

    /**
     * 分页查询所有数据
     */
    @PostMapping("/article/es/pageQuery")
    public ResponseResult<PageResult> pageQuery(@Valid @RequestBody ArticlePageQueryDTO pageQueryDTO) {
        PageResult pageResult = esArticleService.pageQuery(pageQueryDTO);
        log.info("articleService.pageResult:{}", pageResult);
        return ResponseResult.success(pageResult);
    }
}
