package com.chess_client.network;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Gọi API matchmaking trên server để ghép trận P2P.
 */
public class PeerService {
    private final String baseUrl; // ví dụ: "http://192.168.1.10:3000/api/matchmaking"
    private final String jwtToken; // token lấy từ AuthService

    public PeerService(String baseUrl, String jwtToken) {
        this.baseUrl = baseUrl;
        this.jwtToken = jwtToken;
    }

    /**
     * Gửi port mà PeerServer đang lắng nghe lên server để tham gia hàng đợi ghép
     * trận.
     *
     * @param socketPort port local đang listen
     * @return JSON response từ server
     */
    public JSONObject joinMatchmaking(int socketPort) throws IOException {
        URL url = new URL(baseUrl + "/join");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        conn.setDoOutput(true);

        JSONObject body = new JSONObject();
        body.put("socketPort", socketPort);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        System.out.println("[PeerService] joinMatchmaking status = " + status);
        InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String bodyText = sb.toString();
            System.out.println("[PeerService] joinMatchmaking body = " + bodyText);
            JSONObject json = bodyText.isEmpty() ? new JSONObject() : new JSONObject(bodyText);
            json.put("statusCode", status);
            return json;
        }
    }

    /**
     * Kiểm tra trạng thái ghép trận hiện tại cho user (dựa trên JWT).
     */
    public JSONObject checkMatchStatus() throws IOException {
        URL url = new URL(baseUrl + "/status");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + jwtToken);

        int status = conn.getResponseCode();
        System.out.println("[PeerService] checkMatchStatus status = " + status);
        InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String bodyText = sb.toString();
            System.out.println("[PeerService] checkMatchStatus body = " + bodyText);
            JSONObject json = bodyText.isEmpty() ? new JSONObject() : new JSONObject(bodyText);
            json.put("statusCode", status);
            return json;
        }
    }
}
