package com.rajansethi.api.pojos;

/**
 * POJO for the POST /orders request body.
 * Jackson converts this object to JSON automatically:
 * { "bookId": 1, "customerName": "John" }
 */
public class OrderRequest {

    private int bookId;
    private String customerName;

    public OrderRequest() {
    }

    public OrderRequest(int bookId, String customerName) {
        this.bookId = bookId;
        this.customerName = customerName;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
