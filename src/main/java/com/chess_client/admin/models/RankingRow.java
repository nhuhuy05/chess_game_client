package com.chess_client.controllers.admin.models;

/**
 * Data class cho hàng trong bảng xếp hạng
 */
public class RankingRow {
    private final Integer rank;
    private final Integer userId;
    private final String username;
    private final String displayName;
    private final Integer gamesPlayed;
    private final Integer wins;
    private final Integer losses;
    private final Integer draws;
    private final Integer score;

    public RankingRow(Integer rank, Integer userId, String username, String displayName,
            Integer gamesPlayed, Integer wins, Integer losses, Integer draws, Integer score) {
        this.rank = rank;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
        this.score = score;
    }

    public Integer getRank() {
        return rank;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public Integer getWins() {
        return wins;
    }

    public Integer getLosses() {
        return losses;
    }

    public Integer getDraws() {
        return draws;
    }

    public Integer getScore() {
        return score;
    }
}

