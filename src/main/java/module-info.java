module com.chessclient.chess_client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.net.http;
    requires org.json;

    opens com.chess_client to javafx.fxml;

    exports com.chess_client;
    exports com.chess_client.controllers;

    opens com.chess_client.controllers to javafx.fxml;

    exports com.chess_client.controllers.admin;

    opens com.chess_client.controllers.admin to javafx.fxml;
    opens com.chess_client.controllers.admin.models to javafx.base;
}