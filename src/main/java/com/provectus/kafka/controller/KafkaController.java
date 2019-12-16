package com.provectus.kafka.controller;

import com.provectus.kafka.model.KafkaRecordBuilder;
import com.provectus.kafka.model.schema.TopicSwaggerSchema;
import com.provectus.kafka.service.KafkaSwaggerService;
import com.provectus.kafka.swagger.KafkaClientUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.data.util.Pair;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/kafka")
public class KafkaController {

    private final KafkaSwaggerService kafkaSwaggerService;

    @PostMapping("/{kafka}/topics/{topic}")
    public void sendMessageToTopic(@PathVariable String kafka,
                                   @PathVariable String topic,
                                   @RequestBody String requestJson) {
        JSONObject jsonObj = new JSONObject(requestJson);

        TopicSwaggerSchema topicSwaggerSchema = kafkaSwaggerService.getTopicSwaggerSchema(kafka, topic);
        Pair<Object, Object> data = new KafkaRecordBuilder().build(topicSwaggerSchema, jsonObj);

        KafkaTemplate<Object, Object> template = KafkaClientUtils.createTemplate(kafkaSwaggerService.getKafkaSwagger(kafka).getConfig());
        template.send(topic, data.getFirst(), data.getSecond()).addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onFailure(Throwable ex) {
                log.trace("error on send to topic: " + topic, ex);
            }

            @Override
            public void onSuccess(SendResult<Object, Object> result) {
                log.trace("sucessfuly sended: {}", result);
            }
        });
    }
}
