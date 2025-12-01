package com.chess_client.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class AuthService {
    private static final String BASE_URL = "http://localhost:5000/api/auth";

    public static JSONObject signIn(String username, String password) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/signin"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());

            // chỉ parse nếu là JSON thật
            String bodyText = response.body().trim();
            if (!bodyText.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ: " + bodyText);
                return error;
            }

            return new JSONObject(bodyText);

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

    public static void signOutSync() {
        try {
            String refresh = TokenStorage.getRefreshToken();
            if (refresh == null || refresh.isEmpty()) {
                return;
            }

            JSONObject body = new JSONObject();
            body.put("refreshToken", refresh);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/signout"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TokenStorage.clear();
        }
    }

    public static void signOutAsync() {
        new Thread(AuthService::signOutSync).start();
    }

    public static JSONObject signUp(String username, String password, String email, String displayName) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);
            body.put("email", email);
            body.put("displayName", displayName);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/signup"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Response: " + response.body());

            // ====== Xử lý trường hợp 204 No Content ======
            if (response.statusCode() == 204) {
                JSONObject ok = new JSONObject();
                ok.put("status", 204);
                ok.put("message", "Đăng ký thành công!");
                return ok;
            }

            // ====== Nếu body không phải JSON ======
            String text = response.body().trim();
            if (!text.startsWith("{")) {
                JSONObject error = new JSONObject();
                error.put("message", "Server không trả về JSON hợp lệ: " + text);
                return error;
            }

            return new JSONObject(text);

        } catch (Exception e) {
            e.printStackTrace();
            JSONObject error = new JSONObject();
            error.put("message", "Lỗi kết nối: " + e.getMessage());
            return error;
        }
    }

}
