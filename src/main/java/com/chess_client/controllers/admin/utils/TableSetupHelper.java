package com.chess_client.controllers.admin.utils;

import com.chess_client.controllers.admin.models.GameRow;
import com.chess_client.controllers.admin.models.RankingRow;
import com.chess_client.controllers.admin.models.UserRow;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

/**
 * Helper class để setup các bảng trong admin panel
 */
public class TableSetupHelper {

    public static void setupUserTable(
            TableView<UserRow> tblUsers,
            TableColumn<UserRow, Integer> colUserId,
            TableColumn<UserRow, String> colUsername,
            TableColumn<UserRow, String> colDisplayName,
            TableColumn<UserRow, String> colEmail,
            TableColumn<UserRow, String> colPhone,
            TableColumn<UserRow, String> colStatus,
            TableColumn<UserRow, Void> colUserActions,
            java.util.function.Consumer<UserRow> onEditUser,
            java.util.function.Consumer<UserRow> onDeleteUser) {

        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colDisplayName.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colUserActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");
            private final HBox hbox = new HBox(5, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #4a9eff; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                btnEdit.setOnAction(e -> {
                    UserRow user = getTableView().getItems().get(getIndex());
                    if (onEditUser != null && user != null) {
                        onEditUser.accept(user);
                    }
                });

                btnDelete.setOnAction(e -> {
                    UserRow user = getTableView().getItems().get(getIndex());
                    if (onDeleteUser != null && user != null) {
                        onDeleteUser.accept(user);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }

    public static void setupGameTable(
            TableView<GameRow> tblGames,
            TableColumn<GameRow, Integer> colGameId,
            TableColumn<GameRow, String> colWhitePlayer,
            TableColumn<GameRow, String> colBlackPlayer,
            TableColumn<GameRow, String> colGameMode,
            TableColumn<GameRow, String> colGameStatus,
            TableColumn<GameRow, String> colWinner) {

        colGameId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colWhitePlayer.setCellValueFactory(new PropertyValueFactory<>("whitePlayer"));
        colBlackPlayer.setCellValueFactory(new PropertyValueFactory<>("blackPlayer"));
        colGameMode.setCellValueFactory(new PropertyValueFactory<>("mode"));
        colGameStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colWinner.setCellValueFactory(new PropertyValueFactory<>("winner"));
    }

    public static void setupRankingTable(
            TableView<RankingRow> tblRankings,
            TableColumn<RankingRow, Integer> colRank,
            TableColumn<RankingRow, String> colRankUsername,
            TableColumn<RankingRow, String> colRankDisplayName,
            TableColumn<RankingRow, Integer> colGamesPlayed,
            TableColumn<RankingRow, Integer> colWins,
            TableColumn<RankingRow, Integer> colLosses,
            TableColumn<RankingRow, Integer> colDraws,
            TableColumn<RankingRow, Integer> colScore) {

        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colRankUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRankDisplayName.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        colGamesPlayed.setCellValueFactory(new PropertyValueFactory<>("gamesPlayed"));
        colWins.setCellValueFactory(new PropertyValueFactory<>("wins"));
        colLosses.setCellValueFactory(new PropertyValueFactory<>("losses"));
        colDraws.setCellValueFactory(new PropertyValueFactory<>("draws"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
    }
}
