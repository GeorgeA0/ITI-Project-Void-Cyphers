package com.iwish.client.controller;
import com.iwish.client.AppContext;
import com.iwish.client.model.Notification;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
public class NotificationsController {
    @FXML
    private ListView<Notification> notificationsListView;
    private final ObservableList<Notification> notifications = FXCollections.observableArrayList();
    @FXML
    private void initialize() {
        notificationsListView.setItems(notifications);
        refresh();}
    @FXML
    private void handleRefresh() {
        refresh();
    }
    private void refresh() {
        notifications.setAll(AppContext.getService().getNotifications());
    }}
