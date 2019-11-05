package com.provectus.kafka.schemaregistry;

import com.provectus.kafka.schemaregistry.model.Schema;

import java.util.List;

public interface KafkaSchemaRegistryRestClient {

    List<String> getSubjects();

    List<Integer> getSubjectVersions(String subject);

    Schema getSubjectSchema(String subject, Integer version);

    Schema getSubjectLatestSchema(String subject);
}
