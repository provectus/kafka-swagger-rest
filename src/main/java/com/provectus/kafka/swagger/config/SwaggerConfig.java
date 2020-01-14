package com.provectus.kafka.swagger.config;

import com.provectus.kafka.model.config.KafkaSwaggerConfig;
import com.provectus.kafka.service.KafkaSwaggerService;
import com.provectus.kafka.service.impl.KafkaSwaggerServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;

//import springfox.documentation.swagger.web.*;

@RequiredArgsConstructor
@Configuration
@ComponentScan
public class SwaggerConfig implements WebFluxConfigurer {

    @Bean
    @ConfigurationProperties("app.kafka")
    public KafkaSwaggerConfig kafkaSwaggerConfig() {
        return new KafkaSwaggerConfig();
    }

    @Bean
    public KafkaSwaggerService kafkaSwaggerService() {
        return new KafkaSwaggerServiceImpl(kafkaSwaggerConfig());
    }

    @Bean
    @ConditionalOnMissingBean
    public UiConfiguration uiConfiguration() {
        return UiConfigurationBuilder.builder()
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityConfiguration securityConfiguration() {
        return SecurityConfigurationBuilder.builder()
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        SwaggerConfig.addSwaggerResourceHandlers(registry);
    }

    private static void addSwaggerResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger/swagger-ui.html**")
                .addResourceLocations("classpath:/META-INF/resources/swagger-ui.html");
        registry.addResourceHandler("/swagger/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
