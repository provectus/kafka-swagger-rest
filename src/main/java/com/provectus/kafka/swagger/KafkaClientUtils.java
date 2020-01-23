package com.provectus.kafka.swagger;

import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

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
}
