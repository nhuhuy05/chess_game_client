package com.chess_client.ui;

import com.chess_client.models.Move;
import com.chess_client.models.Piece;
import com.chess_client.services.AIPlayer;
import javafx.scene.control.Label;

/**
 * Cập nhật các label UI hiển thị thông tin game.
 */
public class UIGameInfoUpdater {

    private final Label turnLabel;
    private final Label statusLabel;
    private final Label lastMoveLabel;
    private final Label playerLabel;
    private final Label opponentPlayerLabel;
    private final Label playerNameLabel;
    private final Label opponentNameLabel;

    private Piece.Color playerColor;
    private AIPlayer aiPlayer;

    public UIGameInfoUpdater(Label turnLabel, Label statusLabel, Label lastMoveLabel,
                             Label playerLabel, Label opponentPlayerLabel,
                             Label playerNameLabel, Label opponentNameLabel,
                             Piece.Color playerColor, AIPlayer aiPlayer) {
        this.turnLabel = turnLabel;
        this.statusLabel = statusLabel;
        this.lastMoveLabel = lastMoveLabel;
        this.playerLabel = playerLabel;
        this.opponentPlayerLabel = opponentPlayerLabel;
        this.playerNameLabel = playerNameLabel;
        this.opponentNameLabel = opponentNameLabel;
        this.playerColor = playerColor;
        this.aiPlayer = aiPlayer;
    }

    public void setPlayerColor(Piece.Color playerColor) {
        this.playerColor = playerColor;
    }

    public void setAiPlayer(AIPlayer aiPlayer) {
        this.aiPlayer = aiPlayer;
    }


    /**
     * Cập nhật label lượt đi.
     */
    public void updateTurnLabel(Piece.Color currentPlayer) {
        turnLabel.setText("Lượt đi: " + (currentPlayer == Piece.Color.WHITE ? "TRẮNG" : "ĐEN"));
    }

    /**
     * Cập nhật label trạng thái game.
     */
    public void updateStatusLabel(String status) {
        statusLabel.setText(status);
    }

    /**
     * Cập nhật label nước đi cuối cùng.
     */
    public void updateLastMoveLabel(Move move) {
        String from = getSquareName(move.getFromRow(), move.getFromCol());
        String to = getSquareName(move.getToRow(), move.getToCol());
        String pieceName = getPieceName(move.getPieceMoved());
        String moveText = pieceName + " " + from + " → " + to;

        // Xác định bên nào vừa đi: Bạn / Đối thủ / Máy
        String prefix = getMovePrefix(move);

        // Thêm thông tin đặc biệt
        if (move.isCastling()) {
            moveText += " (Nhập thành)";
        } else if (move.isEnPassant()) {
            moveText += " (En passant)";
        } else if (move.isPromotion()) {
            moveText += " (Phong hậu)";
        } else if (move.getPieceCaptured() != null) {
            moveText += " (Ăn " + getPieceName(move.getPieceCaptured()) + ")";
        }

        lastMoveLabel.setText(prefix + moveText);
    }

    /**
     * Cập nhật label thông tin người chơi.
     */
    public void updatePlayerInfo(String playerName, String opponentName) {
        if (playerNameLabel != null) {
            playerNameLabel.setText(playerName != null ? playerName : "Bạn");
        }
        if (opponentNameLabel != null) {
            opponentNameLabel.setText(opponentName != null ? opponentName : "Đối thủ");
        }
    }

    /**
     * Cập nhật label màu quân của người chơi.
     */
    public void updatePlayerLabels() {
        if (playerColor == Piece.Color.WHITE) {
            playerLabel.setText("Quân Trắng");
            opponentPlayerLabel.setText("Quân Đen");
        } else {
            playerLabel.setText("Quân Đen");
            opponentPlayerLabel.setText("Quân Trắng");
        }
    }

    /**
     * Reset về trạng thái ban đầu.
     */
    public void reset() {
        statusLabel.setText("Trò chơi bắt đầu");
        lastMoveLabel.setText("Chưa có");
    }

    private String getMovePrefix(Move move) {
        Piece.Color moverColor = move.getPieceMoved().getColor();
        boolean isPlayerMove = moverColor == playerColor;
        boolean isAiMove = aiPlayer != null && moverColor == aiPlayer.getAiColor();

        if (isAiMove) {
            return "Máy: ";
        } else if (isPlayerMove) {
            return "Bạn: ";
        } else {
            return "Đối thủ: ";
        }
    }

    private String getSquareName(int row, int col) {
        return "" + (char) ('a' + col) + (8 - row);
    }

    private String getPieceName(Piece piece) {
        return switch (piece.getType()) {
            case KING -> "Vua";
            case QUEEN -> "Hậu";
            case ROOK -> "Xe";
            case BISHOP -> "Tượng";
            case KNIGHT -> "Mã";
            case PAWN -> "Tốt";
            default -> "";
        };
    }
}

