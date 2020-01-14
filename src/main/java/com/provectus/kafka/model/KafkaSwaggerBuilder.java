package com.provectus.kafka.model;

import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import com.provectus.kafka.model.schema.KafkaSwaggerSchema;
import com.provectus.kafka.model.schema.TopicParamSchema;
import com.provectus.kafka.model.schema.TopicSwaggerSchema;
import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.*;
import org.apache.avro.Schema;

import java.util.List;

public class KafkaSwaggerBuilder {

    private Swagger swagger = new Swagger();
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

        swagger.addDefinition("ProducerResult", producerResult());

        return this;
    }

    private Model producerResult() {
        return new ModelImpl()
                .type("object")
                .property("offset", new LongProperty())
                .property("timestamp", new LongProperty())
                .property("serializedKeySize", new LongProperty())
                .property("serializedValueSize", new LongProperty())
                .property("partition", new IntegerProperty())
                .property("topic", new StringProperty());
    }

    private KafkaSwaggerBuilder topicApi(TopicSwaggerSchema topicSwaggerSchema) {
        String modelName = topicSwaggerSchema.getTopic() + "Model";
        String modelNameKeyValue = modelName + "KeyValue";
        String topicPathKeyValue = "/kafka/" + kafkaSwaggerConfig.getGroupName() + "/topics/kv/" + topicSwaggerSchema.getTopic();
        String topicPath = "/kafka/" + kafkaSwaggerConfig.getGroupName() + "/topics/" + topicSwaggerSchema.getTopic();

        Operation op = new Operation()
                .tag("/kafka/" + kafkaSwaggerConfig.getGroupName())
                .operationId("post" + topicSwaggerSchema.getTopic())
                .consumes("application/json")
                .parameter(new BodyParameter()
                        .name("message")
                        .description("message")
                        .schema(new RefModel(modelName)))
                .response(200, new Response()
                        .description("OK").responseSchema(new RefModel("ProducerResult")));

        Operation opKeyValue = new Operation()
                .tags(op.getTags())
                .operationId("post" + topicSwaggerSchema.getTopic()+"KeyValue")
                .consumes(op.getConsumes())
                .parameter(new BodyParameter()
                        .name("message")
                        .description("message")
                        .schema(new RefModel(modelNameKeyValue)));
        opKeyValue.setResponses(op.getResponses());


        swagger.path(topicPath, new Path().post(op));
        swagger.addDefinition(modelName, model(topicSwaggerSchema.getValueSchema()));
        swagger.path(topicPathKeyValue, new Path().post(opKeyValue));
        swagger.addDefinition(modelNameKeyValue,
                new ModelImpl()
                        .type("object")
                        .property("key", property(topicSwaggerSchema.getKeySchema()))
                        .property("value", new RefProperty(modelName)));



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

    private Model model(TopicParamSchema topicParamSchema) {
        switch (topicParamSchema.getType()) {
            case STRING:
                return new ModelImpl().type("string");
            case AVRO:
                return avroModel(topicParamSchema.getAvroSchema().getAvroSchema());
            default:
                throw new RuntimeException("Unknown TopicParamSchema type: " + topicParamSchema.getType());
        }
    }

    private Model avroModel(Schema avroSchema) {
        List<Schema.Field> fields = avroSchema.getFields();
        ModelImpl model = new ModelImpl();
        for (Schema.Field field : fields) {
            model.property(field.name(), fieldProperty(field.schema()));
        }
        return model;
    }

    private Property avroProperty(Schema avroSchema) {
        if (avroSchema.getType() == Schema.Type.RECORD) {
            List<Schema.Field> fields = avroSchema.getFields();
            ObjectProperty objectProperty = new ObjectProperty();
            for (Schema.Field field : fields) {
                objectProperty.property(field.name(), fieldProperty(field.schema()));
            }
            return objectProperty;
        } else {
            return fieldProperty(avroSchema);
        }
    }

    private Property fieldProperty(Schema schema) {
        //TODO: handle types MAP?, UNION, FIXED?, NULL;

        switch(schema.getType()) {
            case STRING:
                return stringProperty();
            case INT:
                return new IntegerProperty();
            case FIXED:
                return new LongProperty();
            case LONG:
                return new LongProperty();
            case FLOAT:
                return new FloatProperty();
            case BYTES:
                return stringProperty();
            case DOUBLE:
                return new DoubleProperty();
            case RECORD:
                return avroProperty(schema);
            case ARRAY:
                return new ArrayProperty(fieldProperty(schema.getElementType()));
            case BOOLEAN:
                return new BooleanProperty();
            case ENUM:
                return stringProperty()._enum(schema.getEnumSymbols());
            case MAP:
                return new MapProperty(fieldProperty(schema.getValueType()));
        }
        return stringProperty();
    }

    private StringProperty stringProperty() {
        return new StringProperty()
                .required(true)
                .example("");
    }
}
