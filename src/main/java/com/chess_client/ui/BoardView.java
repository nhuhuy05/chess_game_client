package com.chess_client.ui;

import com.chess_client.models.Board;
import com.chess_client.models.Move;
import com.chess_client.models.Piece;
import com.chess_client.services.GameLogic;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Chịu trách nhiệm vẽ bàn cờ, xử lý chọn quân, highlight nước đi hợp lệ.
 * Khi người chơi chọn xong nước đi hợp lệ, lớp này sẽ callback ra
 * GameController.
 */
public class BoardView {

    private final GridPane chessBoard;
    private final Board board;
    private final GameLogic gameLogic;
    private final Consumer<Move> onMoveConfirmed;

    private Piece.Color playerColor;
    private Piece.Color currentPlayer;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private final List<StackPane> highlightedSquares = new ArrayList<>();
    private Move lastMove;

    // ===================== CONSTANTS =====================
    private static final int SQUARE_SIZE = 70;
    private static final Color LIGHT_SQUARE = Color.web("#f0d9b5");
    private static final Color DARK_SQUARE = Color.web("#b58863");
    private static final Color SELECTED_COLOR = Color.web("#baca44");
    private static final Color VALID_MOVE_COLOR = Color.web("#769656", 0.7);

    public BoardView(GridPane chessBoard,
            Board board,
            GameLogic gameLogic,
            Piece.Color playerColor,
            Consumer<Move> onMoveConfirmed) {
        this.chessBoard = chessBoard;
        this.board = board;
        this.gameLogic = gameLogic;
        this.playerColor = playerColor;
        this.onMoveConfirmed = onMoveConfirmed;
    }

    public void setPlayerColor(Piece.Color playerColor) {
        this.playerColor = playerColor;
        refreshBoard();
    }

    public void setCurrentPlayer(Piece.Color currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public void setLastMove(Move lastMove) {
        this.lastMove = lastMove;
    }

    public void refreshBoard() {
        drawBoard();
    }

    // ===================== BOARD RENDERING =====================
    private void drawBoard() {
        chessBoard.getChildren().clear();
        clearSelection();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = createSquare(row, col);

                // Tính vị trí hiển thị theo màu của người chơi
                int displayRow = playerColor == Piece.Color.WHITE ? row : 7 - row;
                int displayCol = playerColor == Piece.Color.WHITE ? col : 7 - col;

                chessBoard.add(square, displayCol, displayRow);
            }
        }
    }

    private StackPane createSquare(int row, int col) {
        StackPane square = new StackPane();
        square.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);

        // Lưu lại vị trí logic để dùng khi reset màu
        square.setUserData(new int[] { row, col });

        // Background color
        Rectangle background = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
        Color baseColor = (row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;

        // Highlight nhẹ nước đi cuối (from/to)
        if (lastMove != null &&
                ((row == lastMove.getFromRow() && col == lastMove.getFromCol()) ||
                        (row == lastMove.getToRow() && col == lastMove.getToCol()))) {
            // Pha trộn màu nền với một chút vàng nhạt
            Color highlight = Color.web("#f7ec88", 0.6);
            background.setFill(baseColor.interpolate(highlight, 0.4));
        } else {
            background.setFill(baseColor);
        }
        square.getChildren().add(background);

        // Piece
        Piece piece = board.getPiece(row, col);
        if (piece != null) {
            Text pieceText = new Text(piece.getUnicode());
            pieceText.setFont(Font.font(50));
            pieceText.setFill(Color.BLACK);
            square.getChildren().add(pieceText);
        }

        // Click event (sử dụng tọa độ logic)
        final int r = row, c = col;
        square.setOnMouseClicked(e -> handleSquareClick(r, c, square));
        return square;
    }

    // ===================== MOVE HANDLING & HIGHLIGHT =====================
    private void handleSquareClick(int row, int col, StackPane square) {
        // Chưa đến lượt mình thì không được phép chọn / đi quân
        if (currentPlayer == null || currentPlayer != playerColor) {
            return;
        }

        Piece clickedPiece = board.getPiece(row, col);

        // Nếu đã chọn quân và click vào ô hợp lệ
        if (selectedRow != -1 && selectedCol != -1) {
            Move move = new Move(selectedRow, selectedCol, row, col, board.getPiece(selectedRow, selectedCol));
            if (gameLogic.isValidMove(move, currentPlayer)) {
                clearSelection();
                if (onMoveConfirmed != null) {
                    onMoveConfirmed.accept(move);
                }
                return;
            }
        }

        // Chọn quân mới
        if (clickedPiece != null && clickedPiece.getColor() == currentPlayer) {
            clearSelection();
            selectedRow = row;
            selectedCol = col;
            highlightSquare(square, SELECTED_COLOR);
            showValidMoves(row, col);
        } else {
            clearSelection();
        }
    }

    private void showValidMoves(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece == null)
            return;

        for (int toRow = 0; toRow < 8; toRow++) {
            for (int toCol = 0; toCol < 8; toCol++) {
                Move move = new Move(row, col, toRow, toCol, piece);
                if (gameLogic.isValidMove(move, currentPlayer)) {
                    int displayRow = playerColor == Piece.Color.WHITE ? toRow : 7 - toRow;
                    int displayCol = playerColor == Piece.Color.WHITE ? toCol : 7 - toCol;
                    StackPane target = getSquareAtDisplay(displayRow, displayCol);
                    if (target != null) {
                        highlightSquare(target, VALID_MOVE_COLOR);
                        highlightedSquares.add(target);
                    }
                }
            }
        }
    }

    /**
     * Lấy ô vuông theo tọa độ hiển thị (row/col trên GridPane),
     * không phụ thuộc vào thứ tự children trong GridPane.
     */
    private StackPane getSquareAtDisplay(int displayRow, int displayCol) {
        for (Node node : chessBoard.getChildren()) {
            if (!(node instanceof StackPane square)) continue;
            Integer r = GridPane.getRowIndex(square);
            Integer c = GridPane.getColumnIndex(square);
            int row = r == null ? 0 : r;
            int col = c == null ? 0 : c;
            if (row == displayRow && col == displayCol) {
                return square;
            }
        }
        return null;
    }

    private void highlightSquare(StackPane square, Color color) {
        if (square.getChildren().get(0) instanceof Rectangle bg) {
            bg.setFill(color);
        }
    }

    private void clearHighlights() {
        for (StackPane square : highlightedSquares) {
            Object data = square.getUserData();
            int row = 0;
            int col = 0;
            if (data instanceof int[] coords && coords.length == 2) {
                row = coords[0];
                col = coords[1];
            }

            if (square.getChildren().get(0) instanceof Rectangle bg) {
                bg.setFill((row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
            }
        }
        highlightedSquares.clear();
    }

    public void clearSelection() {
        clearHighlights();
        selectedRow = -1;
        selectedCol = -1;
    }
}
