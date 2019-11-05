package com.provectus.kafka.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provectus.kafka.schemaregistry.SchemaRegistryListener;
import com.provectus.kafka.schemaregistry.model.Schema;
import com.provectus.kafka.swagger.model.KafkaSwaggerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
public class KafkaSwaggerClient implements MessageListener<Object, Object> {

    private final KafkaSwaggerConfig kafkaSwaggerConfig;
    private KafkaMessageListenerContainer kafkaMessageListenerContainer;

    private SchemaRegistryListener schemaRegistryListener;

    public KafkaSwaggerClient(KafkaSwaggerConfig kafkaSwaggerConfig) {
        this.kafkaSwaggerConfig = kafkaSwaggerConfig;
        initKafkaListenerContainer();
    }

    private void initKafkaListenerContainer() {
        ContainerProperties containerProps = new ContainerProperties("_schemas");
        containerProps.setMessageListener(this);
        kafkaMessageListenerContainer = KafkaClientUtils.createContainer(kafkaSwaggerConfig, containerProps);
        kafkaMessageListenerContainer.start();
    }

    public void subscribe(SchemaRegistryListener schemaRegistryListener) {
        this.schemaRegistryListener = schemaRegistryListener;
    }

    public void start() {
        kafkaMessageListenerContainer.start();
    }

    public void stop() {
        kafkaMessageListenerContainer.stop();
    }

    @Override
    public void onMessage(ConsumerRecord<Object, Object> data) {
        log.info("onMessage {}", data);

        if (schemaRegistryListener == null) return;

        if (data.value() != null) {
            try {
                Schema schema = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .readValue((String) data.value(), Schema.class);

                schemaRegistryListener.onSchema(schema);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onMessage(ConsumerRecord<Object, Object> data, Acknowledgment acknowledgment) {
        log.info("onMessage {} {}", data, acknowledgment);
    }

    @Override
    public void onMessage(ConsumerRecord<Object, Object> data, Consumer<?, ?> consumer) {
        log.info("onMessage {} {}", data, consumer);
    }

    @Override
    public void onMessage(ConsumerRecord<Object, Object> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
        log.info("onMessage {} {}", data, acknowledgment, consumer);
    }
}
