package com.example._0260811.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public final static String TOPIC20260930 = "topic20260930";

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(TOPIC20260930)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
