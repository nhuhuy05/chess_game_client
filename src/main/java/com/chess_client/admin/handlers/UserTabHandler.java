package com.chess_client.controllers.admin.handlers;

import com.chess_client.controllers.admin.models.UserRow;
import com.chess_client.services.AdminService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Handler cho tab quản lý người dùng
 */
public class UserTabHandler {

    private final ObservableList<UserRow> userList;
    private final BiConsumer<Alert.AlertType, String> showAlert;
    private final Runnable refreshStats;
    private int currentPage = 1;
    private int limit = 20;

    public UserTabHandler(ObservableList<UserRow> userList, BiConsumer<Alert.AlertType, String> showAlert,
            Runnable refreshStats) {
        this.userList = userList;
        this.showAlert = showAlert;
        this.refreshStats = refreshStats;
    }

    public void loadUsers(String search) {
        // Reset to first page when searching
        if (search != null && !search.trim().isEmpty()) {
            currentPage = 1;
        }

        new Thread(() -> {
            try {
                JSONObject result = AdminService.getAllUsers(currentPage, limit, search);

                Platform.runLater(() -> {
                    if (result.has("statusCode") && result.getInt("statusCode") == 200) {
                        userList.clear();
                        JSONArray users = result.getJSONArray("users");

                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = users.getJSONObject(i);
                            // Chỉ hiển thị user thường, không hiển thị admin
                            if ("user".equals(user.optString("role", ""))) {
                                userList.add(new UserRow(
                                        user.getInt("id"),
                                        user.getString("username"),
                                        user.optString("display_name", ""),
                                        user.optString("email", ""),
                                        user.optString("phone", ""),
                                        user.optString("status", "active")));
                            }
                        }
                    } else {
                        showAlert.accept(Alert.AlertType.ERROR,
                                result.optString("message", "Không thể tải danh sách người dùng"));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert.accept(Alert.AlertType.ERROR, "Lỗi kết nối: " + e.getMessage()));
            }
        }).start();
    }

    public void handleEditUser(UserRow user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông tin người dùng");
        dialog.setHeaderText("Chỉnh sửa: " + user.getUsername());

        ButtonType btnSave = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtDisplayName = new TextField(user.getDisplayName());
        TextField txtEmail = new TextField(user.getEmail());
        TextField txtPhone = new TextField(user.getPhone());

        grid.add(new Label("Tên hiển thị:"), 0, 0);
        grid.add(txtDisplayName, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(txtEmail, 1, 1);
        grid.add(new Label("Số điện thoại:"), 0, 2);
        grid.add(txtPhone, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == btnSave) {
            String displayName = txtDisplayName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();

            // Basic validation
            if (email.isEmpty() && phone.isEmpty()) {
                showAlert.accept(Alert.AlertType.WARNING, "Vui lòng nhập ít nhất email hoặc số điện thoại");
                return;
            }

            new Thread(() -> {
                JSONObject updateResult = AdminService.updateUser(
                        user.getId(),
                        displayName,
                        email,
                        phone,
                        null,
                        null);

                Platform.runLater(() -> {
                    if (updateResult.has("statusCode") && updateResult.getInt("statusCode") == 200) {
                        showAlert.accept(Alert.AlertType.INFORMATION, "Đã cập nhật người dùng thành công");
                        loadUsers("");
                        if (refreshStats != null) {
                            refreshStats.run();
                        }
                    } else {
                        showAlert.accept(Alert.AlertType.ERROR,
                                updateResult.optString("message", "Không thể cập nhật"));
                    }
                });
            }).start();
        }
    }

    public void handleDeleteUser(UserRow user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa người dùng: " + user.getUsername());
        confirm.setContentText("Bạn có chắc chắn muốn xóa người dùng này?\nHành động này không thể hoàn tác!");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                JSONObject deleteResult = AdminService.deleteUser(user.getId());

                Platform.runLater(() -> {
                    if (deleteResult.has("statusCode") && deleteResult.getInt("statusCode") == 200) {
                        showAlert.accept(Alert.AlertType.INFORMATION, "Đã xóa người dùng thành công");
                        loadUsers("");
                        if (refreshStats != null) {
                            refreshStats.run();
                        }
                    } else {
                        showAlert.accept(Alert.AlertType.ERROR,
                                deleteResult.optString("message", "Không thể xóa người dùng"));
                    }
                });
            }).start();
        }
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }
}
