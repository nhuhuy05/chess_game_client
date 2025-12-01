package com.chess_client.controllers;

import com.chess_client.models.Piece;
import com.chess_client.services.AuthService;
import com.chess_client.services.HomeMatchmakingResult;
import com.chess_client.services.HomeService;
import com.chess_client.services.TokenStorage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.Socket;

public class HomeController {

    @FXML
    private VBox btnRandom;

    @FXML
    private VBox btnComputer;

    @FXML
    private VBox btnFriend;

    @FXML
    private VBox btnLeaderboard;

    @FXML
    private VBox btnProfile;

    @FXML
    private VBox btnExit;

    @FXML
    private Label lblWelcome;

    private Alert waitingAlert;

    @FXML
    private void handleRandomMatch() {
        btnRandom.setDisable(true);

        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            showAlert("Lỗi", "Bạn chưa đăng nhập hoặc token không hợp lệ.");
            btnRandom.setDisable(false);
            return;
        }

        waitingAlert = new Alert(Alert.AlertType.INFORMATION);
        waitingAlert.setTitle("Đang tìm trận đấu");
        waitingAlert.setHeaderText(null);
        waitingAlert.setContentText("Đang tìm đối thủ, vui lòng chờ...");
        waitingAlert.initOwner(btnRandom.getScene().getWindow());
        waitingAlert.show();

        new Thread(() -> {
            try {
                HomeService homeService = new HomeService("http://localhost:3000/api/matchmaking", token);
                HomeMatchmakingResult result = homeService.startRandomMatch();

                Platform.runLater(() -> {
                    closeWaitingAlert();
                    handleMatchmakingResult(result);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    closeWaitingAlert();
                    showAlert("Lỗi", "Có lỗi xảy ra khi ghép trận: " + e.getMessage());
                    btnRandom.setDisable(false);
                });
            }
        }).start();
    }

    private void handleMatchmakingResult(HomeMatchmakingResult result) {
        if (result.isSuccess()) {
            openGameWithMatch(result.getMatchJson(), result.getSocket(), result.getColor());
        } else if (result.isNotFound()) {
            showAlert("Thông báo", "Không tìm được trận đấu phù hợp, vui lòng thử lại.");
            btnRandom.setDisable(false);
        } else {
            showAlert("Lỗi", result.getErrorMessage() != null ? result.getErrorMessage() : "Ghép trận thất bại.");
            btnRandom.setDisable(false);
        }
    }

    private void openGameWithMatch(org.json.JSONObject res, Socket socket, Piece.Color color) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/game.fxml"));
            Parent root = loader.load();
            GameController controller = loader.getController();

            String gameId = res.optString("gameId", null);
            org.json.JSONObject opponent = res.getJSONObject("opponent");
            String opponentName = opponent.optString("display_name", opponent.optString("username", "Đối thủ"));

            String playerName = "Bạn";

            controller.setGameInfo(gameId, opponentName, playerName);
            controller.setPlayerColor(color);
            controller.setPeerSocket(socket);

            Stage stage = (Stage) btnRandom.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Không thể mở màn hình game: " + ex.getMessage());
        } finally {
            btnRandom.setDisable(false);
        }
    }

    private void closeWaitingAlert() {
        if (waitingAlert != null) {
            waitingAlert.close();
            waitingAlert = null;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handlePlayComputer() {
        System.out.println("Chơi với máy đã được chọn");
    }

    @FXML
    private void handlePlayFriend() {
        System.out.println("Chơi với bạn bè đã được chọn");
    }

    @FXML
    private void handleLeaderboard() {
        System.out.println("Bảng xếp hạng đã được chọn");
    }

    @FXML
    private void handleProfile() {
        System.out.println("Hồ sơ cá nhân đã được chọn");
    }

    @FXML
    private void handleExit() {
        AuthService.signOutSync();
        Platform.exit();
        System.exit(0);
    }
}
