package com.provectus.kafka.error;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaSwaggerException extends RuntimeException {

    public KafkaSwaggerException() {
        super();
    }

    public KafkaSwaggerException(String reason) {
        super(reason);
    }

    public KafkaSwaggerException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
