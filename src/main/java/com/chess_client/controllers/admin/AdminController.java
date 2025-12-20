package com.chess_client.controllers.admin;

import com.chess_client.controllers.admin.handlers.*;
import com.chess_client.controllers.admin.models.GameRow;
import com.chess_client.controllers.admin.models.RankingRow;
import com.chess_client.controllers.admin.models.UserRow;
import com.chess_client.controllers.admin.utils.TableSetupHelper;
import com.chess_client.services.AuthService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller cho trang quản trị admin với 3 tabs: Người dùng, Trận đấu, Xếp
 * hạng
 */
public class AdminController {

    // ==================== COMMON ====================
    private Label lblStats;

    @FXML
    private Label lblTitle;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnRefreshAll;

    // Menu items
    @FXML
    private VBox menuUsers;

    @FXML
    private VBox menuGames;

    @FXML
    private VBox menuRankings;

    // Content area
    @FXML
    private StackPane contentArea;

    // Views (loaded dynamically)
    private VBox viewUsers;
    private VBox viewGames;
    private VBox viewRankings;

    // ==================== TAB NGƯỜI DÙNG ====================
    private TextField txtSearchUser;
    private Button btnSearchUser;
    private TableView<UserRow> tblUsers;
    private TableColumn<UserRow, Integer> colUserId;
    private TableColumn<UserRow, String> colUsername;
    private TableColumn<UserRow, String> colDisplayName;
    private TableColumn<UserRow, String> colEmail;
    private TableColumn<UserRow, String> colPhone;
    private TableColumn<UserRow, String> colStatus;
    private TableColumn<UserRow, Void> colUserActions;

    private ObservableList<UserRow> userList = FXCollections.observableArrayList();

    // ==================== TAB TRẬN ĐẤU ====================
    private ComboBox<String> cmbGameStatus;
    private Button btnFilterGames;
    private TableView<GameRow> tblGames;
    private TableColumn<GameRow, Integer> colGameId;
    private TableColumn<GameRow, String> colWhitePlayer;
    private TableColumn<GameRow, String> colBlackPlayer;
    private TableColumn<GameRow, String> colGameMode;
    private TableColumn<GameRow, String> colGameStatus;
    private TableColumn<GameRow, String> colWinner;

    private ObservableList<GameRow> gameList = FXCollections.observableArrayList();

    // ==================== TAB XẾP HẠNG ====================
    private TableView<RankingRow> tblRankings;
    private TableColumn<RankingRow, Integer> colRank;
    private TableColumn<RankingRow, String> colRankUsername;
    private TableColumn<RankingRow, String> colRankDisplayName;
    private TableColumn<RankingRow, Integer> colGamesPlayed;
    private TableColumn<RankingRow, Integer> colWins;
    private TableColumn<RankingRow, Integer> colLosses;
    private TableColumn<RankingRow, Integer> colDraws;
    private TableColumn<RankingRow, Integer> colScore;

    private ObservableList<RankingRow> rankingList = FXCollections.observableArrayList();

    // Handlers
    private StatsHandler statsHandler;
    private UserTabHandler userTabHandler;
    private GameTabHandler gameTabHandler;
    private RankingTabHandler rankingTabHandler;

    @FXML
    public void initialize() {
        // Common buttons
        btnRefreshAll.setOnAction(e -> handleRefreshAll());
        btnLogout.setOnAction(e -> handleLogout());

        // Load views first to inject components
        loadViews();

        // Initialize handlers after views are loaded (lblStats is injected)
        statsHandler = new StatsHandler(lblStats);
        userTabHandler = new UserTabHandler(userList, this::showAlertMessage, statsHandler::loadSystemStats);
        gameTabHandler = new GameTabHandler(gameList, this::showAlertMessage);
        rankingTabHandler = new RankingTabHandler(rankingList, this::showAlertMessage);

        // Setup tables
        setupUserTable();
        setupGameTable();
        setupRankingTable();

        // Load initial data
        if (statsHandler != null) {
            statsHandler.loadSystemStats();
        }
        userTabHandler.loadUsers("");
        gameTabHandler.loadGames("Tất cả");
        rankingTabHandler.loadRankings();

        // Show users view by default
        showView("users");
    }

