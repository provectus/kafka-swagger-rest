package com.provectus.kafka.swagger.model.topic;

import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TopicParamSchema {

    private TopicParamSchemaType type;
    private AvroSchema avroSchema;
}
