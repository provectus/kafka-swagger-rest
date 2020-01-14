package com.provectus.kafka.swagger.config;

import com.provectus.kafka.service.KafkaSwaggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.swagger.web.*;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@ApiIgnore
@RequestMapping("/swagger")
public class SwaggerApiRestController {

    private final SwaggerYamlResourceProvider swaggerResources;

    private final KafkaSwaggerService kafkaSwaggerService;

    @GetMapping("/")
    public ResponseEntity<Object> swaggerRoot() {
        ResponseEntity<Object> responseEntity = ResponseEntity.status(HttpStatus.SEE_OTHER)
                .header(HttpHeaders.LOCATION, "/swagger/swagger-ui.html")
                .build();

        return responseEntity;
    }

    @GetMapping(value = "/swagger-resources/configuration/security")
    public SecurityConfiguration securityConfiguration() {
        return SecurityConfigurationBuilder.builder()
                .clientId("clientId")
                .build();
    }

    @GetMapping(value = "/swagger-resources/configuration/ui")
    public UiConfiguration uiConfiguration() {
        return UiConfigurationBuilder.builder().build();
    }

    @GetMapping(value = "/swagger-resources")
    public List<SwaggerResource> swaggerResources() {
        return swaggerResources.get();
    }

    @GetMapping(value = "/api/{fileName}.yaml")
    public ResponseEntity<String> specYaml(@PathVariable String fileName) throws IOException {
        String yaml = processYaml(new ClassPathResource(String.format("swagger/%s.yaml", fileName)));

        return ResponseEntity.of(Optional.of(yaml));
    }

    @GetMapping(value = "/api/{kafkaGroupName}")
    public ResponseEntity<String> kafkaSpecYaml(@PathVariable String kafkaGroupName) throws IOException {
        String specYaml = kafkaSwaggerService.getSwaggerSpec(kafkaGroupName);

        return ResponseEntity.of(Optional.of(specYaml));
    }

    @GetMapping("/csrf")
    public ResponseEntity csrf() {
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    private String processYaml(Resource resource) throws IOException {
        try (Reader reader = new UnicodeReader(resource.getInputStream())) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlMaps = override(new HashMap<>(), yaml.load(reader));
            return yaml.dump(yamlMaps);
        }
    }

    private Map<String, Object> override(Map<String, Object> from, Map<String, Object> in) {
        for (Map.Entry<String, Object> entry : from.entrySet()) {
            if (entry.getValue() instanceof Map) {
                if (in.containsKey(entry.getKey()) && in.get(entry.getKey()) instanceof Map) {
                    in.put(entry.getKey(), override((Map<String, Object>)entry.getValue(), (Map<String, Object>)in.get(entry.getKey())));
                } else {
                    in.put(entry.getKey(), entry.getValue());
                }
            } else {
                in.put(entry.getKey(), entry.getValue());
            }
        }
        return in;
    }
}