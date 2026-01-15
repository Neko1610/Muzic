package com.example.music.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Models.Song;
import com.example.music.R;
import com.example.music.SongAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class  RecentlyFragment extends Fragment {

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<Song> recentList = new ArrayList<>();
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recently, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewRecently);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SongAdapter(getContext(), recentList);
        recyclerView.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            dbRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("recentlyPlayed");
            loadData();
        }

        return view;
    }

    private void loadData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recentList.clear();
                HashSet<String> addedKeys = new HashSet<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Song s = snap.getValue(Song.class);
                    if (s != null) {
                        String key = (s.getId() != null && !s.getId().isEmpty())
                                ? s.getId()
                                : (s.getTitle() + "_" + s.getArtist());
                        if (!addedKeys.contains(key)) {
                            addedKeys.add(key);
                            recentList.add(s);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
