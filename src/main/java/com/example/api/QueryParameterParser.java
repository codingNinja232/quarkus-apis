package com.example.api;

import com.example.model.PageRequest;
import com.example.model.SearchFilter;
import com.example.model.SearchRequest;
import com.example.model.SearchSort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses HTTP query parameters into a SearchRequest object.
 *
 * This parser converts URL query parameters into an internal SearchRequest model,
 * enabling filtering, sorting, and pagination through simple query string parameters.
 *
 * Query parameter format:
 * - Filters: Field names as parameters (e.g., ?name=John&email=jane@example.com)
 *            Uses 'eq' operator by default; only allows searchable fields from metadata
 * - ID Filter: Special parameter ?id=123 to retrieve a single resource by primary key
 * - Sort: Format 'fieldName:direction' (e.g., ?sort=name:asc,email:desc)
 *         Comma-separated for multiple sort fields
 * - Pagination: ?page=0&pageSize=20 (defaults: page=0, pageSize=20)
 *
 * Example URLs:
 * - GET /api/contacts?name=John&sort=name:asc&page=0&pageSize=10
 * - GET /api/accounts?id=42
 * - GET /api/contacts?sort=email:desc
 */
@ApplicationScoped
public class QueryParameterParser {

    // Query parameter names
    private static final String SORT_PARAM = "sort";
    private static final String PAGE_PARAM = "page";
    private static final String PAGE_SIZE_PARAM = "pageSize";
    private static final String ID_PARAM = "id";

    // Sort field and direction separator
    private static final String SORT_SEPARATOR = ":";

    /**
     * Parses URL query parameters and builds a SearchRequest object.
     *
     * @param queryParams Map of query parameter names to values
     * @param searchableFields List of fields that are allowed for filtering
     * @return SearchRequest object configured with parsed filters, sorts, and pagination
     */
    public SearchRequest parseQueryParameters(Map<String, String> queryParams, List<String> searchableFields) {
        SearchRequest request = new SearchRequest();

        // Collect all filters from query parameters
        List<SearchFilter> filters = new ArrayList<>();

        // Handle ID parameter specially: if present, it becomes an exact match filter
        // ID is always filterable with 'eq' operator, even if not in searchableFields
        String idValue = queryParams.get(ID_PARAM);
        if (idValue != null && !idValue.isBlank()) {
            SearchFilter idFilter = new SearchFilter();
            idFilter.setField("id");
            idFilter.setOperator("eq");
            idFilter.setValue(idValue);
            filters.add(idFilter);
        }

        // Parse filters from searchable fields in metadata
        // Only fields listed in metadata's searchableFields are allowed
        for (String fieldName : searchableFields) {
            String value = queryParams.get(fieldName);
            if (value != null && !value.isBlank()) {
                SearchFilter filter = new SearchFilter();
                filter.setField(fieldName);
                filter.setOperator("eq");  // Default operator for GET requests
                filter.setValue(value);
                filters.add(filter);
            }
        }

        // Attach filters to request only if any were found
        if (!filters.isEmpty()) {
            request.setFilters(filters);
        }

        // Parse sort specification
        // Format: field1:asc,field2:desc
        // Can include multiple sort fields separated by commas
        String sortParam = queryParams.get(SORT_PARAM);
        if (sortParam != null && !sortParam.isBlank()) {
            List<SearchSort> sorts = new ArrayList<>();
            String[] sortSpecs = sortParam.split(",");
            for (String spec : sortSpecs) {
                SearchSort sort = parseSortSpec(spec);
                if (sort != null) {
                    sorts.add(sort);
                }
            }
            if (!sorts.isEmpty()) {
                request.setSorts(sorts);
            }
        }

        // Parse pagination parameters with sensible defaults
        // Default: page 0, 20 items per page
        PageRequest pageRequest = new PageRequest();
        String pageStr = queryParams.get(PAGE_PARAM);
        pageRequest.setPage(pageStr != null ? Integer.parseInt(pageStr) : 0);
        String pageSizeStr = queryParams.get(PAGE_SIZE_PARAM);
        pageRequest.setPageSize(pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 20);
        request.setPage(pageRequest);

        return request;
    }

    /**
     * Parses a single sort specification string.
     *
     * Expected format: "fieldName:direction" where direction is 'asc' or 'desc'
     * Example: "name:asc" or "email:desc"
     *
     * @param spec The sort specification string
     * @return SearchSort object, or null if format is invalid
     */
    private SearchSort parseSortSpec(String spec) {
        // Validate input
        if (spec == null || spec.isBlank()) {
            return null;
        }

        // Parse field name and direction
        String[] parts = spec.trim().split(SORT_SEPARATOR);
        if (parts.length != 2) {
            // Invalid format; skip this sort specification
            return null;
        }

        // Create sort object
        SearchSort sort = new SearchSort();
        sort.setField(parts[0].trim());
        sort.setDirection(parts[1].trim().toLowerCase());
        return sort;
    }
}
