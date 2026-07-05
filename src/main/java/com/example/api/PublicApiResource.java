package com.example.api;

import com.example.config.ResourceConfig;
import com.example.config.ResourceRegistry;
import com.example.exception.InvalidResourceException;
import com.example.model.SearchRequest;
import com.example.service.GenericSearchService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

/**
 * Public REST API layer for resource-specific GET endpoints.
 *
 * This resource provides a simple, query-parameter-based API for searching and retrieving
 * entities. It replaces the JSON POST-based generic endpoint with a more conventional REST
 * approach using HTTP GET with query parameters.
 *
 * Endpoints:
 * - GET /api/{resourceAlias} - List all resources, optionally filtered, sorted, and paginated
 * - GET /api/{resourceAlias}?id=123 - Get a single resource by ID
 * - GET /api/{resourceAlias}?field=value - Filter by searchable field
 *
 * Query Parameters:
 * - id: Primary key to retrieve a single resource (e.g., ?id=42)
 * - {field}: Any searchable field from metadata (e.g., ?name=John&type=business)
 * - sort: Sort specification as field:direction (e.g., ?sort=name:asc,email:desc)
 * - page: Zero-based page number for pagination (default: 0)
 * - pageSize: Number of items per page (default: 20, max from metadata)
 *
 * Examples:
 * - GET /api/contacts - List all contacts
 * - GET /api/contacts?id=42 - Get contact with ID 42
 * - GET /api/contacts?name=John&sort=name:asc&page=0&pageSize=10
 * - GET /api/accounts?type=business - List all business accounts
 * - GET /api/accounts?type=business&sort=name:asc,type:desc
 *
 * The API is metadata-driven: allowed resources, searchable fields, and operators
 * are determined by metadata.yml configuration. Invalid resource names or fields
 * will result in 500 errors (metadata validation errors).
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class PublicApiResource {

    // Dependencies injected via constructor (Quarkus CDI)
    private final ResourceRegistry resourceRegistry;      // Access to resource metadata
    private final GenericSearchService genericSearchService;  // Executes searches
    private final QueryParameterParser queryParameterParser;  // Parses URL query params

    /**
     * Constructor for dependency injection.
     * Quarkus automatically resolves and injects dependencies marked @ApplicationScoped.
     */
    public PublicApiResource(ResourceRegistry resourceRegistry,
                              GenericSearchService genericSearchService,
                              QueryParameterParser queryParameterParser) {
        this.resourceRegistry = resourceRegistry;
        this.genericSearchService = genericSearchService;
        this.queryParameterParser = queryParameterParser;
    }

    /**
     * Handles GET requests to /api/{resourceAlias}
     *
     * This method serves as the main entry point for resource queries. It:
     * 1. Validates that the requested resource exists in metadata
     * 2. Converts JAX-RS MultivaluedMap query params to a simple Map
     * 3. Parses the query parameters into SearchRequest object
     * 4. Delegates to GenericSearchService for query execution
     * 5. Returns results as JSON
     *
     * @param resourceAlias The resource name from URL path (e.g., "contacts", "accounts")
     * @param uriInfo Injected UriInfo containing query parameters
     * @return Response with JSON results or error
     * @throws InvalidResourceException if resource name is not found in metadata
     */
    @GET
    @Path("/{resourceAlias}")
    public Response search(@PathParam("resourceAlias") String resourceAlias,
                          @Context UriInfo uriInfo) {
        // Step 1: Validate resource exists in metadata registry
        // This fails fast if user requests an unknown resource
        ResourceConfig resource = resourceRegistry.getResource(resourceAlias);
        if (resource == null) {
            throw new InvalidResourceException("Unknown resource: " + resourceAlias);
        }

        // Step 2: Extract query parameters from URI
        // JAX-RS UriInfo.getQueryParameters() returns MultivaluedMap<String, List<String>>
        // Convert to simple Map<String, String> by taking first value of each parameter
        var queryParams = uriInfo.getQueryParameters(true);
        var simpleParams = queryParams.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().isEmpty() ? "" : e.getValue().get(0)
            ));

        // Step 3: Parse query parameters into SearchRequest using metadata-aware parser
        // Parser knows about searchable fields from metadata and handles ID filtering
        SearchRequest searchRequest = queryParameterParser.parseQueryParameters(
            simpleParams,
            resource.getSearchableFields()
        );

        // Step 4: Execute search using metadata-driven search service
        // GenericSearchService handles validation and query building
        List<?> results = genericSearchService.search(resourceAlias, searchRequest);

        // Step 5: Return results as JSON with 200 OK status
        return Response.ok(results).build();
    }
}
