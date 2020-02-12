package com.provectus.kafka.model.schema;

import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.provectus.kafka.model.config.TopicConfig;
import lombok.*;
import org.apache.avro.Schema;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TopicSwaggerSchema {

    private String topic;
    private TopicParamSchema keySchema;
    private TopicParamSchema valueSchema;

    private TopicConfig autofillTopicConfig;

    public boolean updateKeySchema(AvroSchema schema) {
        return updateSchema(keySchema, schema);
    }

    public boolean updateValueSchema(AvroSchema schema) {
        return updateSchema(valueSchema, schema);
    }

    private boolean updateSchema(TopicParamSchema paramSchema, AvroSchema schema) {
        if (paramSchema.getType() == TopicParamSchemaType.STRING) {
            paramSchema.setType(TopicParamSchemaType.AVRO);
            paramSchema.setAvroSchema(schema);
            return true;
        }

        paramSchema.setAvroSchema(schema);
        return true;
    }

    public Schema getValueAvroSchema() {
        return valueSchema.getAvroSchema().getAvroSchema();
    }

    public Schema getKeyAvroSchema() {
        return keySchema.getAvroSchema().getAvroSchema();
    }
}
