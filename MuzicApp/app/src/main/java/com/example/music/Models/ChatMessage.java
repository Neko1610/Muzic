package com.example.music.Models;

import java.util.List;

public class ChatMessage {

    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    public static final int TYPE_SONG = 2;

    public int type;
    public String message;
    public List<Song> songs;

    public ChatMessage(int type, String message){
        this.type = type;
        this.message = message;
    }

    public ChatMessage(List<Song> songs){
        this.type = TYPE_SONG;
        this.songs = songs;
    }
}