package com.chess_client.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

public class FriendUIHelper {

    private static final String ITEM_STYLE = "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 15;";
    private static final String AVATAR_STYLE = "-fx-font-size: 32px;";
    private static final String NAME_STYLE = "-fx-text-fill: #f5f5f5; -fx-font-size: 16px; -fx-font-weight: bold;";
    private static final String USERNAME_STYLE = "-fx-text-fill: #b0b0b0; -fx-font-size: 12px;";
    private static final String SUCCESS_BUTTON_STYLE = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;";
    private static final String DANGER_BUTTON_STYLE = "-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;";
    private static final String PRIMARY_BUTTON_STYLE = "-fx-background-color: #4a9eff; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;";
    private static final String DISABLED_BUTTON_STYLE = "-fx-background-color: #555555; -fx-text-fill: #aaaaaa; -fx-background-radius: 5; -fx-padding: 8 15;";
    private static final String WARNING_BUTTON_STYLE = "-fx-background-color: #FFA500; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;";

    /**
     * Tạo item hiển thị một người bạn trong danh sách
     */
    public static HBox createFriendItem(JSONObject friend,
            javafx.event.EventHandler<javafx.event.ActionEvent> onPlayAction,
            javafx.event.EventHandler<javafx.event.ActionEvent> onDeleteAction) {
        HBox item = createBaseItem("👤");
        Label avatarLabel = (Label) item.getChildren().get(0);

        VBox infoBox = createUserInfoBox(friend);

        String status = friend.optString("status", "offline");
        boolean isOnline = "online".equalsIgnoreCase(status);
        Label statusLabel = createStatusLabel(isOnline);

        HBox buttonBox = new HBox(10);
        Button playButton = createPlayButton(isOnline, onPlayAction);
        Button deleteButton = createButton("Xóa", DANGER_BUTTON_STYLE, onDeleteAction);

        buttonBox.getChildren().addAll(playButton, deleteButton);

        HBox rightContainer = new HBox(15);
        rightContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        rightContainer.getChildren().addAll(statusLabel, buttonBox);

        item.getChildren().clear();
        item.getChildren().addAll(avatarLabel, infoBox);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        item.getChildren().add(rightContainer);

        return item;
    }

    /**
     * Tạo item hiển thị kết quả tìm kiếm người dùng
     */
    public static HBox createSearchResultItem(JSONObject user,
            javafx.event.EventHandler<javafx.event.ActionEvent> onAddAction) {
        HBox item = createBaseItem("👤");
        Label avatarLabel = (Label) item.getChildren().get(0);

        VBox infoBox = createUserInfoBox(user);
        Button addButton = createAddFriendButton(user, onAddAction);

        item.getChildren().clear();
        item.getChildren().addAll(avatarLabel, infoBox);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        if (addButton != null && addButton.getText() != null && !addButton.getText().isEmpty()) {
            item.getChildren().add(addButton);
        }

        return item;
    }

    /**
     * Tạo item hiển thị lời mời kết bạn
     */
    public static HBox createFriendRequestItem(JSONObject request,
            javafx.event.EventHandler<javafx.event.ActionEvent> onAcceptAction,
            javafx.event.EventHandler<javafx.event.ActionEvent> onDeclineAction) {
        HBox item = createBaseItem("👤");
        Label avatarLabel = (Label) item.getChildren().get(0);

        VBox infoBox = createUserInfoBox(request);

        HBox buttonBox = new HBox(10);
        Button acceptButton = createButton("✓ Chấp nhận", SUCCESS_BUTTON_STYLE, onAcceptAction);
        Button declineButton = createButton("✗ Từ chối", DANGER_BUTTON_STYLE, onDeclineAction);

        buttonBox.getChildren().addAll(acceptButton, declineButton);

        item.getChildren().clear();
        item.getChildren().addAll(avatarLabel, infoBox);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        item.getChildren().add(buttonBox);

        return item;
    }

