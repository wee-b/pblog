package com.pblog.common.listener;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CanalListener {

    @Autowired
    private CanalConnector connector;

    @Value("${spring.canal.filter}")
    private String filter;

    @Value("${spring.canal.batch-size}")
    private int batchSize;

    @Value("${spring.canal.retry-interval}")
    private int retryInterval;

    @Autowired // 注入所有实现 CanalRowHandler 接口的 Bean
    private List<CanalRowHandler> rowHandlers;

    @PostConstruct
    public void start() {
        Thread thread = new Thread(this::process);
        thread.setName("canal-listener");
        thread.setDaemon(true);
        thread.start();
    }

    private void process() {
        while (true) {
            try {
                log.info("Connecting to Canal server...");
                connector.connect();
                connector.subscribe(filter);
                connector.rollback();
                log.info("Canal listener started, filter: {}", filter);

                while (true) {
                    Message message = connector.getWithoutAck(batchSize);
                    List<CanalEntry.Entry> entries = message.getEntries();

                    if (message.getId() == -1 || entries.isEmpty()) {
                        Thread.sleep(1000);
                        continue;
                    }

                    for (CanalEntry.Entry entry : entries) {
                        if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) continue;

                        String tableName = entry.getHeader().getTableName();
                        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

                        // 分发事件给对应的处理器
                        dispatchEvent(tableName, rowChange);
                    }

                    log.info("Receive binlog size={}", entries.size());
                    connector.ack(message.getId());
                }

            } catch (Exception e) {
                log.error("Canal listener error", e);
                try {
                    connector.disconnect();
                    Thread.sleep(retryInterval);
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * 将事件分发给对应的处理器
     */
    private void dispatchEvent(String tableName, CanalEntry.RowChange rowChange) {
        CanalEntry.EventType eventType = rowChange.getEventType();

        // 遍历所有处理器，找到处理对应表的处理器
        for (CanalRowHandler handler : rowHandlers) {
            if (handler.getTargetTable().equalsIgnoreCase(tableName)) {
                // 直接将所有行数据作为一个列表传递
                switch (eventType) {
                    case INSERT -> handler.handleBatchInsert(rowChange.getRowDatasList());
                    case UPDATE -> handler.handleBatchUpdate(rowChange.getRowDatasList());
                    case DELETE -> handler.handleBatchDelete(rowChange.getRowDatasList());
                }
                break; // 找到并处理后退出循环
            }
        }
    }
}