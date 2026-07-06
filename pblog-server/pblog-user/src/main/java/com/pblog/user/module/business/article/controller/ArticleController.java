package com.pblog.user.module.business.article.controller;

import com.pblog.common.domain.dto.Article.ArticleDTO;
import com.pblog.common.domain.dto.Article.ArticlePageQueryDTO;
import com.pblog.common.domain.dto.Article.updateArticleDTO;
import com.pblog.common.domain.result.PageResult;
import com.pblog.common.domain.result.ResponseResult;
import com.pblog.common.domain.vo.ArticleDetailVO;
import com.pblog.common.domain.vo.ArticleVO;
import com.pblog.user.module.business.article.service.ArticleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 博客文章表(Article)表控制层（适配 MyBatis-Plus）
 * @since 2025-11-29 16:20:52
 */
@Slf4j
@RestController
public class ArticleController {
    /**
     * 服务对象（注入 MP 的 IService 接口，而非实现类）
     */
    @Autowired
    private ArticleService articleService;

    /**
     * 分页查询所有数据
     */
    @PostMapping("/article/pageQuery")
    public ResponseResult<PageResult> pageQuery(@Valid @RequestBody ArticlePageQueryDTO pageQueryDTO) {
        log.info("收到请求：/article/pageQuery，参数{}", pageQueryDTO);
        PageResult pageResult = articleService.pageQuery(pageQueryDTO);
        return ResponseResult.success(pageResult);
    }

    /**
     * 通过主键查询单条数据
     */
    @GetMapping("/article/queryById/{id}")
    public ResponseResult<ArticleDetailVO> queryById(@PathVariable("id") Integer id) {
        log.info("收到请求：/article/queryById，参数{}", id);
        ArticleDetailVO one = articleService.queryById(id);
        return ResponseResult.success(one);
    }

    /**
     * 发布文章
     */
    @PostMapping("/article/insert")
    public ResponseResult<Integer> insert(@Valid @RequestBody ArticleDTO articledto) {
        log.info("收到请求：/article/insert，参数{}", articledto);
        Integer res = articleService.insert(articledto,"/insert");
        return ResponseResult.success(res ) ;
    }

    /**
     * 保存文章为草稿
     */
    @PostMapping("/article/insertDraft")
    public ResponseResult<Integer> insertDraft(@Valid @RequestBody ArticleDTO articledto) {
        log.info("收到请求：/article/insertDraft，参数{}", articledto);
        Integer res = articleService.insert(articledto,"/insertDraft");
        return ResponseResult.success(res ) ;
    }

    /**
     * 修改数据
     */
    @PutMapping("/article/update")
    public ResponseResult<String> update(@Valid @RequestBody updateArticleDTO articledto) {
        log.info("收到请求：/article/update，参数{}", articledto);
        boolean success = articleService.update(articledto);
        return ResponseResult.success(success?"修改成功":"修改失败") ;
    }

    /**
     * 删除数据（调用 MP removeById 方法）
     */
    @DeleteMapping("/article/delete/{id}")
    public ResponseResult<String> deleteById(@PathVariable("id") Integer id) {
        log.info("收到请求：/article/delete，参数{}", id);
        boolean success = articleService.removeById(id); // MP 内置方法
        return ResponseResult.success(success?"删除成功":"删除失败") ;
    }


    /**
     * 下架/投稿文章
     */
    @PostMapping("/article/status/{id}")
    public ResponseResult<String> status(@PathVariable("id") Integer id) {
        log.info("收到请求：/article/status，参数{}", id);
        boolean success = articleService.status(id);
        return ResponseResult.success(success?"下架/投稿文章成功":"下架/投稿文章失败") ;
    }

    /**
     * 设为置顶/取消置顶
     */
    @PostMapping("/article/setSticky/{id}")
    public ResponseResult<String> setSticky(@PathVariable("id") Integer id) {
        log.info("收到请求：/article/setSticky，参数{}", id);
        boolean success = articleService.setSticky(id);
        return ResponseResult.success(success?"操作成功":"操作失败") ;
    }

    /**
     * 设为推荐/取消推荐
     */
    @PostMapping("/article/setFeatured/{id}")
    public ResponseResult<String> setFeatured(@PathVariable("id") Integer id) {
        log.info("收到请求：/article/setFeatured，参数{}", id);
        boolean success = articleService.setFeatured(id);
        return ResponseResult.success(success?"操作成功":"操作失败") ;
    }

    /**
     * 获取推荐文章
     */
    @GetMapping("/article/getFeaturedArticles")
    public ResponseResult<List<ArticleVO>> getFeaturedArticles() {
        log.info("收到请求：/article/getFeaturedArticles");
        List<ArticleVO> res = articleService.getFeaturedArticles();
        return ResponseResult.success(res) ;
    }



}
