package com.chess_client.controllers;

import com.chess_client.services.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.json.JSONObject;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;

    @FXML
    public void initialize() {
        registerButton.setOnAction(e -> onRegisterClicked());
        loginLink.setOnAction(e -> onBackToLoginClicked());
    }

    private void onRegisterClicked() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // === Validate input ===
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (username.length() < 4) {
            showAlert(Alert.AlertType.ERROR, "Tên đăng nhập phải ≥ 4 ký tự!");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.ERROR, "Email không hợp lệ!");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Mật khẩu phải ≥ 6 ký tự!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Mật khẩu xác nhận không trùng khớp!");
            return;
        }

        // ======= Gọi API =======
        JSONObject res = AuthService.signUp(username, password, email, fullName);

        if (res == null) {
            showAlert(Alert.AlertType.ERROR, "Không kết nối được tới server!");
            return;
        }

        if (res.has("message") && res.getString("message").contains("tồn tại")) {
            showAlert(Alert.AlertType.ERROR, res.getString("message"));
            return;
        }

        if (res.has("status") && res.getInt("status") == 204) {
            showAlert(Alert.AlertType.INFORMATION, "Đăng ký thành công! Quay lại đăng nhập.");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Đăng ký thành công!");
        }

        onBackToLoginClicked();
    }

    // Chuyển về màn hình đăng nhập
    private void onBackToLoginClicked() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/chess_client/fxml/login.fxml")
            );
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 930, 740);
            javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Không thể quay lại đăng nhập!");
        }
    }

    // =======================
    // Hàm hiển thị Alert
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Lỗi" : "Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
