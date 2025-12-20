package com.chess_client.network;

import com.chess_client.models.Move;
import javafx.application.Platform;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Xử lý giao tiếp mạng P2P giữa hai client trong trận đấu.
 * Chịu trách nhiệm gửi/nhận nước đi, chat và các game actions.
 */
public class PeerNetworkHandler {

    private Socket peerSocket;
    private BufferedReader peerIn;
    private PrintWriter peerOut;

    // Callbacks
    private OnMoveReceived onMoveReceived;
    private OnChatReceived onChatReceived;
    private OnGameActionReceived onGameActionReceived;

    /**
     * Callback khi nhận được nước đi từ đối thủ.
     * GameController sẽ lấy Piece từ board và tạo Move.
     */
    public interface OnMoveReceived {
        void onMove(int fromRow, int fromCol, int toRow, int toCol);
    }

    public interface OnChatReceived {
        void onChat(String message);
    }

    public interface OnGameActionReceived {
        void onGameAction(String action);
    }

    public PeerNetworkHandler() {
    }

    /**
     * Thiết lập socket P2P và bắt đầu lắng nghe tin nhắn.
     */
    public void setPeerSocket(Socket socket) {
        this.peerSocket = socket;
        try {
            this.peerIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.peerOut = new PrintWriter(socket.getOutputStream(), true);

            // Bắt đầu thread lắng nghe
            Thread listener = new Thread(this::listenForPeerMessages);
            listener.setDaemon(true);
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnMoveReceived(OnMoveReceived callback) {
        this.onMoveReceived = callback;
    }

    public void setOnChatReceived(OnChatReceived callback) {
        this.onChatReceived = callback;
    }

    public void setOnGameActionReceived(OnGameActionReceived callback) {
        this.onGameActionReceived = callback;
    }

    /**
     * Gửi nước đi đến đối thủ qua P2P.
     */
    public void sendMove(Move move) {
        if (peerOut == null)
            return;

        try {
            JSONObject json = new JSONObject();
            json.put("type", "move");
            json.put("fromRow", move.getFromRow());
            json.put("fromCol", move.getFromCol());
            json.put("toRow", move.getToRow());
            json.put("toCol", move.getToCol());
            peerOut.println(json.toString());
            peerOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gửi tin nhắn chat đến đối thủ.
     */
    public void sendChatMessage(String message) {
        if (peerOut == null)
            return;

        try {
            JSONObject json = new JSONObject();
            json.put("type", "chat");
            json.put("message", message);
            peerOut.println(json.toString());
            peerOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gửi game action (resign, offer_draw, accept_draw, reject_draw).
     */
    public void sendGameAction(String action) {
        if (peerOut == null)
            return;

        try {
            JSONObject json = new JSONObject();
            json.put("type", "game_action");
            json.put("action", action);
            peerOut.println(json.toString());
            peerOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lắng nghe tin nhắn từ đối thủ ở thread riêng.
     */
    private void listenForPeerMessages() {
        if (peerIn == null)
            return;

        try {
            String line;
            while ((line = peerIn.readLine()) != null) {
                try {
                    JSONObject json = new JSONObject(line);
                    String type = json.optString("type");

                    switch (type) {
                        case "move" -> handleReceivedMove(json);
                        case "chat" -> handleReceivedChat(json);
                        case "game_action" -> handleReceivedGameAction(json);
                    }
                } catch (Exception parseEx) {
                    parseEx.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleReceivedMove(JSONObject json) {
        if (onMoveReceived == null)
            return;

        int fromRow = json.getInt("fromRow");
        int fromCol = json.getInt("fromCol");
        int toRow = json.getInt("toRow");
        int toCol = json.getInt("toCol");

        // Pass thông tin row/col về GameController, nơi sẽ lấy Piece từ board
        Platform.runLater(() -> {
            if (onMoveReceived != null) {
                onMoveReceived.onMove(fromRow, fromCol, toRow, toCol);
            }
        });
    }

    private void handleReceivedChat(JSONObject json) {
        if (onChatReceived == null)
            return;

        String message = json.getString("message");
        Platform.runLater(() -> onChatReceived.onChat(message));
    }

    private void handleReceivedGameAction(JSONObject json) {
        if (onGameActionReceived == null)
            return;

        String action = json.getString("action");
        Platform.runLater(() -> onGameActionReceived.onGameAction(action));
    }

    /**
     * Đóng kết nối network.
     */
    public void close() {
        try {
            if (peerIn != null)
                peerIn.close();
            if (peerOut != null)
                peerOut.close();
            if (peerSocket != null && !peerSocket.isClosed()) {
                peerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
