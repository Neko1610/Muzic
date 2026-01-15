package com.example.music.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.Normalizer;
import java.util.regex.Pattern;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.CategoryAdapter;
import com.example.music.PlaylistAdapter;
import com.example.music.SongAdapter;
import com.example.music.Models.Category;
import com.example.music.Models.Playlist;
import com.example.music.Models.Song;
import com.example.music.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewCategories, recyclerViewHot, recyclerViewPlaylist, recyclerViewRecently;
    private CategoryAdapter categoryAdapter;

    private PlaylistAdapter playlistAdapter;
    private SongAdapter hotAdapter, recentlyAdapter;
    private List<Category> categoryList;
    private List<Playlist> playlistList;
    private List<Song> hotList, recentlyList;

    // search
    private List<Song> allSongs = new ArrayList<>();
    private List<Song> filteredSongs = new ArrayList<>();
    private SongAdapter searchAdapter;
    private RecyclerView recyclerViewSearch;

    private DatabaseReference dbRef;

    // Auto scroll
    private Handler handler = new Handler(Looper.getMainLooper());
    private int hotIndex = 0;
    private Runnable hotScrollRunnable;
    private int playlistIndex = 0;
    private Runnable playlistScrollRunnable;
    private String removeVietnameseAccents(String str) {
        if (str == null) return "";
        String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").toLowerCase();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // ====== RecyclerView Category ======
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(getContext(), categoryList);
        recyclerViewCategories.setAdapter(categoryAdapter);

        // ====== RecyclerView Hot ======
        recyclerViewHot = view.findViewById(R.id.recyclerViewHotItem);
        recyclerViewHot.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        hotList = new ArrayList<>();
        hotAdapter = new SongAdapter(getContext(), hotList);
        recyclerViewHot.setAdapter(hotAdapter);

        // ====== RecyclerView Playlist ======
        recyclerViewPlaylist = view.findViewById(R.id.recyclerViewPlaylist);
        recyclerViewPlaylist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        playlistList = new ArrayList<>();
        playlistAdapter = new PlaylistAdapter(getContext(), playlistList);
        recyclerViewPlaylist.setAdapter(playlistAdapter);

        // ====== RecyclerView Recently ======
        recyclerViewRecently = view.findViewById(R.id.recyclerViewRecently);
        recyclerViewRecently.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recentlyList = new ArrayList<>();
        recentlyAdapter = new SongAdapter(getContext(), recentlyList);
        recyclerViewRecently.setAdapter(recentlyAdapter);

        // ====== RecyclerView Search Result ======
        recyclerViewSearch = view.findViewById(R.id.recyclerViewSearch);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new SongAdapter(getContext(), filteredSongs);
        recyclerViewSearch.setAdapter(searchAdapter);
        recyclerViewSearch.setVisibility(View.GONE); // ẩn lúc đầu

        // ====== View All ======
        TextView viewAllRecently = view.findViewById(R.id.tvViewAllRecently);
        TextView viewAllPlaylist = view.findViewById(R.id.tvViewAllPlaylist);

        viewAllRecently.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new RecentlyFragment())
                    .addToBackStack(null)
                    .commit();
        });

        viewAllPlaylist.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new PlaylistFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // ====== Firebase ======
        dbRef = FirebaseDatabase.getInstance().getReference();
        loadCategories();
        loadHot();
        loadPlaylists();
        loadRecently();
        loadSongsFromFirebase(); // load cho search

        return view;
    }

    // ================== LOAD DATA ==================
    private void loadCategories() {
        dbRef.child("categories").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Category c = snap.getValue(Category.class);
                    if (c != null) categoryList.add(c);
                }
                categoryAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", error.getMessage());
            }
        });
    }

    private void loadHot() {
        dbRef.child("hotRecommended").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                hotList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Song s = snap.getValue(Song.class);
                    if (s != null) hotList.add(s);
                }
                hotAdapter.notifyDataSetChanged();
                startAutoScrollHot();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadPlaylists() {
        dbRef.child("playlists").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                playlistList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Playlist p = snap.getValue(Playlist.class);
                    if (p != null) playlistList.add(p);
                }
                playlistAdapter.notifyDataSetChanged();
                startAutoScrollPlaylist();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadRecently() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        dbRef.child("users")
                .child(userId)
                .child("recentlyPlayed")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        recentlyList.clear();
                        HashSet<String> addedKeys = new HashSet<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Song s = snap.getValue(Song.class);
                            if (s != null) {
                                String key = (s.getId() != null && !s.getId().isEmpty())
                                        ? s.getId()
                                        : (s.getTitle() + "_" + s.getArtist());
                                if (!addedKeys.contains(key)) {
                                    addedKeys.add(key);
                                    recentlyList.add(s);
                                }
                            }
                        }
                        recentlyAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    private void loadSongsFromFirebase() {
        dbRef.child("songs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allSongs.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Song s = snap.getValue(Song.class);
                    if (s != null) allSongs.add(s);
                }
                filteredSongs.clear();
                filteredSongs.addAll(allSongs);
                searchAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ================== SEARCH FILTER ==================
    public void filterSongs(String query) {
        filteredSongs.clear();
        if (query == null || query.trim().isEmpty()) {
            showNormalUI();
        } else {
            String search = removeVietnameseAccents(query);
            for (Song s : allSongs) {
                String title = removeVietnameseAccents(s.getTitle());
                String artist = removeVietnameseAccents(s.getArtist());
                if (title.contains(search) || artist.contains(search)) {
                    filteredSongs.add(s);
                }
            }
            showSearchUI();
        }
        searchAdapter.notifyDataSetChanged();
    }

    // Hiện giao diện mặc định
    private void showNormalUI() {
        recyclerViewCategories.setVisibility(View.VISIBLE);
        recyclerViewHot.setVisibility(View.VISIBLE);
        recyclerViewPlaylist.setVisibility(View.VISIBLE);
        recyclerViewRecently.setVisibility(View.VISIBLE);

        recyclerViewSearch.setVisibility(View.GONE);
    }

    // Hiện giao diện search
    private void showSearchUI() {
        recyclerViewCategories.setVisibility(View.GONE);
        recyclerViewHot.setVisibility(View.GONE);
        recyclerViewPlaylist.setVisibility(View.GONE);
        recyclerViewRecently.setVisibility(View.GONE);

        recyclerViewSearch.setVisibility(View.VISIBLE);
    }
    // ================== AUTO SCROLL ==================
    private void startAutoScrollHot() {
        if (hotScrollRunnable != null) handler.removeCallbacks(hotScrollRunnable);
        hotScrollRunnable = () -> {
            if (hotList.size() > 0) {
                hotIndex = (hotIndex + 1) % hotList.size();
                recyclerViewHot.smoothScrollToPosition(hotIndex);
                handler.postDelayed(hotScrollRunnable, 4000);
            }
        };
        handler.postDelayed(hotScrollRunnable, 4000);
    }

    private void startAutoScrollPlaylist() {
        if (playlistScrollRunnable != null) handler.removeCallbacks(playlistScrollRunnable);
        playlistScrollRunnable = () -> {
            if (playlistList.size() > 0) {
                playlistIndex = (playlistIndex + 1) % playlistList.size();
                recyclerViewPlaylist.smoothScrollToPosition(playlistIndex);
                handler.postDelayed(playlistScrollRunnable, 4000);
            }
        };
        handler.postDelayed(playlistScrollRunnable, 4000);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(hotScrollRunnable);
        handler.removeCallbacks(playlistScrollRunnable);
    }
}
