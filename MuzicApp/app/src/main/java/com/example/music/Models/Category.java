package com.example.music.Models;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Category implements Serializable {
    private String id;
    private String name;
    private String icon;

    // Constructor rỗng (Firebase yêu cầu)
    public Category() {}

    public Category(String id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon != null ? icon : "";
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @NonNull
    @Override
    public String toString() {
        return "Category{id='" + id + "', name='" + name + "', icon='" + icon + "'}";
    }
}
