package com.provectus.kafka.model;

import com.provectus.kafka.model.schema.TopicParamSchema;
import com.provectus.kafka.model.schema.TopicSwaggerSchema;
import org.apache.avro.generic.GenericRecordBuilder;
import org.json.JSONObject;
import org.springframework.data.util.Pair;

public class KafkaRecordBuilder {

    public Pair<Object, Object> build(TopicSwaggerSchema topicSwaggerSchema, JSONObject jsonObj) {
        Object key = getRecord(jsonObj, "key", topicSwaggerSchema.getKeySchema());
        Object value = getRecord(jsonObj, "value", topicSwaggerSchema.getValueSchema());

        return Pair.of(key, value);
    }

    private Object getRecord(JSONObject jsonObj, String paramName, TopicParamSchema keySchema) {
        Object o = jsonObj.get(paramName);
        if (o instanceof JSONObject) {
            GenericRecordBuilder valueBuilder = new GenericRecordBuilder(keySchema.getAvroSchema().getAvroSchema());

            JSONObject param = (JSONObject) o;
            for (String key : param.keySet()) {
                valueBuilder.set(key, param.get(key));
            }

            return valueBuilder.build();
        }

        return o;
    }
}
