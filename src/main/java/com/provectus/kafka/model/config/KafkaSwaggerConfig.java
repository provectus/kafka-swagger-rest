package com.provectus.kafka.model.config;

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
    private String kafkaSchemaRegistryUrl;
    private Set<String> ignoreTopics = new HashSet<>();

    public KafkaSchemaRegistryConfig schemaRegistryConfig() {
        return KafkaSchemaRegistryConfig.builder()
                .url(kafkaSchemaRegistryUrl)
                .build();
    }
}
