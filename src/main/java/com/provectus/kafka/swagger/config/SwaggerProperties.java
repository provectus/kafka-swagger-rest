package com.provectus.kafka.swagger.config;

import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;

@ConfigurationProperties("swagger")
@Component
@Setter
@Getter
public class SwaggerProperties {

    private List<KafkaSwaggerConfig> kafka = new ArrayList<>();
}