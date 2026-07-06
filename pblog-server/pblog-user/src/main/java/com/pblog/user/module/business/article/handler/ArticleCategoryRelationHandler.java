package com.pblog.user.module.business.article.handler;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.pblog.common.listener.CanalRowHandler;
import com.pblog.user.module.business.article.service.EsArticleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ArticleCategoryRelationHandler implements CanalRowHandler {

    @Resource
    private EsArticleService esArticleService;


    @Override
    public String getTargetTable() {
        return "pb_ac_relation";
    }

    @Override
    public void handleBatchInsert(List<CanalEntry.RowData> rowDatasList) {
        log.info("Handling batch INSERT on pb_ac_relation for {} rows.", rowDatasList.size());

        // 收集所有需要添加的 (articleId, categoryId) 对
        Map<Integer, Set<Integer>> additions = new HashMap<>();
        for (CanalEntry.RowData rowData : rowDatasList) {
            Integer articleId = extractIntegerColumnValue(rowData.getAfterColumnsList(), "article_id");
            Integer categoryId = extractIntegerColumnValue(rowData.getAfterColumnsList(), "category_id");

            if (articleId != null && categoryId != null) {
                additions.computeIfAbsent(articleId, k -> new HashSet<>()).add(categoryId);
            }
        }

        // 对每个 articleId，调用 service 层方法来添加标签
        for (Map.Entry<Integer, Set<Integer>> entry : additions.entrySet()) {
            Integer articleId = entry.getKey();
            String[] categoriesToAdd = entry.getValue().stream()
                    .map(String::valueOf)
                    .toArray(String[]::new);

            // 调用 service 层封装好的方法
            esArticleService.updateArticleTags(articleId, categoriesToAdd, true /* isAdd */);
        }
    }

    @Override
    public void handleBatchUpdate(List<CanalEntry.RowData> rowDatasList) {
        // 对于关联表的 UPDATE，处理起来比较复杂，因为需要知道 before 和 after 的具体变化。
        // 一个简单的策略是：忽略它，因为复杂的更新场景较少，可以通过定时任务修复数据。
        // 或者，认为一次 UPDATE 是一次 DELETE + 一次 INSERT，但这会带来额外开销。
        // 这里我们选择忽略，以保持逻辑简单。
        log.warn("Received UPDATE event for pb_ac_relation. This is usually not expected. Skipping.");
    }

    @Override
    public void handleBatchDelete(List<CanalEntry.RowData> rowDatasList) {
        log.info("Handling batch DELETE on pb_ac_relation for {} rows.", rowDatasList.size());

        // 收集所有需要移除的 (articleId, categoryId) 对
        Map<Integer, Set<Integer>> removals = new HashMap<>();
        for (CanalEntry.RowData rowData : rowDatasList) {
            Integer articleId = extractIntegerColumnValue(rowData.getBeforeColumnsList(), "article_id");
            Integer categoryId = extractIntegerColumnValue(rowData.getBeforeColumnsList(), "category_id");

            if (articleId != null && categoryId != null) {
                removals.computeIfAbsent(articleId, k -> new HashSet<>()).add(categoryId);
            }
        }

        // 对每个 articleId，调用 service 层方法来移除标签
        for (Map.Entry<Integer, Set<Integer>> entry : removals.entrySet()) {
            Integer articleId = entry.getKey();
            String[] categoriesToRemove = entry.getValue().stream()
                    .map(String::valueOf)
                    .toArray(String[]::new);

            // 调用 service 层封装好的方法
            esArticleService.updateArticleTags(articleId, categoriesToRemove, false /* isAdd */);
        }
    }

    // 移除私有方法 updateEsArticleTags，因为逻辑已在 service 层
    // private void updateEsArticleTags(Integer articleId, String[] tagIds, boolean isAdd) { ... }

    private Integer extractIntegerColumnValue(List<CanalEntry.Column> columns, String columnName) {
        for (CanalEntry.Column column : columns) {
            if (columnName.equalsIgnoreCase(column.getName())) {
                String value = column.getValue();
                if (value != null && !value.trim().isEmpty()) {
                    try {
                        return Integer.valueOf(value);
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse integer from column '{}' with value '{}'", columnName, value, e);
                    }
                }
            }
        }
        return null;
    }
}