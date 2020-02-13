# Docker compose examples
    
    Run Kafka Server & Kafka Swagger Rest in one container
    docker-compose -f kafka_schema-registry_kafka-swagger-rest.yml up
        
    Run Kafka Server & Kafka Swagger Rest in two containers
    docker-compose -f kafka_schema-registry.yml up
    docker-compose -f kafka-swagger-rest.yml up
    
## kafka-string-topics:

    docker-compose -f ./kafka-string-topics/kafka_schema-registry_kafka-swagger-rest.yml up

    zookeeper (localhost)
    kafka (localhost)
    schema-registry (localhost)
    kafka-swagger-rest (localhost)
    
    init topics: users, messages 

## kafka-avro-topics

    docker-compose -f ./kafka-avro-topics/kafka_schema-registry_kafka-swagger-rest.yml up
        
    zookeeper (localhost)
    kafka (localhost)
    schema-registry (localhost)
    kafka-swagger-rest (localhost)
    
    init topics: users
    init schema: User
        
## kafka-swagger-rest.yml
    kafka-swagger-rest (localhost)
