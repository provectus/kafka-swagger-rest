package com.provectus.kafka.container;

import okhttp3.*;
import org.assertj.core.api.Assertions;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaRegistryContainer extends FixedHostPortGenericContainer<SchemaRegistryContainer> {

    private final GenericContainer kafka;

    private final static MediaType SCHEMA_CONTENT =
            MediaType.parse("application/vnd.schemaregistry.v1+json");

    public SchemaRegistryContainer(String imageVersion, GenericContainer kafka) {
        super("confluentinc/cp-schema-registry:" + imageVersion);

        this.kafka = kafka;
        withNetwork(Network.SHARED);
        withExposedPorts(8081);
        withFixedExposedPort(8081,8081);
        withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry");
        withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081");
    }

    public String getUrl() {
        return String.format("http://%s:%d", this.getContainerIpAddress(), this.getFirstMappedPort());
    }

    public Response sendSchema(String schemaName, String schema) throws IOException {
        final OkHttpClient client = new OkHttpClient();

        String schemaRequestBody = "{\"schema\": " + schema + "}";

        Request request = new Request.Builder()
                .post(RequestBody.create(schemaRequestBody, SCHEMA_CONTENT))
                .url(getUrl() + "/subjects/" + schemaName + "/versions")
                .build();

        Response response = client.newCall(request).execute();

        assertThat(response.code()).isEqualTo(200);

        return response;
    }

    @Override
    public void start() {
        withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://" + kafka.getNetworkAliases().get(0) + ":9092");

        super.start();
    }
}
