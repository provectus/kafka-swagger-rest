package com.provectus.kafka.container;

import lombok.Getter;
import org.assertj.core.api.Assertions;

@Getter
public class KafkaTestEnvironmentCluster {

    private KafkaContainer kafka;
    private SchemaRegistryContainer schemaRegistry;

    private String imageVersion;

    public KafkaTestEnvironmentCluster(String imageVersion) {
        this.imageVersion = imageVersion;
    }

    public KafkaTestEnvironmentCluster withKafka() {
        kafka = new KafkaContainer(imageVersion);

        return this;
    }

    public KafkaTestEnvironmentCluster withSchemaRegistry() {
        Assertions.assertThat(kafka).isNotNull();

        schemaRegistry = new SchemaRegistryContainer(imageVersion, kafka);

        return this;
    }

    public KafkaTestEnvironmentCluster start() {
        if (kafka != null) kafka.start();
        if (schemaRegistry != null) schemaRegistry.start();

        return this;
    }
}
