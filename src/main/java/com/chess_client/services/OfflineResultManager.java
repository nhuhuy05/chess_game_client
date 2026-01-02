package com.chess_client.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class để quản lý kết quả trận đấu offline khi server sập hoặc mất kết
 * nối.
 * Lưu trữ trong file offline_results.json
 */
public class OfflineResultManager {
    private static final String OFFLINE_RESULTS_FILE = "offline_results.json";

    /**
     * Lưu kết quả trận đấu vào file offline_results.json
     * 
     * @param gameId ID của trận đấu
     * @param winner Màu người thắng ("white", "black", hoặc null nếu hòa)
     * @param status Trạng thái ("pending")
     */
    public static void saveOfflineResult(String gameId, String winner, String status) {
        try {
            JSONArray results = loadOfflineResults();

            // Kiểm tra xem gameId đã tồn tại chưa
            boolean exists = false;
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                if (item.getString("gameId").equals(gameId)) {
                    // Cập nhật entry hiện có
                    item.put("winner", winner);
                    item.put("status", status);
                    exists = true;
                    break;
                }
            }

            // Nếu chưa tồn tại, thêm mới
            if (!exists) {
                JSONObject newResult = new JSONObject();
                newResult.put("gameId", gameId);
                newResult.put("winner", winner);
                newResult.put("status", status);
                results.put(newResult);
            }

            // Lưu lại file
            saveOfflineResults(results);
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu kết quả offline: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Đọc tất cả kết quả offline từ file
     * 
     * @return JSONArray chứa các kết quả offline
     */
    public static JSONArray loadOfflineResults() {
        try {
            Path filePath = Paths.get(OFFLINE_RESULTS_FILE);
            if (!Files.exists(filePath)) {
                return new JSONArray();
            }

            String content = new String(Files.readAllBytes(filePath));
            if (content.trim().isEmpty()) {
                return new JSONArray();
            }

            return new JSONArray(content);
        } catch (Exception e) {
            System.err.println("Lỗi khi đọc file offline_results.json: " + e.getMessage());
            return new JSONArray();
        }
    }

    /**
     * Lưu JSONArray vào file
     */
    private static void saveOfflineResults(JSONArray results) throws IOException {
        Path filePath = Paths.get(OFFLINE_RESULTS_FILE);
        Files.write(filePath, results.toString(2).getBytes());
    }

    /**
     * Xóa một kết quả offline theo gameId
     * 
     * @param gameId ID của trận đấu cần xóa
     */
    public static void removeOfflineResult(String gameId) {
        try {
            JSONArray results = loadOfflineResults();
            JSONArray newResults = new JSONArray();

            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                if (!item.getString("gameId").equals(gameId)) {
                    newResults.put(item);
                }
            }

            saveOfflineResults(newResults);
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa kết quả offline: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kiểm tra xem có kết quả offline nào chưa được sync không
     * 
     * @return true nếu có kết quả pending
     */
    public static boolean hasPendingResults() {
        JSONArray results = loadOfflineResults();
        return results.length() > 0;
    }
}
