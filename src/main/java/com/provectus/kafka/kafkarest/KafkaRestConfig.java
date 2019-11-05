package com.provectus.kafka.kafkarest;

import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class KafkaRestConfig {

    private String url;
}
