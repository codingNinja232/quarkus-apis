package com.example.api;

import com.example.model.SearchFilter;
import com.example.model.SearchRequest;
import com.example.model.SearchSort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueryParameterParserTest {

    private final QueryParameterParser parser = new QueryParameterParser();

    @Test
    void parseEmptyQueryParametersReturnsDefaultPageRequest() {
        Map<String, String> params = Map.of();
        List<String> searchableFields = List.of("name", "email");
        
        SearchRequest request = parser.parseQueryParameters(params, searchableFields);
        
        assertNotNull(request);
        assertNull(request.getFilters());
        assertNull(request.getSorts());
        assertNotNull(request.getPage());
        assertEquals(0, request.getPage().getPage());
        assertEquals(20, request.getPage().getPageSize());
    }

    @Test
    void parseFilterQueryParametersCreatesFilters() {
        Map<String, String> params = Map.of("name", "John", "email", "john@example.com");
        List<String> searchableFields = List.of("name", "email");
        
        SearchRequest request = parser.parseQueryParameters(params, searchableFields);
        
        assertNotNull(request.getFilters());
        assertEquals(2, request.getFilters().size());
        
        SearchFilter nameFilter = request.getFilters().stream()
            .filter(f -> "name".equals(f.getField()))
            .findFirst()
            .orElse(null);
        assertNotNull(nameFilter);
        assertEquals("John", nameFilter.getValue());
        assertEquals("eq", nameFilter.getOperator());
    }

    @Test
    void parseSortQueryParametersCreatesSorts() {
        Map<String, String> params = Map.of("sort", "name:asc");
        List<String> searchableFields = List.of();
        
        SearchRequest request = parser.parseQueryParameters(params, searchableFields);
        
        assertNotNull(request.getSorts());
        assertEquals(1, request.getSorts().size());
        
        SearchSort sort = request.getSorts().get(0);
        assertEquals("name", sort.getField());
        assertEquals("asc", sort.getDirection());
    }

    @Test
    void parseMultipleSortParametersCreatesMultipleSorts() {
        Map<String, String> params = Map.of("sort", "name:asc,email:desc");
        List<String> searchableFields = List.of();
        
        SearchRequest request = parser.parseQueryParameters(params, searchableFields);
        
        assertNotNull(request.getSorts());
        assertEquals(2, request.getSorts().size());
        assertEquals("name", request.getSorts().get(0).getField());
        assertEquals("asc", request.getSorts().get(0).getDirection());
        assertEquals("email", request.getSorts().get(1).getField());
        assertEquals("desc", request.getSorts().get(1).getDirection());
    }

    @Test
    void parsePaginationQueryParametersSetPageAndPageSize() {
        Map<String, String> params = Map.of("page", "2", "pageSize", "50");
        List<String> searchableFields = List.of();
        
        SearchRequest request = parser.parseQueryParameters(params, searchableFields);
        
        assertNotNull(request.getPage());
        assertEquals(2, request.getPage().getPage());
        assertEquals(50, request.getPage().getPageSize());
    }

    @Test
    void parseIgnoresUnknownQueryParameters() {
        Map<String, String> params = Map.of(
            "name", "John",
            "unknown", "value",
            "sort", "name:asc"
        );
        List<String> searchableFields = List.of("name");
        
        SearchRequest request = parser.parseQueryParameters(params, searchableFields);
        
        assertNotNull(request.getFilters());
        assertEquals(1, request.getFilters().size());
        assertEquals("name", request.getFilters().get(0).getField());
    }

    @Test
    void parseIgnoresBlankQueryParameterValues() {
        Map<String, String> params = Map.of("name", "", "email", "  ");
        List<String> searchableFields = List.of("name", "email");
        
        SearchRequest request = parser.parseQueryParameters(params, searchableFields);
        
        assertNull(request.getFilters());
    }

    @Test
    void parseCombinesAllParameterTypes() {
        Map<String, String> params = Map.of(
            "name", "John",
            "sort", "name:asc,email:desc",
            "page", "1",
            "pageSize", "25"
        );
        List<String> searchableFields = List.of("name", "email");
        
        SearchRequest request = parser.parseQueryParameters(params, searchableFields);
        
        // Verify filters
        assertNotNull(request.getFilters());
        assertEquals(1, request.getFilters().size());
        
        // Verify sorts
        assertNotNull(request.getSorts());
        assertEquals(2, request.getSorts().size());
        
        // Verify pagination
        assertEquals(1, request.getPage().getPage());
        assertEquals(25, request.getPage().getPageSize());
    }

    @Test
    void parseIdQueryParameterCreatesIdFilter() {
        // ID should be filterable even if not in searchableFields
        Map<String, String> params = Map.of("id", "42");
        List<String> searchableFields = List.of("name", "email");
        
        SearchRequest request = parser.parseQueryParameters(params, searchableFields);
        
        assertNotNull(request.getFilters());
        assertEquals(1, request.getFilters().size());
        
        SearchFilter idFilter = request.getFilters().get(0);
        assertEquals("id", idFilter.getField());
        assertEquals("eq", idFilter.getOperator());
        assertEquals("42", idFilter.getValue());
    }

    @Test
    void parseIdWithOtherFiltersIncludesBoth() {
        // ID filter should be combined with other filters
        Map<String, String> params = Map.of(
            "id", "42",
            "name", "John"
        );
        List<String> searchableFields = List.of("name", "email");
        
        SearchRequest request = parser.parseQueryParameters(params, searchableFields);
        
        assertNotNull(request.getFilters());
        // ID filter + name filter
        assertEquals(2, request.getFilters().size());
        
        // Verify ID is first
        assertEquals("id", request.getFilters().get(0).getField());
        assertEquals("name", request.getFilters().get(1).getField());
    }
}
