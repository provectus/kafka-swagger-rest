package com.provectus.kafka.model.schema;

import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.provectus.kafka.model.config.TopicConfig;
import com.provectus.kafka.schemaregistry.model.Schema;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class KafkaSwaggerSchema {

    private Map<String, TopicSwaggerSchema> topics = new HashMap<>();

    public TopicSwaggerSchema addDefaultTopicSchema(String topic, TopicConfig autofillTopicConfig) {
        TopicSwaggerSchema topicSwaggerSchema = TopicSwaggerSchema.builder()
                .topic(topic)
                .keySchema(new TopicParamSchema(TopicParamSchemaType.STRING, null))
                .valueSchema(new TopicParamSchema(TopicParamSchemaType.STRING, null))
                .autofillTopicConfig(autofillTopicConfig)
                .build();

        this.topics.put(topic, topicSwaggerSchema);

        return topicSwaggerSchema;
    }

    public TopicSwaggerSchema addDefaultTopicSchema(String topic) {
        return addDefaultTopicSchema(topic, null);
    }

    public boolean updateKeySchema(String topic, AvroSchema keySchema) {
        TopicSwaggerSchema topicSwaggerSchema = topics.get(topic);

        return topicSwaggerSchema.updateKeySchema(keySchema);
    }

    public boolean updateValueSchema(String topic, AvroSchema keySchema) {
        TopicSwaggerSchema topicSwaggerSchema = topics.get(topic);

        return topicSwaggerSchema.updateValueSchema(keySchema);
    }

    public boolean updateSchema(String topic, Schema schema) {
        if (schema.isKeySchema()) {
            return updateKeySchema(schema.getTopic(), schema.getAvroSchema());
        }
        if (schema.isValueSchema()) {
            return updateValueSchema(schema.getTopic(), schema.getAvroSchema());
        }

        //TODO: unknown schema
        return false;
    }
}
