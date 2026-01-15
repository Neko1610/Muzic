package com.example.music.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Models.Album;
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
import java.util.List;

public class SongAlbumFragment extends Fragment {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private Album album;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_album, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAlbumSongs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            album = (Album) getArguments().getSerializable("album");
        }

        if (album != null) {
            loadSongsFromAlbum(album);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy album!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadSongsFromAlbum(Album album) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("albums")
                .child(album.getId())
                .child("songs");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                songList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Song song = snap.getValue(Song.class);
                    if (song != null) songList.add(song);
                }

                if (songList.isEmpty()) {
                    Toast.makeText(getContext(), "Album này chưa có bài hát nào!", Toast.LENGTH_SHORT).show();
                }

                // SongAlbumFragment → Song trong album user
                songAdapter = new SongAdapter(getContext(), songList, album.getId());

                recyclerView.setAdapter(songAdapter);
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
