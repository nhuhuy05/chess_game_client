package com.chess_client.controllers;

import com.chess_client.models.Board;
import com.chess_client.models.Move;
import com.chess_client.models.Piece;
import com.chess_client.services.GameLogic;
import com.chess_client.services.GameService;
import com.chess_client.services.GameStateChecker;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

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
    private String gameId;
    private String opponentName;
    private String playerName;

    // Handlers
    private PeerNetworkHandler peerNetworkHandler;
    private AIPlayer aiPlayer;
    private GameStateChecker gameStateChecker;
    private GameActionHandler gameActionHandler;
    private UIGameInfoUpdater uiUpdater;

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

        // Khởi tạo quản lý chat
        chatManager = new ChatManager(chatScrollPane, chatMessagesBox, chatInput, sendMessageButton);
        chatManager.initialize();

        // Khởi tạo network handler
        peerNetworkHandler = new PeerNetworkHandler();
        setupNetworkCallbacks();

        // Khởi tạo game state checker
        gameStateChecker = new GameStateChecker(board, gameLogic);

        // Khởi tạo UI updater (sẽ update aiPlayer sau khi setupVsComputer được gọi)
        uiUpdater = new UIGameInfoUpdater(turnLabel, statusLabel, lastMoveLabel,
                playerLabel, opponentPlayerLabel, playerNameLabel, opponentNameLabel,
                playerColor, aiPlayer);

        // Khởi tạo game action handler
        gameActionHandler = new GameActionHandler(statusLabel, chatManager, peerNetworkHandler,
                playerColor, this::endGame, () -> disableGameButtons(false));

        setupEventHandlers();

        // Reset UI về trạng thái ban đầu
        if (uiUpdater != null) {
            uiUpdater.reset();
            uiUpdater.updateTurnLabel(currentPlayer);
            uiUpdater.updatePlayerLabels();
        }
    }

    /**
     * Được gọi từ HomeController sau khi ghép trận để set gameId và tên đối thủ.
     */
    public void setGameInfo(String gameId, String opponentName, String playerName) {
        this.gameId = gameId;
        this.opponentName = opponentName;
        this.playerName = playerName;
        if (uiUpdater != null) {
            uiUpdater.updatePlayerInfo(playerName, opponentName);
        }
    }

    /**
     * Thiết lập chế độ chơi với máy.
     *
     * @param difficulty 1 = dễ, 2 = trung bình, 3 = khó
     * @param humanColor màu quân của người chơi (thường là TRẮNG)
     */
    public void setupVsComputer(int difficulty, Piece.Color humanColor) {
        this.playerColor = humanColor;
        Piece.Color computerColor = (humanColor == Piece.Color.WHITE) ? Piece.Color.BLACK : Piece.Color.WHITE;
        this.aiPlayer = new AIPlayer(board, gameLogic, computerColor, difficulty);

        // Chơi với máy thì không cho cầu hòa
        if (drawButton != null) {
            drawButton.setDisable(true);
            drawButton.setVisible(false);
        }

        // Cập nhật lại UI
        if (uiUpdater != null) {
            uiUpdater.setPlayerColor(playerColor);
            uiUpdater.setAiPlayer(aiPlayer);
            uiUpdater.updatePlayerLabels();
            uiUpdater.updatePlayerInfo(playerName, opponentName);
        }
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
        if (uiUpdater != null) {
            uiUpdater.setPlayerColor(color);
            uiUpdater.updatePlayerLabels();
        }
        if (boardView != null) {
            boardView.setPlayerColor(color);
            boardView.refreshBoard();
        }
    }

    /**
     * Socket P2P đã được thiết lập giữa hai client (LAN).
     */
    public void setPeerSocket(Socket socket) {
        if (peerNetworkHandler != null) {
            peerNetworkHandler.setPeerSocket(socket);
        }
    }

    /**
     * Thiết lập các callbacks cho network handler.
     */
    private void setupNetworkCallbacks() {
        peerNetworkHandler.setOnMoveReceived((fromRow, fromCol, toRow, toCol) -> {
            Piece piece = board.getPiece(fromRow, fromCol);
            if (piece == null)
                return;

            Move move = new Move(fromRow, fromCol, toRow, toCol, piece);
            executeMove(move, true); // true = từ network
        });

        peerNetworkHandler.setOnChatReceived(message -> {
            if (chatManager != null) {
                chatManager.addChatMessage(
                        opponentName != null ? opponentName : "Đối thủ",
                        message,
                        false);
            }
        });

        peerNetworkHandler.setOnGameActionReceived(action -> {
            if (gameActionHandler != null) {
                gameActionHandler.handleGameAction(action);
            }
        });
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

        if (!fromNetwork && peerNetworkHandler != null) {
            peerNetworkHandler.sendMove(move);
        }

        currentPlayer = currentPlayer == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;

        // Cập nhật UI nước đi
        if (uiUpdater != null) {
            uiUpdater.updateLastMoveLabel(move);
        }

        // Kiểm tra trạng thái game
        GameStateChecker.GameStateResult stateResult = gameStateChecker.checkGameState(currentPlayer);
        handleGameStateResult(stateResult);

        // Cập nhật UI
        if (uiUpdater != null) {
            uiUpdater.updateTurnLabel(currentPlayer);
        }
        if (boardView != null) {
            boardView.setCurrentPlayer(currentPlayer);
            boardView.setLastMove(move);
            boardView.refreshBoard();
        }

        // Nếu đang chơi với máy và đến lượt AI -> AI tự động đi sau một khoảng trễ nhỏ
        if (aiPlayer != null && currentPlayer == aiPlayer.getAiColor()) {
            new Thread(() -> {
                try {
                    Thread.sleep(600); // delay ~0.6s cho tự nhiên
                } catch (InterruptedException ignored) {
                }
                Platform.runLater(this::makeComputerMove);
            }).start();
        }
    }

    private void handleGameStateResult(GameStateChecker.GameStateResult result) {
        if (uiUpdater != null) {
            uiUpdater.updateStatusLabel(gameStateChecker.getStatusText(result));
        }

        if (result.getState() == GameStateChecker.GameStateResult.State.NORMAL ||
                result.getState() == GameStateChecker.GameStateResult.State.CHECK) {
            return; // Game tiếp tục
        }

        // Game kết thúc
        disableGameButtons(false);
        endGame(result.getWinner());
    }

    // ===================== GAME ACTIONS =====================
    private void offerDraw() {
        if (gameActionHandler != null) {
            gameActionHandler.offerDraw();
        }
    }

    private void resign() {
        if (gameActionHandler != null) {
            gameActionHandler.resign();
        }
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
        if (aiPlayer == null)
            return;

        try {
            Move chosen = aiPlayer.makeMove();
            if (chosen == null) {
                // Không còn nước đi hợp lệ -> kiểm tra chiếu hết / hòa
                Piece.Color aiColor = aiPlayer.getAiColor();
                GameStateChecker.GameStateResult result = gameStateChecker.checkGameState(aiColor);
                if (result.getState() != GameStateChecker.GameStateResult.State.NORMAL &&
                        result.getState() != GameStateChecker.GameStateResult.State.CHECK) {
                    endGame(result.getWinner());
                }
                return;
            }

            executeMove(chosen, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}