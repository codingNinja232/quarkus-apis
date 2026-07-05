package com.example.config;

import java.util.List;

public class ResourceConfig {

    private String name;
    private String entityClass;
    private String endpoint;
    private List<String> searchableFields;
    private List<String> sortableFields;
    private List<String> defaultSort;
    private List<JoinConfig> joins;
    private List<String> allowedOperators;
    private List<String> displayFields;
    private int maxPageSize;
    private List<FieldConfig> fields;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(String entityClass) {
        this.entityClass = entityClass;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<String> getSearchableFields() {
        return searchableFields;
    }

    public void setSearchableFields(List<String> searchableFields) {
        this.searchableFields = searchableFields;
    }

    public List<String> getSortableFields() {
        return sortableFields;
    }

    public void setSortableFields(List<String> sortableFields) {
        this.sortableFields = sortableFields;
    }

    public List<String> getDefaultSort() {
        return defaultSort;
    }

    public void setDefaultSort(List<String> defaultSort) {
        this.defaultSort = defaultSort;
    }

    public List<JoinConfig> getJoins() {
        return joins;
    }

    public void setJoins(List<JoinConfig> joins) {
        this.joins = joins;
    }

    public List<String> getAllowedOperators() {
        return allowedOperators;
    }

    public void setAllowedOperators(List<String> allowedOperators) {
        this.allowedOperators = allowedOperators;
    }

    public List<String> getDisplayFields() {
        return displayFields;
    }

    public void setDisplayFields(List<String> displayFields) {
        this.displayFields = displayFields;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public List<FieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<FieldConfig> fields) {
        this.fields = fields;
    }
}
