package com.rajansethi.api.base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static io.restassured.RestAssured.given;

/**
 * Shared setup for all API tests.
 *
 * Builds ONE reusable RequestSpecification (base URL + JSON content type +
 * request/response logging) so individual tests never repeat it.
 *
 * Also registers a new API client and stores the access token,
 * exactly like the authentication flow in Postman:
 * POST /api-clients  ->  accessToken  ->  Authorization: Bearer <token>
 */
public class ApiBaseTest {

    protected RequestSpecification requestSpec;
    protected String accessToken;

    @BeforeClass
    public void setUp() throws IOException {
        Properties properties = new Properties();
        try (FileInputStream file = new FileInputStream("src/test/resources/api.properties")) {
            properties.load(file);
        }
        String baseUrl = properties.getProperty("baseUrl");

        RestAssured.baseURI = baseUrl;

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .addFilter(new io.restassured.filter.log.RequestLoggingFilter())
                .addFilter(new io.restassured.filter.log.ResponseLoggingFilter())
                .build();

        accessToken = registerApiClient();
    }

    /**
     * Registers a fresh API client. A unique email is generated every run
     * because the same email cannot be registered twice (API returns 409).
     */
    private String registerApiClient() {
        String uniqueEmail = "rajan." + System.currentTimeMillis() + "@example.com";
        String body = """
                {
                  "clientName": "Rajan Sethi",
                  "clientEmail": "%s"
                }
                """.formatted(uniqueEmail);

        return given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api-clients")
                .then()
                .statusCode(201)
                .extract()
                .path("accessToken");
    }

    /** Request specification WITH the Bearer token, for the protected /orders endpoints. */
    protected RequestSpecification authSpec() {
        return new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
    }
}
