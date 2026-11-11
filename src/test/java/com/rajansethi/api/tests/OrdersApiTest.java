package com.rajansethi.api.tests;

import com.rajansethi.api.base.ApiBaseTest;
import com.rajansethi.api.pojos.OrderRequest;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for the protected Orders endpoints (Bearer token required).
 *
 * This class is one connected workflow - REQUEST CHAINING:
 *   1. GET /books        -> take an available book's id from the response
 *   2. POST /orders      -> create an order with that book id, save the orderId
 *   3. GET /orders/{id}  -> verify the order exists
 *   4. PATCH /orders/{id}-> update the customer name
 *   5. DELETE /orders/{id}-> delete it, then GET again and expect 404
 *
 * dependsOnMethods makes TestNG run them in this order and
 * skip the rest if an earlier step fails.
 */
public class OrdersApiTest extends ApiBaseTest {

    private String orderId;

    @Test(description = "Orders endpoints reject requests without a token - 401")
    public void ordersWithoutTokenReturn401() {
        given()
                .spec(requestSpec)
                .when()
                .get("/orders")
                .then()
                .statusCode(401);
    }

    @Test(description = "POST /orders creates an order for an available book taken from GET /books")
    public void createOrderForAvailableBook() {
        // Step 1 - chaining: get the id of the first AVAILABLE book, do not hard-code it
        List<Map<String, Object>> books = given()
                .spec(requestSpec)
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$");

        int availableBookId = books.stream()
                .filter(book -> Boolean.TRUE.equals(book.get("available")))
                .map(book -> (Integer) book.get("id"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No available book found"));

        // Step 2 - create the order with a POJO body, save the orderId for the next tests
        OrderRequest order = new OrderRequest(availableBookId, "Rajan Sethi");

        orderId = given()
                .spec(authSpec())
                .body(order)
                .when()
                .post("/orders")
                .then()
                .statusCode(201)
                .body("created", equalTo(true))
                .extract()
                .path("orderId");

        assertNotNull(orderId, "orderId was not returned by POST /orders");
    }

    @Test(dependsOnMethods = "createOrderForAvailableBook",
            description = "GET /orders/{orderId} returns the order just created")
    public void getCreatedOrder() {
        given()
                .spec(authSpec())
                .pathParam("orderId", orderId)
                .when()
                .get("/orders/{orderId}")
                .then()
                .statusCode(200)
                .body("id", equalTo(orderId))
                .body("customerName", equalTo("Rajan Sethi"));
    }

    @Test(dependsOnMethods = "getCreatedOrder",
            description = "PATCH /orders/{orderId} updates the customer name")
    public void updateOrderCustomerName() {
        given()
                .spec(authSpec())
                .pathParam("orderId", orderId)
                .body(Map.of("customerName", "Rajan S."))
                .when()
                .patch("/orders/{orderId}")
                .then()
                .statusCode(204);

        given()
                .spec(authSpec())
                .pathParam("orderId", orderId)
                .when()
                .get("/orders/{orderId}")
                .then()
                .statusCode(200)
                .body("customerName", equalTo("Rajan S."));
    }

    @Test(dependsOnMethods = "updateOrderCustomerName",
            description = "DELETE /orders/{orderId} deletes the order, then GET returns 404")
    public void deleteOrder() {
        given()
                .spec(authSpec())
                .pathParam("orderId", orderId)
                .when()
                .delete("/orders/{orderId}")
                .then()
                .statusCode(204);

        given()
                .spec(authSpec())
                .pathParam("orderId", orderId)
                .when()
                .get("/orders/{orderId}")
                .then()
                .statusCode(404);
    }

    @Test(description = "GET /orders with a valid token returns 200 and a list")
    public void listOrdersWithToken() {
        List<Object> orders = given()
                .spec(authSpec())
                .when()
                .get("/orders")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("$");

        assertNotNull(orders, "GET /orders did not return a list");
    }
}
