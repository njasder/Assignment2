package com.example.claire.assignment2;

/**
 * Created by claire on 28/9/17.
 * This is type for sub-type list items.
 */

public class SubItem {
    private String type;
    private String subType;
    private int imageFile;

    public SubItem(String type, String subType, int imageFile) {
        this.type = type;
        this.subType = subType;
        this.imageFile = imageFile;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getSubType() {
        return subType;
    }

    public int getImageFile() {
        return imageFile;
    }
}
