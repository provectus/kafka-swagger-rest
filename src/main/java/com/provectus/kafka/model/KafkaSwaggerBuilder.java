package com.provectus.kafka.model;

import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import com.provectus.kafka.model.schema.KafkaSwaggerSchema;
import com.provectus.kafka.model.schema.TopicParamSchema;
import com.provectus.kafka.model.schema.TopicSwaggerSchema;
import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import org.apache.avro.Schema;

import java.util.List;

public class KafkaSwaggerBuilder {

    Swagger swagger = new Swagger();

    private KafkaSwaggerConfig kafkaSwaggerConfig;

    public Swagger build(KafkaSwaggerConfig kafkaSwaggerConfig, KafkaSwaggerSchema kafkaSwaggerSchema) {
        this.kafkaSwaggerConfig = kafkaSwaggerConfig;

        kafkaInfo("/kafka/" + kafkaSwaggerConfig.getGroupName());

        kafkaSwaggerSchema.getTopics().values().stream()
                .forEach(this::topicApi);

        return swagger;
    }

    private KafkaSwaggerBuilder kafkaInfo(String tag) {
        swagger.info(
                new Info()
                        .title("localhost kafka swagger")
                        .version("1.0"))
                .basePath("/")
                .tag(new Tag().name(tag))
                .consumes("application/json")
                .produces("application/json");

        return this;
    }

    private KafkaSwaggerBuilder topicApi(TopicSwaggerSchema topicSwaggerSchema) {
        String modelName = topicSwaggerSchema.getTopic() + "Model";
        String topicPath = "/kafka/" + kafkaSwaggerConfig.getGroupName() + "/topics/" + topicSwaggerSchema.getTopic();

        swagger.path(topicPath,
                new Path().post(
                        new Operation()
                                .tag("/kafka/" + kafkaSwaggerConfig.getGroupName())
                                .operationId("post" + topicSwaggerSchema.getTopic())
                                .consumes("application/json")
                                .parameter(new BodyParameter()
                                        .name("message")
                                        .description("message")
                                        .schema(new RefModel(modelName)))
                                .response(200, new Response()
                                        .description("OK"))
                ));

        swagger.addDefinition(modelName,
                new ModelImpl()
                        .type("object")
                        .property("key", property(topicSwaggerSchema.getKeySchema()))
                        .property("value", property(topicSwaggerSchema.getValueSchema())));

        return this;
    }

    private Property property(TopicParamSchema topicParamSchema) {
        switch (topicParamSchema.getType()) {
            case STRING:
                return stringProperty();
            case AVRO:
                return avroProperty(topicParamSchema.getAvroSchema().getAvroSchema());
            default:
                throw new RuntimeException("Unknown TopicParamSchema type: " + topicParamSchema.getType());
        }
    }

    private Property avroProperty(Schema avroSchema) {
        if (avroSchema.getType() == Schema.Type.RECORD) {
            List<Schema.Field> fields = avroSchema.getFields();
            ObjectProperty objectProperty = new ObjectProperty();
            fields.stream()
                    .forEach(field -> objectProperty.property(field.name(), fieldProperty(field)));
            return objectProperty;
        }

        return stringProperty();
    }

    private Property fieldProperty(Schema.Field field) {
        //TODO: handle types ENUM, ARRAY, MAP, UNION, FIXED, BOOLEAN, NULL;

        switch(field.schema().getType()) {
            case STRING:
                return stringProperty();
            case INT:
            case LONG:
            case FLOAT:
            case BYTES:
            case DOUBLE:
                return new IntegerProperty()._default(0);
        }
        return stringProperty();
    }

    private StringProperty stringProperty() {
        return new StringProperty()
                .required(true)
                ._default("")
                .example("");
    }
}
