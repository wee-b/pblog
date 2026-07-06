package com.pblog.user.module.business.article.handler;

import com.alibaba.otter.canal.protocol.CanalEntry;


import com.pblog.common.listener.CanalRowHandler;
import com.pblog.user.module.business.article.esEntity.EsArticle;
import com.pblog.user.module.business.article.service.EsArticleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ArticleTableHandler implements CanalRowHandler {

    @Resource
    private EsArticleService esArticleService;

    @Override
    public String getTargetTable() {
        return "pb_article";
    }

    @Override
    public void handleBatchInsert(List<CanalEntry.RowData> rowDatasList) {
        log.info("Handling batch INSERT on pb_article for {} rows.", rowDatasList.size());
        for (CanalEntry.RowData rowData : rowDatasList) {
            EsArticle article = convertToArticle(rowData.getAfterColumnsList());
            log.debug("Processing INSERT for article: {}", article.getTitle());
            esArticleService.syncArticle(article); // 这个方法内部会将 articleTagIds 置为空
        }
    }

    @Override
    public void handleBatchUpdate(List<CanalEntry.RowData> rowDatasList) {
        log.info("Handling batch UPDATE on pb_article for {} rows.", rowDatasList.size());
        for (CanalEntry.RowData rowData : rowDatasList) {
            EsArticle article = convertToArticle(rowData.getAfterColumnsList());
            log.debug("Processing UPDATE for article: {}", article.getTitle());
            esArticleService.syncArticle(article); // 这个方法内部会将 articleTagIds 置为空或忽略
        }
    }

    @Override
    public void handleBatchDelete(List<CanalEntry.RowData> rowDatasList) {
        log.info("Handling batch DELETE on pb_article for {} rows.", rowDatasList.size());
        for (CanalEntry.RowData rowData : rowDatasList) {
            EsArticle article = convertToArticle(rowData.getBeforeColumnsList());
            log.debug("Processing DELETE for article: {}", article.getTitle());
            esArticleService.deleteArticle(article.getId());
        }
    }

    private EsArticle convertToArticle(List<CanalEntry.Column> columns) {
        EsArticle article = new EsArticle();
        for (CanalEntry.Column column : columns) {
            String name = column.getName();
            String value = column.getValue();
            try {
                switch (name) {
                    case "id": article.setId(Integer.valueOf(value)); break;
                    case "title": article.setTitle(value); break;
                    case "content": article.setContent(value); break;
                    case "summary": article.setSummary(value); break;
                    case "cover_image": article.setCoverImage(value); break;
                    case "author_username": article.setAuthorUsername(value); break;
                    case "author_nickname": article.setAuthorNickName(value); break;
                    case "status": article.setStatus(value); break;
                    case "view_count": article.setViewCount(value != null ? Integer.valueOf(value) : 0); break;
                    case "like_count": article.setLikeCount(value != null ? Integer.valueOf(value) : 0); break;
                    case "comment_count": article.setCommentCount(value != null ? Integer.valueOf(value) : 0); break;
                    case "sticky": article.setSticky(value); break;
                    case "featured": article.setFeatured(value); break;
                    case "del_flag": article.setDelFlag(value); break;
                    case "published_at":
                        if (value != null && !value.isEmpty()) {
                            article.setPublishedAt(LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        }
                        break;
                    case "category_names": article.setCategoryName(value.split(":"));
                }
            } catch (NumberFormatException e) {
                log.error("Failed to parse number for column '{}' with value '{}'", name, value, e);
            }
        }
        // --- 关键：设置关联字段为空，实现分步更新 ---
        article.setArticleTagIds(Collections.emptyList());

        // --- 关键 ---
        return article;
    }
}