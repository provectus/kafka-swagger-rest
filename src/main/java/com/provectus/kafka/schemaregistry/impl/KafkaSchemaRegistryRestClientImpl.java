package com.provectus.kafka.schemaregistry.impl;

import com.provectus.kafka.error.KafkaSwaggerException;
import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryConfig;
import com.provectus.kafka.schemaregistry.KafkaSchemaRegistryRestClient;
import com.provectus.kafka.schemaregistry.model.Schema;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

@Slf4j
public class KafkaSchemaRegistryRestClientImpl implements KafkaSchemaRegistryRestClient {

    private final RestTemplate restTemplate;

    private final KafkaSchemaRegistryConfig config;

    public KafkaSchemaRegistryRestClientImpl(KafkaSchemaRegistryConfig config) {
        this.config = config;
        restTemplate = new RestTemplate();
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

    @Override
    public void waitIsReady() {
        waitIsReady(60000);
    }

    @SneakyThrows
    @Override
    public void waitIsReady(Integer timeout) {
        long startTime = System.currentTimeMillis();
        boolean isReady = false;

        // workaround to check when schema-registry is ready
        // if schema-registry isn't ready then rest api will returns SocketException: Unexpected end of file from server
        while (!isReady) {
            try {
                restTemplate.getForEntity(config.getUrl(), String.class).getBody();
                isReady = true;
            } catch (Exception e) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    throw new KafkaSwaggerException(HttpStatus.INTERNAL_SERVER_ERROR, "Error on init KafkaSchemaRegistryClient by timeout. " + e.getMessage(), e);
                }
                Thread.sleep(1000);
            }
        }
    }
}