package com.provectus.kafka.swagger.model;

import com.provectus.kafka.kafkarest.KafkaRestConfig;
import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryConfig;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class KafkaSwaggerConfig {

    private String groupName;
    private String kafkaUrl;
    private String kafkaRestUrl;
    private String kafkaSchemaRegistryUrl;
    private Set<String> ignoreTopics = new HashSet<>();

    public KafkaRestConfig restClientConfig() {
        return KafkaRestConfig.builder()
                .url(kafkaRestUrl)
                .build();
    }

    public KafkaSchemaRegistryConfig schemaRegistryConfig() {
        return KafkaSchemaRegistryConfig.builder()
                .url(kafkaSchemaRegistryUrl)
                .build();
    }
}
