# Kafka Swagger Rest
Kafka Swagger Rest is a Swagger API that allows to send data to kafka topics via Restful api

Supports 2 types of message format for topic:
- plain text
- avro

## Configuration
Docker compose example:

    version: '2'
    services:
      kafka-swagger-rest:
        image: kafka-swagger-rest:0.1-SNAPSHOT-latest
        ports:
          - 8080:8080
        command: [ "java", "-Dreactor.netty.http.server.accessLogEnabled=true",
                   "-jar", "/kafka-swagger-rest-0.1-SNAPSHOT.jar"]
        environment:
          server.port: '8080'
    
          swagger_kafka[0]_groupName: 'localhost-kafka'
          swagger_kafka[0]_bootstrapServers: 'kafka:29092'
          swagger_kafka[0]_schemaRegistryUrl: 'http://host.docker.internal:8081'
          
          swagger.kafka[0].consumerConfig."[enable.auto.commit]": 'true'
          swagger.kafka[0].consumerConfig."[auto.commit.interval.ms]": '100'
          
          swagger.kafka[0].producerConfig."[retries]": '0'
          swagger.kafka[0].producerConfig."[batch.size]": '16384'
          
          swagger.kafka[0].topicConfig[0].topicName: 'users-avro2'
          swagger.kafka[0].topicConfig[0].autofillKeyParamName: 'ID'


| key | description |
| --- | ----------- |
| swagger_kafka[0]_groupName | groupName for swagger spec |
| swagger_kafka[0]_bootstrapServers | kafka bootstrapServers |
| swagger_kafka[0]_schemaRegistryUrl | url to kafka-schema-registry |
| --- | --- |
| swagger.kafka[0].consumerConfig.* | Group of configs for consumer. Consumer is used to read schemas from kafka topic '_schemas'. http://kafka.apache.org/documentation.html#consumerconfigs - here you can find all config keys for consumer config |
| swagger.kafka[0].producerConfig.* | Group of configs for producer. Producer is used to send data into kafka topics. http://kafka.apache.org/documentation.html#producerconfigs - here you can find all config keys for producer config |  
| --- | --- |
| swagger.kafka[0].topicConfig[0] | Topic Config |
| swagger.kafka[0].topicConfig[0].topicName | Topic Name |
| swagger.kafka[0].topicConfig[0].autofillKeyParamName | ParamName to autofill Key from Value data. If topic message is Avro object and you want to autofill key from some field (like: ID) then you need to set this param value to "ID" and key value will be autofilled from "ID" |

## How to build
Required:
- java 11 +
- docker


    run ./mvnw clean install
    
Build results:
- docker image: kafka-swagger-rest:0.1-SNAPSHOT-latest

## Quick start in docker:

    build application: ./mvnw clean install
    docker-compose -f ./docker/kafka_schema-registry_kafka-swagger-rest.yml up
    open in browser: http://localhost:8080/swagger/swagger-ui.html

