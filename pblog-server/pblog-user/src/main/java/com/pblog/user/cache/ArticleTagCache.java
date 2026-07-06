package com.pblog.user.cache;


import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于缓存文章ID与标签ID列表的映射关系，数据来源为 pb_ac_relation 的 binlog。
 * 该缓存由 Canal 监听器实时更新。
 */
@Component
public class ArticleTagCache {

    private final Map<Integer, List<Integer>> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 可以在这里从数据库加载初始状态，但为了完全依赖 binlog，可以留空。
        // 或者启动时加载一次，后续全靠 binlog 维护。
    }

    /**
     * 当一条关联记录被插入时，更新缓存
     * @param articleId 文章ID
     * @param categoryId 分类ID
     */
    public void addRelation(int articleId, int categoryId) {
        cache.computeIfAbsent(articleId, k -> new ArrayList<>()).add(categoryId);
    }

    /**
     * 当一条关联记录被删除时，更新缓存
     * @param articleId 文章ID
     * @param categoryId 分类ID
     */
    public void removeRelation(int articleId, int categoryId) {
        List<Integer> list = cache.get(articleId);
        if (list != null) {
            list.remove(Integer.valueOf(categoryId));
            if (list.isEmpty()) {
                cache.remove(articleId);
            }
        }
    }

    /**
     * 获取某个文章的所有标签ID
     * @param articleId 文章ID
     * @return 标签ID列表
     */
    public List<Integer> getTagIdsByArticleId(int articleId) {
        List<Integer> ids = cache.get(articleId);
        return ids != null ? new ArrayList<>(ids) : new ArrayList<>();
    }

    /**
     * 清空某个文章的缓存
     * @param articleId 文章ID
     */
    public void invalidate(int articleId) {
        cache.remove(articleId);
    }
}
