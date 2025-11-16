package com.chess_client.controllers;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HomeController {

    @FXML
    private Button btnRandom;

    @FXML
    private Button btnComputer;

    @FXML
    private Button btnFriend;

    @FXML
    private Button btnLeaderboard;

    @FXML
    private Button btnProfile;

    @FXML
    private Button btnExit;

    @FXML
    private Label lblWelcome;

    @FXML
    private void handleRandomMatch() {
        System.out.println("Ghép ngẫu nhiên đã được chọn");
        // Thêm logic ghép ngẫu nhiên
    }

    @FXML
    private void handlePlayComputer() {
        System.out.println("Chơi với máy đã được chọn");
        // Thêm logic chơi với máy
    }

    @FXML
    private void handlePlayFriend() {
        System.out.println("Chơi với bạn bè đã được chọn");
        // Thêm logic chơi với bạn bè
    }

    @FXML
    private void handleLeaderboard() {
        System.out.println("Bảng xếp hạng đã được chọn");
        // Thêm logic hiển thị bảng xếp hạng
    }

    @FXML
    private void handleProfile() {
        System.out.println("Hồ sơ cá nhân đã được chọn");
        // Thêm logic hiển thị hồ sơ
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }
}
