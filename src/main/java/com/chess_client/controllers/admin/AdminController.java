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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller cho trang quản trị admin với 3 tabs: Người dùng, Trận đấu, Xếp
 * hạng
 */
public class AdminController {

    // ==================== COMMON ====================
    @FXML
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

    // Views
    @FXML
    private VBox viewUsers;

    @FXML
    private VBox viewGames;

    @FXML
    private VBox viewRankings;

    // ==================== TAB NGƯỜI DÙNG ====================
    @FXML
    private TextField txtSearchUser;

    @FXML
    private Button btnSearchUser;

    @FXML
    private TableView<UserRow> tblUsers;

    @FXML
    private TableColumn<UserRow, Integer> colUserId;

    @FXML
    private TableColumn<UserRow, String> colUsername;

    @FXML
    private TableColumn<UserRow, String> colDisplayName;

    @FXML
    private TableColumn<UserRow, String> colEmail;

    @FXML
    private TableColumn<UserRow, String> colPhone;

    @FXML
    private TableColumn<UserRow, String> colStatus;

    @FXML
    private TableColumn<UserRow, Void> colUserActions;

    private ObservableList<UserRow> userList = FXCollections.observableArrayList();

    // ==================== TAB TRẬN ĐẤU ====================
    @FXML
    private ComboBox<String> cmbGameStatus;

    @FXML
    private Button btnFilterGames;

    @FXML
    private TableView<GameRow> tblGames;

    @FXML
    private TableColumn<GameRow, Integer> colGameId;

    @FXML
    private TableColumn<GameRow, String> colWhitePlayer;

    @FXML
    private TableColumn<GameRow, String> colBlackPlayer;

    @FXML
    private TableColumn<GameRow, String> colGameMode;

    @FXML
    private TableColumn<GameRow, String> colGameStatus;

    @FXML
    private TableColumn<GameRow, String> colWinner;

    private ObservableList<GameRow> gameList = FXCollections.observableArrayList();

    // ==================== TAB XẾP HẠNG ====================
    @FXML
    private TableView<RankingRow> tblRankings;

    @FXML
    private TableColumn<RankingRow, Integer> colRank;

    @FXML
    private TableColumn<RankingRow, String> colRankUsername;

    @FXML
    private TableColumn<RankingRow, String> colRankDisplayName;

    @FXML
    private TableColumn<RankingRow, Integer> colGamesPlayed;

    @FXML
    private TableColumn<RankingRow, Integer> colWins;

    @FXML
    private TableColumn<RankingRow, Integer> colLosses;

    @FXML
    private TableColumn<RankingRow, Integer> colDraws;

    @FXML
    private TableColumn<RankingRow, Integer> colScore;

    private ObservableList<RankingRow> rankingList = FXCollections.observableArrayList();

    // Handlers
    private StatsHandler statsHandler;
    private UserTabHandler userTabHandler;
    private GameTabHandler gameTabHandler;
    private RankingTabHandler rankingTabHandler;

    @FXML
    public void initialize() {
        // Initialize handlers
        statsHandler = new StatsHandler(lblStats);
        userTabHandler = new UserTabHandler(userList, this::showAlertMessage, statsHandler::loadSystemStats);
        gameTabHandler = new GameTabHandler(gameList, this::showAlertMessage);
        rankingTabHandler = new RankingTabHandler(rankingList, this::showAlertMessage);

        // Setup tables
        setupUserTable();
        setupGameTable();
        setupRankingTable();

        // Load initial data
        statsHandler.loadSystemStats();
        userTabHandler.loadUsers("");
        gameTabHandler.loadGames("Tất cả");
        rankingTabHandler.loadRankings();

        // Common buttons
        btnRefreshAll.setOnAction(e -> handleRefreshAll());
        btnLogout.setOnAction(e -> handleLogout());

        // User tab buttons
        btnSearchUser.setOnAction(e -> handleSearchUser());
        txtSearchUser.setOnAction(e -> handleSearchUser());

        // Game tab buttons
        cmbGameStatus.getItems().addAll("Tất cả", "waiting", "playing", "finished");
        cmbGameStatus.setValue("Tất cả");
        btnFilterGames.setOnAction(e -> handleFilterGames());

        // Show users view by default
        showView("users");
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
        // Hide all views
        viewUsers.setVisible(false);
        viewUsers.setManaged(false);
        viewGames.setVisible(false);
        viewGames.setManaged(false);
        viewRankings.setVisible(false);
        viewRankings.setManaged(false);

        // Reset menu styles
        resetMenuStyles();

        // Show selected view and highlight menu
        switch (viewName) {
            case "users":
                viewUsers.setVisible(true);
                viewUsers.setManaged(true);
                lblTitle.setText("Quản lý Người dùng");
                highlightMenu(menuUsers);
                break;
            case "games":
                viewGames.setVisible(true);
                viewGames.setManaged(true);
                lblTitle.setText("Quản lý Trận đấu");
                highlightMenu(menuGames);
                break;
            case "rankings":
                viewRankings.setVisible(true);
                viewRankings.setManaged(true);
                lblTitle.setText("Quản lý Xếp hạng");
                highlightMenu(menuRankings);
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
        userTabHandler.setCurrentPage(1);
        userTabHandler.loadUsers(txtSearchUser.getText().trim());
    }

    // ==================== GAME HANDLERS ====================

    private void handleFilterGames() {
        gameTabHandler.loadGames(cmbGameStatus.getValue());
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
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Chess - Đăng nhập");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
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
