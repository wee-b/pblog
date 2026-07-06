package com.pblog;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring Boot 环境下的 RocketMQ 发送测试
 */
@SpringBootTest
// 如需排除Security自动配置，可添加：
// @SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration")
public class RocketMQSpringSendTest {
    private static final Logger log = LoggerFactory.getLogger(RocketMQSpringSendTest.class);

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Test
    public void testSyncSendWithTemplate() {
        try {
            // 构建消息体
            String messageContent = "{\"operateType\":\"1\",\"targetId\":3,\"targetType\":\"2\",\"username\":\"1234567890\"}";
            // 构建Spring Message对象
            Message<String> message = MessageBuilder.withPayload(messageContent)
                    .setHeader(RocketMQHeaders.KEYS, "like:unique:1234567890:2:3")
                    .setHeader("DELAY", "2")
                    .build();

            // 同步发送，指定超时时间5秒
            long start = System.currentTimeMillis();
            org.apache.rocketmq.client.producer.SendResult sendResult =
                    rocketMQTemplate.syncSend("like_topic", message, 5000);
            long cost = System.currentTimeMillis() - start;

            log.info("Spring模板同步发送成功！耗时：{}ms", cost);
            log.info("消息ID：{}", sendResult.getMsgId());
        } catch (Exception e) {
            log.error("Spring模板发送失败！", e);
            assert false : "模板发送失败：" + e.getMessage();
        }
    }
}
