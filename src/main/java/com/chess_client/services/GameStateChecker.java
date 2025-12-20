package com.chess_client.services;

import com.chess_client.models.Board;
import com.chess_client.models.Piece;

/**
 * Kiểm tra trạng thái game: checkmate, stalemate, check, king captured.
 * Service layer - business logic không phụ thuộc vào UI.
 */
public class GameStateChecker {

    private final Board board;
    private final GameLogic gameLogic;

    public GameStateChecker(Board board, GameLogic gameLogic) {
        this.board = board;
        this.gameLogic = gameLogic;
    }

    /**
     * Kết quả kiểm tra trạng thái game sau một nước đi.
     */
    public static class GameStateResult {
        public enum State {
            KING_CAPTURED, // Vua bị ăn
            CHECKMATE, // Chiếu hết
            STALEMATE, // Hòa cờ
            CHECK, // Chiếu tướng
            NORMAL // Bình thường
        }

        private final State state;
        private final Piece.Color winner; // null nếu hòa hoặc bình thường

        public GameStateResult(State state, Piece.Color winner) {
            this.state = state;
            this.winner = winner;
        }

        public State getState() {
            return state;
        }

        public Piece.Color getWinner() {
            return winner;
        }
    }

    /**
     * Kiểm tra trạng thái game sau một nước đi.
     * 
     * @param currentPlayer người chơi hiện tại (sau khi đã đổi lượt)
     */
    public GameStateResult checkGameState(Piece.Color currentPlayer) {
        // Kiểm tra vua có bị ăn không
        boolean whiteHasKing = gameLogic.hasKing(Piece.Color.WHITE);
        boolean blackHasKing = gameLogic.hasKing(Piece.Color.BLACK);

        if (!whiteHasKing || !blackHasKing) {
            Piece.Color winner = whiteHasKing ? Piece.Color.WHITE : Piece.Color.BLACK;
            return new GameStateResult(GameStateResult.State.KING_CAPTURED, winner);
        }

        // Kiểm tra chiếu hết
        if (gameLogic.isCheckmate(currentPlayer)) {
            Piece.Color winner = currentPlayer == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;
            return new GameStateResult(GameStateResult.State.CHECKMATE, winner);
        }

        // Kiểm tra hòa
        if (gameLogic.isStalemate(currentPlayer)) {
            return new GameStateResult(GameStateResult.State.STALEMATE, null);
        }

        // Kiểm tra chiếu tướng
        boolean whiteInCheck = gameLogic.isKingInCheck(board, Piece.Color.WHITE);
        boolean blackInCheck = gameLogic.isKingInCheck(board, Piece.Color.BLACK);
        if (whiteInCheck || blackInCheck) {
            return new GameStateResult(GameStateResult.State.CHECK, null);
        }

        return new GameStateResult(GameStateResult.State.NORMAL, null);
    }

    /**
     * Lấy text mô tả trạng thái để hiển thị trên UI.
     * Method này trả về String thuần túy, không phụ thuộc vào UI framework.
     */
    public String getStatusText(GameStateResult result) {
        return switch (result.getState()) {
            case KING_CAPTURED -> {
                String winnerText = result.getWinner() == Piece.Color.WHITE ? "TRẮNG" : "ĐEN";
                yield "VUA BỊ ĂN! " + winnerText + " THẮNG!";
            }
            case CHECKMATE -> {
                String winnerText = result.getWinner() == Piece.Color.WHITE ? "TRẮNG" : "ĐEN";
                yield "CHIẾU HẾT! " + winnerText + " THẮNG!";
            }
            case STALEMATE -> "HÒA CỜ (Stalemate)";
            case CHECK -> "CHIẾU TƯỚNG!";
            case NORMAL -> "Trò chơi đang diễn ra";
        };
    }
}
