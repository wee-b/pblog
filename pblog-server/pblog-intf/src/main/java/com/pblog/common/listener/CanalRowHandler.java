package com.pblog.common.listener;


import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;

public interface CanalRowHandler {

    /**
     * 返回处理器关注的目标表名
     * @return 表名
     */
    String getTargetTable();

    /**
     * 批量处理 INSERT 事件
     * @param rowDatasList 变更的行数据列表
     */
    void handleBatchInsert(List<CanalEntry.RowData> rowDatasList);

    /**
     * 批量处理 UPDATE 事件
     * @param rowDatasList 变更的行数据列表
     */
    void handleBatchUpdate(List<CanalEntry.RowData> rowDatasList);

    /**
     * 批量处理 DELETE 事件
     * @param rowDatasList 变更的行数据列表
     */
    void handleBatchDelete(List<CanalEntry.RowData> rowDatasList);


}
