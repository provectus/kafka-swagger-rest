package com.provectus.kafka.swagger.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;

@RequiredArgsConstructor
@Configuration
@ComponentScan
public class SwaggerConfig implements WebFluxConfigurer {

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
