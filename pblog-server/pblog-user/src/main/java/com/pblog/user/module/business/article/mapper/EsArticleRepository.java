package com.pblog.user.module.business.article.mapper;

import com.pblog.user.module.business.article.esEntity.EsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface EsArticleRepository extends ElasticsearchRepository<EsArticle, Integer> {

    /**
     * 文章搜索
     *
     * keyword：全文搜索关键词（可为空）
     * categoryIds：分类/标签ID集合（可为空）
     */
    @Query("""
    {
      "bool": {
        "must": [
          {
            "multi_match": {
              "query": "?0",
              "fields": ["title^3", "summary^2", "content"]
            }
          }
        ],
        "filter": [
          {
            "terms": {
              "articleTagIds": ?1
            }
          },
          {
            "term": {
              "delFlag": "0"
            }
          }
        ]
      }
    }
    """)
    Page<EsArticle> search(String keyword, Collection<String> categoryIds, Pageable pageable);

}