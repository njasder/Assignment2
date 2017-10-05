package com.example.claire.assignment2;

/**
 * Created by claire on 28/9/17.
 * This class is type of each item
 * added to the system.
 */

public class Item {

    private int itemId;
    private String title;
    private String proDate;
    private String shelfLife;
    private String expDate;
    private String remainingDays;
    private String type;
    private String subType;
    private String image;

    public Item (int itemId, String title, String proDate, String shelfLife, String expDate, String remainingDays, String type, String subType, String image) {
        this.itemId = itemId;
        this.title = title;
        this.proDate = proDate;
        this.shelfLife = shelfLife;
        this.expDate = expDate;
        this.remainingDays = remainingDays;
        this.type = type;
        this.subType = subType;
        this.image = image;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProDate() {
        return proDate;
    }

    public void setProDate(String proDate) {
        this.proDate = proDate;
    }

    public String getShelfLife() {
        return shelfLife;
    }

    public void setShelfLife(String shelfLife) {
        this.shelfLife = shelfLife;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(String remainingDays) {
        this.remainingDays = remainingDays;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
