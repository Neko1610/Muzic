package com.example.microservice.model;

public class Song {

    private String artist;
    private String category;
    private String coverUrl;
    private String id;
    private String mp3Url;
    private String title;
    private int viewCount;

    // Firebase cần constructor rỗng
    public Song() {}

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMp3Url() { return mp3Url; }
    public void setMp3Url(String mp3Url) { this.mp3Url = mp3Url; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
}
