package com.chess_client.controllers.admin;

import com.chess_client.controllers.admin.handlers.*;
import com.chess_client.controllers.admin.models.GameRow;
import com.chess_client.controllers.admin.models.RankingRow;
import com.chess_client.controllers.admin.models.UserRow;
import com.chess_client.controllers.admin.utils.ComponentInjector;
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

import java.util.Map;

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
        userTabHandler = new UserTabHandler(userList,
                (type, msg) -> showAlert(type, type == Alert.AlertType.ERROR ? "Lỗi" : "Thông báo", msg),
                statsHandler::loadSystemStats);
        gameTabHandler = new GameTabHandler(gameList, msg -> showAlert(Alert.AlertType.ERROR, "Lỗi", msg));
        rankingTabHandler = new RankingTabHandler(rankingList, msg -> showAlert(Alert.AlertType.ERROR, "Lỗi", msg));

        // Setup tables
        setupUserTable();
        setupGameTable();
        setupRankingTable();

        // Load initial data
        statsHandler.loadSystemStats();
        userTabHandler.loadUsers("");
        gameTabHandler.loadGames("Tất cả");
        rankingTabHandler.loadRankings();

        // Show users view by default
        showView("users");
    }

    private void loadViews() {
        try {
            viewUsers = loadView("/com/chess_client/fxml/admin-users.fxml");
            injectUserComponents(viewUsers);

            viewGames = loadView("/com/chess_client/fxml/admin-games.fxml");
            injectGameComponents(viewGames);

            viewRankings = loadView("/com/chess_client/fxml/admin-rankings.fxml");
            injectRankingComponents(viewRankings);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải giao diện: " + e.getMessage());
        }
    }

    private VBox loadView(String resourcePath) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
        return loader.load();
    }

    @SuppressWarnings("unchecked")
    private void injectUserComponents(Parent root) {
        txtSearchUser = ComponentInjector.lookup(root, "txtSearchUser", TextField.class);
        btnSearchUser = ComponentInjector.lookup(root, "btnSearchUser", Button.class);
        lblStats = ComponentInjector.lookup(root, "lblStats", Label.class);
        tblUsers = ComponentInjector.lookup(root, "tblUsers", TableView.class);

        // Extract columns using helper
        if (tblUsers != null) {
            Map<String, TableColumn<UserRow, ?>> columnMap = ComponentInjector.extractTableColumns(tblUsers);
            colUserId = (TableColumn<UserRow, Integer>) columnMap.get("colUserId");
            colUsername = (TableColumn<UserRow, String>) columnMap.get("colUsername");
            colDisplayName = (TableColumn<UserRow, String>) columnMap.get("colDisplayName");
            colEmail = (TableColumn<UserRow, String>) columnMap.get("colEmail");
            colPhone = (TableColumn<UserRow, String>) columnMap.get("colPhone");
            colStatus = (TableColumn<UserRow, String>) columnMap.get("colStatus");
            colUserActions = (TableColumn<UserRow, Void>) columnMap.get("colUserActions");
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
        cmbGameStatus = ComponentInjector.lookup(root, "cmbGameStatus", ComboBox.class);
        btnFilterGames = ComponentInjector.lookup(root, "btnFilterGames", Button.class);
        tblGames = ComponentInjector.lookup(root, "tblGames", TableView.class);

        // Extract columns using helper
        if (tblGames != null) {
            Map<String, TableColumn<GameRow, ?>> columnMap = ComponentInjector.extractTableColumns(tblGames);
            colGameId = (TableColumn<GameRow, Integer>) columnMap.get("colGameId");
            colWhitePlayer = (TableColumn<GameRow, String>) columnMap.get("colWhitePlayer");
            colBlackPlayer = (TableColumn<GameRow, String>) columnMap.get("colBlackPlayer");
            colGameMode = (TableColumn<GameRow, String>) columnMap.get("colGameMode");
            colGameStatus = (TableColumn<GameRow, String>) columnMap.get("colGameStatus");
            colWinner = (TableColumn<GameRow, String>) columnMap.get("colWinner");
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
        tblRankings = ComponentInjector.lookup(root, "tblRankings", TableView.class);

        // Extract columns using helper
        if (tblRankings != null) {
            Map<String, TableColumn<RankingRow, ?>> columnMap = ComponentInjector.extractTableColumns(tblRankings);
            colRank = (TableColumn<RankingRow, Integer>) columnMap.get("colRank");
            colRankUsername = (TableColumn<RankingRow, String>) columnMap.get("colRankUsername");
            colRankDisplayName = (TableColumn<RankingRow, String>) columnMap.get("colRankDisplayName");
            colGamesPlayed = (TableColumn<RankingRow, Integer>) columnMap.get("colGamesPlayed");
            colWins = (TableColumn<RankingRow, Integer>) columnMap.get("colWins");
            colLosses = (TableColumn<RankingRow, Integer>) columnMap.get("colLosses");
            colDraws = (TableColumn<RankingRow, Integer>) columnMap.get("colDraws");
            colScore = (TableColumn<RankingRow, Integer>) columnMap.get("colScore");
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
        statsHandler.loadSystemStats();
        userTabHandler.loadUsers(txtSearchUser.getText().trim());
        gameTabHandler.loadGames(cmbGameStatus.getValue());
        rankingTabHandler.loadRankings();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã làm mới dữ liệu");
    }

    private void showView(String viewName) {
        contentArea.getChildren().clear();
        resetMenuStyles();

        VBox view = null;
        String title = "";
        VBox menu = null;

        switch (viewName) {
            case "users":
                view = viewUsers;
                title = "Quản lý Người dùng";
                menu = menuUsers;
                break;
            case "games":
                view = viewGames;
                title = "Quản lý Trận đấu";
                menu = menuGames;
                break;
            case "rankings":
                view = viewRankings;
                title = "Quản lý Xếp hạng";
                menu = menuRankings;
                break;
        }

        if (view != null) {
            contentArea.getChildren().add(view);
            lblTitle.setText(title);
            highlightMenu(menu);
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
        if (tblUsers == null || colUserId == null || colUsername == null || colDisplayName == null ||
                colEmail == null || colPhone == null || colStatus == null || colUserActions == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khởi tạo bảng người dùng");
            return;
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
        if (tblGames == null || colGameId == null || colWhitePlayer == null || colBlackPlayer == null ||
                colGameMode == null || colGameStatus == null || colWinner == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khởi tạo bảng trận đấu");
            return;
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
        if (tblRankings == null || colRank == null || colRankUsername == null || colRankDisplayName == null ||
                colGamesPlayed == null || colWins == null || colLosses == null ||
                colDraws == null || colScore == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khởi tạo bảng xếp hạng");
            return;
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
}
