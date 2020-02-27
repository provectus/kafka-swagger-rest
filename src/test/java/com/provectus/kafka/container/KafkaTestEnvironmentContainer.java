package com.provectus.kafka.container;

import lombok.Getter;
import org.assertj.core.api.Assertions;

@Getter
public class KafkaTestEnvironmentContainer {

    private KafkaContainer kafka;
    private SchemaRegistryContainer schemaRegistry;

    private String imageVersion;

    public KafkaTestEnvironmentContainer(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    public KafkaTestEnvironmentContainer withKafka() {
        kafka = new KafkaContainer(imageVersion);

        return this;
    }

    public KafkaTestEnvironmentContainer withSchemaRegistry() {
        Assertions.assertThat(kafka).isNotNull();

        schemaRegistry = new SchemaRegistryContainer(imageVersion, kafka);

        return this;
    }

    public KafkaTestEnvironmentContainer start() {
        if (kafka != null) kafka.start();
        if (schemaRegistry != null) schemaRegistry.start();

        return this;
    }
}
