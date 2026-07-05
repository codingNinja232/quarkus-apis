package com.example.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class YamlLoader {

    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    @PostConstruct
    void init() {
        // Marker for lifecycle readiness.
    }

    public List<ResourceConfig> loadResources(String resourcePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Metadata file not found: " + resourcePath);
            }
            Map<String, List<ResourceConfig>> root = objectMapper.readValue(inputStream, new TypeReference<>() {
            });
            return root.getOrDefault("resources", List.of());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load metadata file: " + resourcePath, exception);
        }
    }
}
