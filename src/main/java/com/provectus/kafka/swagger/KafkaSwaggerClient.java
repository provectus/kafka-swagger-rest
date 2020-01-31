package com.provectus.kafka.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provectus.kafka.schemaregistry.SchemaRegistryListener;
import com.provectus.kafka.schemaregistry.model.Schema;
import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Slf4j
public class KafkaSwaggerClient {

    private final KafkaSwaggerConfig kafkaSwaggerConfig;
    private KafkaMessageListenerContainer kafkaMessageListenerContainer;

    private List<SchemaRegistryListener> schemaRegistryListeners = new ArrayList<>();

    public KafkaSwaggerClient(KafkaSwaggerConfig kafkaSwaggerConfig) {
        this.kafkaSwaggerConfig = kafkaSwaggerConfig;
        initKafkaListenerContainer();
    }

    private void initKafkaListenerContainer() {
        ContainerProperties containerProps = new ContainerProperties("_schemas");
        containerProps.setMessageListener(new MessageListener<>() {

            @Override
            public void onMessage(ConsumerRecord<Object, Object> data) {
                log.trace("onMessage {}", data);

                if (schemaRegistryListeners.isEmpty()) return;

                if (data.value() != null) {
                    try {
                        Schema schema = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                .readValue((String) data.value(), Schema.class);

                        schemaRegistryListeners.stream()
                                .forEach(schemaRegistryListener -> schemaRegistryListener.onSchema(schema));
                    } catch (JsonProcessingException e) {
                        log.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onMessage(ConsumerRecord<Object, Object> data, Acknowledgment acknowledgment) {
                log.trace("onMessage {} {}", data, acknowledgment);
            }

            @Override
            public void onMessage(ConsumerRecord<Object, Object> data, Consumer<?, ?> consumer) {
                log.trace("onMessage {} {}", data, consumer);
            }

            @Override
            public void onMessage(ConsumerRecord<Object, Object> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
                log.trace("onMessage {} {}", data, acknowledgment, consumer);
            }
        });

        kafkaMessageListenerContainer = KafkaClientUtils.createContainer(kafkaSwaggerConfig, containerProps);
        kafkaMessageListenerContainer.start();
    }

    public Set<String> getTopics() {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaSwaggerConfig.getKafkaUrl());

        AdminClient adminClient = AdminClient.create(properties);

        ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
        listTopicsOptions.listInternal(true);

        try {
            return adminClient.listTopics(listTopicsOptions).names().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void subscribe(SchemaRegistryListener schemaRegistryListener) {
        this.schemaRegistryListeners.add(schemaRegistryListener);
    }

    public void start() {
        kafkaMessageListenerContainer.start();
    }

    public void stop() {
        kafkaMessageListenerContainer.stop();
    }
}