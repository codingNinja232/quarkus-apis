# Hello Quarkus - Metadata-Driven REST API

A modern, type-safe REST API framework built with **Java 25** and **Quarkus 3.37.1** that enables generic CRUD operations on any entity through declarative YAML configuration. No need to write resource classes—just configure your entities in `metadata.yml` and the API automatically exposes them.

## Features

- **Metadata-Driven Architecture**: Define resources, searchable fields, and operators in YAML
- **RESTful GET API**: Simple, query-parameter-based interface (no JSON request bodies)
- **Flexible Filtering**: Multiple searchable fields with configurable operators (eq, contains)
- **Advanced Sorting**: Single or multi-field sorting with direction control
- **Pagination**: Configurable page size with overflow protection
- **ID Lookups**: Direct access to individual resources by primary key
- **Type-Safe**: Leverages Quarkus + Hibernate ORM for compile-time safety
- **PostgreSQL Native**: Built for PostgreSQL with Panache repositories

## Technology Stack

- **Java 25** - Latest LTS features
- **Quarkus 3.37.1** - Supersonic subatomic framework
- **Hibernate ORM + Panache** - Type-safe persistence
- **PostgreSQL 14** - Primary data store
- **RESTEasy Reactive** - Non-blocking REST endpoints
- **Jackson YAML** - Configuration parsing
- **JUnit 5 + Testcontainers** - Integration testing

## Project Structure

```
src/main/java/com/example/
├── api/                          # Public REST layer
│   ├── PublicApiResource.java    # GET /api/{resource} endpoints
│   └── QueryParameterParser.java # Query param parsing
├── service/                      # Business logic
│   └── GenericSearchService.java # Metadata-driven search engine
├── config/                       # Configuration & metadata
│   ├── ResourceRegistry.java     # Resource metadata registry
│   ├── ConfigurationValidator.java
│   ├── ResourceConfig.java       # Resource definition (from YAML)
│   ├── FieldConfig.java          # Field definition
│   └── YamlLoader.java           # YAML configuration loader
├── query/                        # Query building
│   └── QueryBuilder.java         # JPQL query builder
├── domain/                       # JPA entities
│   ├── Contact.java
│   └── Account.java
├── model/                        # DTOs for API
│   ├── SearchRequest.java
│   ├── SearchFilter.java
│   ├── SearchSort.java
│   └── PageRequest.java
└── exception/                    # Error handling
    └── ApiExceptionHandler.java

src/main/resources/
├── metadata.yml                  # Resource definitions
└── application.properties        # Quarkus configuration
```

## Quick Start

### Prerequisites

- Java 25+
- PostgreSQL 14+
- Maven 3.8+

### Configure Database

Edit `src/main/resources/application.properties`:

```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=pocuser
quarkus.datasource.password=pocPassword
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/pocdb
```

### Define Resources

Edit `src/main/resources/metadata.yml` to expose entities:

```yaml
resources:
  - name: contacts              # API resource name
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
        searchable: false
        sortable: true
      - name: name
        type: string
        searchable: true
        operators: [eq, contains]
```

### Run in Dev Mode

```bash
./mvnw compile quarkus:dev
```

API is available at: `http://localhost:8080/api/{resource}`

## API Documentation

### Base URL

```
http://localhost:8080/api
```

### Endpoints

All endpoints are **GET requests** with query parameters. No request body.

#### List All Resources

```http
GET /api/contacts
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "type": "personal"
  },
  {
    "id": 2,
    "name": "Jane Smith",
    "email": "jane@example.com",
    "type": "business"
  }
]
```

#### Get Resource by ID

```http
GET /api/contacts?id=42
```

**Response (200 OK):**
```json
[
  {
    "id": 42,
    "name": "John Doe",
    "email": "john@example.com"
  }
]
```

#### Filter by Searchable Field

Only fields listed in metadata's `searchableFields` can be filtered.

```http
GET /api/contacts?name=John
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
]
```

#### Multiple Filters (AND condition)

```http
GET /api/accounts?type=business&name=Acme
```

All filters are combined with AND logic.

#### Sorting

Sort by one or more fields. Format: `field:direction`

```http
GET /api/contacts?sort=name:asc
```

Multiple sort fields (comma-separated):

```http
GET /api/contacts?sort=name:asc,email:desc
```

Allowed directions: `asc`, `desc`

#### Pagination

```http
GET /api/contacts?page=0&pageSize=10
```

- `page`: Zero-based page number (default: 0)
- `pageSize`: Items per page (default: 20, max: from metadata)

#### Complex Query

Combine all features:

