package com.provectus.kafka.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.provectus.kafka.model.schema.TopicParamSchema;
import com.provectus.kafka.model.schema.TopicSwaggerSchema;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

public class KafkaRecordBuilder {

    private final ObjectMapper om = new ObjectMapper();
    private final JsonUnionEnricher enricher = new JsonUnionEnricher(om);

    public Map.Entry<Object, Object> buildKeyValue(TopicSwaggerSchema topicSwaggerSchema, JsonNode jsonNode) throws Exception {
        Object key = getObject(jsonNode, "key", topicSwaggerSchema.getKeySchema());
        Object value = getObject(jsonNode, "value", topicSwaggerSchema.getValueSchema());

        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public Map.Entry<Object, Object> buildAutofillKeyValue(TopicSwaggerSchema topicSwaggerSchema, JsonNode jsonNode) throws Exception {
        String autofillKeyParam = topicSwaggerSchema.getAutofillTopicConfig().getAutofillKeyParamName();

        Object key = getObject(jsonNode, autofillKeyParam, topicSwaggerSchema.getKeySchema());
        Object value = getObject(jsonNode, Optional.empty(), topicSwaggerSchema.getValueSchema());

        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public Object buildValue(TopicSwaggerSchema topicSwaggerSchema, JsonNode jsonNode) throws Exception {
        return getObject(jsonNode, Optional.empty(), topicSwaggerSchema.getValueSchema());
    }

    private Object getObject(JsonNode jsonNode, String paramName, TopicParamSchema schema) throws Exception {
        return getObject(jsonNode, Optional.of(paramName), schema);
    }

    private Object getObject(JsonNode jsonNode, Optional<String> paramName, TopicParamSchema schema) throws Exception {
        JsonNode node = paramName.map(jsonNode::get).orElse(jsonNode);

        switch (schema.getType()) {
            case STRING:
                return node.asText();
            case AVRO:
                return parseJson(node, schema.getAvroSchema().getAvroSchema());
        }

        return null;
    }

    private Object parseJson(JsonNode json, Schema schema) throws Exception {
        final JsonNode enriched = enricher.enrich(json, schema);
        InputStream input = new ByteArrayInputStream(enriched.toString().getBytes());
        DataInputStream din = new DataInputStream(input);

        DatumReader reader = new GenericDatumReader(schema);
        Decoder decoder = DecoderFactory.get().jsonDecoder(schema, din);
        return reader.read(null, decoder);
    }
}
