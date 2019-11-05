package com.provectus.kafka.schemaregistry.model;

import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Schema {

    private String subject;
    private Integer version;
    private Integer id;
    private String schema;

    public String getTopic() {
        if (isKeySchema()) return subject.substring(0, subject.length() - 4);
        if (isValueSchema()) return subject.substring(0, subject.length() - 6);

        return null;
    }

    public boolean isKeySchema() {
        return subject.endsWith("-key");
    }

    public boolean isValueSchema() {
        return subject.endsWith("-value");
    }

    public AvroSchema getAvroSchema() {
        return new AvroSchema(getRawSchema());
    }

    public org.apache.avro.Schema getRawSchema() {
        return new org.apache.avro.Schema.Parser().setValidate(true).parse(schema);
    }
}