    private void loadViews() {
        try {
            // Load users view
            FXMLLoader usersLoader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/admin-users.fxml"));
            viewUsers = usersLoader.load();
            injectUserComponents(viewUsers);

            // Load games view
            FXMLLoader gamesLoader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/admin-games.fxml"));
            viewGames = gamesLoader.load();
            injectGameComponents(viewGames);

            // Load rankings view
            FXMLLoader rankingsLoader = new FXMLLoader(
                    getClass().getResource("/com/chess_client/fxml/admin-rankings.fxml"));
            viewRankings = rankingsLoader.load();
            injectRankingComponents(viewRankings);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải giao diện: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void injectUserComponents(Parent root) {
        txtSearchUser = (TextField) root.lookup("#txtSearchUser");
        btnSearchUser = (Button) root.lookup("#btnSearchUser");
        lblStats = (Label) root.lookup("#lblStats");
        tblUsers = (TableView<UserRow>) root.lookup("#tblUsers");

        // Get columns from TableView by ID
        if (tblUsers != null && !tblUsers.getColumns().isEmpty()) {
            for (TableColumn<UserRow, ?> col : tblUsers.getColumns()) {
                String id = col.getId();
                if (id != null) {
                    switch (id) {
                        case "colUserId":
                            colUserId = (TableColumn<UserRow, Integer>) col;
                            break;
                        case "colUsername":
                            colUsername = (TableColumn<UserRow, String>) col;
                            break;
                        case "colDisplayName":
                            colDisplayName = (TableColumn<UserRow, String>) col;
                            break;
                        case "colEmail":
                            colEmail = (TableColumn<UserRow, String>) col;
                            break;
                        case "colPhone":
                            colPhone = (TableColumn<UserRow, String>) col;
                            break;
                        case "colStatus":
                            colStatus = (TableColumn<UserRow, String>) col;
                            break;
                        case "colUserActions":
                            colUserActions = (TableColumn<UserRow, Void>) col;
                            break;
                    }
                }
            }
        }

        // Setup button handlers
        if (btnSearchUser != null) {
            btnSearchUser.setOnAction(e -> handleSearchUser());
        }
        if (txtSearchUser != null) {
            txtSearchUser.setOnAction(e -> handleSearchUser());
        }
    }

    @SuppressWarnings("unchecked")
    private void injectGameComponents(Parent root) {
        cmbGameStatus = (ComboBox<String>) root.lookup("#cmbGameStatus");
        btnFilterGames = (Button) root.lookup("#btnFilterGames");
        tblGames = (TableView<GameRow>) root.lookup("#tblGames");

        // Get columns from TableView by ID
        if (tblGames != null && !tblGames.getColumns().isEmpty()) {
            for (TableColumn<GameRow, ?> col : tblGames.getColumns()) {
                String id = col.getId();
                if (id != null) {
                    switch (id) {
                        case "colGameId":
                            colGameId = (TableColumn<GameRow, Integer>) col;
                            break;
                        case "colWhitePlayer":
                            colWhitePlayer = (TableColumn<GameRow, String>) col;
                            break;
                        case "colBlackPlayer":
                            colBlackPlayer = (TableColumn<GameRow, String>) col;
                            break;
                        case "colGameMode":
                            colGameMode = (TableColumn<GameRow, String>) col;
                            break;
                        case "colGameStatus":
                            colGameStatus = (TableColumn<GameRow, String>) col;
                            break;
                        case "colWinner":
                            colWinner = (TableColumn<GameRow, String>) col;
                            break;
                    }
                }
            }
        }

        // Setup button handlers
        if (cmbGameStatus != null) {
            cmbGameStatus.getItems().addAll("Tất cả", "waiting", "playing", "finished");
            cmbGameStatus.setValue("Tất cả");
        }
        if (btnFilterGames != null) {
            btnFilterGames.setOnAction(e -> handleFilterGames());
        }
    }

    @SuppressWarnings("unchecked")
    private void injectRankingComponents(Parent root) {
        tblRankings = (TableView<RankingRow>) root.lookup("#tblRankings");

        // Get columns from TableView by ID
        if (tblRankings != null && !tblRankings.getColumns().isEmpty()) {
            for (TableColumn<RankingRow, ?> col : tblRankings.getColumns()) {
                String id = col.getId();
                if (id != null) {
                    switch (id) {
                        case "colRank":
                            colRank = (TableColumn<RankingRow, Integer>) col;
                            break;
                        case "colRankUsername":
                            colRankUsername = (TableColumn<RankingRow, String>) col;
                            break;
                        case "colRankDisplayName":
                            colRankDisplayName = (TableColumn<RankingRow, String>) col;
                            break;
                        case "colGamesPlayed":
                            colGamesPlayed = (TableColumn<RankingRow, Integer>) col;
                            break;
                        case "colWins":
                            colWins = (TableColumn<RankingRow, Integer>) col;
                            break;
                        case "colLosses":
                            colLosses = (TableColumn<RankingRow, Integer>) col;
                            break;
                        case "colDraws":
                            colDraws = (TableColumn<RankingRow, Integer>) col;
                            break;
                        case "colScore":
                            colScore = (TableColumn<RankingRow, Integer>) col;
                            break;
                    }
                }
            }
        }
    }

    // ==================== MENU HANDLERS ====================

    @FXML
    private void handleMenuUsers() {
        showView("users");
    }

    @FXML
    private void handleMenuGames() {
        showView("games");
    }

    @FXML
    private void handleMenuRankings() {
        showView("rankings");
    }

    private void handleRefreshAll() {
        if (statsHandler != null) {
            statsHandler.loadSystemStats();
        }
        if (userTabHandler != null) {
            if (txtSearchUser != null) {
                userTabHandler.loadUsers(txtSearchUser.getText().trim());
            } else {
                userTabHandler.loadUsers("");
            }
        }
        if (gameTabHandler != null) {
            if (cmbGameStatus != null) {
                gameTabHandler.loadGames(cmbGameStatus.getValue());
            } else {
                gameTabHandler.loadGames("Tất cả");
            }
        }
        if (rankingTabHandler != null) {
            rankingTabHandler.loadRankings();
        }
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã làm mới dữ liệu");
    }

    private void showView(String viewName) {
        // Clear content area
        contentArea.getChildren().clear();

        // Reset menu styles
        resetMenuStyles();

        // Show selected view and highlight menu
        switch (viewName) {
            case "users":
                if (viewUsers != null) {
                    contentArea.getChildren().add(viewUsers);
                    lblTitle.setText("Quản lý Người dùng");
                    highlightMenu(menuUsers);
                }
                break;
            case "games":
                if (viewGames != null) {
                    contentArea.getChildren().add(viewGames);
                    lblTitle.setText("Quản lý Trận đấu");
                    highlightMenu(menuGames);
                }
                break;
            case "rankings":
                if (viewRankings != null) {
                    contentArea.getChildren().add(viewRankings);
                    lblTitle.setText("Quản lý Xếp hạng");
                    highlightMenu(menuRankings);
                }
                break;
        }
    }

    private void resetMenuStyles() {
        String defaultStyle = "-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 10; -fx-padding: 15; -fx-cursor: hand;";
        menuUsers.setStyle(defaultStyle);
        menuGames.setStyle(defaultStyle);
        menuRankings.setStyle(defaultStyle);
    }

    private void highlightMenu(VBox menu) {
        String highlightStyle = "-fx-background-color: rgba(74, 158, 255, 0.2); -fx-background-radius: 10; -fx-padding: 15; -fx-cursor: hand;";
        menu.setStyle(highlightStyle);
    }

    // ==================== SETUP TABLES ====================

    private void setupUserTable() {
        if (tblUsers == null) {
            return;
        }
        if (colUserId == null || colUsername == null || colDisplayName == null ||
                colEmail == null || colPhone == null || colStatus == null || colUserActions == null) {
            if (viewUsers != null) {
                injectUserComponents(viewUsers);
            }
            // Check again after re-injection
            if (colUserId == null || colUsername == null || colDisplayName == null ||
                    colEmail == null || colPhone == null || colStatus == null || colUserActions == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khởi tạo bảng người dùng");
                return;
            }
        }
        TableSetupHelper.setupUserTable(
                tblUsers,
                colUserId,
                colUsername,
                colDisplayName,
                colEmail,
                colPhone,
                colStatus,
                colUserActions,
                userTabHandler::handleEditUser,
                userTabHandler::handleDeleteUser);
        tblUsers.setItems(userList);
    }

    private void setupGameTable() {
        if (tblGames == null) {
            return;
        }
        if (colGameId == null || colWhitePlayer == null || colBlackPlayer == null ||
                colGameMode == null || colGameStatus == null || colWinner == null) {
            if (viewGames != null) {
                injectGameComponents(viewGames);
            }
            // Check again after re-injection
            if (colGameId == null || colWhitePlayer == null || colBlackPlayer == null ||
                    colGameMode == null || colGameStatus == null || colWinner == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khởi tạo bảng trận đấu");
                return;
            }
        }
        TableSetupHelper.setupGameTable(
                tblGames,
                colGameId,
                colWhitePlayer,
                colBlackPlayer,
                colGameMode,
                colGameStatus,
                colWinner);
        tblGames.setItems(gameList);
    }

    private void setupRankingTable() {
        if (tblRankings == null) {
            return;
        }
        if (colRank == null || colRankUsername == null || colRankDisplayName == null ||
                colGamesPlayed == null || colWins == null || colLosses == null ||
                colDraws == null || colScore == null) {
            if (viewRankings != null) {
                injectRankingComponents(viewRankings);
            }
            // Check again after re-injection
            if (colRank == null || colRankUsername == null || colRankDisplayName == null ||
                    colGamesPlayed == null || colWins == null || colLosses == null ||
                    colDraws == null || colScore == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khởi tạo bảng xếp hạng");
                return;
            }
        }
        // Remove any extra columns that don't match expected IDs
        if (tblRankings != null) {
            java.util.List<TableColumn<RankingRow, ?>> columnsToRemove = new java.util.ArrayList<>();
            for (TableColumn<RankingRow, ?> col : tblRankings.getColumns()) {
                String id = col.getId();
                if (id == null || (!id.equals("colRank") && !id.equals("colRankUsername") &&
                        !id.equals("colRankDisplayName") && !id.equals("colGamesPlayed") &&
                        !id.equals("colWins") && !id.equals("colLosses") &&
                        !id.equals("colDraws") && !id.equals("colScore"))) {
                    columnsToRemove.add(col);
                }
            }
            tblRankings.getColumns().removeAll(columnsToRemove);
        }

        TableSetupHelper.setupRankingTable(
                tblRankings,
                colRank,
                colRankUsername,
                colRankDisplayName,
                colGamesPlayed,
                colWins,
                colLosses,
                colDraws,
                colScore);
        tblRankings.setItems(rankingList);
    }

    // ==================== USER HANDLERS ====================

    private void handleSearchUser() {
        if (txtSearchUser != null) {
            userTabHandler.setCurrentPage(1);
            userTabHandler.loadUsers(txtSearchUser.getText().trim());
        }
    }

    // ==================== GAME HANDLERS ====================

    private void handleFilterGames() {
        if (cmbGameStatus != null) {
            gameTabHandler.loadGames(cmbGameStatus.getValue());
        }
    }

    // ==================== COMMON HANDLERS ====================

    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận đăng xuất");
        confirm.setHeaderText("Đăng xuất khỏi hệ thống");
        confirm.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        if (confirm.showAndWait().orElse(null) == ButtonType.OK) {
            AuthService.signOutSync();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chess_client/fxml/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                Scene scene = new Scene(root, 500, 600);
                stage.setScene(scene);
                stage.setTitle("Chess - Đăng nhập");
                stage.setResizable(false);
                stage.show();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể quay về màn hình đăng nhập");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlertMessage(String message) {
        showAlert(Alert.AlertType.ERROR, "Lỗi", message);
    }
}
