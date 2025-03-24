package com.example.stayconnect.models;

public class ModelCategory {

    String category;
    int icon;

    public ModelCategory() {
    }

    public ModelCategory(String category, int icon) {
        this.category = category;
        this.icon = icon;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
