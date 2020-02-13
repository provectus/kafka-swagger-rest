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
