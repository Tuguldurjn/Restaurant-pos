package com.restaurant.models;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int itemId;
    private String itemName; 
    private double price;
    private int quantity;
    private double subTotal;

    public OrderItem(int orderItemId, int orderId, int itemId, String itemName, double price, int quantity, double subTotal) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
        this.subTotal = subTotal;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setQuantity(int quantity) {
    this.quantity = quantity;
    this.subTotal = this.price * quantity;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }
}