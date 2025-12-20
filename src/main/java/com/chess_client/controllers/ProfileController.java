package com.chess_client.controllers;

import com.chess_client.services.ProfileService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.json.JSONObject;

public class ProfileController {

    @FXML
    private Button backButton;

    @FXML
    private Label avatarLabel;

    @FXML
    private Label displayNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private TextField displayNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Button saveButton;

    @FXML
    private Label totalGamesLabel;

    @FXML
    private Label winsLabel;

    @FXML
    private Label lossesLabel;

    @FXML
    private Label drawsLabel;

    @FXML
    private Label scoreLabel;

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button changePasswordButton;

    @FXML
    public void initialize() {
        loadProfile();
    }

    private void loadProfile() {
        new Thread(() -> {
            try {
                JSONObject profile = ProfileService.getProfile();
                Platform.runLater(() -> {
                    updateUI(profile);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể tải thông tin profile: " + e.getMessage());
                });
            }
        }).start();
    }

    private void updateUI(JSONObject profile) {
        // Thông tin cá nhân
        String displayName = profile.optString("display_name", "");
        String username = profile.optString("username", "");
        String email = profile.optString("email", "");
        String phone = profile.optString("phone", "");

        displayNameLabel.setText(displayName.isEmpty() ? username : displayName);
        usernameLabel.setText("@" + username);

        displayNameField.setText(displayName);
        emailField.setText(email);
        phoneField.setText(phone);

        // Thống kê
        JSONObject ranking = profile.optJSONObject("ranking");
        if (ranking != null) {
            int gamesPlayed = ranking.optInt("games_played", 0);
            int wins = ranking.optInt("wins", 0);
            int losses = ranking.optInt("losses", 0);
            int draws = ranking.optInt("draws", 0);
            int score = ranking.optInt("score", 0);

            totalGamesLabel.setText(String.valueOf(gamesPlayed));
            winsLabel.setText(String.valueOf(wins));
            lossesLabel.setText(String.valueOf(losses));
            drawsLabel.setText(String.valueOf(draws));
            scoreLabel.setText(String.valueOf(score));
        } else {
            totalGamesLabel.setText("0");
            winsLabel.setText("0");
            lossesLabel.setText("0");
            drawsLabel.setText("0");
            scoreLabel.setText("0");
        }
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
    private void handleSaveProfile() {
        String displayName = displayNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (displayName.isEmpty()) {
            showAlert("Cảnh báo", "Tên hiển thị không được để trống");
            return;
        }

        saveButton.setDisable(true);
        new Thread(() -> {
            try {
                ProfileService.updateProfile(displayName, email, phone);
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    showAlert("Thành công", "Đã cập nhật thông tin profile");
                    loadProfile(); // Reload để cập nhật UI
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    showAlert("Lỗi", "Không thể cập nhật profile: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Cảnh báo", "Vui lòng điền đầy đủ thông tin");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Cảnh báo", "Mật khẩu mới và xác nhận mật khẩu không khớp");
            return;
        }

        if (newPassword.length() < 6) {
            showAlert("Cảnh báo", "Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }

        changePasswordButton.setDisable(true);
        new Thread(() -> {
            try {
                ProfileService.changePassword(oldPassword, newPassword);
                Platform.runLater(() -> {
                    changePasswordButton.setDisable(false);
                    showAlert("Thành công", "Đã đổi mật khẩu thành công");
                    // Xóa các field
                    oldPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    changePasswordButton.setDisable(false);
                    showAlert("Lỗi", "Không thể đổi mật khẩu: " + e.getMessage());
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
