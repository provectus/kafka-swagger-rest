package com.provectus.kafka.error;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
@Setter
public class KafkaSwaggerException extends ResponseStatusException {

    public KafkaSwaggerException(HttpStatus status) {
        super(status);
    }

    public KafkaSwaggerException(HttpStatus status, String reason) {
        super(status, reason);
    }

    public KafkaSwaggerException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }
}
