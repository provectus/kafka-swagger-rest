FROM openjdk:11.0.3-slim
VOLUME /tmp
ARG JAR_FILE
COPY "/target/${JAR_FILE}" "/${JAR_FILE}"

EXPOSE 8080
CMD java -jar kafka-swagger-rest-0.1-SNAPSHOT.jar