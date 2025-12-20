package com.chess_client.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LeaderboardService {
    private static final String BASE_URL = ApiConfig.BASE_URL + "/api/leaderboard";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static JSONArray getLeaderboard(int limit) throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?limit=" + limit))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            return json.getJSONArray("leaderboard");
        } else {
            throw new IOException("Lỗi khi lấy bảng xếp hạng: " + response.statusCode());
        }
    }
}







