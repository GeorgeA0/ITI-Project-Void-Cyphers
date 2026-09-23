package com.iwish.client;
import com.iwish.client.service.ClientService;
import com.iwish.client.service.MockClientService;
import com.iwish.client.service.SocketClientService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
public final class AppContext {

    private static final String SERVER_HOST = System.getProperty("iwish.host", "localhost");
    private static final int SERVER_PORT = Integer.getInteger("iwish.port", 5050);

    private static final ClientService service = createService();
    private static Stage primaryStage;

    /**
     * Tries to connect to the real i-Wish server first; if it isn't running
     * (e.g. while working on the GUI alone), transparently falls back to the
     * in-memory MockClientService so the app still starts and is demoable.
     */
    private static ClientService createService() {
        try {
            ClientService socketService = new SocketClientService(SERVER_HOST, SERVER_PORT);
            System.out.println("[AppContext] Connected to i-Wish server at " + SERVER_HOST + ":" + SERVER_PORT);
            return socketService;
        } catch (IOException e) {
            System.out.println("[AppContext] Could not reach the i-Wish server (" + e.getMessage()
                    + ") - using in-memory mock data instead.");
            return new MockClientService();
        }
    }
    private AppContext() {}
    public static ClientService getService() {
        return service;
    }
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    public static void switchScene(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(AppContext.class.getResource("/com/iwish/client/fxml/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(AppContext.class.getResource("/com/iwish/client/css/style.css").toExternalForm());
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Could not load screen: " + fxmlFile, e);}}}
