package com.chess_client.services;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ProfileService {
    private static final String BASE_URL = ApiConfig.BASE_URL + "/api/users";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static JSONObject getProfile() throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/me"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return new JSONObject(response.body());
        } else {
            throw new IOException("Lỗi khi lấy profile: " + response.statusCode());
        }
    }

    public static void updateProfile(String displayName, String email, String phone) throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        JSONObject body = new JSONObject();
        if (displayName != null && !displayName.isEmpty()) {
            body.put("displayName", displayName);
        }
        if (email != null && !email.isEmpty()) {
            body.put("email", email);
        }
        if (phone != null && !phone.isEmpty()) {
            body.put("phone", phone);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/me"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.optString("message", "Lỗi khi cập nhật profile"));
        }
    }

    public static void changePassword(String currentPassword, String newPassword) throws IOException, InterruptedException {
        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Chưa đăng nhập");
        }

        JSONObject body = new JSONObject();
        body.put("currentPassword", currentPassword);
        body.put("newPassword", newPassword);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/me/password"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new IOException(errorJson.optString("message", "Lỗi khi đổi mật khẩu"));
        }
    }
}







