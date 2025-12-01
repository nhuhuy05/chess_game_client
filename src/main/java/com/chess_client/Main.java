package com.chess_client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import com.chess_client.services.AuthService;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setResizable(false);
        stage.setTitle("Chess Game");
        stage.getIcons().add(new Image(
                getClass().getResourceAsStream("/com/chess_client/images/logo.png")));
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            // Gọi logout đồng bộ khi đóng cửa sổ bằng dấu X
            AuthService.signOutSync();
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
