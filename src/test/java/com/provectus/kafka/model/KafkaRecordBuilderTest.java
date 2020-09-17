package com.provectus.kafka.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.provectus.kafka.model.schema.TopicParamSchema;
import com.provectus.kafka.model.schema.TopicParamSchemaType;
import com.provectus.kafka.model.schema.TopicSwaggerSchema;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

class KafkaRecordBuilderTest {

    private final ObjectMapper om = new ObjectMapper();

    @Test
    public void simpleSchemaTest() throws Exception {
        Schema valueRaw = SchemaBuilder
                .record("Employee").namespace("org.apache.avro.ipc")
                        .fields()
                        .name("name").type().stringType().noDefault()
                        .name("age").type().intType().noDefault()
                        .name("emails").type().array().items().stringType().noDefault()
                        .name("boss").type().unionOf().nullType().and().type("Employee").endUnion().nullDefault()
                        .endRecord();

        AvroSchema valueSchema = new AvroSchema(valueRaw);
        TopicParamSchema valueTopicParamSchema = new TopicParamSchema(TopicParamSchemaType.AVRO, valueSchema);
        TopicParamSchema keyTopicParamSchema = new TopicParamSchema(TopicParamSchemaType.STRING, null);

        TopicSwaggerSchema swaggerSchema = new TopicSwaggerSchema("test", keyTopicParamSchema, valueTopicParamSchema, null);

        ObjectNode kv = om.createObjectNode();
        kv.put("key", UUID.randomUUID().toString());
        ObjectNode valueNode = kv.putObject("value");
        valueNode.put("name", UUID.randomUUID().toString());
        valueNode.put("age", ThreadLocalRandom.current().nextInt());
        valueNode.putNull("boss");
        ArrayNode emails = valueNode.putArray("emails");
        emails.add("info@test.com");

        Map.Entry<Object, Object> build = new KafkaRecordBuilder().buildKeyValue(swaggerSchema, kv);

        assertEquals(kv.get("key").asText(), build.getKey());
    }

    @Test
    public void testComplexUnion() throws Exception {
        Schema valueRaw = SchemaBuilder
            .record("Employee").namespace("org.apache.avro.ipc")
            .fields()
            .name("name").type().stringType().noDefault()
            .name("age").type().intType().noDefault()
            .name("emails").type().array().items().stringType().noDefault()
            .name("boss").type().unionOf().nullType().and().type("Employee").endUnion().nullDefault()
            .endRecord();

        AvroSchema valueSchema = new AvroSchema(valueRaw);
        TopicParamSchema valueTopicParamSchema = new TopicParamSchema(TopicParamSchemaType.AVRO, valueSchema);
        TopicParamSchema keyTopicParamSchema = new TopicParamSchema(TopicParamSchemaType.STRING, null);

        TopicSwaggerSchema swaggerSchema = new TopicSwaggerSchema("test", keyTopicParamSchema, valueTopicParamSchema, null);

        ObjectNode kv = om.createObjectNode();
        kv.put("key", UUID.randomUUID().toString());
        ObjectNode valueNode = kv.putObject("value");
        valueNode.put("name", UUID.randomUUID().toString());
        valueNode.put("age", ThreadLocalRandom.current().nextInt());
        ArrayNode emails = valueNode.putArray("emails");
        emails.add("info@test.com");
        final ObjectNode bossNode = valueNode.deepCopy();
        //bossNode.putNull("boss");
        valueNode.set("boss", bossNode);

        Map.Entry<Object, Object> build = new KafkaRecordBuilder().buildKeyValue(swaggerSchema, kv);

        assertEquals(kv.get("key").asText(), build.getKey());
    }

    @Test
    public void testUnion() throws Exception {
        Schema valueRaw = SchemaBuilder
            .record("Employee").namespace("org.apache.avro.ipc")
            .fields()
            .name("name").type().unionOf().nullType().and().type("string").endUnion().nullDefault()
            .endRecord();
        AvroSchema valueSchema = new AvroSchema(valueRaw);
        TopicParamSchema valueTopicParamSchema = new TopicParamSchema(TopicParamSchemaType.AVRO, valueSchema);
        TopicParamSchema keyTopicParamSchema = new TopicParamSchema(TopicParamSchemaType.STRING, null);
        TopicSwaggerSchema swaggerSchema = new TopicSwaggerSchema("test", keyTopicParamSchema, valueTopicParamSchema, null);

        ObjectNode kv = om.createObjectNode();
        kv.put("key", UUID.randomUUID().toString());
        ObjectNode valueNode = kv.putObject("value");
        valueNode.put("name", UUID.randomUUID().toString());

        Map.Entry<Object, Object> build = new KafkaRecordBuilder().buildKeyValue(swaggerSchema, kv);

        assertEquals(kv.get("key").asText(), build.getKey());
        assertTrue(build.getValue() instanceof GenericRecord);
        final GenericRecord value = (GenericRecord) build.getValue();
        assertTrue(value.get("name") instanceof CharSequence);

    }
}