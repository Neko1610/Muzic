package com.example.music.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.AlbumAdapter;
import com.example.music.Models.Album;
import com.example.music.Models.Playlist;
import com.example.music.Models.Song;
import com.example.music.PlaylistAdapter;
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

public class MusicFragment extends Fragment {

    public static final String ARG_OPEN_ALBUM_TAB = "open_album_tab";

    private TextView tabAllSongs, tabPlaylists, tabAlbums;
    private RecyclerView recyclerViewMusic;

    private final List<Song> songList = new ArrayList<>();
    private final List<Playlist> playlistList = new ArrayList<>();
    private final List<Album> albumList = new ArrayList<>();

    private DatabaseReference dbRef;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);

        tabAllSongs = view.findViewById(R.id.tab_all_songs);
        tabPlaylists = view.findViewById(R.id.tab_playlists);
        tabAlbums = view.findViewById(R.id.tab_albums);
        recyclerViewMusic = view.findViewById(R.id.recyclerViewMusic);

        recyclerViewMusic.setLayoutManager(new LinearLayoutManager(getContext()));
        dbRef = FirebaseDatabase.getInstance().getReference();

        Bundle args = getArguments();
        if (args != null && args.getBoolean(ARG_OPEN_ALBUM_TAB, false)) {
            highlightTab(tabAlbums);
            loadAlbums();
        } else {
            highlightTab(tabAllSongs);
            loadAllSongs();
        }

        tabAllSongs.setOnClickListener(v -> {
            highlightTab(tabAllSongs);
            loadAllSongs();
        });

        tabPlaylists.setOnClickListener(v -> {
            highlightTab(tabPlaylists);
            loadPlaylists();
        });

        tabAlbums.setOnClickListener(v -> {
            highlightTab(tabAlbums);
            loadAlbums();
        });

        return view;
    }

    private void highlightTab(TextView selectedTab) {
        tabAllSongs.setTextColor(getResources().getColor(R.color.gray));
        tabPlaylists.setTextColor(getResources().getColor(R.color.gray));
        tabAlbums.setTextColor(getResources().getColor(R.color.gray));
        selectedTab.setTextColor(getResources().getColor(R.color.pink));
    }

    private void loadAllSongs() {
        dbRef.child("songs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                songList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Song s = snap.getValue(Song.class);
                    if (s != null) songList.add(s);
                }
                recyclerViewMusic.setAdapter(new SongAdapter(getContext(), songList));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadPlaylists() {
        dbRef.child("playlists").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                playlistList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Playlist p = snap.getValue(Playlist.class);
                    if (p != null) playlistList.add(p);
                }
                recyclerViewMusic.setAdapter(new PlaylistAdapter(getContext(), playlistList));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadAlbums() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        dbRef.child("users")
                .child(user.getUid())
                .child("albums")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        albumList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Album a = snap.getValue(Album.class);
                            if (a != null) albumList.add(a);
                        }
                        recyclerViewMusic.setAdapter(new AlbumAdapter(getContext(), albumList));
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
