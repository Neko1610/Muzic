package com.example.music.Models;

import java.io.Serializable;

public class Song implements Serializable {
    private String id;
    private String title;
    private String artist;
    private String coverUrl;
    private String mp3Url;
    private String category;  // 🔥 thêm trường thể loại

    public Song() {}

    public Song(String id, String title, String artist, String coverUrl, String mp3Url, String category) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.coverUrl = coverUrl;
        this.mp3Url = mp3Url;
        this.category = category;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getMp3Url() { return mp3Url; }
    public void setMp3Url(String mp3Url) { this.mp3Url = mp3Url; }

    public String getCategory() { return category != null ? category : ""; }
    public void setCategory(String category) { this.category = category; }

}
