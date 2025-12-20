package com.chess_client.controllers.admin.handlers;

import com.chess_client.controllers.admin.models.RankingRow;
import com.chess_client.services.AdminService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Consumer;

/**
 * Handler cho tab quản lý xếp hạng
 */
public class RankingTabHandler {

    private final ObservableList<RankingRow> rankingList;
    private final Consumer<String> showAlert;

    public RankingTabHandler(ObservableList<RankingRow> rankingList, Consumer<String> showAlert) {
        this.rankingList = rankingList;
        this.showAlert = showAlert;
    }

    public void loadRankings() {
        new Thread(() -> {
            try {
                JSONObject result = AdminService.getAllRankings(1, 20);

                Platform.runLater(() -> {
                    if (result.has("statusCode") && result.getInt("statusCode") == 200) {
                        rankingList.clear();
                        JSONArray rankings = result.getJSONArray("rankings");

                        for (int i = 0; i < rankings.length(); i++) {
                            JSONObject ranking = rankings.getJSONObject(i);
                            rankingList.add(new RankingRow(
                                    i + 1,
                                    ranking.getInt("user_id"),
                                    ranking.getString("username"),
                                    ranking.optString("display_name", ""),
                                    ranking.getInt("games_played"),
                                    ranking.getInt("wins"),
                                    ranking.getInt("losses"),
                                    ranking.getInt("draws"),
                                    ranking.getInt("score")));
                        }
                    } else {
                        showAlert.accept(result.optString("message", "Không thể tải bảng xếp hạng"));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert.accept("Lỗi kết nối: " + e.getMessage()));
            }
        }).start();
    }
}
