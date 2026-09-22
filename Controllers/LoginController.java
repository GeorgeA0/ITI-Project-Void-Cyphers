package com.iwish.client.controller;
import com.iwish.client.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        boolean ok = AppContext.getService().login(username, password);
        if (ok) {
            AppContext.switchScene("main.fxml", "i-Wish - " + username);
        } else {
            showError("Wrong username or password.");}}
    @FXML
    private void goToRegister() {
        AppContext.switchScene("register.fxml", "i-Wish - Create account");
    }
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);}}
