package com.chess_client.services;

import com.chess_client.models.Piece;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service chịu trách nhiệm gọi API game server (kết thúc trận, cập nhật
 * ranking, ...).
 * Controller chỉ gọi các hàm public ở đây và không xử lý HTTP trực tiếp.
 */
public class GameService {

    private final String baseUrl;
    private final HttpClient httpClient;

    public GameService() {
        this(ApiConfig.BASE_URL);
    }

    public GameService(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Gọi API kết thúc trận đấu:
     * - winner != null: gửi winnerColor = "white"/"black"
     * - winner == null: gửi result = "draw"
     */
    public void endGame(String gameId, Piece.Color winner) {
        if (gameId == null || gameId.isEmpty()) {
            return;
        }

        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                return;
            }

            URI uri = URI.create(baseUrl + "/api/games/" + gameId + "/end");
            JSONObject body = new JSONObject();

            if (winner != null) {
                body.put("winnerColor", winner == Piece.Color.WHITE ? "white" : "black");
            } else {
                body.put("result", "draw");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("GameService.endGame response: " +
                    response.statusCode() + " - " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
