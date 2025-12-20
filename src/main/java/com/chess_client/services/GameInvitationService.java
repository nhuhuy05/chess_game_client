package com.chess_client.services;

import com.chess_client.network.PeerClient;
import com.chess_client.network.PeerServer;
import javafx.application.Platform;
import org.json.JSONObject;

import java.net.Socket;
import java.util.function.Consumer;

/**
 * Service xử lý logic game invitation (gửi/nhận lời mời chơi cờ với bạn bè).
 * Tương tự HomeService, xử lý flow phức tạp với async operations và peer
 * connections.
 */
public class GameInvitationService {

    public interface GameOpenCallback {
        void openGame(JSONObject gameResult, Socket socket);
    }

    public interface ErrorCallback {
        void onError(String message);
    }

    /**
     * Gửi lời mời chơi cờ tới bạn bè
     */
    public static void inviteFriend(int friendId, String friendName,
            GameOpenCallback onGameOpen,
            Consumer<String> onSuccess,
            ErrorCallback onError) {
        new Thread(() -> {
            try {
                PeerServer peerServer = new PeerServer();
                int localPort = peerServer.start(0);
                FriendService.inviteFriendToPlay(friendId, localPort);

                Platform.runLater(() -> {
                    if (onSuccess != null) {
                        onSuccess.accept("Đã gửi lời mời chơi cờ đến " + friendName + ". Đang chờ phản hồi...");
                    }
                    waitForGameAcceptance(peerServer, friendId, onGameOpen, onError);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (onError != null) {
                        onError.onError("Không thể gửi lời mời chơi cờ: " + e.getMessage());
                    }
                });
            }
        }).start();
    }

    private static void waitForGameAcceptance(PeerServer peerServer, int friendId,
            GameOpenCallback onGameOpen,
            ErrorCallback onError) {
        new Thread(() -> {
            try {
                Socket socket = peerServer.waitForOpponent();

                // Polling để lấy thông tin game
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(500);
                    JSONObject gameStatus = FriendService.getFriendGameStatus();
                    if (gameStatus != null) {
                        Platform.runLater(() -> onGameOpen.openGame(gameStatus, socket));
                        return;
                    }
                }

                // Không tìm thấy game
                socket.close();
                peerServer.stop();
                Platform.runLater(() -> {
                    if (onError != null) {
                        onError.onError("Không thể tìm thấy thông tin trận đấu");
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    peerServer.stop();
                } catch (Exception ex) {
                    // Ignore
                }
                Platform.runLater(() -> {
                    if (onError != null) {
                        onError.onError("Lỗi khi chờ phản hồi: " + e.getMessage());
                    }
                });
            }
        }).start();
    }

    /**
     * Chấp nhận lời mời chơi cờ
     */
    public static void acceptInvitation(JSONObject invitation,
            GameOpenCallback onGameOpen,
            ErrorCallback onError) {
        new Thread(() -> {
            try {
                JSONObject gameResult = FriendService.acceptGameInvitation(invitation.getInt("senderId"));
                String opponentIp = invitation.getString("ip");
                int opponentPort = invitation.getInt("socketPort");

                PeerClient client = new PeerClient();
                Socket socket = client.connectToOpponent(opponentIp, opponentPort);

                Platform.runLater(() -> onGameOpen.openGame(gameResult, socket));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (onError != null) {
                        onError.onError("Không thể chấp nhận lời mời: " + e.getMessage());
                    }
                });
            }
        }).start();
    }
}
