package com.chess_client.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Quản lý phần chat trong màn hình game.
 * Lớp này xử lý UI chat phía client và gửi/nhận tin nhắn qua callback.
 */
public class ChatManager {

    private final ScrollPane chatScrollPane;
    private final VBox chatMessagesBox;
    private final TextField chatInput;
    private final Button sendMessageButton;
    private java.util.function.Consumer<String> onSendMessage;

    public ChatManager(ScrollPane chatScrollPane,
            VBox chatMessagesBox,
            TextField chatInput,
            Button sendMessageButton) {
        this.chatScrollPane = chatScrollPane;
        this.chatMessagesBox = chatMessagesBox;
        this.chatInput = chatInput;
        this.sendMessageButton = sendMessageButton;
    }

    /**
     * Set callback để gửi tin nhắn qua network
     */
    public void setOnSendMessage(java.util.function.Consumer<String> onSendMessage) {
        this.onSendMessage = onSendMessage;
    }

    public void initialize() {
        setupEventHandlers();
        setupChatEnterKey();
    }

    private void setupEventHandlers() {
        sendMessageButton.setOnAction(e -> sendMessage());
    }

    private void setupChatEnterKey() {
        chatInput.setOnAction(e -> sendMessage());
    }

    /**
     * Gửi tin nhắn từ người chơi.
     */
    public void sendMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            addChatMessage("Bạn", message, true);
            chatInput.clear();
            // Gửi tin nhắn qua network nếu có callback
            if (onSendMessage != null) {
                onSendMessage.accept(message);
            }
        }
    }

    public void addChatMessage(String sender, String message, boolean isPlayer) {
        HBox messageBox = new HBox(5);
        messageBox.setAlignment(isPlayer ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(3);
        bubble.setStyle("-fx-background-color: " + (isPlayer ? "#4a9eff" : "#4a4541") + ";" +
                "-fx-background-radius: 8; -fx-padding: 8 12 8 12;");

        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-text-fill: #f0d9b5; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(220);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        bubble.getChildren().addAll(senderLabel, messageLabel);
        messageBox.getChildren().add(bubble);
        chatMessagesBox.getChildren().add(messageBox);

        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    public void addSystemMessage(String message) {
        Label systemMsg = new Label(message);
        systemMsg.setStyle("-fx-text-fill: #999; -fx-font-size: 11px; -fx-font-style: italic; -fx-padding: 5 0 5 0;");
        systemMsg.setAlignment(Pos.CENTER);
        systemMsg.setMaxWidth(Double.MAX_VALUE);
        chatMessagesBox.getChildren().add(systemMsg);
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }
}
