package com.provectus.kafka.schemaregistry;

import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class KafkaSchemaRegistryConfig {

    private String url;
}
