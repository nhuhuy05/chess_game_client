package com.chess_client.controllers.admin.models;

/**
 * Data class cho hàng trong bảng trận đấu
 */
public class GameRow {
    private final Integer id;
    private final String whitePlayer;
    private final String blackPlayer;
    private final String mode;
    private final String status;
    private final String winner;

    public GameRow(Integer id, String whitePlayer, String blackPlayer, String mode,
            String status, String winner) {
        this.id = id;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.mode = mode;
        this.status = status;
        this.winner = winner;
    }

    public Integer getId() {
        return id;
    }

    public String getWhitePlayer() {
        return whitePlayer;
    }

    public String getBlackPlayer() {
        return blackPlayer;
    }

    public String getMode() {
        return mode;
    }

    public String getStatus() {
        return status;
    }

    public String getWinner() {
        return winner;
    }
}

