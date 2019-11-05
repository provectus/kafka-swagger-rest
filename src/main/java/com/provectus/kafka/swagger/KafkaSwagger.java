package com.provectus.kafka.swagger;

import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.provectus.kafka.kafkarest.KafkarestRestClient;
import com.provectus.kafka.kafkarest.impl.KafkaRestClientImpl;
import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryRestClient;
import com.provectus.kafka.schemaregistry.SchemaRegistryListener;
import com.provectus.kafka.schemaregistry.impl.KafkaSchemaRegistryRestClientImpl;
import com.provectus.kafka.schemaregistry.model.Schema;
import com.provectus.kafka.swagger.model.KafkaSwaggerConfig;
import com.provectus.kafka.swagger.model.KafkaSwaggerSchema;
import com.provectus.kafka.swagger.model.topic.TopicSwaggerSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import springfox.documentation.builders.ApiListingBuilder;
import springfox.documentation.builders.DocumentationBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.Model;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.Operation;
import springfox.documentation.service.Parameter;
import springfox.documentation.spring.web.DocumentationCache;

import javax.annotation.Nullable;
import java.util.*;

@Slf4j
public class KafkaSwagger {

    private final KafkarestRestClient kafkarestRestClient;
    private final KafkaSchemaRegistryRestClient kafkaSchemaRegistryRestClient;
    private final DocumentationCache documentationCache;

    private final KafkaSwaggerConfig config;

    private KafkaSwaggerClient kafkaSwaggerClient;
    private KafkaSwaggerSchema kafkaSwaggerSchema;

    public KafkaSwagger(KafkaSwaggerConfig config, DocumentationCache documentationCache) {
        kafkarestRestClient = new KafkaRestClientImpl(config.restClientConfig());
        kafkaSchemaRegistryRestClient = new KafkaSchemaRegistryRestClientImpl(config.schemaRegistryConfig());

        this.config = config;
        this.documentationCache = documentationCache;
    }

    public void init() {
        initKafkaSwagger();

        kafkaSwaggerClient = new KafkaSwaggerClient(config);
        kafkaSwaggerClient.subscribe(schema -> syncSchema(schema));
        kafkaSwaggerClient.start();
    }

    public void stop() {
        kafkaSwaggerClient.stop();
    }

    public KafkaSwaggerSchema getKafkaSwaggerSchema() {
        return kafkaSwaggerSchema;
    }

    private void initKafkaSwagger() {
        kafkaSwaggerSchema = new KafkaSwaggerSchema();

        List<String> topics = kafkarestRestClient.getTopics();

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
        AvroSchema avroSchema = schema.getAvroSchema();
        org.apache.avro.Schema schema_ = avroSchema.getAvroSchema();

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
        Multimap<String, ApiListing> apiListings = LinkedListMultimap.create();

        List<Operation> operations = new ArrayList<>();

        for (TopicSwaggerSchema topicSwaggerSchema : kafkaSwaggerSchema.getTopics().values()) {
            List<Parameter> parameters = new ArrayList<>();
            Map<String, Model> models = new LinkedHashMap<>();

            String apiPath = "/kafka/" + config.getGroupName() + "/topics/" + topicSwaggerSchema.getTopic();
            String apiModelName = topicSwaggerSchema.getTopic() + "model";

            Parameter bodyParameter = new ParameterBuilder().name("message")
                    .description("kafka message")
                    .modelRef(new ModelRef(apiModelName, null, null, false))
                    .parameterType("body")
                    .order(Integer.MAX_VALUE)
                    .hidden(false)
                    .build();
            parameters.add(bodyParameter);

            Operation operation = new Operation(HttpMethod.POST, "Send message to " + topicSwaggerSchema.getTopic(),
                    null, null, UUID.randomUUID().toString(), 0,
                    new HashSet<>(Arrays.asList(apiPath)),
                    new HashSet<>(Arrays.asList("*/*")),
                    new HashSet<>(Arrays.asList("application/json")),
                    new HashSet<>(),
                    new ArrayList<>(),
                    parameters,
                    new HashSet<>(),
                    "false",
                    false,
                    new ArrayList<>());
            operations.add(operation);

            Map<String, String> values = new HashMap<>();
            values.put("key", "1");
            values.put("value", "value");

            models.put(apiModelName, new Model(
                    apiModelName,
                    apiModelName,
                    new TypeResolver().resolve(Object.class),
                    null,
                    new HashMap<>(),
                    "",
                    "",
                    "",
                    new ArrayList<>(),
                    values,
                    null
            ));

            ApiDescription apiDescription = new ApiDescription(topicSwaggerSchema.getTopic(), apiPath, "Send message to " + topicSwaggerSchema.getTopic(), operations, false);

            ApiListing apiListing = new ApiListingBuilder(new Ordering<ApiDescription>() {
                @Override
                public int compare(@Nullable ApiDescription apiDescription, @Nullable ApiDescription t1) {
                    return apiDescription.getDescription().compareTo(t1.getDescription());
                }
            })
                    .resourcePath(topicSwaggerSchema.getTopic())
                    .basePath("/")
                    .apis(Arrays.asList(apiDescription))
                    .models(models)
                    .build();

            apiListings.put(apiListing.getResourcePath(), apiListing);
        }

        DocumentationBuilder documentationBuilder = new DocumentationBuilder();
        documentationBuilder.name(config.getGroupName());
        documentationBuilder.apiListingsByResourceGroupName(apiListings);

        documentationCache.addDocumentation(documentationBuilder.build());
    }
}
