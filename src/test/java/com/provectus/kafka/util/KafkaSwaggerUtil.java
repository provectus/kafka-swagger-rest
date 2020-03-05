package com.provectus.kafka.util;

import com.provectus.kafka.container.KafkaTestEnvironmentCluster;
import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import com.provectus.kafka.swagger.KafkaSwagger;

public class KafkaSwaggerUtil {

    public static KafkaSwagger buildSwagger(String groupName, KafkaTestEnvironmentCluster kafkaCluster) {
        KafkaSwaggerConfig kafkaSwaggerConfig = KafkaSwaggerConfig.builder()
                .groupName(groupName)
                .bootstrapServers(kafkaCluster.getKafka().getBootstrapServers())
                .schemaRegistryUrl(kafkaCluster.getSchemaRegistry().getUrl())
                .build();

        KafkaSwagger kafkaSwagger = new KafkaSwagger(kafkaSwaggerConfig);

        return kafkaSwagger;
    }
}
