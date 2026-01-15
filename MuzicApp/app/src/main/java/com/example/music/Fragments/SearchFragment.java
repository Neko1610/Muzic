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

import com.example.music.Models.Song;
import com.example.music.R;
import com.example.music.SongAdapter;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerViewSearch;
    private SongAdapter searchAdapter;
    private List<Song> allSongs = new ArrayList<>();
    private List<Song> filteredSongs = new ArrayList<>();
    private DatabaseReference dbRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerViewSearch = view.findViewById(R.id.recyclerViewSearch);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new SongAdapter(getContext(), filteredSongs);
        recyclerViewSearch.setAdapter(searchAdapter);

        dbRef = FirebaseDatabase.getInstance().getReference();
        loadAllSongs();

        return view;
    }

    private void loadAllSongs() {
        dbRef.child("songs").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                allSongs.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Song s = snap.getValue(Song.class);
                    if (s != null) allSongs.add(s);
                }
                filteredSongs.clear();
                filteredSongs.addAll(allSongs);
                searchAdapter.updateList(filteredSongs);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", error.getMessage());
            }
        });
    }

    public void filterSongs(String query) {
        filteredSongs.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredSongs.addAll(allSongs);
        } else {
            for (Song s : allSongs) {
                if (s.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        s.getArtist().toLowerCase().contains(query.toLowerCase())) {
                    filteredSongs.add(s);
                }
            }
        }
        searchAdapter.updateList(filteredSongs);
    }
}
