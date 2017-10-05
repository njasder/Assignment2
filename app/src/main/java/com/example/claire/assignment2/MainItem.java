package com.example.claire.assignment2;

import android.graphics.drawable.Drawable;

/**
 * Created by claire on 22/9/17.
 * Type for main types objects.
 */

public class MainItem {
    private String type;
    private int imageFile;

    public MainItem(String type, int imageFile) {
        this.type = type;
        this.imageFile = imageFile;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getImageFile() {
        return imageFile;
    }
}
