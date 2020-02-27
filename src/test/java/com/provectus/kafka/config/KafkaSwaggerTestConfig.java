package com.provectus.kafka.config;

import com.provectus.kafka.container.KafkaTestEnvironmentContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaSwaggerTestConfig {

    @Bean
    KafkaTestEnvironmentContainer kafkaContainerCluster() {
        return new KafkaTestEnvironmentContainer("5.1.0")
                .withKafka()
                .withSchemaRegistry()
                .start();
    }
}