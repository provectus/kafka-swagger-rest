package com.provectus.kafka.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.provectus.kafka.model.KafkaSwaggerBuilder;
import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryRestClient;
import com.provectus.kafka.schemaregistry.impl.KafkaSchemaRegistryRestClientImpl;
import com.provectus.kafka.schemaregistry.model.Schema;
import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import com.provectus.kafka.model.schema.KafkaSwaggerSchema;
import com.provectus.kafka.model.schema.TopicSwaggerSchema;
import io.swagger.models.Swagger;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KafkaSwagger {

    private final KafkaSchemaRegistryRestClient kafkaSchemaRegistryRestClient;

    private final KafkaSwaggerConfig config;

    private KafkaSwaggerClient kafkaSwaggerClient;
    private KafkaSwaggerSchema kafkaSwaggerSchema;

    private Swagger swagger;
    private String swaggerYaml;

    public KafkaSwagger(KafkaSwaggerConfig config) {
        kafkaSchemaRegistryRestClient = new KafkaSchemaRegistryRestClientImpl(config.schemaRegistryConfig());

        this.config = config;
    }

    public void init() {
        kafkaSwaggerClient = new KafkaSwaggerClient(config);
        kafkaSwaggerClient.subscribe(this::syncSchema);

        initKafkaSchema();

        kafkaSwaggerClient.start();
    }

    public void stop() {
        kafkaSwaggerClient.stop();
    }

    private void initKafkaSchema() {
        kafkaSwaggerSchema = new KafkaSwaggerSchema();

        List<String> topics = kafkaSwaggerClient.getTopics().stream()
                .filter(topicName -> !topicName.startsWith("_"))
                .collect(Collectors.toList());

        topics.stream()
                .filter(topic -> !config.getIgnoreTopics().contains(topic))
                .forEach(kafkaSwaggerSchema::addDefaultTopicSchema);

        initTopicsSchemas(topics);
        rebuildSwaggerDocumentation();
    }

    private void initTopicsSchemas(List<String> topics) {
        Set<String> subjects = new HashSet<>(kafkaSchemaRegistryRestClient.getSubjects());

        for (String topic : topics) {
            if (subjects.contains(topic + "-key")) {
                Schema keySchema = kafkaSchemaRegistryRestClient.getSubjectLatestSchema(topic + "-key");
                kafkaSwaggerSchema.updateKeySchema(topic, keySchema.getAvroSchema());
            }
            if (subjects.contains(topic + "-value")) {
                Schema valueSchema = kafkaSchemaRegistryRestClient.getSubjectLatestSchema(topic + "-value");
                kafkaSwaggerSchema.updateValueSchema(topic, valueSchema.getAvroSchema());
            }
        }
    }

    private void syncSchema(Schema schema) {
        String topic = schema.getTopic();

        if (topic == null) {
            log.warn("found unknown topic schema: {}", schema);
            return;
        }

        log.debug("found new schema: {}", schema);

        TopicSwaggerSchema topicSwaggerSchema = kafkaSwaggerSchema.getTopics().get(topic);

        if (topicSwaggerSchema == null) {
            kafkaSwaggerSchema.addDefaultTopicSchema(topic);
        }

        kafkaSwaggerSchema.updateSchema(topic, schema);

        rebuildSwaggerDocumentation();
    }

    private void rebuildSwaggerDocumentation() {
        swagger = new KafkaSwaggerBuilder()
                .build(config, kafkaSwaggerSchema);

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.findAndRegisterModules();
        try {
            swaggerYaml = mapper.writeValueAsString(swagger);
        } catch (JsonProcessingException e) {
            log.error("Unable to buildKeyValue swaggerSpec: " + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public KafkaSwaggerConfig getConfig() {
        return config;
    }

    public KafkaSwaggerSchema getKafkaSwaggerSchema() {
        return kafkaSwaggerSchema;
    }

    public Swagger getSwagger() {
        return swagger;
    }

    public String getSwaggerYaml() {
        return swaggerYaml;
    }
}