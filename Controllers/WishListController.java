package com.iwish.client.controller;
import com.iwish.client.AppContext;
import com.iwish.client.model.WishItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
public class WishListController {
    @FXML
    private ListView<WishItem> itemsListView;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private Label statusLabel;
    private final ObservableList<WishItem> items = FXCollections.observableArrayList();
    @FXML
    private void initialize() {
        itemsListView.setItems(items);
        itemsListView.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(WishItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String funded = item.isFullyFunded() ? "  (fully funded!)"
                            : String.format("  ($%.2f of $%.2f funded)", item.getContributedAmount(), item.getPrice());
                    setText(item.getName() + " - $" + item.getPrice() + funded);
                }}
        });
        itemsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                nameField.setText(newItem.getName());
                priceField.setText(String.valueOf(newItem.getPrice()));
            }});
        refresh();}
    private void refresh() {
        items.setAll(AppContext.getService().getMyWishList());
    }
    @FXML
    private void handleAdd() {
        Double price = parsePrice();
        String name = nameField.getText().trim();
        if (name.isEmpty() || price == null) {
            showStatus("Enter a valid name and price.");
            return;}
        AppContext.getService().addWishItem(name, price);
        clearForm();
        refresh();}
    @FXML
    private void handleUpdate() {
        WishItem selected = itemsListView.getSelectionModel().getSelectedItem();
        Double price = parsePrice();
        String name = nameField.getText().trim();
        if (selected == null) {
            showStatus("Select an item to update first.");
            return;}
        if (name.isEmpty() || price == null) {
            showStatus("Enter a valid name and price.");
            return;}
        AppContext.getService().updateWishItem(selected.getId(), name, price);
        clearForm();
        refresh();}
    @FXML
    private void handleDelete() {
        WishItem selected = itemsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select an item to delete first.");
            return;}
        AppContext.getService().deleteWishItem(selected.getId());
        clearForm();
        refresh();}
    private Double parsePrice() {
        try {
            double value = Double.parseDouble(priceField.getText().trim());
            return value >= 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;}}
    private void clearForm() {
        nameField.clear();
        priceField.clear();
        itemsListView.getSelectionModel().clearSelection();
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);}
    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);}}
