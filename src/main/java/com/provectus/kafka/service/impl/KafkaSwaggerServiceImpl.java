package com.provectus.kafka.service.impl;

import com.provectus.kafka.swagger.KafkaSwagger;
import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import com.provectus.kafka.model.schema.TopicSwaggerSchema;
import com.provectus.kafka.service.KafkaSwaggerService;
import io.swagger.models.Swagger;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class KafkaSwaggerServiceImpl implements KafkaSwaggerService {

    private Map<String, KafkaSwagger> kafkaSwaggerMap = new HashMap<>();

    public KafkaSwaggerServiceImpl(KafkaSwaggerConfig kafkaSwaggerConfig) {
        this.registerKafka(kafkaSwaggerConfig);
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