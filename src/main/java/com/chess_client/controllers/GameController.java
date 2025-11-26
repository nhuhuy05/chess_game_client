package com.chess_client.controllers;

import com.chess_client.models.Board;
import com.chess_client.models.Move;
import com.chess_client.models.Piece;
import com.chess_client.services.GameLogic;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameController {

    // ===================== UI COMPONENTS - BOARD =====================
    @FXML
    private GridPane chessBoard;

    // ===================== UI COMPONENTS - PLAYER INFO =====================
    @FXML
    private Label opponentPlayerLabel;
    @FXML
    private Label playerLabel;

    // ===================== UI COMPONENTS - GAME INFO =====================
    @FXML
    private Label turnLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label lastMoveLabel;

    // ===================== UI COMPONENTS - BUTTONS =====================
    @FXML
    private Button drawButton;
    @FXML
    private Button resignButton;

    // ===================== UI COMPONENTS - CHAT =====================
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private VBox chatMessagesBox;
    @FXML
    private TextField chatInput;
    @FXML
    private Button sendMessageButton;

    private ChatManager chatManager;
    private BoardView boardView;

    // ===================== GAME STATE =====================
    private Board board;
    private GameLogic gameLogic;
    private Piece.Color currentPlayer;
    private Piece.Color playerColor;
    private String playerName;
    private String opponentName;
    private List<Move> moveHistory;
    private Socket peerSocket;
    private BufferedReader peerIn;
    private PrintWriter peerOut;

    // ===================== INITIALIZATION =====================
    @FXML
    public void initialize() {
        moveHistory = new ArrayList<>();
        playerColor = Piece.Color.WHITE; // Mặc định người chơi là TRẮNG
        initializeGame();

        // Khởi tạo BoardView để vẽ và điều khiển bàn cờ
        boardView = new BoardView(chessBoard, board, gameLogic, playerColor, this::executeMove);
        boardView.setCurrentPlayer(currentPlayer);
        boardView.refreshBoard();

        setupEventHandlers();

        // Khởi tạo quản lý chat
        chatManager = new ChatManager(chatScrollPane, chatMessagesBox, chatInput, sendMessageButton);
        chatManager.initialize();
    }

    /**
     * Được gọi từ HomeController sau khi ghép trận để set tên người chơi và đối thủ.
     */
    public void setPlayerNames(String playerName, String opponentName) {
        this.playerName = playerName;
        this.opponentName = opponentName;
        updatePlayerLabels();
    }

    /**
     * Được gọi từ HomeController sau khi ghép trận để set màu quân (WHITE/BLACK).
     */
    public void setPlayerColor(Piece.Color color) {
        this.playerColor = color;
        updatePlayerLabels();
        if (boardView != null) {
            boardView.setPlayerColor(color);
            boardView.refreshBoard();
        }
    }

    /**
     * Socket P2P đã được thiết lập giữa hai client (LAN).
     * TODO: sử dụng socket này để gửi/nhận nước đi, chat realtime.
     */
    public void setPeerSocket(Socket socket) {
        this.peerSocket = socket;
        try {
            this.peerIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.peerOut = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Lắng nghe nước đi từ đối thủ ở thread riêng
        Thread listener = new Thread(this::listenForPeerMoves);
        listener.setDaemon(true);
        listener.start();
    }

    private void setupEventHandlers() {
        drawButton.setOnAction(e -> offerDraw());
        resignButton.setOnAction(e -> resign());
    }

    private void initializeGame() {
        board = new Board();
        gameLogic = new GameLogic(board);
        currentPlayer = Piece.Color.WHITE;
        moveHistory.clear();

        updateLabels();
        statusLabel.setText("Trò chơi bắt đầu");
        lastMoveLabel.setText("Chưa có");
        updatePlayerLabels();
    }

    private void updatePlayerLabels() {
        if (playerColor == Piece.Color.WHITE) {
            playerLabel.setText("Quân Trắng\n" + (playerName != null ? playerName : "Bạn"));
            opponentPlayerLabel.setText("Quân Đen\n" + (opponentName != null ? opponentName : "Đối thủ"));
        } else {
            playerLabel.setText("Quân Đen\n" + (playerName != null ? playerName : "Bạn"));
            opponentPlayerLabel.setText("Quân Trắng\n" + (opponentName != null ? opponentName : "Đối thủ"));
        }
    }

    private void executeMove(Move move) {
        executeMove(move, false);
    }

    /**
     * @param fromNetwork true nếu nước đi đến từ socket P2P (không gửi lại tránh vòng lặp)
     */
    private void executeMove(Move move, boolean fromNetwork) {
        board.movePiece(move);
        moveHistory.add(move);
        updateLastMoveLabel(move);

        if (!fromNetwork) {
            sendMoveToPeer(move);
        }

        // Sau khi đi xong, kiểm tra xem bên nào đã mất vua
        boolean whiteHasKing = gameLogic.hasKing(Piece.Color.WHITE);
        boolean blackHasKing = gameLogic.hasKing(Piece.Color.BLACK);

        if (!whiteHasKing || !blackHasKing) {
            Piece.Color winner = whiteHasKing ? Piece.Color.WHITE : Piece.Color.BLACK;
            statusLabel.setText("VUA BỊ ĂN! " + (winner == Piece.Color.WHITE ? "TRẮNG" : "ĐEN") + " THẮNG!");
            disableGameButtons(false);

            // Ngừng cho phép tiếp tục đi cờ
            if (boardView != null) {
                boardView.setCurrentPlayer(null);
                boardView.refreshBoard();
            }
            return;
        }

        // Nếu cả hai bên vẫn còn vua, tiếp tục ván đấu như bình thường
        currentPlayer = currentPlayer == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;

        // Hiển thị trạng thái chiếu tướng (chỉ để báo, không giới hạn nước đi)
        if (gameLogic.isKingInCheck(board, currentPlayer)) {
            statusLabel.setText("CHIẾU TƯỚNG!");
        } else {
            statusLabel.setText("Trò chơi đang diễn ra");
        }

        updateLabels();
        if (boardView != null) {
            boardView.setCurrentPlayer(currentPlayer);
            boardView.refreshBoard();
        }
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

        if (move.isCastling())
            moveText += " (Nhập thành)";
        else if (move.isEnPassant())
            moveText += " (En passant)";
        else if (move.isPromotion())
            moveText += " (Phong hậu)";
        else if (move.getPieceCaptured() != null) {
            moveText += " (Ăn " + getPieceName(move.getPieceCaptured()) + ")";
        }

        lastMoveLabel.setText(moveText);
    }

    // ===================== NETWORK SYNC =====================
    private void sendMoveToPeer(Move move) {
        if (peerOut == null) return;
        try {
            JSONObject json = new JSONObject();
            json.put("type", "move");
            json.put("fromRow", move.getFromRow());
            json.put("fromCol", move.getFromCol());
            json.put("toRow", move.getToRow());
            json.put("toCol", move.getToCol());
            peerOut.println(json.toString());
            peerOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForPeerMoves() {
        if (peerIn == null) return;
        try {
            String line;
            while ((line = peerIn.readLine()) != null) {
                try {
                    JSONObject json = new JSONObject(line);
                    if (!"move".equals(json.optString("type"))) continue;

                    int fromRow = json.getInt("fromRow");
                    int fromCol = json.getInt("fromCol");
                    int toRow = json.getInt("toRow");
                    int toCol = json.getInt("toCol");

                    Piece piece = board.getPiece(fromRow, fromCol);
                    if (piece == null) continue;

                    Move move = new Move(fromRow, fromCol, toRow, toCol, piece);
                    Platform.runLater(() -> executeMove(move, true));
                } catch (Exception parseEx) {
                    parseEx.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===================== GAME ACTIONS =====================
    private void offerDraw() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cầu hòa");
        alert.setHeaderText("Bạn muốn đề nghị hòa?");
        alert.setContentText("Đối thủ sẽ được hỏi có chấp nhận không.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (chatManager != null) {
                    chatManager.addSystemMessage("Bạn đã đề nghị hòa");
                }
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
                if (chatManager != null) {
                    chatManager.addSystemMessage("Bạn đã đầu hàng");
                }
            }
        });
    }

    private void disableGameButtons(boolean enable) {
        drawButton.setDisable(!enable);
        resignButton.setDisable(!enable);
    }

    // ===================== HELPER METHODS =====================
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