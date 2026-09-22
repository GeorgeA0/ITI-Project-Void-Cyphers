package com.iwish.client.model;
import java.io.Serializable;
public class WishItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private double price;
    private double contributedAmount;
    private String ownerUsername;
    public WishItem(int id, String name, double price, double contributedAmount, String ownerUsername) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.contributedAmount = contributedAmount;
        this.ownerUsername = ownerUsername;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public double getContributedAmount() {
        return contributedAmount;
    }
    public void addContribution(double amount) {
        this.contributedAmount += amount;
    }
    public String getOwnerUsername() {
        return ownerUsername;
    }
    public boolean isFullyFunded() {
        return contributedAmount >= price;
    }
    public double getRemainingAmount() {
        return Math.max(0, price - contributedAmount);
    }
    @Override
    public String toString() {
        return name + " - $" + price;
    }}
