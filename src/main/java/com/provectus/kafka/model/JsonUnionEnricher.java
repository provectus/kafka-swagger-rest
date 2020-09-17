package com.provectus.kafka.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.List;
import org.apache.avro.Schema;

public class JsonUnionEnricher {

  private final ObjectMapper mapper;

  public JsonUnionEnricher(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public JsonNode enrich(JsonNode in, Schema schema) {
    if (in.isObject()) {
      ObjectNode node = in.deepCopy();
      for (Schema.Field field : schema.getFields()) {
        final Schema fieldSchema = field.schema();
        final JsonNode value = node.get(field.name());

        if (fieldSchema.isUnion() && fieldSchema.isNullable() &&
            fieldSchema.getTypes().size() == 2) {
          if (value != null && !value.isNull()) {

            final Schema dataSchema = fieldSchema.getTypes().stream()
                .filter(t -> !t.isNullable())
                .findFirst().get();

            List<Schema.Type> skipped = Arrays.asList(
                Schema.Type.RECORD,
                Schema.Type.MAP,
                Schema.Type.ARRAY,
                Schema.Type.UNION,
                Schema.Type.NULL,
                Schema.Type.ENUM
            );

            if (!skipped.contains(dataSchema.getType())) {
              setObject(node, field.name(), dataSchema.getType().name().toLowerCase(), value);
            }

            if (dataSchema.getType().equals(Schema.Type.RECORD)) {
              setObject(node, field.name(), dataSchema.getFullName(),
                  this.enrich(value, dataSchema));
            }
          } else if (value == null) {
            node.putNull(field.name());
          }
        } else if (fieldSchema.getType().equals(Schema.Type.RECORD)) {
          node.set(field.name(), this.enrich(value, fieldSchema));
        }
      }
      return node;
    } else {
      return in;
    }
  }

  private ObjectNode setObject(ObjectNode source, String fieldName, String type, JsonNode value) {
    final ObjectNode objectNode = mapper.createObjectNode();
    objectNode.set(type, value);
    source.set(fieldName, objectNode);
    return source;
  }
}