```http
GET /api/accounts?type=business&sort=name:asc,createdAt:desc&page=1&pageSize=25
```

### Query Parameters Reference

| Parameter | Type | Required | Example | Description |
|-----------|------|----------|---------|-------------|
| `id` | integer | No | `?id=42` | Filter by primary key |
| `{field}` | string | No | `?name=John` | Filter by searchable field (any field in `searchableFields`) |
| `sort` | string | No | `?sort=name:asc` | Sort by field(s), comma-separated, format: `field:asc\|desc` |
| `page` | integer | No | `?page=0` | Zero-based page number (default: 0) |
| `pageSize` | integer | No | `?pageSize=20` | Items per page (default: 20) |

### Error Responses

#### Invalid Resource Name (500)

```http
GET /api/unknownResource
```

```json
{
  "details": "Unknown resource: unknownResource",
  "stack": "..."
}
```

#### Invalid Field in Sort (500)

```http
GET /api/contacts?sort=invalidField:asc
```

The query builder will raise an error if the field is not defined in the entity.

## Configuration (metadata.yml)

The `metadata.yml` file controls the entire API. Each resource definition includes:

### Resource Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `name` | string | ✓ | Resource name used in URL (e.g., "contacts") |
| `entityClass` | string | ✓ | Fully qualified Java class (e.g., "com.example.domain.Contact") |
| `searchableFields` | array | ✓ | Fields allowed in query filters |
| `sortableFields` | array | ✓ | Fields allowed in sort specifications |
| `allowedOperators` | array | ✓ | Operators (eq, contains) |
| `maxPageSize` | integer | ✓ | Maximum page size to prevent abuse |
| `fields` | array | ✓ | Field definitions with type and operators |

### Operators

- **eq**: Exact match (default)
- **contains**: Substring match (LIKE)

## Development

### Compile

```bash
./mvnw clean compile
```

### Run Tests

```bash
./mvnw test
```

Tests include:
- Unit tests for query parameter parsing
- Integration tests for REST endpoints
- Resource registry validation

### Hot Reload

Quarkus Dev Mode enables live code editing:

```bash
./mvnw compile quarkus:dev
```

Changes to Java classes and resources (except metadata.yml) auto-reload.

## Building for Production

### JVM Build

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Native Build (GraalVM)

```bash
./mvnw package -Dnative
./target/hello-quarkus-1.0.0-SNAPSHOT-runner
```

Native executables provide:
- **Faster startup**: ~100ms vs 4+ seconds JVM
- **Lower memory**: ~50-100MB vs 300MB+
- **Better container deployments**: Smaller images

## Architecture

### Metadata-Driven Pattern

1. **Configuration** (`metadata.yml`): Define resources
2. **Metadata Loading** (`YamlLoader`): Parse YAML
3. **Validation** (`ConfigurationValidator`): Ensure consistency
4. **Registration** (`ResourceRegistry`): In-memory index
5. **API** (`PublicApiResource`): Accept HTTP requests
6. **Query Parsing** (`QueryParameterParser`): Convert params
7. **Search** (`GenericSearchService`): Execute queries
8. **Query Building** (`QueryBuilder`): Generate JPQL

### Data Flow

```
HTTP GET /api/contacts?name=John&sort=name:asc
    ↓
PublicApiResource.search()
    ↓
QueryParameterParser.parseQueryParameters()
    ↓
SearchRequest { filters=[name=John], sorts=[name:asc] }
    ↓
GenericSearchService.search()
    ↓
ResourceRegistry.getResource("contacts")
    ↓
QueryBuilder.build() → "from Contact where name = ? order by name asc"
    ↓
EntityManager.createQuery() → Hibernate
    ↓
Database Query
    ↓
Results → JSON Response
```

## Key Design Decisions

1. **Metadata in YAML**: Easy to read, version-control friendly
2. **GET with Query Params**: RESTful, cacheable, shareable URLs
3. **Type-Safe Entities**: Compile-time safety via Hibernate ORM
4. **Generic Search Engine**: Single `GenericSearchService` handles all entities
5. **Metadata Validation at Startup**: Fail fast on configuration errors
6. **Constructor Injection**: Quarkus best practice, immutability

## Future Enhancements

- GraphQL endpoint
- OpenAPI/Swagger documentation
- Join support (relationships)
- Custom operators (in, between, gt, lt)
- Field-level security
- Caching strategies
- Batch operations

## References

- [Quarkus Guide](https://quarkus.io/)
- [RESTEasy Reactive](https://quarkus.io/guides/resteasy-reactive)
- [Hibernate ORM Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/)
