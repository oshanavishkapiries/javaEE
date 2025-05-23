module com.chatapp.chatapp {
    requires javafx.controls;
    requires javafx.fxml;



    opens com.chatapp.chatapp.client to javafx.fxml;
    opens com.chatapp.chatapp.server to javafx.fxml;
    exports com.chatapp.chatapp;
}