# API Quick Reference

## Base Endpoint
```
http://localhost:8080/api/{resourceAlias}
```

## Common Queries

### Get all records
```bash
curl http://localhost:8080/api/contacts
```

### Get by ID
```bash
curl http://localhost:8080/api/contacts?id=42
```

### Filter by field
```bash
curl http://localhost:8080/api/contacts?name=John
```

### Multiple filters (AND)
```bash
curl 'http://localhost:8080/api/accounts?type=business&name=Acme'
```

### Sort ascending
```bash
curl 'http://localhost:8080/api/contacts?sort=name:asc'
```

### Sort descending
```bash
curl 'http://localhost:8080/api/contacts?sort=email:desc'
```

### Multiple sort fields
```bash
curl 'http://localhost:8080/api/contacts?sort=name:asc,email:desc'
```

### Pagination
```bash
curl 'http://localhost:8080/api/contacts?page=1&pageSize=10'
```

### Combined query
```bash
curl 'http://localhost:8080/api/accounts?type=business&sort=name:asc,createdAt:desc&page=0&pageSize=20'
```

### Get by ID with sorting
```bash
curl 'http://localhost:8080/api/contacts?id=42&sort=name:asc'
```

## Query Parameters

| Parameter | Type | Example | Notes |
|-----------|------|---------|-------|
| `id` | integer | `?id=42` | Primary key filter (always available) |
| `{field}` | string | `?name=John` | Any searchable field from metadata |
| `sort` | string | `?sort=name:asc` | field:asc or field:desc, comma-separated |
| `page` | integer | `?page=0` | Zero-based, default: 0 |
| `pageSize` | integer | `?pageSize=20` | Default: 20, max: from metadata |

## Metadata Configuration

Define resources in `src/main/resources/metadata.yml`:

```yaml
resources:
  - name: contacts
    entityClass: com.example.domain.Contact
    searchableFields:
      - name
      - email
    sortableFields:
      - name
      - email
    allowedOperators:
      - eq
      - contains
    maxPageSize: 50
    fields:
      - name: id
        type: integer
      - name: name
        type: string
```

## Response Format

All responses are JSON arrays:

```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane@example.com"
  }
]
```

Single record query (by ID) also returns array:

```json
[
  {
    "id": 42,
    "name": "John Doe",
    "email": "john@example.com"
  }
]
```

## Error Responses

Invalid resource (500 error):

```json
{
  "details": "Unknown resource: unknownResource",
  "stack": "..."
}
```

## Development

### Run tests
```bash
./mvnw test
```

### Run in dev mode
```bash
./mvnw compile quarkus:dev
```

### Build for production
```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Build native executable
```bash
./mvnw package -Dnative
./target/hello-quarkus-1.0.0-SNAPSHOT-runner
```

## Implementation Details

### Code Structure
- `PublicApiResource.java`: REST endpoint handler
- `QueryParameterParser.java`: URL parameter parsing
- `GenericSearchService.java`: Query execution engine
- `ResourceRegistry.java`: Metadata storage
- `QueryBuilder.java`: JPQL generation

### Request Flow
1. HTTP GET request arrives at `/api/{resourceAlias}`
2. PublicApiResource validates resource in metadata
3. QueryParameterParser converts URL params to SearchRequest
4. GenericSearchService executes search with validation
5. QueryBuilder generates JPQL query
6. Hibernate executes query and returns results
7. Results serialized as JSON and returned

### Supported Operators
- `eq` - Exact match (default)
- `contains` - Substring match (LIKE)

### Important Notes
- All filters use AND logic (no OR support yet)
- ID field is always filterable (special handling)
- Page size is capped by metadata maxPageSize
- Results returned in array format (always)
- Empty result sets return empty array `[]`
