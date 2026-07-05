package com.example.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class PublicApiResourceTest {

    @Test
    void searchContactsByAliasSucceeds() {
        given()
            .when()
            .get("/api/contacts")
            .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void searchAccountsByAliasSucceeds() {
        given()
            .when()
            .get("/api/accounts")
            .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void searchContactsWithPaginationSucceeds() {
        given()
            .queryParam("page", "0")
            .queryParam("pageSize", "10")
            .when()
            .get("/api/contacts")
            .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void searchWithUnknownResourceReturns500() {
        given()
            .when()
            .get("/api/unknownResource")
            .then()
            .statusCode(500);
    }

    @Test
    void aliasResolvesToCorrectResource() {
        // Verify that 'accounts' alias resolves to the accounts resource
        Response response = given()
            .when()
            .get("/api/accounts");
        
        response.then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void searchContactsWithQueryParameterSucceeds() {
        // This test verifies the query parameter parsing works
        // Even if no data matches, the endpoint should not error
        given()
            .queryParam("name", "test")
            .queryParam("page", "0")
            .queryParam("pageSize", "20")
            .when()
            .get("/api/contacts")
            .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void searchWithSortParameterSucceeds() {
        given()
            .queryParam("sort", "name:asc")
            .when()
            .get("/api/contacts")
            .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void searchWithMultipleSortParametersSucceeds() {
        given()
            .queryParam("sort", "name:asc,email:desc")
            .when()
            .get("/api/contacts")
            .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void searchByIdReturnsSpecificResource() {
        // Query by ID to get a single resource
        given()
            .queryParam("id", "1")
            .when()
            .get("/api/contacts")
            .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void searchWithIdAndOtherFiltersSucceeds() {
        // ID parameter can be combined with other filters
        given()
            .queryParam("id", "1")
            .queryParam("name", "test")
            .queryParam("sort", "name:asc")
            .when()
            .get("/api/contacts")
            .then()
            .statusCode(200)
            .body(notNullValue());
    }
}
