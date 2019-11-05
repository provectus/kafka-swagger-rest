package com.provectus.kafka.swagger.service;

import com.provectus.kafka.swagger.KafkaSwagger;
import com.provectus.kafka.swagger.model.KafkaSwaggerConfig;
import com.provectus.kafka.swagger.model.topic.TopicSwaggerSchema;

public interface KafkaSwaggerService {

    KafkaSwagger registerKafka(KafkaSwaggerConfig kafkaSwaggerConfig);

    TopicSwaggerSchema getKafkaSwagger(String group, String topic);
}
