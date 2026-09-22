package com.iwish.client.controller;
import com.iwish.client.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
public class MainController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private void initialize() {
        welcomeLabel.setText("Hi, " + AppContext.getService().getCurrentUsername() + "!");
    }
    @FXML
    private void handleLogout() {
        AppContext.getService().logout();
        AppContext.switchScene("login.fxml", "i-Wish - Sign in");}}
