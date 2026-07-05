package com.example.config;

import com.example.exception.ConfigurationException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class MetadataBootstrap {

    private final YamlLoader yamlLoader;
    private final ConfigurationValidator configurationValidator;
    private final ResourceRegistry resourceRegistry;

    public MetadataBootstrap(YamlLoader yamlLoader,
                             ConfigurationValidator configurationValidator,
                             ResourceRegistry resourceRegistry) {
        this.yamlLoader = yamlLoader;
        this.configurationValidator = configurationValidator;
        this.resourceRegistry = resourceRegistry;
    }

    @PostConstruct
    void initialize() {
        List<ResourceConfig> resources = yamlLoader.loadResources("metadata.yml");
        configurationValidator.validate(resources);
        resourceRegistry.initialize(resources);

        if (resourceRegistry.getResource("contacts") == null) {
            throw new ConfigurationException("Expected contacts resource to be loaded");
        }
    }
}
