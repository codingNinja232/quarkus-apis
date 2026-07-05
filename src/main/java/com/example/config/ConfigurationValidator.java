package com.example.config;

import com.example.domain.Account;
import com.example.domain.Contact;
import com.example.exception.ConfigurationException;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ConfigurationValidator {

    public void validate(List<ResourceConfig> resources) {
        if (resources == null) {
            throw new ConfigurationException("No resources configured");
        }

        Set<String> resourceNames = new LinkedHashSet<>();
        Set<String> endpoints = new LinkedHashSet<>();

        for (ResourceConfig resource : resources) {
            validateRequired(resource.getName(), "resource name");
            validateRequired(resource.getEntityClass(), "entity class");
            validateRequired(resource.getEndpoint(), "endpoint");

            if (!resourceNames.add(resource.getName())) {
                throw new ConfigurationException("Duplicate resource: " + resource.getName());
            }
            if (!endpoints.add(resource.getEndpoint())) {
                throw new ConfigurationException("Duplicate endpoint: " + resource.getEndpoint());
            }

            validateEntity(resource.getEntityClass());
            validateFields(resource);
            validateOperators(resource);
            validateSort(resource);
            validateJoins(resource);
        }
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("Missing " + fieldName);
        }
    }

    private void validateEntity(String entityClass) {
        if (!Contact.class.getName().equals(entityClass) && !Account.class.getName().equals(entityClass)) {
            throw new ConfigurationException("Unknown entity: " + entityClass);
        }
    }

    private void validateFields(ResourceConfig resource) {
        if (resource.getFields() == null || resource.getFields().isEmpty()) {
            throw new ConfigurationException("No fields configured for resource: " + resource.getName());
        }

        for (FieldConfig field : resource.getFields()) {
            validateRequired(field.getName(), "field name for resource " + resource.getName());
            validateRequired(field.getType(), "field type for resource " + resource.getName());
        }
    }

    private void validateOperators(ResourceConfig resource) {
        if (resource.getAllowedOperators() == null || resource.getAllowedOperators().isEmpty()) {
            throw new ConfigurationException("No allowed operators configured for resource: " + resource.getName());
        }
    }

    private void validateSort(ResourceConfig resource) {
        if (resource.getDefaultSort() == null || resource.getDefaultSort().isEmpty()) {
            throw new ConfigurationException("No default sort configured for resource: " + resource.getName());
        }
    }

    private void validateJoins(ResourceConfig resource) {
        if (resource.getJoins() == null) {
            return;
        }
        for (JoinConfig join : resource.getJoins()) {
            validateRequired(join.getName(), "join name for resource " + resource.getName());
            validateRequired(join.getTargetEntity(), "join target entity for resource " + resource.getName());
            validateRequired(join.getSourceField(), "join source field for resource " + resource.getName());
            validateRequired(join.getTargetField(), "join target field for resource " + resource.getName());
        }
    }
}
