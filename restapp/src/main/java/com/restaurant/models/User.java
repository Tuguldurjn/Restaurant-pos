package com.restaurant.models;

public class User {
    private int cashierId;
    private String username;
    private String fullName;

    public User(int cashierId, String username, String fullName) {
        this.cashierId = cashierId;
        this.username = username;
        this.fullName = fullName;
    }

    public int getCashierId() {
        return cashierId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }
}