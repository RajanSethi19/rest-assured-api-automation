package com.rajansethi.api.tests;

import com.rajansethi.api.base.ApiBaseTest;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the public (no-auth) Books endpoints of the Simple Books API.
 * These are the same requests made manually in Postman, now as Java code.
 */
public class BooksApiTest extends ApiBaseTest {

    @Test(description = "GET /status returns 200 and status OK")
    public void apiStatusIsOk() {
        given()
                .spec(requestSpec)
                .when()
                .get("/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("OK"));
    }

    @Test(description = "GET /books returns the list of books")
    public void listBooks() {
        given()
                .spec(requestSpec)
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)));
    }

    @Test(description = "GET /books?type=fiction returns only fiction books - query parameter test")
    public void filterBooksByType() {
        given()
                .spec(requestSpec)
                .queryParam("type", "fiction")
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("type", everyItem(equalTo("fiction")));
    }

    @Test(description = "GET /books?limit=2 returns exactly 2 books")
    public void limitBooks() {
        given()
                .spec(requestSpec)
                .queryParam("limit", 2)
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body("$", hasSize(2));
    }

    @Test(description = "GET /books with an invalid type returns 400")
    public void invalidTypeReturns400() {
        given()
                .spec(requestSpec)
                .queryParam("type", "comics")
                .when()
                .get("/books")
                .then()
                .statusCode(400);
    }

    @Test(description = "Take the first book id from the list, then GET /books/{id} - response extraction")
    public void getSingleBookByIdFromList() {
        List<Integer> ids = given()
                .spec(requestSpec)
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        assertTrue(ids.size() > 0, "Book list was empty");
        int firstBookId = ids.get(0);

        String name = given()
                .spec(requestSpec)
                .pathParam("bookId", firstBookId)
                .when()
                .get("/books/{bookId}")
                .then()
                .statusCode(200)
                .body("id", equalTo(firstBookId))
                .extract()
                .path("name");

        assertTrue(name != null && !name.isBlank(), "Book name was empty");
    }
}
