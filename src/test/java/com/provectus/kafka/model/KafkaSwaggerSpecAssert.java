package com.provectus.kafka.model;

import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import io.swagger.models.Swagger;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaSwaggerSpecAssert {

    public static Class<KafkaSwaggerSpecAssert> assertSpec(Swagger kafkaSwagger, KafkaSwaggerConfig kafkaSwaggerConfig) {
        assertThat(kafkaSwagger.getInfo().getTitle()).isEqualTo("/kafka/" + kafkaSwaggerConfig.getGroupName() + " kafka swagger");

        assertThat(kafkaSwagger.getTags().size()).isEqualTo(1);
        assertThat(kafkaSwagger.getTags().get(0).getName()).isEqualTo("/kafka/" + kafkaSwaggerConfig.getGroupName());

        return KafkaSwaggerSpecAssert.class;
    }

    public static Class<KafkaSwaggerSpecAssert> assertTopicsEndpoints(Swagger kafkaSwagger, Set<String> topics) {
        topics.stream()
                .forEach(topic -> assertTopicEndpoints(kafkaSwagger, topic));

        return KafkaSwaggerSpecAssert.class;
    }

    public static Class<KafkaSwaggerSpecAssert> assertTopicEndpoints(Swagger kafkaSwagger, String topic) {
        String topicsPath = kafkaSwagger.getTags().get(0).getName() + "/topics";

        assertThat(kafkaSwagger.getPaths()).containsKey(topicsPath + "/" + topic);
        assertThat(kafkaSwagger.getPaths()).containsKey(topicsPath + "/kv/" + topic);

        return KafkaSwaggerSpecAssert.class;
    }
}
