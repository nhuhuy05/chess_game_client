package com.chess_client.services;

public class TokenStorage {
    private static String accessToken;
    private static String refreshToken;
    private static String displayName;
    private static String userId;

    public static void save(String access, String refresh, String displayName, String userId) {
        accessToken = access;
        refreshToken = refresh;
        TokenStorage.displayName = displayName;
        TokenStorage.userId = userId;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static String getRefreshToken() {
        return refreshToken;
    }

    public static String getDisplayName() {
        return displayName;
    }

    public static String getUserId() {
        return userId;
    }
}
