package com.example.query;

import com.example.config.ResourceConfig;
import com.example.model.SearchFilter;
import com.example.model.SearchRequest;
import com.example.model.SearchSort;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class QueryBuilder {

    public QuerySpec build(Class<? extends PanacheEntityBase> entityClass, ResourceConfig resource, SearchRequest request) {
        StringBuilder jpql = new StringBuilder("from ").append(entityClass.getSimpleName());
        List<Object> parameters = new ArrayList<>();

        if (request != null && request.getFilters() != null && !request.getFilters().isEmpty()) {
            List<String> predicates = new ArrayList<>();
            for (SearchFilter filter : request.getFilters()) {
                if (filter == null) {
                    continue;
                }
                predicates.add(buildPredicate(filter, parameters));
            }
            if (!predicates.isEmpty()) {
                jpql.append(" where ").append(String.join(" and ", predicates));
            }
        }

        if (request != null && request.getSorts() != null && !request.getSorts().isEmpty()) {
            jpql.append(buildSortClauseFromSearchSorts(request.getSorts()));
        } else if (resource != null && resource.getDefaultSort() != null && !resource.getDefaultSort().isEmpty()) {
            jpql.append(buildSortClauseFromDefaultSort(resource.getDefaultSort()));
        }

        return new QuerySpec(jpql.toString(), parameters);
    }

    private String buildPredicate(SearchFilter filter, List<Object> parameters) {
        String field = filter.getField();
        String operator = filter.getOperator();
        String value = filter.getValue();

        if ("eq".equalsIgnoreCase(operator)) {
            parameters.add(value);
            return field + " = ?" + parameters.size();
        }

        if ("contains".equalsIgnoreCase(operator)) {
            parameters.add("%" + value + "%");
            return field + " like ?" + parameters.size();
        }

        throw new IllegalArgumentException("Unsupported operator: " + operator);
    }

    private String buildSortClauseFromSearchSorts(List<SearchSort> sorts) {
        StringBuilder clause = new StringBuilder(" order by ");
        List<String> entries = new ArrayList<>();
        for (SearchSort sort : sorts) {
            if (sort == null) {
                continue;
            }
            String direction = "desc".equalsIgnoreCase(sort.getDirection()) ? "desc" : "asc";
            entries.add(sort.getField() + " " + direction);
        }
        return clause.append(String.join(", ", entries)).toString();
    }

    private String buildSortClauseFromDefaultSort(List<String> defaultSort) {
        StringBuilder clause = new StringBuilder(" order by ");
        List<String> entries = new ArrayList<>();
        for (String sortEntry : defaultSort) {
            if (sortEntry == null || sortEntry.isBlank()) {
                continue;
            }
            String[] parts = sortEntry.split(":", 2);
            String field = parts[0];
            String direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? "desc" : "asc";
            entries.add(field + " " + direction);
        }
        return clause.append(String.join(", ", entries)).toString();
    }

    public static class QuerySpec {
        private final String jpql;
        private final List<Object> parameters;

        public QuerySpec(String jpql, List<Object> parameters) {
            this.jpql = jpql;
            this.parameters = parameters;
        }

        public String getJpql() {
            return jpql;
        }

        public List<Object> getParameters() {
            return parameters;
        }
    }
}
