package com.provectus.kafka.kafkarest.impl;

import com.provectus.kafka.kafkarest.KafkaRestConfig;
import com.provectus.kafka.kafkarest.KafkarestRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;

@Slf4j
public class KafkaRestClientImpl implements KafkarestRestClient {

    private final RestTemplate restTemplate;

    public KafkaRestClientImpl(KafkaRestConfig config) {
        restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(config.getUrl()));
    }

    @Override
    public List<String> getTopics() {
        ResponseEntity<List> topicsResponseEntity = restTemplate.getForEntity("/topics", List.class);
        return topicsResponseEntity.getBody();
    }
}