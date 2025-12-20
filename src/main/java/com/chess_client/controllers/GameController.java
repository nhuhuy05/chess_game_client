package com.chess_client.controllers;

import com.chess_client.models.Board;
import com.chess_client.models.Move;
import com.chess_client.models.Piece;
import com.chess_client.services.GameLogic;
import com.chess_client.services.GameService;
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
    private Label opponentNameLabel;
    @FXML
    private Label playerLabel;
    @FXML
    private Label playerNameLabel;

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
    private List<Move> moveHistory;
    private Socket peerSocket;
    private BufferedReader peerIn;
    private PrintWriter peerOut;
    private String gameId;
    private String opponentName;
    private String playerName;

    // Chế độ chơi
    private boolean vsComputer = false;
    private Piece.Color aiColor;
    private int aiDifficulty = 1; // 1: dễ, 2: trung bình, 3: khó

    // Service gọi API game server
    private final GameService gameService = new GameService();

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
        chatManager.setOnSendMessage(this::sendChatMessage);
        chatManager.initialize();
    }

    /**
     * Được gọi từ HomeController sau khi ghép trận để set gameId và tên đối thủ.
     */
    public void setGameInfo(String gameId, String opponentName, String playerName) {
        this.gameId = gameId;
        this.opponentName = opponentName;
        this.playerName = playerName;
        updatePlayerInfo();
    }

    /**
     * Thiết lập chế độ chơi với máy.
     *
     * @param difficulty 1 = dễ, 2 = trung bình, 3 = khó
     * @param humanColor màu quân của người chơi (thường là TRẮNG)
     */
    public void setupVsComputer(int difficulty, Piece.Color humanColor) {
        this.vsComputer = true;
        this.aiDifficulty = difficulty;
        this.playerColor = humanColor;
        this.aiColor = (humanColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;

        // Chơi với máy thì không cho cầu hòa
        if (drawButton != null) {
            drawButton.setDisable(true);
            drawButton.setVisible(false);
        }

        // Cập nhật lại label và boardView nếu đã khởi tạo
        updatePlayerLabels();
        updatePlayerInfo();
        if (boardView != null) {
            boardView.setPlayerColor(playerColor);
            boardView.setCurrentPlayer(currentPlayer);
            boardView.refreshBoard();
        }
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

        // Lắng nghe nước đi và chat từ đối thủ ở thread riêng
        Thread listener = new Thread(this::listenForPeerMessages);
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
            playerLabel.setText("Quân Trắng");
            opponentPlayerLabel.setText("Quân Đen");
        } else {
            playerLabel.setText("Quân Đen");
            opponentPlayerLabel.setText("Quân Trắng");
        }
    }

    private void updatePlayerInfo() {
        // Cập nhật thông tin người chơi
        if (playerNameLabel != null) {
            playerNameLabel.setText(playerName != null ? playerName : "Bạn");
        }

        // Cập nhật thông tin đối thủ
        if (opponentNameLabel != null) {
            opponentNameLabel.setText(opponentName != null ? opponentName : "Đối thủ");
        }
    }

    private void executeMove(Move move) {
        executeMove(move, false);
    }

    /**
     * @param fromNetwork true nếu nước đi đến từ socket P2P (không gửi lại tránh
     *                    vòng lặp)
     */
    private void executeMove(Move move, boolean fromNetwork) {
        board.movePiece(move);
        moveHistory.add(move);
        updateLastMoveLabel(move);

        if (!fromNetwork) {
            sendMoveToPeer(move);
        }

        currentPlayer = currentPlayer == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;

        // Kiểm tra vua có bị ăn không
        boolean whiteHasKing = gameLogic.hasKing(Piece.Color.WHITE);
        boolean blackHasKing = gameLogic.hasKing(Piece.Color.BLACK);

        if (!whiteHasKing || !blackHasKing) {
            Piece.Color winner = whiteHasKing ? Piece.Color.WHITE : Piece.Color.BLACK;
            String winnerText = winner == Piece.Color.WHITE ? "TRẮNG" : "ĐEN";
            statusLabel.setText("VUA BỊ ĂN! " + winnerText + " THẮNG!");
            disableGameButtons(false);
            endGame(winner);
            return;
        }

        if (gameLogic.isCheckmate(currentPlayer)) {
            Piece.Color winner = currentPlayer == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;
            String winnerText = winner == Piece.Color.WHITE ? "TRẮNG" : "ĐEN";
            statusLabel.setText("CHIẾU HẾT! " + winnerText + " THẮNG!");
            disableGameButtons(false);
            endGame(winner);
        } else if (gameLogic.isStalemate(currentPlayer)) {
            statusLabel.setText("HÒA CỜ (Stalemate)");
            disableGameButtons(false);
            endGame(null); // null = hòa
        } else {
            // Kiểm tra xem có vua nào đang bị chiếu không
            boolean whiteInCheck = gameLogic.isKingInCheck(board, Piece.Color.WHITE);
            boolean blackInCheck = gameLogic.isKingInCheck(board, Piece.Color.BLACK);

            if (whiteInCheck || blackInCheck) {
                statusLabel.setText("CHIẾU TƯỚNG!");
            } else {
                statusLabel.setText("Trò chơi đang diễn ra");
            }
        }

        updateLabels();
        if (boardView != null) {
            boardView.setCurrentPlayer(currentPlayer);
            boardView.setLastMove(move);
            boardView.refreshBoard();
        }

        // Nếu đang chơi với máy và đến lượt AI -> AI tự động đi sau một khoảng trễ nhỏ
        if (vsComputer && currentPlayer == aiColor) {
            new Thread(() -> {
                try {
                    Thread.sleep(600); // delay ~0.6s cho tự nhiên
                } catch (InterruptedException ignored) {
                }
                Platform.runLater(this::makeComputerMove);
            }).start();
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

        // Xác định bên nào vừa đi: Bạn / Đối thủ / Máy
        String prefix;
        Piece.Color moverColor = move.getPieceMoved().getColor();
        boolean isPlayerMove = moverColor == playerColor;
        boolean isAiMove = vsComputer && moverColor == aiColor;

        if (isAiMove) {
            prefix = "Máy: ";
        } else if (isPlayerMove) {
            prefix = "Bạn: ";
        } else {
            prefix = "Đối thủ: ";
        }

        if (move.isCastling())
            moveText += " (Nhập thành)";
        else if (move.isEnPassant())
            moveText += " (En passant)";
        else if (move.isPromotion())
            moveText += " (Phong hậu)";
        else if (move.getPieceCaptured() != null) {
            moveText += " (Ăn " + getPieceName(move.getPieceCaptured()) + ")";
        }

        lastMoveLabel.setText(prefix + moveText);
    }

    // ===================== NETWORK SYNC =====================
    private void sendMoveToPeer(Move move) {
        if (peerOut == null)
            return;
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

    private void listenForPeerMessages() {
        if (peerIn == null)
            return;
        try {
            String line;
            while ((line = peerIn.readLine()) != null) {
                try {
                    JSONObject json = new JSONObject(line);
                    String type = json.optString("type");

                    if ("move".equals(type)) {
                        int fromRow = json.getInt("fromRow");
                        int fromCol = json.getInt("fromCol");
                        int toRow = json.getInt("toRow");
                        int toCol = json.getInt("toCol");

                        Piece piece = board.getPiece(fromRow, fromCol);
                        if (piece == null)
                            continue;

                        Move move = new Move(fromRow, fromCol, toRow, toCol, piece);
                        Platform.runLater(() -> executeMove(move, true));
                    } else if ("chat".equals(type)) {
                        String message = json.getString("message");
                        Platform.runLater(() -> {
                            if (chatManager != null) {
                                chatManager.addChatMessage(opponentName != null ? opponentName : "Đối thủ", message,
                                        false);
                            }
                        });
                    } else if ("game_action".equals(type)) {
                        String action = json.getString("action");
                        Platform.runLater(() -> handleGameAction(action));
                    }
                } catch (Exception parseEx) {
                    parseEx.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendChatMessage(String message) {
        if (peerOut == null)
            return;
        try {
            JSONObject json = new JSONObject();
            json.put("type", "chat");
            json.put("message", message);
            peerOut.println(json.toString());
            peerOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendGameAction(String action) {
        if (peerOut == null)
            return;
        try {
            JSONObject json = new JSONObject();
            json.put("type", "game_action");
            json.put("action", action);
            peerOut.println(json.toString());
            peerOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGameAction(String action) {
        if ("resign".equals(action)) {
            // Đối thủ đã đầu hàng
            Piece.Color winner = playerColor; // Bạn thắng vì đối thủ đầu hàng
            String winnerText = winner == Piece.Color.WHITE ? "TRẮNG" : "ĐEN";
            statusLabel.setText("ĐỐI THỦ ĐÃ ĐẦU HÀNG! " + winnerText + " THẮNG!");
            disableGameButtons(false);
            if (chatManager != null) {
                chatManager.addSystemMessage("Đối thủ đã đầu hàng");
            }
            endGame(winner);
        } else if ("offer_draw".equals(action)) {
            // Đối thủ đề nghị hòa
            if (chatManager != null) {
                chatManager.addSystemMessage("Đối thủ đề nghị hòa");
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Đề nghị hòa");
            alert.setHeaderText("Đối thủ đề nghị hòa");
            alert.setContentText("Bạn có chấp nhận hòa không?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Chấp nhận hòa
                    if (chatManager != null) {
                        chatManager.addSystemMessage("Bạn đã chấp nhận hòa");
                    }
                    statusLabel.setText("TRẬN ĐẤU HÒA!");
                    disableGameButtons(false);
                    sendGameAction("accept_draw");
                    endGame(null); // null = hòa
                } else {
                    // Từ chối hòa
                    if (chatManager != null) {
                        chatManager.addSystemMessage("Bạn đã từ chối hòa");
                    }
                    sendGameAction("reject_draw");
                }
            });
        } else if ("accept_draw".equals(action)) {
            // Đối thủ đã chấp nhận hòa
            statusLabel.setText("TRẬN ĐẤU HÒA!");
            disableGameButtons(false);
            if (chatManager != null) {
                chatManager.addSystemMessage("Đối thủ đã chấp nhận hòa");
            }
            endGame(null); // null = hòa
        } else if ("reject_draw".equals(action)) {
            // Đối thủ đã từ chối hòa
            if (chatManager != null) {
                chatManager.addSystemMessage("Đối thủ đã từ chối hòa");
            }
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
                // Gửi yêu cầu hòa qua P2P
                sendGameAction("offer_draw");
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
                String winnerText = winner == Piece.Color.WHITE ? "TRẮNG" : "ĐEN";
                statusLabel.setText("BẠN ĐÃ ĐẦU HÀNG! " + winnerText + " THẮNG!");
                disableGameButtons(false);
                if (chatManager != null) {
                    chatManager.addSystemMessage("Bạn đã đầu hàng");
                }
                // Gửi thông báo đầu hàng qua P2P
                sendGameAction("resign");
                endGame(winner);
            }
        });
    }

    private void endGame(Piece.Color winner) {
        if (boardView != null) {
            boardView.setCurrentPlayer(null);
            boardView.refreshBoard();
        }

        // Gọi API để cập nhật game và ranking (qua GameService)
        new Thread(() -> gameService.endGame(gameId, winner)).start();

        // Hiển thị dialog thông báo kết thúc
        Platform.runLater(() -> {
            String message;
            if (winner == null) {
                message = "Trận đấu kết thúc HÒA!";
            } else if (winner == playerColor) {
                message = "CHÚC MỪNG! Bạn đã THẮNG!";
            } else {
                message = "Bạn đã THUA trận đấu này.";
            }

            Alert endAlert = new Alert(Alert.AlertType.INFORMATION);
            endAlert.setTitle("Kết thúc trận đấu");
            endAlert.setHeaderText(null);
            endAlert.setContentText(message);
            endAlert.showAndWait();

            // Quay về trang chủ
            returnToHome();
        });
    }

    private void returnToHome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/chess_client/fxml/home.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 930, 740);
            javafx.stage.Stage stage = (javafx.stage.Stage) chessBoard.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableGameButtons(boolean enable) {
        drawButton.setDisable(!enable);
        resignButton.setDisable(!enable);
    }

    // ===================== COMPUTER PLAYER =====================
    private void makeComputerMove() {
        try {
            List<Move> validMoves = gameLogic.getAllValidMoves(aiColor);
            if (validMoves.isEmpty()) {
                // Không còn nước đi hợp lệ -> kiểm tra chiếu hết / hòa
                if (gameLogic.isCheckmate(aiColor)) {
                    Piece.Color winner = (aiColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
                    endGame(winner);
                } else if (gameLogic.isStalemate(aiColor)) {
                    endGame(null);
                }
                return;
            }

            Move chosen = chooseMoveByDifficulty(validMoves);
            if (chosen != null) {
                executeMove(chosen, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Move chooseMoveByDifficulty(List<Move> moves) {
        if (moves.isEmpty()) return null;

        java.util.Random random = new java.util.Random();

        // Dễ: chọn ngẫu nhiên
        if (aiDifficulty <= 1) {
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