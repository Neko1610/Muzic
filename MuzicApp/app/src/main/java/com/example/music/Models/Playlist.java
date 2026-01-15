package com.example.music.Models;

import java.io.Serializable;
import java.util.List;

public class Playlist implements Serializable {
    private String id;
    private String name;
    private String coverUrl;
    private List<Song> songs;

    public Playlist() {} // cần constructor rỗng cho Firebase

    public Playlist(String id, String name, String coverUrl, List<Song> songs) {
        this.id = id;
        this.name = name;
        this.coverUrl = coverUrl;
        this.songs = songs;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCoverUrl() { return coverUrl; }
    public List<Song> getSongs() { return songs; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public void setSongs(List<Song> songs) { this.songs = songs; }
}
