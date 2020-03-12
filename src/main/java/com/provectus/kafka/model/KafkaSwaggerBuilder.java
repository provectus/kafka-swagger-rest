package com.provectus.kafka.model;

import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import com.provectus.kafka.model.schema.KafkaSwaggerSchema;
import com.provectus.kafka.model.schema.TopicParamSchema;
import com.provectus.kafka.model.schema.TopicSwaggerSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Arrays;
import java.util.List;

public class KafkaSwaggerBuilder {

    private OpenAPI openAPI = new OpenAPI();
    private KafkaSwaggerConfig kafkaSwaggerConfig;

    public OpenAPI build(KafkaSwaggerConfig kafkaSwaggerConfig, KafkaSwaggerSchema kafkaSwaggerSchema) {
        this.kafkaSwaggerConfig = kafkaSwaggerConfig;

        kafkaInfo("/kafka/" + kafkaSwaggerConfig.getGroupName());

        kafkaSwaggerSchema.getTopics().values().stream()
                .forEach(this::topicApi);

        return openAPI;
    }

    private KafkaSwaggerBuilder kafkaInfo(String tag) {
        openAPI.info(
                new Info()
                        .title(tag + " kafka swagger")
                        .version("1.0"))
                .addTagsItem(new Tag().name(tag));
        openAPI.servers(Arrays.asList(new Server().url("/")));

        openAPI.setComponents(new Components());
        openAPI.getComponents().addSchemas("ProducerResult", producerResult());

        return this;
    }

    private Schema producerResult() {
        return new Schema()
                .type("object")
                .addProperties("offset", new IntegerSchema())
                .addProperties("timestamp", new IntegerSchema())
                .addProperties("serializedKeySize", new IntegerSchema())
                .addProperties("serializedValueSize", new IntegerSchema())
                .addProperties("partition", new IntegerSchema())
                .addProperties("topic", new StringSchema());
    }

    private KafkaSwaggerBuilder topicApi(TopicSwaggerSchema topicSwaggerSchema) {
        String modelName = topicSwaggerSchema.getTopic() + "Value";
        String modelNameKeyValue = modelName + "KeyValue";
        String topicPathKeyValue = "/kafka/" + kafkaSwaggerConfig.getGroupName() + "/topics/kv/" + topicSwaggerSchema.getTopic();
        String topicPath = "/kafka/" + kafkaSwaggerConfig.getGroupName() + "/topics/" + topicSwaggerSchema.getTopic();

        Operation op = new Operation()
                .addTagsItem("/kafka/" + kafkaSwaggerConfig.getGroupName())
                .operationId("post" + topicSwaggerSchema.getTopic())
                .requestBody(new RequestBody()
                        .description("message")
                        .content(new Content().addMediaType("application/json",
                                new MediaType().schema(new Schema().$ref(modelName)))))
                .responses(new ApiResponses()
                        .addApiResponse("200",
                                new ApiResponse().description("OK")
                                        .content(new Content().addMediaType("application/json",
                                                new MediaType().schema(new Schema().$ref("ProducerResult"))))
                        ));

        Operation opKeyValue = new Operation()
                .tags(op.getTags())
                .operationId("post" + topicSwaggerSchema.getTopic() + "KeyValue")
                .requestBody(new RequestBody()
                        .description("message")
                        .content(new Content().addMediaType("application/json",
                                new MediaType().schema(new Schema().$ref(modelNameKeyValue)))))
                .responses(op.getResponses());
        opKeyValue.setResponses(op.getResponses());


        openAPI.path(topicPath, new PathItem().post(op));
        openAPI.getComponents().addSchemas(modelName, topicSchema(topicSwaggerSchema.getValueSchema()));
        openAPI.path(topicPathKeyValue, new PathItem().post(opKeyValue));
        openAPI.getComponents().addSchemas(modelNameKeyValue,
                new ObjectSchema()
                        .type("object")
                        .addProperties("key", topicSchema(topicSwaggerSchema.getKeySchema()))
                        .addProperties("value", new Schema().$ref(modelName)));

        return this;
    }

    private Schema topicSchema(TopicParamSchema topicParamSchema) {
        switch (topicParamSchema.getType()) {
            case STRING:
                return stringSchema();
            case AVRO:
                return buildSwaggerSchema(topicParamSchema.getAvroSchema().getAvroSchema());
            default:
                throw new RuntimeException("Unknown TopicParamSchema type: " + topicParamSchema.getType());
        }
    }


    private Schema buildSwaggerSchema(org.apache.avro.Schema avroSchema) {
        Schema schema = openAPI.getComponents().getSchemas().get(avroSchema.getName() + "Record");
        if (schema != null) return new Schema().$ref(avroSchema.getName() + "Record");

        switch (avroSchema.getType()) {
            case STRING:
            case BYTES:
                return stringSchema().name(avroSchema.getName());
            case INT:
            case LONG:
            case FIXED:
                return new IntegerSchema().name(avroSchema.getName());
            case FLOAT:
            case DOUBLE:
                return new NumberSchema().name(avroSchema.getName());
            case RECORD:
                ObjectSchema objectSchema = new ObjectSchema();
                objectSchema.setName(avroSchema.getName());
                openAPI.getComponents().addSchemas(avroSchema.getName() + "Record", objectSchema);

                List<org.apache.avro.Schema.Field> fields = avroSchema.getFields();
                for (org.apache.avro.Schema.Field field : fields) {
                    objectSchema.addProperties(field.name(), buildSwaggerSchema(field.schema()));
                }

                return objectSchema;
            case UNION:
                ComposedSchema oneOfSchema = new ComposedSchema();
                oneOfSchema.name(avroSchema.getName());
                for (org.apache.avro.Schema typeSchema : avroSchema.getTypes()) {
                    oneOfSchema.addOneOfItem(buildSwaggerSchema(typeSchema));
                    if (typeSchema.getType() == org.apache.avro.Schema.Type.NULL) {
                        oneOfSchema.example("null");
                    }
                }
                return oneOfSchema;
            case ARRAY:
                return new ArraySchema()
                        .items(buildSwaggerSchema(avroSchema.getElementType()))
                        .name(avroSchema.getName());
            case BOOLEAN:
                return new BooleanSchema()
                        .name(avroSchema.getName());
            case ENUM:
                return stringSchema()
                        ._enum(avroSchema.getEnumSymbols())
                        .name(avroSchema.getName());
            case MAP:
                return new MapSchema()
                        .addProperties(avroSchema.getName(), buildSwaggerSchema(avroSchema.getValueType()))
                        .name(avroSchema.getName());
            case NULL:
                return stringSchema()
                        ._enum(Arrays.asList("null"))
                        .nullable(true)
                        .name(avroSchema.getName());
        }
        return stringSchema();
    }

    private StringSchema stringSchema() {
        StringSchema stringSchema = new StringSchema();

        stringSchema.example("");

        return stringSchema;
    }
}
