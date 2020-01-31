package com.provectus.kafka.model.config;

import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Builder(toBuilder = true)
@AllArgsConstructor
public class KafkaSwaggerConfig {

    private String groupName;
    private String kafkaUrl;
    private String kafkaSchemaRegistryUrl;
    private Map<String, Object> consumerConfig = new HashMap<>();
    private Map<String, Object> producerConfig = new HashMap<>();

    private Set<String> ignoreTopics = new HashSet<>();

    public KafkaSwaggerConfig() {
        initConsumerConfig();
        initProducerConfig();
    }

    private void initConsumerConfig() {
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-swagger-api");
        consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        consumerConfig.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    }

    private void initProducerConfig() {
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0);
        producerConfig.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        producerConfig.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        producerConfig.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getKafkaUrl() {
        return kafkaUrl;
    }

    public void setKafkaUrl(String kafkaUrl) {
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);

        this.kafkaUrl = kafkaUrl;
    }

    public String getKafkaSchemaRegistryUrl() {
        return kafkaSchemaRegistryUrl;
    }

    public void setKafkaSchemaRegistryUrl(String kafkaSchemaRegistryUrl) {
        consumerConfig.put("schema.registry.url", kafkaSchemaRegistryUrl);
        producerConfig.put("schema.registry.url", kafkaSchemaRegistryUrl);

        this.kafkaSchemaRegistryUrl = kafkaSchemaRegistryUrl;
    }

    public Map<String, Object> getConsumerConfig() {
        return consumerConfig;
    }

    public void setConsumerConfig(Map<String, Object> consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public Map<String, Object> getProducerConfig() {
        return producerConfig;
    }

    public void setProducerConfig(Map<String, Object> producerConfig) {
        this.producerConfig = producerConfig;
    }

    public Set<String> getIgnoreTopics() {
        return ignoreTopics;
    }

    public void setIgnoreTopics(Set<String> ignoreTopics) {
        this.ignoreTopics = ignoreTopics;
    }

    public KafkaSchemaRegistryConfig schemaRegistryConfig() {
        return KafkaSchemaRegistryConfig.builder()
                .url(kafkaSchemaRegistryUrl)
                .build();
    }

    public static class KafkaSwaggerConfigBuilder {
        public KafkaSwaggerConfigBuilder producerConfigParam(String key, Object value) {
            this.producerConfig.put(key, value);
            return this;
        }

        public KafkaSwaggerConfigBuilder consumerConfigParam(String key, Object value) {
            this.consumerConfig.put(key, value);
            return this;
        }
    }
}
