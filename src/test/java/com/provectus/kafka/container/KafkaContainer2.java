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
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class KafkaContainer2 extends FixedHostPortGenericContainer<KafkaContainer2> {

    private ZookeeperContainer zookeeperContainer;

    private KafkaConsumer<String, String> consumer;
    private KafkaProducer<String, String> producer;
    private AdminClient adminClient;

    public KafkaContainer2(String imageVersion, ZookeeperContainer zookeeperContainer) {
        super("confluentinc/cp-kafka:" + imageVersion);

        this.zookeeperContainer = zookeeperContainer;

        withExposedPorts(9092);
        withFixedExposedPort(9092, 9092);

        withEnv("KAFKA_BROKER_ID", "1");
        withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092");
        withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT");
        withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "PLAINTEXT");
        withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1");
    }

    public KafkaContainer2 zookeeperConnect(String ... zookeepers) {
        String zookeepersConnect = String.join(",", zookeepers);

        withEnv("KAFKA_ZOOKEEPER_CONNECT", zookeepersConnect);

        return this;
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

    public String getBootstrapServers() {
        return String.format("PLAINTEXT://%s:%s", getContainerIpAddress(), 9092);
    }

    public void createTopics(Set<String> topics) {
        List<NewTopic> newTopics = topics.stream()
                .map(topic -> new NewTopic(topic, 1, (short) 1))
                .collect(Collectors.toList());

        adminClient.createTopics(newTopics);
    }
}