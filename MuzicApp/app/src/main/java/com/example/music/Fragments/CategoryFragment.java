package com.example.music.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.music.Models.Song;
import com.example.music.R;
import com.example.music.SongAdapter;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {
    private RecyclerView recyclerViewMusic;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private DatabaseReference dbRef;
    private String categoryName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        recyclerViewMusic = view.findViewById(R.id.recyclerViewMusic);
        recyclerViewMusic.setLayoutManager(new LinearLayoutManager(getContext()));

        // lấy category từ arguments
        if (getArguments() != null) {
            categoryName = getArguments().getString("categoryName", "");
        }

        dbRef = FirebaseDatabase.getInstance().getReference();
        loadSongsByCategory(categoryName);

        // hiển thị title category (nếu muốn)
        TextView title = view.findViewById(R.id.category_title);
        if (title != null) {
            title.setText(categoryName);
        }

        return view;
    }

    private void loadSongsByCategory(String categoryName) {
        dbRef.child("songs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                songList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Song s = snap.getValue(Song.class);
                    if (s != null && s.getCategory() != null) {
                        // Cho phép nhiều thể loại, ví dụ: "VietNam,Trending"
                        String[] categories = s.getCategory().split(",");
                        for (String c : categories) {
                            if (categoryName.equalsIgnoreCase(c.trim())) {
                                songList.add(s);
                                break;
                            }
                        }
                    }
                }
                songAdapter = new SongAdapter(getContext(), songList);
                recyclerViewMusic.setAdapter(songAdapter);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
