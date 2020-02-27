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
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class KafkaContainer extends org.testcontainers.containers.KafkaContainer {

    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private AdminClient adminClient;

    public KafkaContainer(String imageVersion) {
        super(imageVersion);
    }

    @Override
    public void start() {
        super.start();

        consumer = new KafkaConsumer<>(
                ImmutableMap.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers(),
                        ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString(),
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
                )
        );

        producer = new KafkaProducer<>(
                ImmutableMap.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers(),
                        ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString()
                ),
                new StringSerializer(),
                new StringSerializer()
        );

        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());

        adminClient = AdminClient.create(properties);
    }

    public void createTopics(Set<String> topics) {
        List<NewTopic> newTopics = topics.stream()
                .map(topic -> new NewTopic(topic, 1, (short) 1))
                .collect(Collectors.toList());

        adminClient.createTopics(newTopics);
    }
}