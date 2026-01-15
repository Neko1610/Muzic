package com.example.music.Models;

import java.io.Serializable;
import java.util.List;

public class Album implements Serializable {  // 👈 THÊM implements Serializable

    private String id;
    private String name;
    private String imageUrl;
    private List<String> songIds;

    public Album() {}

    public Album(String id, String name, String imageUrl, List<String> songIds) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.songIds = songIds;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getSongIds() { return songIds; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSongIds(List<String> songIds) { this.songIds = songIds; }
}
