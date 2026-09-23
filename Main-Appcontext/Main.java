package com.iwish.client;
import javafx.application.Application;
import javafx.stage.Stage;
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        AppContext.setPrimaryStage(primaryStage);
        AppContext.switchScene("login.fxml", "i-Wish - Sign in");
        primaryStage.setMinWidth(480);
        primaryStage.setMinHeight(420);
        primaryStage.show();}
    public static void main(String[] args) {
        launch(args);
    }}
