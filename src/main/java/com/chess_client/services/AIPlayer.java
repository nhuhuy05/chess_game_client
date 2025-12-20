package com.chess_client.services;

import com.chess_client.models.Board;
import com.chess_client.models.Move;
import com.chess_client.models.Piece;
import com.chess_client.services.GameLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Xử lý logic cho AI/Computer player trong chế độ chơi với máy.
 */
public class AIPlayer {

    private final Board board;
    private final GameLogic gameLogic;
    private final Piece.Color aiColor;
    private final int difficulty; // 1: dễ, 2: trung bình, 3: khó
    private final Random random = new Random();

    public AIPlayer(Board board, GameLogic gameLogic, Piece.Color aiColor, int difficulty) {
        this.board = board;
        this.gameLogic = gameLogic;
        this.aiColor = aiColor;
        this.difficulty = difficulty;
    }

    /**
     * AI chọn và thực hiện nước đi.
     * 
     * @return Move được chọn, hoặc null nếu không có nước đi hợp lệ
     */
    public Move makeMove() {
        List<Move> validMoves = gameLogic.getAllValidMoves(aiColor);

        if (validMoves.isEmpty()) {
            return null; // Không có nước đi hợp lệ
        }

        return chooseMoveByDifficulty(validMoves);
    }

    /**
     * Chọn nước đi dựa trên mức độ khó.
     */
    private Move chooseMoveByDifficulty(List<Move> moves) {
        if (moves.isEmpty())
            return null;

        // Dễ: chọn ngẫu nhiên
        if (difficulty <= 1) {
            return moves.get(random.nextInt(moves.size()));
        }

        // Trung bình/Khó: ưu tiên nước ăn quân, nếu không có thì chọn ngẫu nhiên
        List<Move> capturingMoves = new ArrayList<>();
        for (Move m : moves) {
            Piece target = board.getPiece(m.getToRow(), m.getToCol());
            if (target != null && target.getColor() != aiColor) {
                capturingMoves.add(m);
            }
        }

        List<Move> pool = capturingMoves.isEmpty() ? moves : capturingMoves;
        return pool.get(random.nextInt(pool.size()));
    }

    public Piece.Color getAiColor() {
        return aiColor;
    }

    public int getDifficulty() {
        return difficulty;
    }
}
