package com.chess_client.controllers;

import com.chess_client.models.*;
import com.chess_client.services.GameLogic;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;

public class GameController {

    // ===================== UI COMPONENTS - BOARD =====================
    @FXML private GridPane chessBoard;

    // ===================== UI COMPONENTS - PLAYER INFO =====================
    @FXML private Label opponentPlayerLabel;
    @FXML private Label opponentTimeLabel;
    @FXML private Label playerLabel;
    @FXML private Label playerTimeLabel;

    // ===================== UI COMPONENTS - GAME INFO =====================
    @FXML private Label turnLabel;
    @FXML private Label statusLabel;
    @FXML private Label lastMoveLabel;

    // ===================== UI COMPONENTS - BUTTONS =====================
    @FXML private Button drawButton;
    @FXML private Button resignButton;

    // ===================== UI COMPONENTS - CHAT =====================
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatMessagesBox;
    @FXML private TextField chatInput;
    @FXML private Button sendMessageButton;

    // ===================== GAME STATE =====================
    private Board board;
    private GameLogic gameLogic;
    private Piece.Color currentPlayer;
    private Piece.Color playerColor;
    private StackPane selectedSquare;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private List<StackPane> highlightedSquares;
    private List<Move> moveHistory;

    // ===================== CONSTANTS =====================
    private static final int SQUARE_SIZE = 70;
    private static final Color LIGHT_SQUARE = Color.web("#f0d9b5");
    private static final Color DARK_SQUARE = Color.web("#b58863");
    private static final Color SELECTED_COLOR = Color.web("#baca44");
    private static final Color VALID_MOVE_COLOR = Color.web("#769656", 0.7);

    // ===================== INITIALIZATION =====================
    @FXML
    public void initialize() {
        highlightedSquares = new ArrayList<>();
        moveHistory = new ArrayList<>();
        playerColor = Piece.Color.WHITE; // Mặc định người chơi là TRẮNG
        initializeGame();
        drawBoard();
        setupEventHandlers();
        setupChatEnterKey();
    }

    private void setupEventHandlers() {
        drawButton.setOnAction(e -> offerDraw());
        resignButton.setOnAction(e -> resign());
        sendMessageButton.setOnAction(e -> sendMessage());
    }

    private void setupChatEnterKey() {
        chatInput.setOnAction(e -> sendMessage());
    }

    private void initializeGame() {
        board = new Board();
        gameLogic = new GameLogic(board);
        currentPlayer = Piece.Color.WHITE;
        selectedRow = -1;
        selectedCol = -1;
        selectedSquare = null;
        highlightedSquares.clear();
        moveHistory.clear();

        updateLabels();
        statusLabel.setText("Trò chơi bắt đầu");
        lastMoveLabel.setText("Chưa có");
        updatePlayerLabels();
    }

    private void updatePlayerLabels() {
        if (playerColor == Piece.Color.WHITE) {
            playerLabel.setText("Bạn: TRẮNG");
            opponentPlayerLabel.setText("Đối thủ: ĐEN");
        } else {
            playerLabel.setText("Bạn: ĐEN");
            opponentPlayerLabel.setText("Đối thủ: TRẮNG");
        }
        playerTimeLabel.setText("10:00");
        opponentTimeLabel.setText("10:00");
    }

