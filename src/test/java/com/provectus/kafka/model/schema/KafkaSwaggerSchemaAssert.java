package com.provectus.kafka.model.schema;

import io.swagger.models.Swagger;
import org.assertj.core.api.Assertions;

public class KafkaSwaggerSchemaAssert {

    public static Class<KafkaSwaggerSchemaAssert> assertKeyValueSchema(KafkaSwaggerSchema kafkaSwaggerSchema, String topic, TopicParamSchemaType keyType, TopicParamSchemaType valueType) {
        TopicSwaggerSchema topicSwaggerSchema = kafkaSwaggerSchema.getTopics().get(topic);

        Assertions.assertThat(topicSwaggerSchema.getKeySchema().getType()).isEqualTo(keyType);
        Assertions.assertThat(topicSwaggerSchema.getValueSchema().getType()).isEqualTo(valueType);

        return KafkaSwaggerSchemaAssert.class;
    }

    public static Class<KafkaSwaggerSchemaAssert> assertValueSchema(KafkaSwaggerSchema kafkaSwaggerSchema, String topic, TopicParamSchemaType valueType) {
        TopicSwaggerSchema topicSwaggerSchema = kafkaSwaggerSchema.getTopics().get(topic);

        Assertions.assertThat(topicSwaggerSchema.getValueSchema().getType()).isEqualTo(valueType);

        return KafkaSwaggerSchemaAssert.class;
    }

    public static Class<KafkaSwaggerSchemaAssert> assertValueSchema(KafkaSwaggerSchema kafkaSwaggerSchema, String topic, TopicParamSchemaType valueType, String schema) {
        TopicSwaggerSchema topicSwaggerSchema = kafkaSwaggerSchema.getTopics().get(topic);

        Assertions.assertThat(topicSwaggerSchema.getValueSchema().getType()).isEqualTo(valueType);

        return KafkaSwaggerSchemaAssert.class;
    }

}
