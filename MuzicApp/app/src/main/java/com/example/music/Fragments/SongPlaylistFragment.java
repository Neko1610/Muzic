package com.example.music.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Models.Playlist;
import com.example.music.Models.Song;
import com.example.music.R;
import com.example.music.SongAdapter;

import java.util.ArrayList;
import java.util.List;

public class SongPlaylistFragment extends Fragment {

    private Playlist playlist;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_playlist, container, false);

        if (getArguments() != null) {
            playlist = (Playlist) getArguments().getSerializable("playlist");
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewSongsInPlaylist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Song> songs = new ArrayList<>();
        if (playlist != null && playlist.getSongs() != null) {
            // loại bỏ các phần tử null
            for (Song s : playlist.getSongs()) {
                if (s != null) songs.add(s);
            }
        } else {
            Log.e("SongPlaylistFragment", "Playlist hoặc danh sách songs null!");
        }

        SongAdapter adapter = new SongAdapter(getContext(), songs);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
