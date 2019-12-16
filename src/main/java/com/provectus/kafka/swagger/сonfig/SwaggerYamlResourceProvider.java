package com.provectus.kafka.swagger.сonfig;

import com.provectus.kafka.service.KafkaSwaggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
@Primary
public class SwaggerYamlResourceProvider implements SwaggerResourcesProvider {

    private final ResourcePatternResolver resourcePatternResolver;

    private final KafkaSwaggerService kafkaSwaggerService;

    @Override
    public List<SwaggerResource> get() {
        try {
            List<SwaggerResource> swaggerResources = new ArrayList<>();

            swaggerResources.addAll(Arrays.stream(resourcePatternResolver.getResources("classpath*:/swagger/*.yaml"))
                    .map(resource -> {
                        SwaggerResource swaggerResource = new SwaggerResource();
                        swaggerResource.setLocation("/api/" + resource.getFilename());
                        swaggerResource.setName(resource.getFilename());
                        swaggerResource.setUrl("/api/" + resource.getFilename());
                        return swaggerResource;
                    })
                    .collect(Collectors.toList()));

            kafkaSwaggerService.getKafkaSwaggers().stream()
                    .forEach(kafkaSwagger -> {
                        SwaggerResource swaggerResource = new SwaggerResource();
                        swaggerResource.setLocation("/api/" + kafkaSwagger.getConfig().getGroupName());
                        swaggerResource.setName(kafkaSwagger.getConfig().getGroupName());
                        swaggerResource.setUrl("/api/" + kafkaSwagger.getConfig().getGroupName());

                        swaggerResources.add(swaggerResource);
                    });

            return swaggerResources;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
