package com.provectus.kafka.model.config;

import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;

@ToString
@Builder(toBuilder = true)
public class KafkaSwaggerConfig {

    private String groupName;
    private String bootstrapServers;
    private String schemaRegistryUrl;
    private Map<String, Object> consumerConfig = new HashMap<>();
    private Map<String, Object> producerConfig = new HashMap<>();
    private List<TopicConfig> topicConfig = new ArrayList<>();

    private Set<String> ignoreTopics = new HashSet<>();
    private Set<String> ignoreTopicsRegexp = new HashSet<>();

    public KafkaSwaggerConfig(String groupName, String bootstrapServers, String schemaRegistryUrl, Map<String, Object> consumerConfig, Map<String, Object> producerConfig, List<TopicConfig> topicConfig, Set<String> ignoreTopics, Set<String> ignoreTopicsRegexp) {
        this.groupName = groupName;
        this.consumerConfig = consumerConfig;
        this.producerConfig = producerConfig;
        this.topicConfig = topicConfig;
        this.ignoreTopics = ignoreTopics;
        this.ignoreTopicsRegexp = ignoreTopicsRegexp;

        setSchemaRegistryUrl(schemaRegistryUrl);
        setBootstrapServers(bootstrapServers);
        initConsumerConfig();
        initProducerConfig();
        initDefaultIgnoreTopicsRegexp();
    }

    public KafkaSwaggerConfig() {
        initConsumerConfig();
        initProducerConfig();
        initDefaultIgnoreTopicsRegexp();
    }

    private void initConsumerConfig() {
        consumerConfig.putIfAbsent(ConsumerConfig.GROUP_ID_CONFIG, "kafka-swagger-rest");
        consumerConfig.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerConfig.putIfAbsent(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        consumerConfig.putIfAbsent(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        consumerConfig.putIfAbsent(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "5000");
        consumerConfig.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    }

    private void initProducerConfig() {
        producerConfig.putIfAbsent(ProducerConfig.RETRIES_CONFIG, 0);
        producerConfig.putIfAbsent(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        producerConfig.putIfAbsent(ProducerConfig.LINGER_MS_CONFIG, 1);
        producerConfig.putIfAbsent(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        producerConfig.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    }

    private void initDefaultIgnoreTopicsRegexp() {
        ignoreTopicsRegexp.add("_.+"); // by default ignore all topics that starts with "_"
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        this.bootstrapServers = bootstrapServers;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public void setSchemaRegistryUrl(String schemaRegistryUrl) {
        consumerConfig.put("schema.registry.url", schemaRegistryUrl);
        producerConfig.put("schema.registry.url", schemaRegistryUrl);

        this.schemaRegistryUrl = schemaRegistryUrl;
    }

    public Map<String, Object> getConsumerConfig() {
        return consumerConfig;
    }

    public void setConsumerConfig(Map<String, Object> consumerConfig) {
        this.consumerConfig = MapParameters.mergeMapParams(consumerConfig);
    }

    public Map<String, Object> getProducerConfig() {
        return producerConfig;
    }

    public void setProducerConfig(Map<String, Object> producerConfig) {
        this.producerConfig = MapParameters.mergeMapParams(producerConfig);
    }

    public Set<String> getIgnoreTopics() {
        return ignoreTopics;
    }

    public Set<String> getIgnoreTopicsRegexp() {
        return ignoreTopicsRegexp;
    }

    public void setIgnoreTopics(Set<String> ignoreTopics) {
        this.ignoreTopics = ignoreTopics;
    }

    public List<TopicConfig> getTopicConfig() {
        return topicConfig;
    }

    public void setTopicConfig(List<TopicConfig> topicConfig) {
        this.topicConfig = topicConfig;
    }

    public KafkaSchemaRegistryConfig schemaRegistryConfig() {
        return KafkaSchemaRegistryConfig.builder()
                .url(schemaRegistryUrl)
                .build();
    }

    public TopicConfig getAutofillTopicConfig(String topic) {
        Optional<TopicConfig> configOptional = topicConfig.stream()
                .filter(config -> config.getTopicName().equals(topic))
                .findFirst();

        if (configOptional.isPresent()) return configOptional.get();

        return null;
    }

    public static class KafkaSwaggerConfigBuilder {

        public KafkaSwaggerConfigBuilder() {
            this.producerConfig = new HashMap<>();
            this.consumerConfig = new HashMap<>();
            this.topicConfig = new ArrayList<>();
            this.ignoreTopics = new HashSet<>();
            this.ignoreTopicsRegexp = new HashSet<>();
        }

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
