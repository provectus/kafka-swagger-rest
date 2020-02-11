package com.provectus.kafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.provectus.kafka.model.KafkaRecordBuilder;
import com.provectus.kafka.model.ProducerResult;
import com.provectus.kafka.model.schema.TopicSwaggerSchema;
import com.provectus.kafka.service.KafkaSwaggerService;
import com.provectus.kafka.swagger.KafkaClientUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/kafka")
public class KafkaController {

    private final KafkaSwaggerService kafkaSwaggerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/{kafka}/topics/kv/{topic}")
    public Mono<ProducerResult> sendMessageToTopicKeyValue(@PathVariable String kafka,
                                                   @PathVariable String topic,
                                                   @RequestBody String requestJson) throws Exception {
        return this.send(kafka, topic, requestJson, this::buildKeyValue);
    }

    @PostMapping("/{kafka}/topics/{topic}")
    public Mono<ProducerResult> sendMessageToTopicOnlyValue(@PathVariable String kafka,
                                                   @PathVariable String topic,
                                                   @RequestBody String requestJson) throws JsonProcessingException {
        return this.send(kafka, topic, requestJson, this::buildValue);
    }

    private Mono<ProducerResult> send(String kafka, String topic, String requestJson, BiFunction<JsonNode,TopicSwaggerSchema,Mono<Map.Entry<Object, Object>>> supplier) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(requestJson);

        TopicSwaggerSchema topicSwaggerSchema = kafkaSwaggerService.getTopicSwaggerSchema(kafka, topic);
        try {

            Mono<Map.Entry<Object, Object>> data = supplier.apply(jsonNode, topicSwaggerSchema);

            return data.flatMap(
                    d -> sendData(kafka, topic, d)
            );

        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<? extends ProducerResult> sendData(String kafka, String topic, Map.Entry<Object, Object> data) {
        KafkaTemplate<Object, Object> template = KafkaClientUtils.createTemplate(kafkaSwaggerService.getKafkaSwagger(kafka).getConfig());
        ListenableFuture<SendResult<Object, Object>> future = template.send(new ProducerRecord<>(topic, data.getKey(), data.getValue()));
        return Mono.fromFuture(future.completable()).map(r -> ProducerResult.fromRecordMetadata(r.getRecordMetadata()));
    }

    private Mono<Map.Entry<Object, Object>> buildValue(JsonNode node, TopicSwaggerSchema schema) {
        try {
            if (schema.getAutofillTopicConfig() != null) {
                Map.Entry<Object, Object> data = new KafkaRecordBuilder().buildAutofillKeyValue(schema, node);
                return Mono.just(data);
            } else {
                Object data = new KafkaRecordBuilder().buildValue(schema, node);
                return Mono.just(new AbstractMap.SimpleEntry<>(null, data));
            }
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<Map.Entry<Object, Object>> buildKeyValue(JsonNode node, TopicSwaggerSchema schema) {
        try {
            Map.Entry<Object,Object> data = new KafkaRecordBuilder().buildKeyValue(schema, node);
            return Mono.just(data);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
