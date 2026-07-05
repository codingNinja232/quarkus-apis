package com.example.config;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class ResourceRegistryTest {

    @Inject
    ResourceRegistry resourceRegistry;

    @Test
    void loadsMetadataResourcesAtRuntime() {
        assertNotNull(resourceRegistry.getResource("contacts"));
        assertNotNull(resourceRegistry.getResource("accounts"));
    }
}