    // ===================== BOARD RENDERING =====================
    private void drawBoard() {
        chessBoard.getChildren().clear();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = createSquare(row, col);
                chessBoard.add(square, col, row);
            }
        }
    }

    private StackPane createSquare(int row, int col) {
        StackPane square = new StackPane();
        square.setPrefSize(SQUARE_SIZE, SQUARE_SIZE);

        // Background color
        Rectangle background = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
        background.setFill((row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
        square.getChildren().add(background);

        // Piece
        Piece piece = board.getPiece(row, col);
        if (piece != null) {
            Text pieceText = new Text(piece.getUnicode());
            pieceText.setFont(Font.font(50));
            pieceText.setFill(Color.BLACK);
            square.getChildren().add(pieceText);
        }

        // Click event
        final int r = row, c = col;
        square.setOnMouseClicked(e -> handleSquareClick(r, c, square));
        return square;
    }

    // ===================== MOVE HANDLING =====================
    private void handleSquareClick(int row, int col, StackPane square) {
        Piece clickedPiece = board.getPiece(row, col);

        // Nếu đã chọn quân và click vào ô hợp lệ
        if (selectedRow != -1 && selectedCol != -1) {
            Move move = new Move(selectedRow, selectedCol, row, col, board.getPiece(selectedRow, selectedCol));
            if (gameLogic.isValidMove(move, currentPlayer)) {
                executeMove(move);
                return;
            }
        }

        // Chọn quân mới
        if (clickedPiece != null && clickedPiece.getColor() == currentPlayer) {
            clearHighlights();
            selectedRow = row;
            selectedCol = col;
            selectedSquare = square;
            highlightSquare(square, SELECTED_COLOR);
            showValidMoves(row, col);
        } else {
            clearSelection();
        }
    }

    private void executeMove(Move move) {
        board.movePiece(move);
        moveHistory.add(move);
        updateLastMoveLabel(move);

        currentPlayer = currentPlayer == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;

        if (gameLogic.isCheckmate(currentPlayer)) {
            Piece.Color winner = currentPlayer == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;
            statusLabel.setText("CHIẾU HẾT! " + (winner == Piece.Color.WHITE ? "TRẮNG" : "ĐEN") + " THẮNG!");
            disableGameButtons(false);
        } else if (gameLogic.isStalemate(currentPlayer)) {
            statusLabel.setText("HÒA CỜ (Stalemate)");
            disableGameButtons(false);
        } else if (gameLogic.isKingInCheck(board, currentPlayer)) {
            statusLabel.setText("CHIẾU TƯỚNG!");
        } else {
            statusLabel.setText("Trò chơi đang diễn ra");
        }

        updateLabels();
        drawBoard();
    }

    private void showValidMoves(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece == null) return;

        for (int toRow = 0; toRow < 8; toRow++) {
            for (int toCol = 0; toCol < 8; toCol++) {
                Move move = new Move(row, col, toRow, toCol, piece);
                if (gameLogic.isValidMove(move, currentPlayer)) {
                    int index = toRow * 8 + toCol;
                    if (index < chessBoard.getChildren().size()) {
                        StackPane target = (StackPane) chessBoard.getChildren().get(index);
                        highlightSquare(target, VALID_MOVE_COLOR);
                        highlightedSquares.add(target);
                    }
                }
            }
        }
    }

    private void highlightSquare(StackPane square, Color color) {
        if (square.getChildren().get(0) instanceof Rectangle bg) {
            bg.setFill(color);
        }
    }

    private void clearHighlights() {
        for (StackPane square : highlightedSquares) {
            int index = chessBoard.getChildren().indexOf(square);
            int row = index / 8;
            int col = index % 8;
            if (square.getChildren().get(0) instanceof Rectangle bg) {
                bg.setFill((row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
            }
        }
        highlightedSquares.clear();
    }

    private void clearSelection() {
        clearHighlights();
        if (selectedSquare != null) {
            int index = chessBoard.getChildren().indexOf(selectedSquare);
            int row = index / 8;
            int col = index % 8;
            if (selectedSquare.getChildren().get(0) instanceof Rectangle bg) {
                bg.setFill((row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
            }
        }
        selectedRow = -1;
        selectedCol = -1;
        selectedSquare = null;
    }

    // ===================== UI UPDATES =====================
    private void updateLabels() {
        turnLabel.setText("Lượt đi: " + (currentPlayer == Piece.Color.WHITE ? "TRẮNG" : "ĐEN"));
    }

    private void updateLastMoveLabel(Move move) {
        String from = getSquareName(move.getFromRow(), move.getFromCol());
        String to = getSquareName(move.getToRow(), move.getToCol());
        String pieceName = getPieceName(move.getPieceMoved());
        String moveText = pieceName + " " + from + " → " + to;

        if (move.isCastling()) moveText += " (Nhập thành)";
        else if (move.isEnPassant()) moveText += " (En passant)";
        else if (move.isPromotion()) moveText += " (Phong hậu)";
        else if (move.getPieceCaptured() != null) {
            moveText += " (Ăn " + getPieceName(move.getPieceCaptured()) + ")";
        }

        lastMoveLabel.setText(moveText);
    }

    // ===================== GAME ACTIONS =====================
    private void offerDraw() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cầu hòa");
        alert.setHeaderText("Bạn muốn đề nghị hòa?");
        alert.setContentText("Đối thủ sẽ được hỏi có chấp nhận không.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                addSystemMessage("Bạn đã đề nghị hòa");
                // TODO: Gửi yêu cầu hòa đến server
            }
        });
    }

    private void resign() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Nhận thua");
        alert.setHeaderText("Bạn muốn đầu hàng?");
        alert.setContentText("Bạn sẽ thua ván này.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Piece.Color winner = playerColor == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;
                statusLabel.setText("BẠN ĐÃ ĐẦU HÀNG! " + (winner == Piece.Color.WHITE ? "TRẮNG" : "ĐEN") + " THẮNG!");
                disableGameButtons(false);
                addSystemMessage("Bạn đã đầu hàng");
            }
        });
    }

    private void disableGameButtons(boolean enable) {
        drawButton.setDisable(!enable);
        resignButton.setDisable(!enable);
    }

    // ===================== CHAT FUNCTIONS =====================
    private void sendMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            addChatMessage("Bạn", message, true);
            chatInput.clear();
            // TODO: Gửi tin nhắn đến server
        }
    }

    private void addChatMessage(String sender, String message, boolean isPlayer) {
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

    private void addSystemMessage(String message) {
        Label systemMsg = new Label(message);
        systemMsg.setStyle("-fx-text-fill: #999; -fx-font-size: 11px; -fx-font-style: italic; -fx-padding: 5 0 5 0;");
        systemMsg.setAlignment(Pos.CENTER);
        systemMsg.setMaxWidth(Double.MAX_VALUE);
        chatMessagesBox.getChildren().add(systemMsg);
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    // ===================== HELPER METHODS =====================
    private String getSquareName(int row, int col) {
        return "" + (char)('a' + col) + (8 - row);
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