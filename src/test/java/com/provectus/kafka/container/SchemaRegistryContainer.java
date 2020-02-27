package com.provectus.kafka.container;

import okhttp3.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;

import java.io.IOException;

public class SchemaRegistryContainer extends GenericContainer<SchemaRegistryContainer> {

    private final static MediaType SCHEMA_CONTENT =
            MediaType.parse("application/vnd.schemaregistry.v1+json");

    public SchemaRegistryContainer(String imageVersion, KafkaContainer kafka) {
        setDockerImageName("confluentinc/cp-schema-registry:" + imageVersion);
        withNetwork(kafka.getNetwork());
        withExposedPorts(8081);
        withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry");
        withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081");
        withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://" + kafka.getNetworkAliases().get(0) + ":9092");
    }

    public String getUrl() {
        return String.format("http://%s:%d", this.getContainerIpAddress(), this.getFirstMappedPort());
    }

    public Response sendSchema(String schemaName, String schema) throws IOException {
        final OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .post(RequestBody.create(schema, SCHEMA_CONTENT))
                .url(getUrl() + "/subjects/" + schemaName + "/versions")
                .build();

        return client.newCall(request).execute();
    }
}
