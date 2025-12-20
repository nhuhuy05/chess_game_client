package com.chess_client.controllers;

import com.chess_client.models.Piece;
import com.chess_client.services.FriendService;
import com.chess_client.services.GameInvitationService;
import com.chess_client.ui.FriendUIHelper;
import com.chess_client.ui.NavigationHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Function;

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

    }

    @FXML
    private void refreshGameInvitations() {
        runAsync(() -> {
            try {
                return FriendService.getGameInvitations();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, invitations -> {
            if (invitations == null)
                return;
            refreshContainer(gameInvitationsContainer, invitations,
                    "Không có lời mời chơi cờ nào",
                    i -> {
                        JSONObject inv = invitations.getJSONObject(i);
                        return FriendUIHelper.createGameInvitationItem(inv,
                                e -> acceptGameInvitation(inv),
                                e -> declineGameInvitation(inv.getInt("senderId")));
                    });
        });
    }

    private void refreshContainer(VBox container, JSONArray array, String emptyMsg,
            Function<Integer, HBox> createItem) {
        container.getChildren().clear();
        if (array.length() == 0) {
            Label label = new Label(emptyMsg);
            label.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 14px;");
            container.getChildren().add(label);
        } else {
            for (int i = 0; i < array.length(); i++) {
                container.getChildren().add(createItem.apply(i));
            }
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        NavigationHelper.navigateTo(stage, "/com/chess_client/fxml/home.fxml", 930, 740);
    }

    @FXML
    private void refreshFriendsList() {
        runAsync(() -> {
            try {
                return FriendService.getFriends();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Không thể tải danh sách bạn bè: " + e.getMessage(), e);
            }
        }, friends -> {
            refreshContainer(friendsListContainer, friends, "Bạn chưa có bạn bè nào",
                    i -> {
                        JSONObject friend = friends.getJSONObject(i);
                        return FriendUIHelper.createFriendItem(friend,
                                e -> inviteFriendToPlay(friend.getInt("id"),
                                        friend.optString("display_name", friend.getString("username"))),
                                e -> deleteFriend(friend.getInt("id")));
                    });
        }, error -> NavigationHelper.showAlert("Lỗi", error.getMessage()));
    }

    @FXML
    private void refreshFriendRequests() {
        runAsync(() -> {
            try {
                return FriendService.getFriendRequests();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Không thể tải lời mời kết bạn: " + e.getMessage(), e);
            }
        }, requests -> {
            refreshContainer(friendRequestsContainer, requests, "Không có lời mời kết bạn nào",
                    i -> {
                        JSONObject request = requests.getJSONObject(i);
                        return FriendUIHelper.createFriendRequestItem(request,
                                e -> acceptFriendRequest(request.getInt("requester_id")),
                                e -> declineFriendRequest(request.getInt("requester_id")));
                    });
        }, error -> NavigationHelper.showAlert("Lỗi", error.getMessage()));
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchTextField.getText().trim();
        if (searchTerm.isEmpty()) {
            NavigationHelper.showAlert("Cảnh báo", "Vui lòng nhập từ khóa tìm kiếm");
            return;
        }

        searchButton.setDisable(true);
        runAsync(() -> {
            try {
                return FriendService.searchUsers(searchTerm);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Không thể tìm kiếm: " + e.getMessage(), e);
            }
        }, users -> {
            searchButton.setDisable(false);
            refreshContainer(searchResultsContainer, users, "Không tìm thấy người dùng nào",
                    i -> {
                        JSONObject user = users.getJSONObject(i);
                        return FriendUIHelper.createSearchResultItem(user,
                                e -> sendFriendRequest(user.getInt("id")));
                    });
        }, error -> {
            searchButton.setDisable(false);
            NavigationHelper.showAlert("Lỗi", error.getMessage());
        });
    }

    // ============ Async Helper Methods ============

    @FunctionalInterface
    private interface AsyncTask<T> {
        T execute() throws Exception;
    }

    @FunctionalInterface
    private interface AsyncCallback<T> {
        void onSuccess(T result);
    }

    @FunctionalInterface
    private interface AsyncErrorCallback {
        void onError(Exception error);
    }

    private <T> void runAsync(AsyncTask<T> task, AsyncCallback<T> onSuccess) {
        runAsync(task, onSuccess, null);
    }

    private <T> void runAsync(AsyncTask<T> task, AsyncCallback<T> onSuccess, AsyncErrorCallback onError) {
        new Thread(() -> {
            try {
                T result = task.execute();
                Platform.runLater(() -> onSuccess.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                if (onError != null) {
                    Platform.runLater(() -> onError.onError(e));
                }
            }
        }).start();
    }

    // ============ Friend Request Methods ============

    void sendFriendRequest(int userId) {
        runAsync(() -> {
            FriendService.sendFriendRequest(userId);
            return null;
        }, result -> {
            NavigationHelper.showAlert("Thành công", "Đã gửi lời mời kết bạn");
            handleSearch();
        }, error -> NavigationHelper.showAlert("Lỗi", "Không thể gửi lời mời: " + error.getMessage()));
    }

    void acceptFriendRequest(int requesterId) {
        runAsync(() -> {
            FriendService.acceptFriendRequest(requesterId);
            return null;
        }, result -> {
            NavigationHelper.showAlert("Thành công", "Đã chấp nhận lời mời kết bạn");
            refreshFriendRequests();
            refreshFriendsList();
        }, error -> NavigationHelper.showAlert("Lỗi", "Không thể chấp nhận lời mời: " + error.getMessage()));
    }

    void declineFriendRequest(int requesterId) {
        runAsync(() -> {
            FriendService.declineFriendRequest(requesterId);
            return null;
        }, result -> refreshFriendRequests(),
                error -> NavigationHelper.showAlert("Lỗi", "Không thể từ chối lời mời: " + error.getMessage()));
    }

    void deleteFriend(int friendId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Xóa bạn bè");
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa bạn bè này?");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                runAsync(() -> {
                    FriendService.deleteFriend(friendId);
                    return null;
                }, result -> refreshFriendsList(),
                        error -> NavigationHelper.showAlert("Lỗi", "Không thể xóa bạn bè: " + error.getMessage()));
            }
        });
    }

    void inviteFriendToPlay(int friendId, String friendName) {
        GameInvitationService.inviteFriend(friendId, friendName,
                (gameResult, socket) -> openGameWithFriend(gameResult, socket),
                msg -> NavigationHelper.showAlert("Thành công", msg),
                err -> NavigationHelper.showAlert("Lỗi", err));
    }

    void acceptGameInvitation(JSONObject invitation) {
        GameInvitationService.acceptInvitation(invitation,
                (gameResult, socket) -> openGameWithFriend(gameResult, socket),
                err -> NavigationHelper.showAlert("Lỗi", err));
    }

    void declineGameInvitation(int senderId) {
        runAsync(() -> {
            FriendService.declineGameInvitation(senderId);
            return null;
        }, result -> refreshGameInvitations(),
                error -> NavigationHelper.showAlert("Lỗi", "Không thể từ chối lời mời: " + error.getMessage()));
    }

    private void openGameWithFriend(JSONObject gameResult, java.net.Socket socket) {
        String gameId = gameResult.optString("gameId", null);
        String opponentName = gameResult.optString("opponentName", "Bạn bè");
        String colorStr = gameResult.getString("color");
        Piece.Color color = "white".equalsIgnoreCase(colorStr) ? Piece.Color.WHITE : Piece.Color.BLACK;

        Stage stage = (Stage) backButton.getScene().getWindow();
        NavigationHelper.openGame(stage, this, gameId, opponentName, color, socket);
    }
}
