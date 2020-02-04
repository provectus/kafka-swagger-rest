package com.provectus.kafka.schemaregistry.impl;

import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryConfig;
import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryRestClient;
import com.provectus.kafka.schemaregistry.model.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;

@Slf4j
public class KafkaSchemaRegistryRestClientImpl implements KafkaSchemaRegistryRestClient {

    private final RestTemplate restTemplate;

    public KafkaSchemaRegistryRestClientImpl(KafkaSchemaRegistryConfig config) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setReadTimeout(60000);
        rf.setConnectTimeout(60000);

        restTemplate = new RestTemplate(rf);
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(config.getUrl()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getSubjects() {
        return restTemplate.getForEntity("/subjects", List.class).getBody();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Integer> getSubjectVersions(String subject) {
        return restTemplate.getForEntity("/subjects/{subject}/versions", List.class,
                subject).getBody();
    }

    @Override
    public Schema getSubjectSchema(String subject, Integer version) {
        return restTemplate.getForEntity("/subjects/{subject}/versions/{v}", Schema.class,
                subject, version).getBody();
    }

    @Override
    public Schema getSubjectLatestSchema(String subject) {
        return restTemplate.getForEntity("/subjects/{subject}/versions/latest", Schema.class,
                subject).getBody();
    }
}