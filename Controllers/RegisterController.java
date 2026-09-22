package com.iwish.client.controller;
import com.iwish.client.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
public class RegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmField;
    @FXML
    private Label errorLabel;
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;}
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;}
        boolean created = AppContext.getService().register(username, password);
        if (created) {
            AppContext.getService().login(username, password);
            AppContext.switchScene("main.fxml", "i-Wish - " + username);
        } else {
            showError("That username is already taken.");}}
    @FXML
    private void goToLogin() {
        AppContext.switchScene("login.fxml", "i-Wish - Sign in");
    }
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);}}
