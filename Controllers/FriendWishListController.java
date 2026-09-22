package com.iwish.client.controller;
import com.iwish.client.AppContext;
import com.iwish.client.model.WishItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
public class FriendWishListController {
    @FXML
    private Label titleLabel;
    @FXML
    private ListView<WishItem> itemsListView;
    @FXML
    private TextField amountField;
    @FXML
    private Label statusLabel;
    private final ObservableList<WishItem> items = FXCollections.observableArrayList();
    private String friendUsername;
    @FXML
    private void initialize() {
        itemsListView.setItems(items);
        itemsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(WishItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item.isFullyFunded()) {
                    setText(item.getName() + " - $" + item.getPrice() + "  (fully funded, thank you!)");
                } else {
                    setText(String.format("%s - $%.2f  ($%.2f still needed)",
                            item.getName(), item.getPrice(), item.getRemainingAmount()));
                }}
        });}
    public void setFriend(String friendUsername) {
        this.friendUsername = friendUsername;
        titleLabel.setText(friendUsername + "'s Wish List");
        refresh();}
    private void refresh() {
        items.setAll(AppContext.getService().getFriendWishList(friendUsername));
    }
    @FXML
    private void handleContribute() {
        WishItem selected = itemsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select an item first.");
            return;}
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            showStatus("Enter a valid amount.");
            return;}
        boolean ok = AppContext.getService().contribute(selected.getId(), amount);
        if (ok) {
            amountField.clear();
            refresh();
            showStatus("Thanks! Your contribution was recorded.");
        } else {
            showStatus("Could not record that contribution.");}}
    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);}}
