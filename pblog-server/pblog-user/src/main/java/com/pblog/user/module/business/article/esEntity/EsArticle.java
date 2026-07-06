package com.pblog.user.module.business.article.esEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 博客文章 ES 实体
 */
@Document(
        indexName = "pb_article",
        createIndex = true
)
@Setting(settingPath = "elasticsearch/settings.json")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsArticle implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id
    private Integer id;

    /**
     * 标题
     */
    @Field(
            type = FieldType.Text,
            analyzer = "ik_max_word",
            searchAnalyzer = "ik_smart"
    )
    private String title;

    /**
     * 正文
     */
    @Field(
            type = FieldType.Text,
            analyzer = "ik_max_word",
            searchAnalyzer = "ik_smart"
    )
    private String content;

    /**
     * 摘要
     */
    @Field(
            type = FieldType.Text,
            analyzer = "ik_max_word",
            searchAnalyzer = "ik_smart"
    )
    private String summary;

    /**
     * 封面
     */
    @Field(type = FieldType.Keyword)
    private String coverImage;

    /**
     * 作者用户名
     */
    @Field(type = FieldType.Keyword)
    private String authorUsername;

    /**
     * 作者昵称
     */
    @Field(
            type = FieldType.Text,
            analyzer = "ik_max_word"
    )
    private String authorNickName;

    /**
     * 状态
     */
    @Field(type = FieldType.Keyword)
    private String status;

    /**
     * 浏览量
     */
    @Field(type = FieldType.Integer)
    private Integer viewCount;

    /**
     * 点赞数
     */
    @Field(type = FieldType.Integer)
    private Integer likeCount;

    /**
     * 评论数
     */
    @Field(type = FieldType.Integer)
    private Integer commentCount;

    /**
     * 置顶
     */
    @Field(type = FieldType.Keyword)
    private String sticky;

    /**
     * 推荐
     */
    @Field(type = FieldType.Keyword)
    private String featured;

    /**
     * 发布时间
     */
    @Field(type = FieldType.Date)
    private LocalDateTime publishedAt;

    /**
     * 删除标志
     */
    @Field(type = FieldType.Keyword)
    private String delFlag;

    /**
     * 文章标签ID
     */
    @Field(type = FieldType.Keyword)
    private String[] articleTagIds;

    @Field(type = FieldType.Keyword)
    private String[] CategoryName;

    /**
     * List -> String[]
     */
    public void setArticleTagIds(List<Integer> tagIdList) {

        if (tagIdList == null || tagIdList.isEmpty()) {
            this.articleTagIds = new String[0];
            return;
        }

        this.articleTagIds = tagIdList.stream()
                .map(String::valueOf)
                .toArray(String[]::new);
    }

    /**
     * String[] -> List<Integer>
     */
    public List<Integer> getArticleTagIdsAsList() {

        if (articleTagIds == null || articleTagIds.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(articleTagIds)
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }
}