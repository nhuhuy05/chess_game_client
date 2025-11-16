package com.chess_client.models;

public class Board {
    private Piece[][] board;
    private Move lastMove;

    public Board() {
        board = new Piece[8][8];
        initializeBoard();
    }

    // Khởi tạo đội hình ban đầu
    private void initializeBoard() {
        // Quân đen (hàng 0, 1)
        board[0][0] = new Piece(Piece.Type.ROOK, Piece.Color.BLACK);
        board[0][1] = new Piece(Piece.Type.KNIGHT, Piece.Color.BLACK);
        board[0][2] = new Piece(Piece.Type.BISHOP, Piece.Color.BLACK);
        board[0][3] = new Piece(Piece.Type.QUEEN, Piece.Color.BLACK);
        board[0][4] = new Piece(Piece.Type.KING, Piece.Color.BLACK);
        board[0][5] = new Piece(Piece.Type.BISHOP, Piece.Color.BLACK);
        board[0][6] = new Piece(Piece.Type.KNIGHT, Piece.Color.BLACK);
        board[0][7] = new Piece(Piece.Type.ROOK, Piece.Color.BLACK);

        for (int col = 0; col < 8; col++) {
            board[1][col] = new Piece(Piece.Type.PAWN, Piece.Color.BLACK);
        }

        // Quân trắng (hàng 6, 7)
        for (int col = 0; col < 8; col++) {
            board[6][col] = new Piece(Piece.Type.PAWN, Piece.Color.WHITE);
        }

        board[7][0] = new Piece(Piece.Type.ROOK, Piece.Color.WHITE);
        board[7][1] = new Piece(Piece.Type.KNIGHT, Piece.Color.WHITE);
        board[7][2] = new Piece(Piece.Type.BISHOP, Piece.Color.WHITE);
        board[7][3] = new Piece(Piece.Type.QUEEN, Piece.Color.WHITE);
        board[7][4] = new Piece(Piece.Type.KING, Piece.Color.WHITE);
        board[7][5] = new Piece(Piece.Type.BISHOP, Piece.Color.WHITE);
        board[7][6] = new Piece(Piece.Type.KNIGHT, Piece.Color.WHITE);
        board[7][7] = new Piece(Piece.Type.ROOK, Piece.Color.WHITE);
    }

    public Piece getPiece(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        return null;
    }

    public void setPiece(int row, int col, Piece piece) {
        if (isValidPosition(row, col)) {
            board[row][col] = piece;
        }
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public void movePiece(Move move) {
        Piece piece = getPiece(move.getFromRow(), move.getFromCol());

        if (piece != null) {
            // Lưu quân bị ăn
            Piece captured = getPiece(move.getToRow(), move.getToCol());
            move.setPieceCaptured(captured);

            // Di chuyển quân
            setPiece(move.getToRow(), move.getToCol(), piece);
            setPiece(move.getFromRow(), move.getFromCol(), null);

            // Đánh dấu đã di chuyển
            piece.setMoved(true);

            // Xử lý en passant
            if (move.isEnPassant()) {
                int captureRow = move.getFromRow();
                setPiece(captureRow, move.getToCol(), null);
            }

            // Xử lý nhập thành
            if (move.isCastling()) {
                int rookCol = move.getToCol() > move.getFromCol() ? 7 : 0;
                int newRookCol = move.getToCol() > move.getFromCol() ? move.getToCol() - 1 : move.getToCol() + 1;

                Piece rook = getPiece(move.getFromRow(), rookCol);
                setPiece(move.getFromRow(), newRookCol, rook);
                setPiece(move.getFromRow(), rookCol, null);
                if (rook != null) {
                    rook.setMoved(true);
                }
            }

            // Xử lý phong cấp
            if (move.isPromotion()) {
                piece.setType(Piece.Type.QUEEN); // Mặc định phong hậu
            }

            lastMove = move;
        }
    }

    public void undoMove(Move move) {
        Piece piece = getPiece(move.getToRow(), move.getToCol());

        if (piece != null) {
            // Khôi phục vị trí
            setPiece(move.getFromRow(), move.getFromCol(), piece);
            setPiece(move.getToRow(), move.getToCol(), move.getPieceCaptured());

            // Xử lý en passant
            if (move.isEnPassant()) {
                int captureRow = move.getFromRow();
                setPiece(captureRow, move.getToCol(), move.getPieceCaptured());
                setPiece(move.getToRow(), move.getToCol(), null);
            }

            // Xử lý nhập thành
            if (move.isCastling()) {
                int rookCol = move.getToCol() > move.getFromCol() ? 7 : 0;
                int newRookCol = move.getToCol() > move.getFromCol() ? move.getToCol() - 1 : move.getToCol() + 1;

                Piece rook = getPiece(move.getFromRow(), newRookCol);
                setPiece(move.getFromRow(), rookCol, rook);
                setPiece(move.getFromRow(), newRookCol, null);
            }
        }
    }

    public Move getLastMove() {
        return lastMove;
    }

    // Tạo bản sao bàn cờ
    public Board copy() {
        Board newBoard = new Board();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = getPiece(row, col);
                if (piece != null) {
                    Piece newPiece = new Piece(piece.getType(), piece.getColor());
                    newPiece.setMoved(piece.hasMoved());
                    newBoard.setPiece(row, col, newPiece);
                } else {
                    newBoard.setPiece(row, col, null);
                }
            }
        }
        newBoard.lastMove = this.lastMove;
        return newBoard;
    }
}