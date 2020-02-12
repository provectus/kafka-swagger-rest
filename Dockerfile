FROM provectuslabs/kafka-cmds

ARG JAR_FILE
COPY "/target/${JAR_FILE}" "/${JAR_FILE}"

COPY create-kafka-entities.sh /

EXPOSE 8080
CMD /create-kafka-entities.sh && java -jar kafka-swagger-rest-0.1-SNAPSHOT.jar