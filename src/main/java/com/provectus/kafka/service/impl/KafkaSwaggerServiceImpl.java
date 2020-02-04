package com.provectus.kafka.service.impl;

import com.provectus.kafka.error.KafkaSwaggerException;
import com.provectus.kafka.swagger.KafkaSwagger;
import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import com.provectus.kafka.model.schema.TopicSwaggerSchema;
import com.provectus.kafka.service.KafkaSwaggerService;
import com.provectus.kafka.swagger.config.SwaggerProperties;
import io.swagger.models.Swagger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaSwaggerServiceImpl implements KafkaSwaggerService {

    @Autowired
    private SwaggerProperties swaggerProperties;

    private Map<String, KafkaSwagger> kafkaSwaggerMap = new HashMap<>();

    @PostConstruct
    public void initKafkaFromConfiguration() {
        log.info(swaggerProperties.toString());
        for (KafkaSwaggerConfig kafkaSwaggerConfig: swaggerProperties.getKafka()) {
            try {
                registerKafka(kafkaSwaggerConfig);
            } catch (KafkaSwaggerException e) {
                log.error("Unable to register kafka swagger: {}. Error: {}", kafkaSwaggerConfig, e.getMessage());
            }
        }
    }

    @Override
    public List<KafkaSwagger> getKafkaSwaggers() {
        return new ArrayList<>(kafkaSwaggerMap.values());
    }

    @Override
    public List<Swagger> getSwaggers() {
        return kafkaSwaggerMap.values().stream()
                .map(KafkaSwagger::getSwagger)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getSwaggerSpecs() {
        return kafkaSwaggerMap.values().stream()
                .map(KafkaSwagger::getSwaggerYaml)
                .collect(Collectors.toList());
    }

    @Override
    public KafkaSwagger registerKafka(KafkaSwaggerConfig kafkaSwaggerConfig) {
        KafkaSwagger kafkaSwagger = new KafkaSwagger(kafkaSwaggerConfig);
        kafkaSwagger.init();
        kafkaSwaggerMap.put(kafkaSwaggerConfig.getGroupName(), kafkaSwagger);
        log.info("kafka-swagger registered with config: " + kafkaSwaggerConfig);
        return kafkaSwagger;
    }

    @Override
    public TopicSwaggerSchema getTopicSwaggerSchema(String group, String topic) {
        KafkaSwagger kafkaSwagger = kafkaSwaggerMap.get(group);

        if (kafkaSwagger == null) return null;

        return kafkaSwagger.getKafkaSwaggerSchema()
                .getTopics().get(topic);
    }

    @Override
    public KafkaSwagger getKafkaSwagger(String group) {
        return kafkaSwaggerMap.get(group);
    }
    @Override
    public String getSwaggerSpec(String kafkaGroupName) {
        return kafkaSwaggerMap.get(kafkaGroupName).getSwaggerYaml();
    }
}