package com.provectus.kafka.swagger;

import com.provectus.kafka.container.KafkaTestEnvironmentContainer;
import com.provectus.kafka.model.Schemas;
import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import com.provectus.kafka.model.KafkaSwaggerSpecAssert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class KafkaSwaggerTest2 {

    public static KafkaTestEnvironmentContainer kafkaEnvironmentContainer;

    private static final Set TOPICS = Set.of("users", "projects", "topic-n");

    @Before
    public void before() throws IOException {
        kafkaEnvironmentContainer = new KafkaTestEnvironmentContainer("5.1.0")
                .withKafka()
                .withSchemaRegistry()
                .start();

        kafkaEnvironmentContainer.getKafka().createTopics(TOPICS);
        kafkaEnvironmentContainer.getSchemaRegistry().sendSchema("users-value", Schemas.USER_SCHEMA);
    }

    @Test
    public void testSwagger() {
        KafkaSwaggerConfig kafkaSwaggerConfig = KafkaSwaggerConfig.builder()
                .groupName("test-swagger")
                .bootstrapServers(kafkaEnvironmentContainer.getKafka().getBootstrapServers())
                .schemaRegistryUrl(kafkaEnvironmentContainer.getSchemaRegistry().getUrl())
                .build();

        KafkaSwagger kafkaSwagger = new KafkaSwagger(kafkaSwaggerConfig);

        KafkaSwaggerSpecAssert.assertSpec(kafkaSwagger.getSwagger(), kafkaSwaggerConfig);
        KafkaSwaggerSpecAssert.assertTopicsEndpoints(kafkaSwagger.getSwagger(), TOPICS);
        // TODO: Assert Endpoint Model
    }
}
