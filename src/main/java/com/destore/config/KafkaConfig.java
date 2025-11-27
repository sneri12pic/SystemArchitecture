package com.destore.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@ConditionalOnProperty(value = "destore.kafka.enabled", havingValue = "true")
public class KafkaConfig {

    @Bean
    public NewTopic stockLowTopic() {
        return new NewTopic("stock-low", 1, (short) 1);
    }
}
