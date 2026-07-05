package com.example.config;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ResourceRegistry {

    private Map<String, ResourceConfig> resources = Collections.emptyMap();

    @Inject
    YamlLoader yamlLoader;

    public ResourceRegistry() {
        this.resources = Collections.unmodifiableMap(new LinkedHashMap<>());
    }

    @PostConstruct
    void initializeFromMetadata() {
        if (resources.isEmpty() && yamlLoader != null) {
            initialize(yamlLoader.loadResources("metadata.yml"));
        }
    }

    public ResourceRegistry(Map<String, ResourceConfig> resources) {
        this.resources = Collections.unmodifiableMap(new LinkedHashMap<>(resources));
    }

    public void initialize(List<ResourceConfig> resources) {
        Map<String, ResourceConfig> registry = new LinkedHashMap<>();
        for (ResourceConfig resource : resources) {
            if (resource != null && resource.getName() != null) {
                registry.put(resource.getName(), resource);
            }
        }
        this.resources = Collections.unmodifiableMap(registry);
    }

    public ResourceConfig getResource(String resourceName) {
        if (resources.isEmpty() && yamlLoader != null) {
            initialize(yamlLoader.loadResources("metadata.yml"));
        }
        return resources.get(resourceName);
    }

    public boolean exists(String resourceName) {
        return resources.containsKey(resourceName);
    }

    public FieldConfig getField(ResourceConfig resource, String fieldName) {
        if (resource == null || resource.getFields() == null) {
            return null;
        }
        return resource.getFields().stream()
            .filter(field -> field != null && fieldName.equals(field.getName()))
            .findFirst()
            .orElse(null);
    }
}
