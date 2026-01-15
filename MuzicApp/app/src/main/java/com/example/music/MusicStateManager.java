package com.example.music;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music.Models.Song;

import java.util.ArrayList;
import java.util.List;

public class MusicStateManager {

    private static MusicStateManager instance;

    private final MutableLiveData<Song> currentSong = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);

    // 🆕 Danh sách bài hát & index hiện tại
    private List<Song> playlist = new ArrayList<>();
    private int currentIndex = -1;

    private MusicStateManager() {}

    public static synchronized MusicStateManager getInstance() {
        if (instance == null) {
            instance = new MusicStateManager();
        }
        return instance;
    }

    public LiveData<Song> getCurrentSong() {
        return currentSong;
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public void setSong(Song song) {
        currentSong.postValue(song);
    }

    public void setPlaying(boolean playing) {
        isPlaying.postValue(playing);
    }

    // 🆕 Thiết lập danh sách nhạc và bài hiện tại
    public void setPlaylist(List<Song> songs, int index) {
        if (songs != null) {
            this.playlist = new ArrayList<>(songs);
            this.currentIndex = index;
        }
    }

    public List<Song> getPlaylist() {
        return playlist;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }

    // 🆕 Lấy bài kế tiếp
    public Song getNextSong() {
        if (playlist == null || playlist.isEmpty()) return null;
        currentIndex = (currentIndex + 1) % playlist.size();
        return playlist.get(currentIndex);
    }

    // 🆕 Lấy bài trước đó
    public Song getPreviousSong() {
        if (playlist == null || playlist.isEmpty()) return null;
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        return playlist.get(currentIndex);
    }
}
