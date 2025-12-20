package com.chess_client.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class NavigationHelper {

    /**
     * Chuyển đến một scene mới
     */
    public static void navigateTo(Stage stage, String fxmlPath, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể chuyển màn hình: " + e.getMessage());
        }
    }

    /**
     * Mở game với thông tin đã cho
     */
    public static void openGame(Stage stage, Object controller,
            String gameId, String opponentName,
            com.chess_client.models.Piece.Color color,
            java.net.Socket socket) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource("/com/chess_client/fxml/game.fxml"));
            Parent root = loader.load();
            com.chess_client.controllers.GameController gameController = loader.getController();

            gameController.setGameInfo(gameId, opponentName, "Bạn");
            gameController.setPlayerColor(color);
            gameController.setPeerSocket(socket);

            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở màn hình game: " + e.getMessage());
        }
    }

    public static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
