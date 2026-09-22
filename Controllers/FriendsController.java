package com.iwish.client.controller;
import com.iwish.client.AppContext;
import com.iwish.client.model.FriendRequest;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
public class FriendsController {
    @FXML
    private ListView<String> friendsListView;
    @FXML
    private ListView<FriendRequest> requestsListView;
    @FXML
    private TextField addFriendField;
    @FXML
    private Label statusLabel;
    private final ObservableList<String> friends = FXCollections.observableArrayList();
    private final ObservableList<FriendRequest> requests = FXCollections.observableArrayList();
    @FXML
    private void initialize() {
        friendsListView.setItems(friends);
        requestsListView.setItems(requests);
        refresh();}
    private void refresh() {
        friends.setAll(AppContext.getService().getFriends());
        requests.setAll(AppContext.getService().getPendingRequests());}
    @FXML
    private void handleSendRequest() {
        String username = addFriendField.getText().trim();
        if (username.isEmpty()) {
            showStatus("Enter a username first.");
            return;}
        boolean ok = AppContext.getService().sendFriendRequest(username);
        if (ok) {
            addFriendField.clear();
            showStatus("Friend request sent to " + username + ".");
        } else {
            showStatus("Could not send a request to that user.");}}
    @FXML
    private void handleAccept() {
        FriendRequest selected = requestsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a request first.");
            return;}
        AppContext.getService().acceptFriendRequest(selected.getFromUsername());
        refresh();}
    @FXML
    private void handleDecline() {
        FriendRequest selected = requestsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a request first.");
            return;}
        AppContext.getService().declineFriendRequest(selected.getFromUsername());
        refresh();}
    @FXML
    private void handleRemove() {
        String selected = friendsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a friend first.");
            return;}
        AppContext.getService().removeFriend(selected);
        refresh();}
    @FXML
    private void handleViewWishList() {
        String selected = friendsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a friend first.");
            return;}
        openFriendWishList(selected);}
    private void openFriendWishList(String friendUsername) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/iwish/client/fxml/friend_wishlist.fxml"));
            Parent root = loader.load();
            FriendWishListController controller = loader.getController();
            controller.setFriend(friendUsername);
            Stage popup = new Stage();
            popup.setTitle(friendUsername + "'s Wish List");
            popup.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/iwish/client/css/style.css").toExternalForm());
            popup.setScene(scene);
            popup.showAndWait();
            refresh();
        } catch (IOException e) {
            showStatus("Could not open that wish list.");}}
    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);}}