    /**
     * Tạo item hiển thị lời mời chơi cờ
     */
    public static HBox createGameInvitationItem(JSONObject invitation,
            javafx.event.EventHandler<javafx.event.ActionEvent> onAcceptAction,
            javafx.event.EventHandler<javafx.event.ActionEvent> onDeclineAction) {
        HBox item = createBaseItem("🎮");
        Label avatarLabel = (Label) item.getChildren().get(0);

        VBox infoBox = new VBox(5);
        String senderName = invitation.optString("senderName", invitation.optString("senderUsername", "Bạn bè"));
        Label nameLabel = createLabel(senderName, NAME_STYLE);
        Label messageLabel = createLabel("mời bạn chơi cờ", USERNAME_STYLE);
        infoBox.getChildren().addAll(nameLabel, messageLabel);

        HBox buttonBox = new HBox(10);
        Button acceptButton = createButton("✓ Chấp nhận", SUCCESS_BUTTON_STYLE, onAcceptAction);
        Button declineButton = createButton("✗ Từ chối", DANGER_BUTTON_STYLE, onDeclineAction);

        buttonBox.getChildren().addAll(acceptButton, declineButton);

        item.getChildren().clear();
        item.getChildren().addAll(avatarLabel, infoBox);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        item.getChildren().add(buttonBox);

        return item;
    }

    // ============ Helper Methods ============

    private static HBox createBaseItem(String avatarEmoji) {
        HBox item = new HBox(15);
        item.setStyle(ITEM_STYLE);
        item.setPrefWidth(Double.MAX_VALUE);

        Label avatarLabel = createLabel(avatarEmoji, AVATAR_STYLE);
        item.getChildren().add(avatarLabel);

        return item;
    }

    private static VBox createUserInfoBox(JSONObject user) {
        VBox infoBox = new VBox(5);
        String displayName = user.optString("display_name", user.getString("username"));
        Label nameLabel = createLabel(displayName, NAME_STYLE);
        Label usernameLabel = createLabel("@" + user.getString("username"), USERNAME_STYLE);
        infoBox.getChildren().addAll(nameLabel, usernameLabel);
        return infoBox;
    }

    private static Label createStatusLabel(boolean isOnline) {
        Label statusLabel = new Label(isOnline ? "🟢 Đang online" : "⚫ Đang offline");
        statusLabel.setStyle(isOnline
                ? "-fx-text-fill: #4CAF50; -fx-font-size: 12px;"
                : "-fx-text-fill: #888888; -fx-font-size: 12px;");
        return statusLabel;
    }

    private static Button createPlayButton(boolean isOnline,
            javafx.event.EventHandler<javafx.event.ActionEvent> onAction) {
        Button playButton = new Button("Mời chơi");
        if (!isOnline) {
            playButton.setDisable(true);
            playButton.setStyle(DISABLED_BUTTON_STYLE);
        } else {
            playButton.setStyle(PRIMARY_BUTTON_STYLE);
            playButton.setOnAction(onAction);
        }
        return playButton;
    }

    private static Button createAddFriendButton(JSONObject user,
            javafx.event.EventHandler<javafx.event.ActionEvent> onAction) {
        Button addButton = new Button();
        String friendshipStatus = user.optString("friendship_status", "");
        boolean canSendRequest = user.optBoolean("can_send_request", true);

        if (user.optBoolean("is_friend", false)) {
            addButton.setText("✓ Đã là bạn");
            addButton.setDisable(true);
            addButton.setStyle(SUCCESS_BUTTON_STYLE);
        } else if ("pending".equals(friendshipStatus)) {
            addButton.setText("⏳ Đã gửi lời mời");
            addButton.setDisable(true);
            addButton.setStyle(WARNING_BUTTON_STYLE);
        } else if (canSendRequest) {
            addButton.setText("➕ Kết bạn");
            addButton.setStyle(PRIMARY_BUTTON_STYLE);
            addButton.setOnAction(onAction);
        }
        return addButton;
    }

    private static Button createButton(String text, String style,
            javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setStyle(style);
        if (handler != null) {
            button.setOnAction(handler);
        }
        return button;
    }

    private static Label createLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }
}
