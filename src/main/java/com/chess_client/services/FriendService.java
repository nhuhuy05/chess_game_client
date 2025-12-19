package com.chess_client.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FriendService {
    private static final String BASE_URL = ApiConfig.BASE_URL + "/api/friends";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static JSONArray getFriends() throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            return json.getJSONArray("friends");
        } else {
            throw new IOException("Lỗi khi lấy danh sách bạn bè: " + response.statusCode());
        }
    }

    public static JSONArray getFriendRequests() throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/requests"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            return json.getJSONArray("requests");
        } else {
            throw new IOException("Lỗi khi lấy lời mời kết bạn: " + response.statusCode());
        }
    }

    public static JSONArray searchUsers(String searchTerm) throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        String encodedQuery = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/search?q=" + encodedQuery))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            return json.getJSONArray("users");
        } else {
            throw new IOException("Lỗi khi tìm kiếm: " + response.statusCode());
        }
    }

    public static void sendFriendRequest(int addresseeId) throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        JSONObject body = new JSONObject();
        body.put("addressee_id", addresseeId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/request"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Chấp nhận cả 200 và 201 (200 là khi auto-accept từ phía server)
        int statusCode = response.statusCode();
        if (statusCode != 201 && statusCode != 200) {
            String errorMessage = "Lỗi khi gửi lời mời kết bạn";
            try {
                String responseBody = response.body();
                if (responseBody != null && !responseBody.trim().isEmpty()) {
                    JSONObject errorJson = new JSONObject(responseBody);
                    errorMessage = errorJson.optString("message", errorMessage);
                }
            } catch (Exception e) {
                // Nếu không parse được JSON, dùng message mặc định
                errorMessage = "Lỗi khi gửi lời mời kết bạn (Status: " + statusCode + ")";
            }
            throw new IOException(errorMessage);
        }
    }

    public static void acceptFriendRequest(int requesterId) throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        JSONObject body = new JSONObject();
        body.put("requester_id", requesterId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/accept"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.optString("message", "Lỗi khi chấp nhận lời mời"));
        }
    }

    public static void declineFriendRequest(int requesterId) throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        JSONObject body = new JSONObject();
        body.put("requester_id", requesterId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/decline"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.optString("message", "Lỗi khi từ chối lời mời"));
        }
    }

    public static void deleteFriend(int friendId) throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + friendId))
                .header("Authorization", "Bearer " + token)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.optString("message", "Lỗi khi xóa bạn bè"));
        }
    }

    // ===================== GAME INVITATIONS =====================

    public static void inviteFriendToPlay(int friendId, int socketPort) throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        JSONObject body = new JSONObject();
        body.put("friend_id", friendId);
        body.put("socketPort", socketPort);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/game/invite"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.optString("message", "Lỗi khi gửi lời mời chơi cờ"));
        }
    }

    public static JSONArray getGameInvitations() throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/game/invitations"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject json = new JSONObject(response.body());
            return json.getJSONArray("invitations");
        } else {
            throw new IOException("Lỗi khi lấy lời mời chơi cờ: " + response.statusCode());
        }
    }

    public static JSONObject getFriendGameStatus() throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/game/status"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else if (response.statusCode() == 404) {
            return null; // Chưa có game
        } else {
            throw new IOException("Lỗi khi kiểm tra trạng thái game: " + response.statusCode());
        }
    }

    public static JSONObject acceptGameInvitation(int senderId) throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        JSONObject body = new JSONObject();
        body.put("sender_id", senderId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/game/accept"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.optString("message", "Lỗi khi chấp nhận lời mời"));
        }
    }

    public static void declineGameInvitation(int senderId) throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        JSONObject body = new JSONObject();
        body.put("sender_id", senderId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/game/decline"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.optString("message", "Lỗi khi từ chối lời mời"));
        }
    }
}
