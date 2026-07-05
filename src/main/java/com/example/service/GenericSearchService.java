package com.example.service;

import com.example.config.FieldConfig;
import com.example.config.ResourceConfig;
import com.example.config.ResourceRegistry;
import com.example.domain.Account;
import com.example.domain.Contact;
import com.example.exception.InvalidFieldException;
import com.example.exception.InvalidOperatorException;
import com.example.exception.InvalidResourceException;
import com.example.model.PageRequest;
import com.example.model.SearchFilter;
import com.example.model.SearchRequest;
import com.example.model.SearchSort;
import com.example.query.QueryBuilder;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class GenericSearchService {

    private final ResourceRegistry resourceRegistry;
    private final QueryBuilder queryBuilder;
    private final EntityManager entityManager;

    public GenericSearchService(ResourceRegistry resourceRegistry, QueryBuilder queryBuilder, EntityManager entityManager) {
        this.resourceRegistry = resourceRegistry;
        this.queryBuilder = queryBuilder;
        this.entityManager = entityManager;
    }

    @Transactional
    public List<? extends PanacheEntityBase> search(String resourceName, SearchRequest request) {
        ResourceConfig resource = resourceRegistry.getResource(resourceName);
        if (resource == null) {
            throw new InvalidResourceException("Unknown resource: " + resourceName);
        }

        validateRequest(resource, request);

        Class<? extends PanacheEntityBase> entityClass = resolveEntityClass(resource);
        QueryBuilder.QuerySpec querySpec = queryBuilder.build(entityClass, resource, request);

        PageRequest pageRequest = request.getPage();
        int page = pageRequest == null || pageRequest.getPage() < 0 ? 0 : pageRequest.getPage();
        int pageSize = pageRequest == null || pageRequest.getPageSize() <= 0 ? 20 : Math.min(pageRequest.getPageSize(), resource.getMaxPageSize());

        return executeQuery(entityClass, querySpec, page, pageSize);
    }

    private void validateRequest(ResourceConfig resource, SearchRequest request) {
        if (request == null) {
            return;
        }

        List<SearchFilter> filters = request.getFilters();
        if (filters != null) {
            for (SearchFilter filter : filters) {
                if (filter == null) {
                    continue;
                }
                validateField(resource, filter.getField());
                validateOperator(resource, filter.getOperator());
            }
        }

        List<SearchSort> sorts = request.getSorts();
        if (sorts != null) {
            for (SearchSort sort : sorts) {
                if (sort == null) {
                    continue;
                }
                validateSortableField(resource, sort.getField());
            }
        }
    }

    private void validateField(ResourceConfig resource, String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return;
        }
        boolean allowed = resource.getFields().stream()
            .map(FieldConfig::getName)
            .anyMatch(field -> field.equals(fieldName));
        if (!allowed) {
            throw new InvalidFieldException("Field not allowed: " + fieldName);
        }
    }

    private void validateSortableField(ResourceConfig resource, String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return;
        }
        boolean allowed = resource.getSortableFields().stream()
            .anyMatch(field -> field.equals(fieldName));
        if (!allowed) {
            throw new InvalidFieldException("Sort field not allowed: " + fieldName);
        }
    }

    private void validateOperator(ResourceConfig resource, String operator) {
        if (operator == null || operator.isBlank()) {
            return;
        }
        boolean allowed = resource.getAllowedOperators().stream()
            .anyMatch(candidate -> candidate.equalsIgnoreCase(operator));
        if (!allowed) {
            throw new InvalidOperatorException("Operator not allowed: " + operator);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends PanacheEntityBase> resolveEntityClass(ResourceConfig resource) {
        String entityClassName = resource.getEntityClass();
        if (Contact.class.getName().equals(entityClassName)) {
            return (Class<? extends PanacheEntityBase>) Contact.class;
        }
        if (Account.class.getName().equals(entityClassName)) {
            return (Class<? extends PanacheEntityBase>) Account.class;
        }
        throw new InvalidResourceException("Unsupported entity class: " + entityClassName);
    }

    @SuppressWarnings("unchecked")
    private List<? extends PanacheEntityBase> executeQuery(Class<? extends PanacheEntityBase> entityClass,
                                                           QueryBuilder.QuerySpec querySpec,
                                                           int page,
                                                           int pageSize) {
        TypedQuery<PanacheEntityBase> query = (TypedQuery<PanacheEntityBase>) entityManager.createQuery(querySpec.getJpql(), entityClass);
        for (int index = 0; index < querySpec.getParameters().size(); index++) {
            query.setParameter(index + 1, querySpec.getParameters().get(index));
        }
        return query.setFirstResult(page * pageSize)
            .setMaxResults(pageSize)
            .getResultList();
    }
}
