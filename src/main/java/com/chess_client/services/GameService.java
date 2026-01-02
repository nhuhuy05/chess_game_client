package com.chess_client.services;

import com.chess_client.models.Piece;
import org.json.JSONArray;
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
     * Nếu lỗi mạng/server sập, lưu vào offline_results.json
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

            String winnerColor = null;
            if (winner != null) {
                winnerColor = winner == Piece.Color.WHITE ? "white" : "black";
                body.put("winnerColor", winnerColor);
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

            // Xử lý response: 200, 400 (game đã finished), hoặc 409 (đã kết thúc trước đó)
            // thì xóa khỏi offline_results.json
            int statusCode = response.statusCode();
            if (statusCode == 200 || statusCode == 400 || statusCode == 409) {
                // Kiểm tra xem game đã finished chưa
                try {
                    JSONObject responseBody = new JSONObject(response.body());
                    String message = responseBody.optString("message", "");
                    // Nếu server trả về 400/409 với message về kết thúc, xóa khỏi offline
                    if (statusCode == 400 || statusCode == 409 || message.contains("đã kết thúc")
                            || message.contains("đã được kết thúc")) {
                        OfflineResultManager.removeOfflineResult(gameId);
                        System.out.println("Đã xóa kết quả offline cho gameId: " + gameId);
                    } else if (statusCode == 200) {
                        OfflineResultManager.removeOfflineResult(gameId);
                        System.out.println("Đã xóa kết quả offline cho gameId: " + gameId);
                    }
                } catch (Exception e) {
                    // Nếu không parse được JSON, vẫn xóa nếu status 200, 400, hoặc 409
                    if (statusCode == 200 || statusCode == 400 || statusCode == 409) {
                        OfflineResultManager.removeOfflineResult(gameId);
                    }
                }
            }
        } catch (Exception e) {
            // Lỗi mạng/server sập: lưu vào offline_results.json
            System.err.println("Lỗi khi gửi kết quả trận đấu, lưu vào offline: " + e.getMessage());
            e.printStackTrace();

            String winnerStr = null;
            if (winner != null) {
                winnerStr = winner == Piece.Color.WHITE ? "white" : "black";
            }

            OfflineResultManager.saveOfflineResult(gameId, winnerStr, "pending");
            System.out.println("Đã lưu kết quả offline cho gameId: " + gameId);

            // Notify HomeController để khởi động timer nếu chưa chạy
            try {
                com.chess_client.controllers.HomeController.notifyOfflineResultSaved();
            } catch (Exception ex) {
                // Ignore nếu không thể notify (HomeController chưa được khởi tạo)
            }
        }
    }

    /**
     * Đồng bộ các kết quả offline: đọc file và gửi lại request
     * Được gọi khi app khởi động hoặc mỗi 1 phút
     * 
     * @return true nếu còn kết quả pending, false nếu đã sync xong hết
     */
    public boolean syncOfflineResults() {
        try {
            JSONArray offlineResults = OfflineResultManager.loadOfflineResults();
            if (offlineResults.length() == 0) {
                return false; // Không có kết quả nào cần sync
            }

            System.out.println("Bắt đầu sync " + offlineResults.length() + " kết quả offline...");

            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                System.out.println("Không có token, bỏ qua sync offline results");
                return OfflineResultManager.hasPendingResults(); // Trả về true nếu còn kết quả pending
            }

            // Duyệt qua từng kết quả và gửi lại
            for (int i = 0; i < offlineResults.length(); i++) {
                JSONObject result = offlineResults.getJSONObject(i);
                String gameId = result.getString("gameId");
                String winner = result.optString("winner", null);
                String status = result.optString("status", "pending");

                if (!"pending".equals(status)) {
                    continue; // Bỏ qua nếu không phải pending
                }

                try {
                    URI uri = URI.create(baseUrl + "/api/games/" + gameId + "/end");
                    JSONObject body = new JSONObject();

                    if (winner != null && !winner.isEmpty()) {
                        body.put("winnerColor", winner);
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

                    int statusCode = response.statusCode();
                    String responseBody = response.body();
                    System.out.println("Sync offline result - gameId: " + gameId +
                            ", statusCode: " + statusCode + ", response: " + responseBody);

                    // Nếu thành công (200), game đã finished (400), hoặc đã kết thúc trước đó
                    // (409), xóa khỏi offline
                    if (statusCode == 200 || statusCode == 400 || statusCode == 409) {
                        try {
                            JSONObject responseJson = new JSONObject(responseBody);
                            String message = responseJson.optString("message", "");
                            if (statusCode == 400 || statusCode == 409 || message.contains("đã kết thúc")
                                    || message.contains("đã được kết thúc")) {
                                OfflineResultManager.removeOfflineResult(gameId);
                                System.out.println("Đã xóa kết quả offline sau khi sync: " + gameId);
                            } else if (statusCode == 200) {
                                OfflineResultManager.removeOfflineResult(gameId);
                                System.out.println("Đã xóa kết quả offline sau khi sync: " + gameId);
                            }
                        } catch (Exception e) {
                            // Nếu không parse được JSON, vẫn xóa nếu status 200, 400, hoặc 409
                            if (statusCode == 200 || statusCode == 400 || statusCode == 409) {
                                OfflineResultManager.removeOfflineResult(gameId);
                                System.out.println("Đã xóa kết quả offline sau khi sync (parse error): " + gameId);
                            }
                        }
                    } else if (statusCode >= 500) {
                        // Lỗi server (500, 502, 503, etc.) - giữ lại để retry
                        System.err.println("Lỗi server khi sync gameId " + gameId +
                                ": " + statusCode + " - " + responseBody);
                    } else if (statusCode == 404) {
                        // Game không tồn tại - xóa khỏi offline vì không thể sync
                        System.out.println("Game không tồn tại (404), xóa khỏi offline: " + gameId);
                        OfflineResultManager.removeOfflineResult(gameId);
                    } else {
                        // Các lỗi khác (401, 403, etc.) - giữ lại để retry
                        System.err.println("Lỗi khi sync gameId " + gameId +
                                ": " + statusCode + " - " + responseBody);
                    }
                } catch (Exception e) {
                    // Lỗi khi sync, giữ lại trong file để retry lần sau
                    System.err.println("Lỗi khi sync kết quả offline cho gameId " + gameId + ": " + e.getMessage());
                }
            }

            // Kiểm tra xem còn kết quả pending không sau khi sync
            JSONArray remainingResults = OfflineResultManager.loadOfflineResults();
            return remainingResults.length() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi sync offline results: " + e.getMessage());
            e.printStackTrace();
            // Nếu có lỗi, giả sử còn kết quả để retry
            return OfflineResultManager.hasPendingResults();
        }
    }
}
