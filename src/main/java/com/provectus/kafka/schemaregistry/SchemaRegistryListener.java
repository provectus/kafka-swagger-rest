package com.provectus.kafka.schemaregistry;

import com.provectus.kafka.schemaregistry.model.Schema;

public interface SchemaRegistryListener {

    void onSchema(Schema schema);
}
