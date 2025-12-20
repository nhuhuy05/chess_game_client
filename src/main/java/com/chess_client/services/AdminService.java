package com.chess_client.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

/**
 * Service để quản lý các chức năng admin
 */
public class AdminService {
    private static final String BASE_URL = ApiConfig.ADMIN_BASE;

    /**
     * Lấy thống kê tổng quan hệ thống
     */
    public static JSONObject getSystemStats() {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/stats"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401 || response.statusCode() == 403) {
                JSONObject error = new JSONObject();
                error.put("message", "Không có quyền truy cập");
                error.put("statusCode", response.statusCode());
                return error;
            }

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Lấy danh sách tất cả người dùng
     */
    public static JSONObject getAllUsers(int page, int limit, String search) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            String url = BASE_URL + "/users?page=" + page + "&limit=" + limit;
            if (search != null && !search.isEmpty()) {
                url += "&search=" + java.net.URLEncoder.encode(search, "UTF-8");
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401 || response.statusCode() == 403) {
                JSONObject error = new JSONObject();
                error.put("message", "Không có quyền truy cập");
                error.put("statusCode", response.statusCode());
                return error;
            }

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Lấy chi tiết một người dùng
     */
    public static JSONObject getUserDetails(int userId) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Cập nhật thông tin người dùng
     */
    public static JSONObject updateUser(int userId, String displayName, String email, 
                                       String phone, String role, Boolean isActive) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            JSONObject body = new JSONObject();
            if (displayName != null) body.put("displayName", displayName);
            if (email != null) body.put("email", email);
            if (phone != null) body.put("phone", phone);
            if (role != null) body.put("role", role);
            if (isActive != null) body.put("isActive", isActive);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Khóa tài khoản người dùng
     */
    public static JSONObject banUser(int userId) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId + "/ban"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Mở khóa tài khoản người dùng
     */
    public static JSONObject unbanUser(int userId) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId + "/unban"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Xóa người dùng
     */
    public static JSONObject deleteUser(int userId) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Đặt lại mật khẩu cho người dùng
     */
    public static JSONObject resetUserPassword(int userId, String newPassword) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            JSONObject body = new JSONObject();
            body.put("newPassword", newPassword);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId + "/reset-password"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Thăng cấp người dùng lên admin
     */
    public static JSONObject promoteToAdmin(int userId) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId + "/promote"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Hạ cấp admin xuống user thông thường
     */
    public static JSONObject demoteFromAdmin(int userId) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/users/" + userId + "/demote"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    // ==================== QUẢN LÝ TRẬN ĐẤU ====================

    /**
     * Lấy danh sách tất cả trận đấu
     */
    public static JSONObject getAllGames(int page, int limit, String status) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            String url = BASE_URL + "/games?page=" + page + "&limit=" + limit;
            if (status != null && !status.isEmpty()) {
                url += "&status=" + java.net.URLEncoder.encode(status, "UTF-8");
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Lấy chi tiết trận đấu
     */
    public static JSONObject getGameDetails(int gameId) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/games/" + gameId))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Xóa trận đấu
     */
    public static JSONObject deleteGame(int gameId) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/games/" + gameId))
                    .header("Authorization", "Bearer " + token)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    // ==================== QUẢN LÝ XẾP HẠNG ====================

    /**
     * Lấy danh sách xếp hạng
     */
    public static JSONObject getAllRankings(int page, int limit) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            String url = BASE_URL + "/rankings?page=" + page + "&limit=" + limit;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Cập nhật xếp hạng của user
     */
    public static JSONObject updateRanking(int userId, Integer gamesPlayed, Integer wins, 
                                          Integer losses, Integer draws, Integer score) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            JSONObject body = new JSONObject();
            if (gamesPlayed != null) body.put("gamesPlayed", gamesPlayed);
            if (wins != null) body.put("wins", wins);
            if (losses != null) body.put("losses", losses);
            if (draws != null) body.put("draws", draws);
            if (score != null) body.put("score", score);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/rankings/" + userId))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    /**
     * Reset xếp hạng của user về 0
     */
    public static JSONObject resetRanking(int userId) {
        try {
            String token = TokenStorage.getAccessToken();
            if (token == null || token.isEmpty()) {
                JSONObject error = new JSONObject();
                error.put("message", "Chưa đăng nhập");
                return error;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/rankings/" + userId + "/reset"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ");
                return error;
            }

            JSONObject result = new JSONObject(bodyText);
            result.put("statusCode", response.statusCode());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }
}

