package com.provectus.kafka.swagger;

import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

public class KafkaClientUtils {

    public static KafkaMessageListenerContainer<Integer, String> createContainer(
            KafkaSwaggerConfig config,
            ContainerProperties containerProps) {
        DefaultKafkaConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<>(config.getConsumerConfig());
        KafkaMessageListenerContainer<Integer, String> container = new KafkaMessageListenerContainer<>(cf, containerProps);
        return container;
    }

    public static KafkaTemplate<Object, Object> createTemplate(KafkaSwaggerConfig config) {
        ProducerFactory<Object, Object> pf = new DefaultKafkaProducerFactory<>(config.getProducerConfig());
        KafkaTemplate<Object, Object> template = new KafkaTemplate<>(pf);
        return template;
    }

    public static ConsumerFactory<Object, Object> createConsumerFactory(KafkaSwaggerConfig config) {
        return new DefaultKafkaConsumerFactory<>(config.getConsumerConfig());
    }
}
