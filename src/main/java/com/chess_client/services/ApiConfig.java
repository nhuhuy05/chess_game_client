package com.chess_client.services;

/**
 * Cấu hình chung cho các URL backend phía client.
 * Ưu tiên:
 * - System property: -DAPI_BASE_URL=http://host:port
 * - Biến môi trường: API_BASE_URL
 * - Mặc định: http://localhost:5000
 */
public class ApiConfig {

    public static final String BASE_URL;
    public static final String AUTH_BASE;
    public static final String GAMES_BASE;
    public static final String MATCHMAKING_BASE;

    static {
        String fromProp = System.getProperty("API_BASE_URL");
        String fromEnv = System.getenv("API_BASE_URL");
        String base = fromProp != null && !fromProp.isBlank()
                ? fromProp
                : (fromEnv != null && !fromEnv.isBlank() ? fromEnv : "http://localhost:5000");

        // Bỏ dấu / ở cuối nếu có
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        BASE_URL = base;
        AUTH_BASE = BASE_URL + "/api/auth";
        GAMES_BASE = BASE_URL + "/api/games";
        MATCHMAKING_BASE = BASE_URL + "/api/matchmaking";
    }
}
