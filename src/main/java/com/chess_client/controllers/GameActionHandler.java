package com.chess_client.controllers;

import com.chess_client.models.Piece;
import com.chess_client.network.PeerNetworkHandler;
import com.chess_client.ui.ChatManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.util.function.Consumer;

/**
 * Xử lý các game actions: draw, resign, và các actions từ network.
 */
public class GameActionHandler {

    private final Label statusLabel;
    private final ChatManager chatManager;
    private final PeerNetworkHandler peerNetworkHandler;
    private final Piece.Color playerColor;
    private final Consumer<Piece.Color> onGameEnd;
    private final Runnable disableButtons;

    public GameActionHandler(Label statusLabel, ChatManager chatManager,
            PeerNetworkHandler peerNetworkHandler, Piece.Color playerColor,
            Consumer<Piece.Color> onGameEnd, Runnable disableButtons) {
        this.statusLabel = statusLabel;
        this.chatManager = chatManager;
        this.peerNetworkHandler = peerNetworkHandler;
        this.playerColor = playerColor;
        this.onGameEnd = onGameEnd;
        this.disableButtons = disableButtons;
    }

    /**
     * Xử lý đề nghị hòa từ người chơi.
     */
    public void offerDraw() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cầu hòa");
        alert.setHeaderText("Bạn muốn đề nghị hòa?");
        alert.setContentText("Đối thủ sẽ được hỏi có chấp nhận không.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (chatManager != null) {
                    chatManager.addSystemMessage("Bạn đã đề nghị hòa");
                }
                if (peerNetworkHandler != null) {
                    peerNetworkHandler.sendGameAction("offer_draw");
                }
            }
        });
    }

    /**
     * Xử lý đầu hàng từ người chơi.
     */
    public void resign() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Nhận thua");
        alert.setHeaderText("Bạn muốn đầu hàng?");
        alert.setContentText("Bạn sẽ thua ván này.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Piece.Color winner = playerColor == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;
                String winnerText = winner == Piece.Color.WHITE ? "TRẮNG" : "ĐEN";
                statusLabel.setText("BẠN ĐÃ ĐẦU HÀNG! " + winnerText + " THẮNG!");
                disableButtons.run();
                if (chatManager != null) {
                    chatManager.addSystemMessage("Bạn đã đầu hàng");
                }
                if (peerNetworkHandler != null) {
                    peerNetworkHandler.sendGameAction("resign");
                }
                onGameEnd.accept(winner);
            }
        });
    }

    /**
     * Xử lý game action từ network (từ đối thủ).
     */
    public void handleGameAction(String action) {
        switch (action) {
            case "resign" -> handleOpponentResign();
            case "offer_draw" -> handleOpponentOfferDraw();
            case "accept_draw" -> handleOpponentAcceptDraw();
            case "reject_draw" -> handleOpponentRejectDraw();
        }
    }

    private void handleOpponentResign() {
        Piece.Color winner = playerColor; // Bạn thắng vì đối thủ đầu hàng
        String winnerText = winner == Piece.Color.WHITE ? "TRẮNG" : "ĐEN";
        statusLabel.setText("ĐỐI THỦ ĐÃ ĐẦU HÀNG! " + winnerText + " THẮNG!");
        disableButtons.run();
        if (chatManager != null) {
            chatManager.addSystemMessage("Đối thủ đã đầu hàng");
        }
        onGameEnd.accept(winner);
    }

    private void handleOpponentOfferDraw() {
        if (chatManager != null) {
            chatManager.addSystemMessage("Đối thủ đề nghị hòa");
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Đề nghị hòa");
        alert.setHeaderText("Đối thủ đề nghị hòa");
        alert.setContentText("Bạn có chấp nhận hòa không?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Chấp nhận hòa
                if (chatManager != null) {
                    chatManager.addSystemMessage("Bạn đã chấp nhận hòa");
                }
                statusLabel.setText("TRẬN ĐẤU HÒA!");
                disableButtons.run();
                if (peerNetworkHandler != null) {
                    peerNetworkHandler.sendGameAction("accept_draw");
                }
                onGameEnd.accept(null); // null = hòa
            } else {
                // Từ chối hòa
                if (chatManager != null) {
                    chatManager.addSystemMessage("Bạn đã từ chối hòa");
                }
                if (peerNetworkHandler != null) {
                    peerNetworkHandler.sendGameAction("reject_draw");
                }
            }
        });
    }

    private void handleOpponentAcceptDraw() {
        statusLabel.setText("TRẬN ĐẤU HÒA!");
        disableButtons.run();
        if (chatManager != null) {
            chatManager.addSystemMessage("Đối thủ đã chấp nhận hòa");
        }
        onGameEnd.accept(null); // null = hòa
    }

    private void handleOpponentRejectDraw() {
        if (chatManager != null) {
            chatManager.addSystemMessage("Đối thủ đã từ chối hòa");
        }
    }
}
