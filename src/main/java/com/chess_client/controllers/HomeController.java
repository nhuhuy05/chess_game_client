package com.chess_client.controllers;

import com.chess_client.models.Piece;
import com.chess_client.network.PeerClient;
import com.chess_client.network.PeerServer;
import com.chess_client.network.PeerService;
import com.chess_client.services.TokenStorage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.net.Socket;

public class HomeController {

    @FXML
    private Button btnRandom;

    @FXML
    private Button btnComputer;

    @FXML
    private Button btnFriend;

    @FXML
    private Button btnLeaderboard;

    @FXML
    private Button btnProfile;

    @FXML
    private Button btnExit;

    @FXML
    private Label lblWelcome;

    private Alert waitingAlert;

    @FXML
    private void handleRandomMatch() {
        btnRandom.setDisable(true);

        String token = TokenStorage.getAccessToken();
        if (token == null || token.isEmpty()) {
            showAlert("Lỗi", "Bạn chưa đăng nhập hoặc token không hợp lệ.");
            btnRandom.setDisable(false);
            return;
        }

        // Hiển thị dialog "đang chờ ghép"
        waitingAlert = new Alert(Alert.AlertType.INFORMATION);
        waitingAlert.setTitle("Đang tìm trận đấu");
        waitingAlert.setHeaderText(null);
        waitingAlert.setContentText("Đang tìm đối thủ, vui lòng chờ...");
        waitingAlert.initOwner(btnRandom.getScene().getWindow());
        waitingAlert.show();

        // Chạy matchmaking trên thread riêng để không block UI
        new Thread(() -> {
            try {
                PeerServer peerServer = new PeerServer();
                int localPort = peerServer.start(0); // chọn port trống bất kỳ

                PeerService peerService = new PeerService("http://localhost:3000/api/matchmaking", token);

                JSONObject res = peerService.joinMatchmaking(localPort);
                int status = res.optInt("statusCode", 0);

                if (status == 200 && "Match Found!".equals(res.optString("message"))) {
                    // Người vào hàng đợi thứ 2 sẽ nhận được match ngay ở đây
                    setupP2PAndOpenGame(res, peerServer);
                } else if (status == 202) {
                    // Là người vào hàng đợi trước, cần poll /status
                    boolean found = false;
                    for (int i = 0; i < 15; i++) { // thử tối đa ~15 giây
                        Thread.sleep(1000);
                        JSONObject statusRes = peerService.checkMatchStatus();
                        int st = statusRes.optInt("statusCode", 0);
                        if (st == 200 && "Match Found!".equals(statusRes.optString("message"))) {
                            found = true;
                            setupP2PAndOpenGame(statusRes, peerServer);
                            break;
                        } else if (st == 404) {
                            break; // không còn trong hàng đợi
                        }
                    }
                    if (!found) {
                        Platform.runLater(() -> {
                            closeWaitingAlert();
                            showAlert("Thông báo", "Không tìm được trận đấu phù hợp, vui lòng thử lại.");
                            btnRandom.setDisable(false);
                        });
                    }
                } else {
                    Platform.runLater(() -> {
                        closeWaitingAlert();
                        showAlert("Lỗi", res.optString("message", "Ghép trận thất bại."));
                        btnRandom.setDisable(false);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    closeWaitingAlert();
                    showAlert("Lỗi", "Có lỗi xảy ra khi ghép trận: " + e.getMessage());
                    btnRandom.setDisable(false);
                });
            }
        }).start();
    }

    private void setupP2PAndOpenGame(JSONObject res, PeerServer peerServer) {
        try {
            // Đã tìm thấy trận -> đóng dialog chờ ngay lập tức
            Platform.runLater(this::closeWaitingAlert);

            String colorStr = res.getString("color");
            JSONObject opponent = res.getJSONObject("opponent");

            String opponentIp = opponent.getString("ip");
            int opponentPort = opponent.getInt("port");

            Piece.Color color = "white".equalsIgnoreCase(colorStr)
                    ? Piece.Color.WHITE
                    : Piece.Color.BLACK;

            Socket socket;
            if (color == Piece.Color.WHITE) {
                // Bạn là TRẮNG: đóng vai trò "host", chờ đối thủ kết nối tới
                socket = peerServer.waitForOpponent();
            } else {
                // Bạn là ĐEN: chủ động kết nối tới đối thủ
                PeerClient client = new PeerClient();
                socket = client.connectToOpponent(opponentIp, opponentPort);
            }

            openGameWithMatch(res, socket);
        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.runLater(() -> {
                closeWaitingAlert();
                showAlert("Lỗi", "Không thể tạo kết nối P2P: " + ex.getMessage());
                btnRandom.setDisable(false);
            });
        }
    }

    private void openGameWithMatch(JSONObject res, Socket socket) {
        Platform.runLater(() -> {
            try {
                closeWaitingAlert();
                String colorStr = res.getString("color");
                Piece.Color color = "white".equalsIgnoreCase(colorStr)
                        ? Piece.Color.WHITE
                        : Piece.Color.BLACK;

                // Lấy tên mình từ TokenStorage và tên đối thủ từ response
                String myName = TokenStorage.getDisplayName();
                JSONObject opponent = res.getJSONObject("opponent");
                String opponentName = opponent.optString("display_name",
                        opponent.optString("username", "Đối thủ"));

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/game.fxml"));
                Parent root = loader.load();
                GameController controller = loader.getController();
                controller.setPlayerNames(myName, opponentName);
                controller.setPlayerColor(color);
                controller.setPeerSocket(socket);

                Stage stage = (Stage) btnRandom.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Lỗi", "Không thể mở màn hình game: " + ex.getMessage());
            } finally {
                btnRandom.setDisable(false);
            }
        });
    }

    private void closeWaitingAlert() {
        if (waitingAlert != null) {
            waitingAlert.close();
            waitingAlert = null;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handlePlayComputer() {
        System.out.println("Chơi với máy đã được chọn");
        // Thêm logic chơi với máy
    }

    @FXML
    private void handlePlayFriend() {
        System.out.println("Chơi với bạn bè đã được chọn");
        // Thêm logic chơi với bạn bè
    }

    @FXML
    private void handleLeaderboard() {
        System.out.println("Bảng xếp hạng đã được chọn");
        // Thêm logic hiển thị bảng xếp hạng
    }

    @FXML
    private void handleProfile() {
        System.out.println("Hồ sơ cá nhân đã được chọn");
        // Thêm logic hiển thị hồ sơ
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }
}
