package com.chess_client.services;

import com.chess_client.models.Board;
import com.chess_client.models.Move;
import com.chess_client.models.Piece;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {
    private Board board;

    public GameLogic(Board board) {
        this.board = board;
    }

    // Kiểm tra nước đi hợp lệ
    public boolean isValidMove(Move move, Piece.Color currentPlayer) {
        Piece piece = move.getPieceMoved();

        if (piece == null || piece.getColor() != currentPlayer) {
            return false;
        }

        // Kiểm tra theo luật di chuyển của từng quân cờ
        if (!isPieceMoveLegal(move)) {
            return false;
        }

        // Theo yêu cầu: KHÔNG kiểm tra việc vua có còn bị chiếu sau khi đi hay không.
        // Nghĩa là nước đi vẫn được chấp nhận kể cả khi không thoát khỏi chiếu tướng.
        return true;
    }

    // Kiểm tra luật đi của từng quân
    private boolean isPieceMoveLegal(Move move) {
        Piece piece = move.getPieceMoved();
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        // Không đi tại chỗ
        if (fromRow == toRow && fromCol == toCol) {
            return false;
        }

        Piece targetPiece = board.getPiece(toRow, toCol);

        // Không ăn quân cùng màu
        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
            return false;
        }

        switch (piece.getType()) {
            case PAWN:
                return isValidPawnMove(move);
            case ROOK:
                return isValidRookMove(move);
            case KNIGHT:
                return isValidKnightMove(move);
            case BISHOP:
                return isValidBishopMove(move);
            case QUEEN:
                return isValidQueenMove(move);
            case KING:
                return isValidKingMove(move);
            default:
                return false;
        }
    }

    private boolean isValidPawnMove(Move move) {
        Piece pawn = move.getPieceMoved();
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        int direction = pawn.getColor() == Piece.Color.WHITE ? -1 : 1;
        int startRow = pawn.getColor() == Piece.Color.WHITE ? 6 : 1;

        Piece targetPiece = board.getPiece(toRow, toCol);

        // Di chuyển thẳng 1 ô
        if (fromCol == toCol && toRow == fromRow + direction && targetPiece == null) {
            if ((pawn.getColor() == Piece.Color.WHITE && toRow == 0) ||
                    (pawn.getColor() == Piece.Color.BLACK && toRow == 7)) {
                move.setPromotion(true);
            }
            return true;
        }

        // Di chuyển thẳng 2 ô từ vị trí ban đầu
        if (fromCol == toCol && fromRow == startRow && toRow == fromRow + 2 * direction &&
                targetPiece == null && board.getPiece(fromRow + direction, fromCol) == null) {
            return true;
        }

        // Ăn chéo
        if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction) {
            if (targetPiece != null && targetPiece.getColor() != pawn.getColor()) {
                if ((pawn.getColor() == Piece.Color.WHITE && toRow == 0) ||
                        (pawn.getColor() == Piece.Color.BLACK && toRow == 7)) {
                    move.setPromotion(true);
                }
                return true;
            }

            // En passant
            Move lastMove = board.getLastMove();
            if (lastMove != null) {
                Piece lastPiece = board.getPiece(lastMove.getToRow(), lastMove.getToCol());
                if (lastPiece != null && lastPiece.getType() == Piece.Type.PAWN &&
                        Math.abs(lastMove.getToRow() - lastMove.getFromRow()) == 2 &&
                        lastMove.getToCol() == toCol && lastMove.getToRow() == fromRow) {
                    move.setEnPassant(true);
                    move.setPieceCaptured(lastPiece);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isValidRookMove(Move move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        // Đi theo hàng hoặc cột
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }

        return !isPathBlocked(fromRow, fromCol, toRow, toCol);
    }

    private boolean isValidKnightMove(Move move) {
        int rowDiff = Math.abs(move.getToRow() - move.getFromRow());
        int colDiff = Math.abs(move.getToCol() - move.getFromCol());

        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private boolean isValidBishopMove(Move move) {
        int rowDiff = Math.abs(move.getToRow() - move.getFromRow());
        int colDiff = Math.abs(move.getToCol() - move.getFromCol());

        // Đi theo đường chéo
        if (rowDiff != colDiff) {
            return false;
        }

        return !isPathBlocked(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());
    }

    private boolean isValidQueenMove(Move move) {
        return isValidRookMove(move) || isValidBishopMove(move);
    }

    private boolean isValidKingMove(Move move) {
        int rowDiff = Math.abs(move.getToRow() - move.getFromRow());
        int colDiff = Math.abs(move.getToCol() - move.getFromCol());

        // Đi 1 ô
        if (rowDiff <= 1 && colDiff <= 1) {
            return true;
        }

        // Nhập thành
        if (rowDiff == 0 && colDiff == 2) {
            return isValidCastling(move);
        }

        return false;
    }

    private boolean isValidCastling(Move move) {
        Piece king = move.getPieceMoved();
        int row = move.getFromRow();
        int kingCol = move.getFromCol();
        int toCol = move.getToCol();

        // Vua và xe chưa di chuyển
        if (king.hasMoved()) {
            return false;
        }

        // Xác định xe
        int rookCol = toCol > kingCol ? 7 : 0;
        Piece rook = board.getPiece(row, rookCol);

        if (rook == null || rook.getType() != Piece.Type.ROOK || rook.hasMoved()) {
            return false;
        }

        // Kiểm tra đường đi không bị chặn
        int start = Math.min(kingCol, rookCol) + 1;
        int end = Math.max(kingCol, rookCol);

        for (int col = start; col < end; col++) {
            if (board.getPiece(row, col) != null) {
                return false;
            }
        }

        // Vua không bị chiếu và không đi qua ô bị chiếu
        if (isKingInCheck(board, king.getColor())) {
            return false;
        }

        int direction = toCol > kingCol ? 1 : -1;
        for (int col = kingCol; col != toCol + direction; col += direction) {
            Board tempBoard = board.copy();
            tempBoard.setPiece(row, col, king);
            tempBoard.setPiece(row, kingCol, null);

            if (isKingInCheck(tempBoard, king.getColor())) {
                return false;
            }
        }

        move.setCastling(true);
        return true;
    }

    // Kiểm tra đường đi có bị chặn không
    private boolean isPathBlocked(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow || currentCol != toCol) {
            if (board.getPiece(currentRow, currentCol) != null) {
                return true;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return false;
    }

    // Kiểm tra vua có bị chiếu không
    public boolean isKingInCheck(Board checkBoard, Piece.Color kingColor) {
        // Tìm vị trí vua
        int kingRow = -1, kingCol = -1;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = checkBoard.getPiece(row, col);
                if (piece != null && piece.getType() == Piece.Type.KING && piece.getColor() == kingColor) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
            if (kingRow != -1)
                break;
        }

        // Kiểm tra xem có quân địch nào có thể ăn vua không
        Piece.Color enemyColor = kingColor == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = checkBoard.getPiece(row, col);
                if (piece != null && piece.getColor() == enemyColor) {
                    Move attackMove = new Move(row, col, kingRow, kingCol, piece);
                    GameLogic tempLogic = new GameLogic(checkBoard);
                    if (tempLogic.isPieceMoveLegal(attackMove)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Kiểm tra chiếu hết
    public boolean isCheckmate(Piece.Color kingColor) {
        if (!isKingInCheck(board, kingColor)) {
            return false;
        }

        return getAllValidMoves(kingColor).isEmpty();
    }

    // Kiểm tra hòa (stalemate)
    public boolean isStalemate(Piece.Color currentPlayer) {
        if (isKingInCheck(board, currentPlayer)) {
            return false;
        }

        return getAllValidMoves(currentPlayer).isEmpty();
    }

    // Lấy tất cả nước đi hợp lệ
    public List<Move> getAllValidMoves(Piece.Color playerColor) {
        List<Move> validMoves = new ArrayList<>();

        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Piece piece = board.getPiece(fromRow, fromCol);
                if (piece != null && piece.getColor() == playerColor) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            Move move = new Move(fromRow, fromCol, toRow, toCol, piece);
                            if (isValidMove(move, playerColor)) {
                                validMoves.add(move);
                            }
                        }
                    }
                }
            }
        }

        return validMoves;
    }

    // Kiểm tra xem màu quân cờ có còn vua trên bàn cờ không
    public boolean hasKing(Piece.Color color) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getType() == Piece.Type.KING && piece.getColor() == color) {
                    return true;
                }
            }
        }
        return false;
    }
}