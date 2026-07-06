package com.pblog.common.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class CanalConnectorConfig {

    @Value("${spring.canal.server}")
    private String server;

    @Value("${spring.canal.destination}")
    private String destination;

    @Bean
    public CanalConnector canalConnector() {

        String[] arr = server.split(":");

        return CanalConnectors.newSingleConnector(
                new InetSocketAddress(arr[0], Integer.parseInt(arr[1])),
                destination,
                "",
                ""
        );
    }
}