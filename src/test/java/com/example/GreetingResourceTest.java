package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class GreetingResourceTest {
    @Test
    void testHealthEndpoint() {
        given()
            .when().get("/health")
            .then()
            .statusCode(200)
            .body(emptyString());
    }

    @Test
    void testGreetingEndpoint() {
        given()
            .queryParam("name", "Quarkus")
            .when().get("/greeting")
            .then()
            .statusCode(200)
            .contentType("application/json")
            .body("message", is("Hello Quarkus"));
    }

    @Test
    void testGreetingEndpointValidation() {
        given()
            .queryParam("name", "   ")
            .when().get("/greeting")
            .then()
            .statusCode(400)
            .contentType("application/json")
            .body("message", is("Validation failed"))
            .body("errors[0].message", containsString("must not be blank"));
    }
}