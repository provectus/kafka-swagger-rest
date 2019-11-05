package com.provectus.kafka.swagger.service.impl;

import com.provectus.kafka.kafkarest.KafkaRestConfig;
import com.provectus.kafka.kafkarest.KafkarestRestClient;
import com.provectus.kafka.kafkarest.impl.KafkaRestClientImpl;
import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryConfig;
import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryRestClient;
import com.provectus.kafka.schemaregistry.impl.KafkaSchemaRegistryRestClientImpl;
import com.provectus.kafka.swagger.KafkaSwagger;
import com.provectus.kafka.swagger.model.KafkaSwaggerConfig;
import com.provectus.kafka.swagger.model.topic.TopicSwaggerSchema;
import com.provectus.kafka.swagger.service.KafkaSwaggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.DocumentationCache;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class KafkaSwaggerServiceImpl implements KafkaSwaggerService {

    private final KafkarestRestClient kafkarestRestClient;
    private final KafkaSchemaRegistryRestClient kafkaSchemaRegistryRestClient;
    private final DocumentationCache documentationCache;

    private Map<String, KafkaSwagger> kafkaSwaggerMap = new HashMap<>();

    public KafkaSwaggerServiceImpl(DocumentationCache documentationCache) {
        this.documentationCache = documentationCache;

        kafkarestRestClient = new KafkaRestClientImpl(KafkaRestConfig.builder().url("http://localhost:8082").build());
        kafkaSchemaRegistryRestClient = new KafkaSchemaRegistryRestClientImpl(KafkaSchemaRegistryConfig.builder().url("http://localhost:8081").build());

    }

    @Override
    public KafkaSwagger registerKafka(KafkaSwaggerConfig kafkaSwaggerConfig) {
        KafkaSwagger kafkaSwagger = new KafkaSwagger(kafkaSwaggerConfig, documentationCache);
        kafkaSwagger.init();
        kafkaSwaggerMap.put(kafkaSwaggerConfig.getGroupName(), kafkaSwagger);
        return kafkaSwagger;
    }

    @Override
    public TopicSwaggerSchema getKafkaSwagger(String group, String topic) {
        KafkaSwagger kafkaSwagger = kafkaSwaggerMap.get(group);

        if (kafkaSwagger == null) return null;

        TopicSwaggerSchema topicSwaggerSchema = kafkaSwagger.getKafkaSwaggerSchema()
                .getTopics().get(topic);

        return topicSwaggerSchema;
    }
}