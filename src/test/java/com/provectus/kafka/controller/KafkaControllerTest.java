package com.provectus.kafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provectus.kafka.KafkaApplication;
import com.provectus.kafka.container.KafkaTestEnvironmentCluster;
import com.provectus.kafka.model.KeyValueRequest;
import com.provectus.kafka.model.User;
import com.provectus.kafka.util.Resources;
import com.provectus.kafka.util.ResourcesUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.awaitility.Awaitility;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class KafkaControllerTest {

    public static KafkaTestEnvironmentCluster kafkaEnvironmentCluster;
    public static Integer port;

    public static ConfigurableApplicationContext context;

    private static final String STRING_TOPIC = "string-topic";
    private static final String AVRO_TOPIC = "avro-topic";
    private static final String SWAGGER_GROUP_NAME = "localhost";

    private static final Set TOPICS = Set.of(STRING_TOPIC, AVRO_TOPIC);

    @BeforeClass
    public static void before() throws IOException {
        port = SocketUtils.findAvailableTcpPort();
        kafkaEnvironmentCluster = new KafkaTestEnvironmentCluster("5.1.0")
                .withKafka()
                .withSchemaRegistry()
                .start();

        kafkaEnvironmentCluster.getKafka().createTopics(TOPICS);
        kafkaEnvironmentCluster.getSchemaRegistry().sendSchema(AVRO_TOPIC + "-value",
                JSONObject.quote(ResourcesUtil.readResource(Resources.USERS_VALUE_V1)));

        context = applicationContext();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterClass
    public static void shutdown() {
        context.close();
    }

    @Test
    public void test_sendValueMessage_stringFormat() throws IOException {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("\"test\"")
                .post("/kafka/" + SWAGGER_GROUP_NAME + "/topics/" + STRING_TOPIC)
                .then()
                .statusCode(200);

        Awaitility.waitAtMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> verifyMessageSendedToTopic(STRING_TOPIC));
    }

    @Test
    public void test_sendKeyValueMessage_stringFormat() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"key\":\"1\", \"value\": \"message\"}")
                .post("/kafka/" + SWAGGER_GROUP_NAME + "/topics/kv/" + STRING_TOPIC)
                .then()
                .statusCode(200);

        Awaitility.waitAtMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> verifyMessageSendedToTopic(STRING_TOPIC));
    }

    @Test
    public void test_sendValueMessage_avroFormat() throws JsonProcessingException {
        User user = new User();
        user.setName("Test");
        user.setAge(20);

        String userJson = new ObjectMapper().writeValueAsString(user);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(userJson)
                .post("/kafka/" + SWAGGER_GROUP_NAME + "/topics/" + AVRO_TOPIC)
                .then()
                .statusCode(200);

        Awaitility.waitAtMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> verifyMessageSendedToTopic(AVRO_TOPIC));
    }

    @Test
    public void test_sendKeyValueMessage_avroFormat() throws JsonProcessingException {
        User user = new User();
        user.setName("Test");
        user.setAge(20);

        KeyValueRequest keyValueRequest = new KeyValueRequest("1", user);

        String keyValueRequestJson = new ObjectMapper().writeValueAsString(keyValueRequest );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(keyValueRequestJson)
                .post("/kafka/" + SWAGGER_GROUP_NAME + "/topics/kv/" + AVRO_TOPIC)
                .then()
                .statusCode(200);

        Awaitility.waitAtMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> verifyMessageSendedToTopic(AVRO_TOPIC));
    }

    private Boolean verifyMessageSendedToTopic(String topic) {
        KafkaConsumer<String, String> consumer = kafkaEnvironmentCluster.getKafka().getConsumer();
        consumer.subscribe(Arrays.asList(topic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
        return records.count() == 1;
    }

    private static ConfigurableApplicationContext applicationContext() {
        List<String> params = Arrays.asList(
                "--server.port=" + port,
                "--swagger.kafka[0].groupName=" + SWAGGER_GROUP_NAME,
                "--swagger.kafka[0].bootstrapServers=" + kafkaEnvironmentCluster.getKafka().getBootstrapServers(),
                "--swagger.kafka[0].schemaRegistryUrl=" + kafkaEnvironmentCluster.getSchemaRegistry().getUrl()
        );

        return SpringApplication.run(new Class[]{KafkaApplication.class}, params.toArray(new String[params.size()]));
    }
}