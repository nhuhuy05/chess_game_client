package com.chess_client.controllers;

import com.chess_client.models.Piece;
import com.chess_client.network.PeerClient;
import com.chess_client.network.PeerServer;
import com.chess_client.services.FriendService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.Socket;

public class FriendsController {

    @FXML
    private Button backButton;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private VBox friendsListContainer;

    @FXML
    private VBox searchResultsContainer;

    @FXML
    private VBox friendRequestsContainer;

    @FXML
    private TextField searchTextField;

    @FXML
    private Button searchButton;

    @FXML
    private Button refreshFriendsButton;

    @FXML
    private Button refreshRequestsButton;

    @FXML
    private Button refreshGameInvitationsButton;

    @FXML
    private VBox gameInvitationsContainer;

    @FXML
    public void initialize() {
        // Load danh sách bạn bè khi khởi tạo
        refreshFriendsList();
        refreshFriendRequests();
        refreshGameInvitations();

        // Tự động refresh lời mời chơi cờ mỗi 3 giây
        startGameInvitationChecker();
        // Tự động refresh lời mời kết bạn mỗi 3 giây
        startFriendRequestChecker();
    }

    private void startGameInvitationChecker() {
        Thread checkerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(3000); // Kiểm tra mỗi 3 giây
                    Platform.runLater(() -> {
                        refreshGameInvitations();
                    });
                } catch (Exception e) {
                    // Bỏ qua lỗi
                }
            }
        });
        checkerThread.setDaemon(true);
        checkerThread.start();
    }

    private void startFriendRequestChecker() {
        Thread checkerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(3000); // Kiểm tra mỗi 3 giây
                    Platform.runLater(() -> {
                        refreshFriendRequests();
                    });
                } catch (Exception e) {
                    // Bỏ qua lỗi
                }
            }
        });
        checkerThread.setDaemon(true);
        checkerThread.start();
    }

    @FXML
    private void refreshGameInvitations() {
        new Thread(() -> {
            try {
                JSONArray invitations = FriendService.getGameInvitations();
                Platform.runLater(() -> {
                    gameInvitationsContainer.getChildren().clear();
                    if (invitations.length() == 0) {
                        Label noInvitationsLabel = new Label("Không có lời mời chơi cờ nào");
                        noInvitationsLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 14px;");
                        gameInvitationsContainer.getChildren().add(noInvitationsLabel);
                    } else {
                        for (int i = 0; i < invitations.length(); i++) {
                            JSONObject invitation = invitations.getJSONObject(i);
                            gameInvitationsContainer.getChildren().add(createGameInvitationItem(invitation));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Bỏ qua lỗi, không hiển thị alert
            }
        }).start();
    }

    private HBox createGameInvitationItem(JSONObject invitation) {
        HBox item = new HBox(15);
        item.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 15;");
        item.setPrefWidth(Double.MAX_VALUE);

        // Avatar/Icon
        Label avatarLabel = new Label("🎮");
        avatarLabel.setStyle("-fx-font-size: 32px;");

        // Info
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(
                invitation.optString("senderName", invitation.optString("senderUsername", "Bạn bè")));
        nameLabel.setStyle("-fx-text-fill: #f5f5f5; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label messageLabel = new Label("mời bạn chơi cờ");
        messageLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(nameLabel, messageLabel);

        // Buttons
        HBox buttonBox = new HBox(10);
        Button acceptButton = new Button("✓ Chấp nhận");
        acceptButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        acceptButton.setOnAction(e -> acceptGameInvitation(invitation));

        Button declineButton = new Button("✗ Từ chối");
        declineButton.setStyle(
                "-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        declineButton.setOnAction(e -> declineGameInvitation(invitation.getInt("senderId")));

        buttonBox.getChildren().addAll(acceptButton, declineButton);

        item.getChildren().addAll(avatarLabel, infoBox);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        item.getChildren().add(buttonBox);

        return item;
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/home.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 930, 740);
            javafx.stage.Stage stage = (javafx.stage.Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể quay lại màn hình chính: " + e.getMessage());
        }
    }

    @FXML
    private void refreshFriendsList() {
        new Thread(() -> {
            try {
                JSONArray friends = FriendService.getFriends();
                Platform.runLater(() -> {
                    friendsListContainer.getChildren().clear();
                    if (friends.length() == 0) {
                        Label noFriendsLabel = new Label("Bạn chưa có bạn bè nào");
                        noFriendsLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 14px;");
                        friendsListContainer.getChildren().add(noFriendsLabel);
                    } else {
                        for (int i = 0; i < friends.length(); i++) {
                            JSONObject friend = friends.getJSONObject(i);
                            friendsListContainer.getChildren().add(createFriendItem(friend));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể tải danh sách bạn bè: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void refreshFriendRequests() {
        new Thread(() -> {
            try {
                JSONArray requests = FriendService.getFriendRequests();
                Platform.runLater(() -> {
                    friendRequestsContainer.getChildren().clear();
                    if (requests.length() == 0) {
                        Label noRequestsLabel = new Label("Không có lời mời kết bạn nào");
                        noRequestsLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 14px;");
                        friendRequestsContainer.getChildren().add(noRequestsLabel);
                    } else {
                        for (int i = 0; i < requests.length(); i++) {
                            JSONObject request = requests.getJSONObject(i);
                            friendRequestsContainer.getChildren().add(createFriendRequestItem(request));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể tải lời mời kết bạn: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchTextField.getText().trim();
        if (searchTerm.isEmpty()) {
            showAlert("Cảnh báo", "Vui lòng nhập từ khóa tìm kiếm");
            return;
        }

        searchButton.setDisable(true);
        new Thread(() -> {
            try {
                JSONArray users = FriendService.searchUsers(searchTerm);
                Platform.runLater(() -> {
                    searchResultsContainer.getChildren().clear();
                    searchButton.setDisable(false);
                    if (users.length() == 0) {
                        Label noResultsLabel = new Label("Không tìm thấy người dùng nào");
                        noResultsLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 14px;");
                        searchResultsContainer.getChildren().add(noResultsLabel);
                    } else {
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = users.getJSONObject(i);
                            searchResultsContainer.getChildren().add(createSearchResultItem(user));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    searchButton.setDisable(false);
                    showAlert("Lỗi", "Không thể tìm kiếm: " + e.getMessage());
                });
            }
        }).start();
    }

    private HBox createFriendItem(JSONObject friend) {
        HBox item = new HBox(15);
        item.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 15;");
        item.setPrefWidth(Double.MAX_VALUE);

        // Avatar/Icon
        Label avatarLabel = new Label("👤");
        avatarLabel.setStyle("-fx-font-size: 32px;");

        // Info
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(friend.optString("display_name", friend.getString("username")));
        nameLabel.setStyle("-fx-text-fill: #f5f5f5; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label usernameLabel = new Label("@" + friend.getString("username"));
        usernameLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(nameLabel, usernameLabel);

        // Buttons
        HBox buttonBox = new HBox(10);
        Button playButton = new Button("Mời chơi");
        playButton.setStyle(
                "-fx-background-color: #4a9eff; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        playButton.setOnAction(e -> inviteFriendToPlay(friend.getInt("id"),
                friend.optString("display_name", friend.getString("username"))));

        Button deleteButton = new Button("Xóa");
        deleteButton.setStyle(
                "-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        deleteButton.setOnAction(e -> deleteFriend(friend.getInt("id")));

        buttonBox.getChildren().addAll(playButton, deleteButton);

        item.getChildren().addAll(avatarLabel, infoBox);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        item.getChildren().add(buttonBox);

        return item;
    }

    private HBox createSearchResultItem(JSONObject user) {
        HBox item = new HBox(15);
        item.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 15;");
        item.setPrefWidth(Double.MAX_VALUE);

        // Avatar/Icon
        Label avatarLabel = new Label("👤");
        avatarLabel.setStyle("-fx-font-size: 32px;");

        // Info
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(user.optString("display_name", user.getString("username")));
        nameLabel.setStyle("-fx-text-fill: #f5f5f5; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label usernameLabel = new Label("@" + user.getString("username"));
        usernameLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(nameLabel, usernameLabel);

        // Button
        Button addButton = new Button();
        String friendshipStatus = user.optString("friendship_status", "");
        boolean canSendRequest = user.optBoolean("can_send_request", true);

        if (user.optBoolean("is_friend", false)) {
            addButton.setText("✓ Đã là bạn");
            addButton.setDisable(true);
            addButton.setStyle(
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        } else if ("pending".equals(friendshipStatus)) {
            addButton.setText("⏳ Đã gửi lời mời");
            addButton.setDisable(true);
            addButton.setStyle(
                    "-fx-background-color: #FFA500; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        } else if (canSendRequest) {
            addButton.setText("➕ Kết bạn");
            addButton.setStyle(
                    "-fx-background-color: #4a9eff; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
            addButton.setOnAction(e -> sendFriendRequest(user.getInt("id")));
        }

        item.getChildren().addAll(avatarLabel, infoBox);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        if (addButton.getText() != null && !addButton.getText().isEmpty()) {
            item.getChildren().add(addButton);
        }

        return item;
    }

    private HBox createFriendRequestItem(JSONObject request) {
        HBox item = new HBox(15);
        item.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 15;");
        item.setPrefWidth(Double.MAX_VALUE);

        // Avatar/Icon
        Label avatarLabel = new Label("👤");
        avatarLabel.setStyle("-fx-font-size: 32px;");

        // Info
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(request.optString("display_name", request.getString("username")));
        nameLabel.setStyle("-fx-text-fill: #f5f5f5; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label usernameLabel = new Label("@" + request.getString("username"));
        usernameLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(nameLabel, usernameLabel);

        // Buttons
        HBox buttonBox = new HBox(10);
        Button acceptButton = new Button("✓ Chấp nhận");
        acceptButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        acceptButton.setOnAction(e -> acceptFriendRequest(request.getInt("requester_id")));

        Button declineButton = new Button("✗ Từ chối");
        declineButton.setStyle(
                "-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        declineButton.setOnAction(e -> declineFriendRequest(request.getInt("requester_id")));

        buttonBox.getChildren().addAll(acceptButton, declineButton);

        item.getChildren().addAll(avatarLabel, infoBox);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        item.getChildren().add(buttonBox);

        return item;
    }

    private void sendFriendRequest(int userId) {
        new Thread(() -> {
            try {
                FriendService.sendFriendRequest(userId);
                Platform.runLater(() -> {
                    showAlert("Thành công", "Đã gửi lời mời kết bạn");
                    handleSearch(); // Refresh search results
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể gửi lời mời: " + e.getMessage());
                });
            }
        }).start();
    }

    private void acceptFriendRequest(int requesterId) {
        new Thread(() -> {
            try {
                FriendService.acceptFriendRequest(requesterId);
                Platform.runLater(() -> {
                    showAlert("Thành công", "Đã chấp nhận lời mời kết bạn");
                    refreshFriendRequests();
                    refreshFriendsList();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể chấp nhận lời mời: " + e.getMessage());
                });
            }
        }).start();
    }

    private void declineFriendRequest(int requesterId) {
        new Thread(() -> {
            try {
                FriendService.declineFriendRequest(requesterId);
                Platform.runLater(() -> {
                    refreshFriendRequests();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể từ chối lời mời: " + e.getMessage());
                });
            }
        }).start();
    }

    private void deleteFriend(int friendId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Xóa bạn bè");
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa bạn bè này?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        FriendService.deleteFriend(friendId);
                        Platform.runLater(() -> {
                            refreshFriendsList();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            showAlert("Lỗi", "Không thể xóa bạn bè: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
    }

    private void inviteFriendToPlay(int friendId, String friendName) {
        new Thread(() -> {
            try {
                // Khởi tạo PeerServer để lắng nghe kết nối từ bạn bè
                PeerServer peerServer = new PeerServer();
                int localPort = peerServer.start(0); // Chọn port trống bất kỳ

                // Gửi lời mời chơi cờ
                FriendService.inviteFriendToPlay(friendId, localPort);

                Platform.runLater(() -> {
                    showAlert("Thành công", "Đã gửi lời mời chơi cờ đến " + friendName + ". Đang chờ phản hồi...");

                    // Bắt đầu kiểm tra xem bạn bè có chấp nhận không
                    waitForGameAcceptance(peerServer, friendId);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể gửi lời mời chơi cờ: " + e.getMessage());
                });
            }
        }).start();
    }

    private void waitForGameAcceptance(PeerServer peerServer, int friendId) {
        new Thread(() -> {
            try {
                // Chờ kết nối từ bạn bè (B sẽ connect tới A khi chấp nhận)
                final Socket socket = peerServer.waitForOpponent();

                // Sau khi có kết nối, polling để lấy thông tin game (có thể mất vài giây để B
                // chấp nhận và tạo game)
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(500); // Kiểm tra mỗi 0.5 giây, tối đa 5 giây
                    JSONObject gameStatus = FriendService.getFriendGameStatus();
                    if (gameStatus != null) {
                        // Game đã được tạo, mở game
                        Platform.runLater(() -> {
                            openGameWithFriend(gameStatus, socket);
                        });
                        return;
                    }
                }

                // Không tìm thấy game sau khi có kết nối
                Platform.runLater(() -> {
                    try {
                        socket.close();
                        peerServer.stop();
                    } catch (Exception ex) {
                        // Bỏ qua
                    }
                    showAlert("Lỗi", "Không thể tìm thấy thông tin trận đấu");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    try {
                        peerServer.stop();
                    } catch (Exception ex) {
                        // Bỏ qua
                    }
                    showAlert("Lỗi", "Lỗi khi chờ phản hồi: " + e.getMessage());
                });
            }
        }).start();
    }

    private void acceptGameInvitation(JSONObject invitation) {
        new Thread(() -> {
            try {
                // Chấp nhận lời mời (không cần socketPort vì người nhận sẽ connect tới người
                // gửi)
                JSONObject gameResult = FriendService.acceptGameInvitation(
                        invitation.getInt("senderId"));

                // Kết nối tới người gửi (người gửi đang chờ kết nối)
                String opponentIp = invitation.getString("ip");
                int opponentPort = invitation.getInt("socketPort");

                PeerClient client = new PeerClient();
                Socket socket = client.connectToOpponent(opponentIp, opponentPort);

                Platform.runLater(() -> {
                    openGameWithFriend(gameResult, socket);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể chấp nhận lời mời: " + e.getMessage());
                });
            }
        }).start();
    }

    private void declineGameInvitation(int senderId) {
        new Thread(() -> {
            try {
                FriendService.declineGameInvitation(senderId);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi", "Không thể từ chối lời mời: " + e.getMessage());
                });
            }
        }).start();
    }

    private void openGameWithFriend(JSONObject gameResult, Socket socket) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/game.fxml"));
            Parent root = loader.load();
            com.chess_client.controllers.GameController controller = loader.getController();

            String gameId = gameResult.optString("gameId", null);
            String opponentName = gameResult.optString("opponentName", "Bạn bè");
            String colorStr = gameResult.getString("color");
            Piece.Color color = "white".equalsIgnoreCase(colorStr)
                    ? Piece.Color.WHITE
                    : Piece.Color.BLACK;

            controller.setGameInfo(gameId, opponentName, "Bạn");
            controller.setPlayerColor(color);
            controller.setPeerSocket(socket);

            javafx.stage.Stage stage = (javafx.stage.Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi", "Không thể mở màn hình game: " + ex.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
