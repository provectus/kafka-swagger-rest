package com.provectus.kafka.model;

import lombok.*;
import org.apache.kafka.clients.producer.RecordMetadata;

@Data
public class ProducerResult {
    private final long offset;
    private final long timestamp;
    private final int serializedKeySize;
    private final int serializedValueSize;
    private final String topic;
    private final int partition;

    public static ProducerResult fromRecordMetadata(RecordMetadata recordMetadata) {
        return new ProducerResult(
                recordMetadata.offset(),
                recordMetadata.timestamp(),
                recordMetadata.serializedKeySize(),
                recordMetadata.serializedValueSize(),
                recordMetadata.topic(),
                recordMetadata.partition()
        );
    }
}
