package com.chess_client.services;

public class TokenStorage {
    private static String accessToken;
    private static String refreshToken;

    public static void save(String access, String refresh) {
        accessToken = access;
        refreshToken = refresh;
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static String getRefreshToken() {
        return refreshToken;
    }

    public static void clear() {
        accessToken = null;
        refreshToken = null;
    }
}
