package com.provectus.kafka.rest;

import com.provectus.kafka.rest.model.KafkaSendMessageRequestBody;
import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryConfig;
import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryRestClient;
import com.provectus.kafka.schemaregistry.impl.KafkaSchemaRegistryRestClientImpl;
import com.provectus.kafka.schemaregistry.model.Schema;
import com.provectus.kafka.swagger.KafkaClientUtils;
import com.provectus.kafka.swagger.model.KafkaSwaggerConfig;
import com.provectus.kafka.swagger.model.topic.TopicSwaggerSchema;
import com.provectus.kafka.swagger.service.KafkaSwaggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.HashSet;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/kafka")
public class KafkaRest {

    private final KafkaSchemaRegistryRestClient kafkaSchemaRegistryRestClient =
            new KafkaSchemaRegistryRestClientImpl(KafkaSchemaRegistryConfig.builder().url("http://localhost:8081").build());

    private final KafkaSwaggerService kafkaSwaggerService;

    @PostConstruct
    public void init() {
        //TODO: move to configs
        KafkaSwaggerConfig kafkaSwaggerConfig = KafkaSwaggerConfig.builder()
                .groupName("kafka-localhost-swagger")
                .kafkaUrl("http://localhost:9092")
                .kafkaRestUrl("http://localhost:8082")
                .kafkaSchemaRegistryUrl("http://localhost:8081")
                .ignoreTopics(new HashSet<>())
                .build();

        kafkaSwaggerService.registerKafka(kafkaSwaggerConfig);
    }

    @PostMapping("/{kafka}/topics/{topic}")
    public void sendMessageToTopic(@PathVariable String kafka,
                                   @PathVariable String topic,
                                   @RequestBody KafkaSendMessageRequestBody requestBody) {
        log.info("send message to kafka: {}, topic: {}", kafka, topic);
        log.info("Values: {}", requestBody);

        TopicSwaggerSchema topicSwaggerSchema = kafkaSwaggerService.getKafkaSwagger(kafka, topic);

        Schema schema = kafkaSchemaRegistryRestClient.getSubjectLatestSchema("users-value");

        //TODO: build GenericRecord based on topicSwaggerSchema and send it to kafka topic

        GenericRecordBuilder valueBuilder = new GenericRecordBuilder(schema.getRawSchema());
        valueBuilder.set("name", "Kafka");
        valueBuilder.set("age", 1000);
        GenericData.Record value = valueBuilder.build();

        KafkaTemplate<Object, Object> template = KafkaClientUtils.createTemplate();

        template.send("users-avro", "15", value).addCallback(new ListenableFutureCallback<SendResult<Object, Object>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("error on send to topic", ex);
            }

            @Override
            public void onSuccess(SendResult<Object, Object> result) {
                log.info("sucessfuly sended: {}", result);
            }
        });

    }
}
