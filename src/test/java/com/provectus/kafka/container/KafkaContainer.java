package com.provectus.kafka.container;

import lombok.Getter;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.testcontainers.containers.Network;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.stream.Collectors;

public class KafkaContainer extends org.testcontainers.containers.KafkaContainer {

    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private AdminClient adminClient;

    public KafkaContainer(String imageVersion) {
        super(imageVersion);

        setNetwork(Network.SHARED);
    }

    public KafkaConsumer<String, String> getConsumer() {
        if (consumer == null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
            properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

            consumer = new KafkaConsumer<>(properties);
        }

        return consumer;
    }

    public AdminClient getAdminClient() {
        if (adminClient == null) {
            Properties properties = new Properties();
            properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());

            adminClient = AdminClient.create(properties);
        }

        return adminClient;
    }

    public KafkaProducer<String, String> getProducer() {
        if (producer == null) {
            producer = new KafkaProducer<>(
                    ImmutableMap.of(
                            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers(),
                            ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString()
                    ),
                    new StringSerializer(),
                    new StringSerializer()
            );
        }

        return producer;
    }

    public void createTopics(Set<String> topics) {
        List<NewTopic> newTopics = topics.stream()
                .map(topic -> new NewTopic(topic, 1, (short) 1))
                .collect(Collectors.toList());

        getAdminClient().createTopics(newTopics);
    }
}