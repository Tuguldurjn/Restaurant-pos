module com.restaurant {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.restaurant to javafx.fxml;
    opens com.restaurant.controllers to javafx.fxml;
    opens com.restaurant.models to javafx.base, javafx.fxml;

    exports com.restaurant;
    exports com.restaurant.controllers;
    exports com.restaurant.models;
}