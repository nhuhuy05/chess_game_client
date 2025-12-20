package com.chess_client.controllers;

import com.chess_client.services.AuthService;
import com.chess_client.services.ProfileService;
import com.chess_client.services.TokenStorage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.json.JSONObject;

public class LoginController {

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField visiblePasswordField;
    @FXML
    private CheckBox showPasswordCheckbox;
    @FXML
    private Label errorLabel;

    @FXML
    private void onLoginClicked() {
        String username = usernameField.getText().trim();
        String password = showPasswordCheckbox.isSelected()
                ? visiblePasswordField.getText()
                : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Disable login button to prevent multiple clicks
        javafx.scene.Node source = usernameField.getScene().getRoot();
        source.setDisable(true);

        JSONObject result = AuthService.signIn(username, password);
        if (result == null) {
            showError("Không kết nối được tới server.");
            source.setDisable(false);
            return;
        }

        if (result.has("accessToken")) {
            TokenStorage.save(result.getString("accessToken"), result.getString("refreshToken"));
            showInfo("Đăng nhập thành công! Đang chuyển hướng...");

            // Check user role and route accordingly
            new Thread(() -> {
                try {
                    JSONObject profile = ProfileService.getProfile();
                    String role = profile.optString("role", "user");

                    Platform.runLater(() -> {
                        if ("admin".equals(role)) {
                            routeToAdmin();
                        } else {
                            routeToHome();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showError("Lỗi khi lấy thông tin người dùng!");
                        source.setDisable(false);
                    });
                }
            }).start();
        } else {
            showError(result.optString("message", "Đăng nhập thất bại."));
            source.setDisable(false);
        }
    }

    @FXML
    public void onShowPasswordClicked(ActionEvent event) {
        boolean show = showPasswordCheckbox.isSelected();
        if (show) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
        }
    }

    @FXML
    public void onRegisterLinkClicked(ActionEvent event) {
        try {
            // Chuyển sang màn hình đăng ký (Sign Up)
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/chess_client/fxml/register.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 930, 740);
            javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể mở giao diện đăng ký!");
        }
    }

    private void routeToHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/chess_client/fxml/home.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 930, 740);
            javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Chess - Trang chủ");
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể mở giao diện trang chủ!");
        }
    }

    private void routeToAdmin() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/chess_client/fxml/admin.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1200, 800);
            javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Chess - Quản trị hệ thống");
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Không thể mở giao diện quản trị!");
        }
    }

    private void showError(String msg) {
        errorLabel.setVisible(true);
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
        System.err.println("❌ " + msg);
    }

    private void showInfo(String msg) {
        errorLabel.setVisible(true);
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #00ff99;");
        System.out.println("✅ " + msg);
    }
}
