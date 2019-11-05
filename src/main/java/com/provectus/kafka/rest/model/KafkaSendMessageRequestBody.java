package com.provectus.kafka.rest.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class KafkaSendMessageRequestBody {

    private String key;
    private String value;

    public KafkaSendMessageRequestBody(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
