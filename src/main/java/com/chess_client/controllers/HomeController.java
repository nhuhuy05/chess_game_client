package com.chess_client.controllers;

import com.chess_client.models.Piece;
import com.chess_client.services.AuthService;
import com.chess_client.services.ApiConfig;
import com.chess_client.services.HomeMatchmakingResult;
import com.chess_client.services.HomeService;
import com.chess_client.services.TokenStorage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
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
                HomeService homeService = new HomeService(ApiConfig.MATCHMAKING_BASE, token);
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
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setResizable(false);
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
        // Hiển thị dialog chọn độ khó
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Dễ", java.util.List.of("Dễ", "Trung bình", "Khó"));
        dialog.setTitle("Chọn mức độ");
        dialog.setHeaderText("Chơi với máy");
        dialog.setContentText("Chọn độ khó:");
        dialog.initOwner(btnComputer.getScene().getWindow());

        dialog.showAndWait().ifPresent(choice -> {
            int difficulty;
            String opponentName;
            switch (choice) {
                case "Trung bình" -> {
                    difficulty = 2;
                    opponentName = "Máy (Trung bình)";
                }
                case "Khó" -> {
                    difficulty = 3;
                    opponentName = "Máy (Khó)";
                }
                default -> {
                    difficulty = 1;
                    opponentName = "Máy (Dễ)";
                }
            }

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/chess_client/fxml/game.fxml"));
                Parent root = loader.load();
                GameController controller = loader.getController();

                // Thiết lập game chơi với máy: không có gameId server
                controller.setGameInfo(null, opponentName, "Bạn");
                controller.setupVsComputer(difficulty, Piece.Color.WHITE);

                Stage stage = (Stage) btnComputer.getScene().getWindow();
                Scene scene = new Scene(root, 1000, 700);
                stage.setScene(scene);
                stage.setResizable(false);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể mở màn hình chơi với máy: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handlePlayFriend() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/friends.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 930, 740);
            Stage stage = (Stage) btnFriend.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Không thể mở màn hình bạn bè: " + ex.getMessage());
        }
    }

    @FXML
    private void handleLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/leaderboard.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 930, 740);
            Stage stage = (Stage) btnLeaderboard.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Không thể mở màn hình bảng xếp hạng: " + ex.getMessage());
        }
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/profile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 930, 740);
            Stage stage = (Stage) btnProfile.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Không thể mở màn hình hồ sơ: " + ex.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        AuthService.signOutSync();
        Platform.exit();
        System.exit(0);
    }
}
