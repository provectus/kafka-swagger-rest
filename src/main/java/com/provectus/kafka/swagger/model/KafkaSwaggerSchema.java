package com.provectus.kafka.swagger.model;

import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.provectus.kafka.schemaregistry.model.Schema;
import com.provectus.kafka.swagger.model.topic.TopicParamSchema;
import com.provectus.kafka.swagger.model.topic.TopicParamSchemaType;
import com.provectus.kafka.swagger.model.topic.TopicSwaggerSchema;
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

    public void addDefaultTopicSchema(String topic) {
        TopicSwaggerSchema topicSwaggerSchema = TopicSwaggerSchema.builder()
                .topic(topic)
                .keySchema(new TopicParamSchema(TopicParamSchemaType.STRING, null))
                .valueSchema(new TopicParamSchema(TopicParamSchemaType.STRING, null))
                .build();

        this.topics.put(topic, topicSwaggerSchema);
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
