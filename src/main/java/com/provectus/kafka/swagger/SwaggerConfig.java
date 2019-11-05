package com.provectus.kafka.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.regex;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket swaggerApi_v2_0() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName("kafka-0.1");

        docket.select()
                .apis(RequestHandlerSelectors.basePackage("com.provectus.kafka"))
                .paths(regex("/kafka/.*"))
                .build();

        docket.apiInfo(new ApiInfoBuilder()
                .version("0.1")
                .title("Kafka API").build());

        return docket;
    }
}
