package com.chess_client.controllers.admin.handlers;

import com.chess_client.services.AdminService;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.json.JSONObject;

/**
 * Handler cho phần thống kê
 */
public class StatsHandler {

    private final Label lblStats;

    public StatsHandler(Label lblStats) {
        this.lblStats = lblStats;
    }

    public void loadSystemStats() {
        if (lblStats == null) {
            return; // Label not yet injected
        }

        new Thread(() -> {
            try {
                JSONObject result = AdminService.getSystemStats();

                Platform.runLater(() -> {
                    if (lblStats == null)
                        return; // Double check

                    if (result.has("statusCode") && result.getInt("statusCode") == 200) {
                        JSONObject users = result.getJSONObject("users");

                        int totalUsers = users.getInt("total");
                        int onlineUsers = users.getInt("online");
                        int admins = users.getInt("admins");
                        int normalUsers = totalUsers - admins;

                        String statsText = String.format("Người chơi online: %d/%d", onlineUsers, normalUsers);

                        lblStats.setText(statsText);
                    } else {
                        lblStats.setText("Không thể tải thống kê");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (lblStats != null) {
                        lblStats.setText("Lỗi khi tải thống kê");
                    }
                });
            }
        }).start();
    }
}
