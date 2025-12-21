package com.chess_client.controllers.admin.handlers;

import com.chess_client.controllers.admin.models.GameRow;
import com.chess_client.services.AdminService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handler cho tab quản lý trận đấu
 */
public class GameTabHandler {

    private final ObservableList<GameRow> gameList;
    private final Consumer<String> showAlert;

    public GameTabHandler(ObservableList<GameRow> gameList, Consumer<String> showAlert) {
        this.gameList = gameList;
        this.showAlert = showAlert;
    }

    public void loadGames(String status) {
        if ("Tất cả".equals(status)) {
            status = "";
        }

        String finalStatus = status;
        new Thread(() -> {
            try {
                Platform.runLater(() -> gameList.clear());
                int page = 1;
                boolean hasMore = true;
                int maxPages = 10; // Limit to prevent memory issues

                // Load games by paginating through pages (with limit)
                while (hasMore && page <= maxPages) {
                    JSONObject result = AdminService.getAllGames(page, 100, finalStatus); // Reduced page size

                    if (result.has("statusCode") && result.getInt("statusCode") == 200) {
                        JSONArray games = result.getJSONArray("games");

                        if (games.length() == 0) {
                            hasMore = false;
                        } else {
                            // Collect games for this page
                            List<GameRow> pageGames = new ArrayList<>();
                            for (int i = 0; i < games.length(); i++) {
                                JSONObject game = games.getJSONObject(i);
                                pageGames.add(new GameRow(
                                        game.getInt("id"),
                                        game.optString("white_display_name", game.optString("white_username", "N/A")),
                                        game.optString("black_display_name", game.optString("black_username", "N/A")),
                                        game.getString("mode"),
                                        game.getString("status"),
                                        game.optString("winner_display_name",
                                                game.optString("winner_username", "N/A"))));
                            }

                            // Update UI on JavaFX thread
                            Platform.runLater(() -> gameList.addAll(pageGames));

                            // Check if there are more pages
                            if (result.has("pagination")) {
                                JSONObject pagination = result.getJSONObject("pagination");
                                int currentPage = pagination.getInt("page");
                                int totalPages = pagination.getInt("totalPages");
                                if (currentPage >= totalPages) {
                                    hasMore = false;
                                } else {
                                    page++;
                                }
                            } else {
                                hasMore = false;
                            }
                        }
                    } else {
                        hasMore = false;
                        Platform.runLater(() -> showAlert.accept(
                                result.optString("message", "Không thể tải danh sách trận đấu")));
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> showAlert.accept("Lỗi kết nối: " + e.getMessage()));
            }
        }).start();
    }
}
