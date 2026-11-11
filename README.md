# REST Assured API Automation

API test automation built from scratch with **REST Assured, Java 17, TestNG and Maven**.

The API under test is the [Simple Books API](https://simple-books-api.click) — the same public practice API used in the Postman Beginner's Course. Every request that was first made manually in Postman (GET books, register for a token, POST an order, PATCH it, DELETE it) is automated here in Java code.

## What this project demonstrates

- REST Assured's **given / when / then** style for readable API tests
- A reusable **RequestSpecification** built once in a base class (base URL, JSON content type, request/response logging)
- **Authentication flow**: register an API client, extract the `accessToken` from the response, send it as a Bearer token
- **Request chaining**: take an available book's `id` from a GET response and use it in the POST body — no hard-coded IDs; then reuse the returned `orderId` across GET, PATCH and DELETE tests with TestNG `dependsOnMethods`
- A **POJO request body** (`OrderRequest`) serialized to JSON with Jackson
- Positive and negative tests: 200, 201, 204, 400, 401 and 404 cases
- Query parameters, path parameters and Hamcrest assertions
- GitHub Actions CI — tests run on every push

## Project structure

```
rest-assured-api-automation
├── pom.xml                                   -> REST Assured, Jackson, TestNG
├── .github/workflows/maven.yml               -> GitHub Actions CI pipeline
└── src/test
    ├── java/com/rajansethi/api
    │   ├── base
    │   │   └── ApiBaseTest.java              -> request spec + token registration
    │   ├── pojos
    │   │   └── OrderRequest.java             -> POST /orders body POJO
    │   └── tests
    │       ├── BooksApiTest.java             -> status, list, filter, limit, single book, 400 case
    │       └── OrdersApiTest.java            -> 401 case + full order workflow (POST -> GET -> PATCH -> DELETE -> 404)
    └── resources
        ├── api.properties                    -> baseUrl
        └── testng.xml                        -> suite definition
```

## Test coverage

| Area | Tests |
|---|---|
| Books | API status is OK · list books · filter by type (query param) · limit results · invalid type returns 400 · get a single book using an id extracted from the list |
| Orders | no token returns 401 · create order for an available book (chaining) · get the created order · update customer name with PATCH and verify · delete the order and verify 404 · list orders with a valid token |

## How to run

**Prerequisites:** Java 17+ and Maven installed, internet access (the API is public).

```bash
mvn test
```

Or open the project in **IntelliJ IDEA** and run `testng.xml`, or any test class, directly.

Note: each run registers a fresh API client with a unique generated email, because the API does not allow the same email to register twice — watch for that in `ApiBaseTest`.

## Author

Rajan Sethi — QA Automation Engineer / SDET (Java, Selenium, TestNG, REST Assured, Playwright)
