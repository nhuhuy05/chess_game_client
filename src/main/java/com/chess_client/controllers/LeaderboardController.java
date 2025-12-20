package com.chess_client.controllers;

import com.chess_client.services.FriendService;
import com.chess_client.services.LeaderboardService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

public class LeaderboardController {

    @FXML
    private Button backButton;

    @FXML
    private Button refreshButton;

    @FXML
    private VBox leaderboardContainer;

    @FXML
    public void initialize() {
        refreshLeaderboard();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/home.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 930, 740);
            javafx.stage.Stage stage = (javafx.stage.Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể quay lại màn hình chính: " + e.getMessage());
        }
    }

    @FXML
    private void refreshLeaderboard() {
        refreshButton.setDisable(true);
        new Thread(() -> {
            try {
                JSONArray leaderboard = LeaderboardService.getLeaderboard(50); // Top 50
                Platform.runLater(() -> {
                    leaderboardContainer.getChildren().clear();
                    if (leaderboard.length() == 0) {
                        Label noDataLabel = new Label("Chưa có dữ liệu xếp hạng");
                        noDataLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 14px;");
                        leaderboardContainer.getChildren().add(noDataLabel);
                    } else {
                        for (int i = 0; i < leaderboard.length(); i++) {
                            JSONObject player = leaderboard.getJSONObject(i);
                            leaderboardContainer.getChildren().add(createLeaderboardItem(player, i + 1));
                        }
                    }
                    refreshButton.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    refreshButton.setDisable(false);
                    showAlert("Lỗi", "Không thể tải bảng xếp hạng: " + e.getMessage());
                });
            }
        }).start();
    }

    private HBox createLeaderboardItem(JSONObject player, int rank) {
        HBox item = new HBox(10);
        item.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 15;");
        item.setPrefWidth(Double.MAX_VALUE);

        // Hạng
        Label rankLabel = new Label();
        if (rank <= 3) {
            String medal = rank == 1 ? "🥇" : (rank == 2 ? "🥈" : "🥉");
            rankLabel.setText(medal + " " + rank);
            rankLabel
                    .setStyle("-fx-text-fill: #ffc107; -fx-font-size: 18px; -fx-font-weight: bold; -fx-min-width: 60;");
        } else {
            rankLabel.setText(String.valueOf(rank));
            rankLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 16px; -fx-min-width: 60;");
        }

        // Avatar/Icon
        Label avatarLabel = new Label("👤");
        avatarLabel.setStyle("-fx-font-size: 24px;");

        // Thông tin người chơi
        VBox playerInfoBox = new VBox(3);
        String displayName = player.optString("display_name", "");
        String username = player.getString("username");
        Label nameLabel = new Label(displayName.isEmpty() ? username : displayName);
        nameLabel.setStyle("-fx-text-fill: #f5f5f5; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label usernameLabel = new Label("@" + username);
        usernameLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 12px;");
        playerInfoBox.getChildren().addAll(nameLabel, usernameLabel);

        // Điểm số
        int score = player.optInt("score", 0);
        Label scoreLabel = new Label(String.valueOf(score));
        scoreLabel.setStyle("-fx-text-fill: #ffc107; -fx-font-size: 18px; -fx-font-weight: bold; -fx-min-width: 80;");

        // Thắng
        int wins = player.optInt("wins", 0);
        Label winsLabel = new Label(String.valueOf(wins));
        winsLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-min-width: 60;");

        // Thua
        int losses = player.optInt("losses", 0);
        Label lossesLabel = new Label(String.valueOf(losses));
        lossesLabel.setStyle("-fx-text-fill: #f44336; -fx-font-size: 14px; -fx-min-width: 60;");

        // Hòa
        int draws = player.optInt("draws", 0);
        Label drawsLabel = new Label(String.valueOf(draws));
        drawsLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 14px; -fx-min-width: 60;");

        // Nút thêm bạn
        Button addFriendButton = new Button();
        boolean isSelf = player.optBoolean("is_self", false);
        boolean isFriend = player.optBoolean("is_friend", false);
        String friendshipStatus = player.optString("friendship_status", "");
        boolean canSendRequest = player.optBoolean("can_send_request", true);

        if (isSelf) {
            addFriendButton.setText("Bạn");
            addFriendButton.setDisable(true);
            addFriendButton.setStyle(
                    "-fx-background-color: rgba(74, 158, 255, 0.3); -fx-text-fill: #4a9eff; -fx-background-radius: 5; -fx-padding: 8 15; -fx-min-width: 100;");
        } else if (isFriend) {
            addFriendButton.setText("✓ Đã là bạn");
            addFriendButton.setDisable(true);
            addFriendButton.setStyle(
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-min-width: 100;");
        } else if ("pending".equals(friendshipStatus)) {
            addFriendButton.setText("⏳ Đã gửi");
            addFriendButton.setDisable(true);
            addFriendButton.setStyle(
                    "-fx-background-color: #FFA500; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-min-width: 100;");
        } else if (canSendRequest) {
            addFriendButton.setText("➕ Thêm bạn");
            addFriendButton.setStyle(
                    "-fx-background-color: #4a9eff; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15; -fx-cursor: hand; -fx-min-width: 100;");
            int playerId = player.getInt("id");
            addFriendButton.setOnAction(e -> sendFriendRequest(playerId));
        }

        item.getChildren().addAll(rankLabel, avatarLabel, playerInfoBox);
        HBox.setHgrow(playerInfoBox, javafx.scene.layout.Priority.ALWAYS);
        item.getChildren().addAll(scoreLabel, winsLabel, lossesLabel, drawsLabel, addFriendButton);

        return item;
    }

    private void sendFriendRequest(int userId) {
        new Thread(() -> {
            try {
                FriendService.sendFriendRequest(userId);
                Platform.runLater(() -> {
                    showAlert("Thành công", "Đã gửi lời mời kết bạn");
                    refreshLeaderboard(); // Refresh để cập nhật trạng thái
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể gửi lời mời: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
