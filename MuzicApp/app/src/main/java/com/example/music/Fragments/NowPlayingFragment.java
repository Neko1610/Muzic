package com.example.music.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.music.Activities.MainActivity;
import com.example.music.Activities.MusicService;
import com.example.music.Models.Song;
import com.example.music.MusicStateManager;
import com.example.music.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class NowPlayingFragment extends Fragment {

    private static final String ARG_SONGS = "songs";
    private static final String ARG_INDEX = "index";

    private List<Song> songList;
    private int currentIndex;

    private ImageButton btnPrev, btnPlay, btnNext, btnFavorite, btnRandom;
    private ImageView cover;
    private TextView titleTv, artistTv;
    private SeekBar seekBar;

    private Animation rotateAnim;
    private DatabaseReference dbRef;

    private boolean isFavorite = false;
    private boolean isRandom = false;

    public static NowPlayingFragment newInstance(List<Song> songs, int index) {
        NowPlayingFragment fragment = new NowPlayingFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SONGS, new ArrayList<>(songs));
        args.putInt(ARG_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_now_playing, container, false);

        cover = view.findViewById(R.id.cover);
        titleTv = view.findViewById(R.id.title);
        artistTv = view.findViewById(R.id.artist);
        seekBar = view.findViewById(R.id.seekBar);

        btnPrev = view.findViewById(R.id.btnPrev);
        btnPlay = view.findViewById(R.id.btnPlay);
        btnNext = view.findViewById(R.id.btnNext);
        btnFavorite = view.findViewById(R.id.btnFavorite);
        btnRandom = view.findViewById(R.id.btnRandom);

        rotateAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);

        // ✅ Lấy user và đường dẫn Firebase đúng theo user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            dbRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("recentlyPlayed");
        }

        if (getArguments() != null) {
            songList = (List<Song>) getArguments().getSerializable(ARG_SONGS);
            currentIndex = getArguments().getInt(ARG_INDEX, 0);
            loadSong(currentIndex, true);
        }

        btnPlay.setOnClickListener(v -> togglePlayPause());
        btnPrev.setOnClickListener(v -> playPrevious());
        btnNext.setOnClickListener(v -> playNext());
        btnFavorite.setOnClickListener(v -> showAlbumSelectionDialog());
        btnRandom.setOnClickListener(v -> {
            isRandom = !isRandom;
            btnRandom.setImageResource(isRandom ? R.drawable.multiple_layers__2_ : R.drawable.multiple_layers__2_);
            Toast.makeText(getContext(),
                    isRandom ? "🔀 Bật phát ngẫu nhiên" : "⏩ Tắt phát ngẫu nhiên",
                    Toast.LENGTH_SHORT).show();
        });

        // Lắng nghe trạng thái nhạc
        MusicStateManager.getInstance().getCurrentSong().observe(getViewLifecycleOwner(), song -> {
            if (song != null) {
                titleTv.setText(song.getTitle());
                artistTv.setText(song.getArtist());
                Glide.with(this)
                        .load(song.getCoverUrl())
                        .transform(new CircleCrop())
                        .into(cover);
            }
        });

        MusicStateManager.getInstance().getIsPlaying().observe(getViewLifecycleOwner(), playing -> {
            btnPlay.setImageResource(playing ? R.drawable.ic_stop : R.drawable.ic_play);
            if (playing) cover.startAnimation(rotateAnim);
            else cover.clearAnimation();
        });

        // SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent intent = new Intent(getContext(), MusicService.class);
                    intent.setAction(MusicService.ACTION_SEEK);
                    intent.putExtra("position", progress);
                    getContext().startService(intent);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return view;
    }

    // Nhận tiến độ
    private final BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicService.ACTION_UPDATE_PROGRESS.equals(intent.getAction())) {
                int current = intent.getIntExtra("current", 0);
                int total = intent.getIntExtra("total", 0);
                boolean isCompleted = intent.getBooleanExtra("completed", false);

                seekBar.setMax(total);
                seekBar.setProgress(current);

                if (isCompleted) {
                    playNext();
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MusicService.ACTION_UPDATE_PROGRESS);
        requireContext().registerReceiver(progressReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(progressReceiver);
    }

    private void loadSong(int index, boolean autoPlay) {
        Song song = songList.get(index);
        saveToRecently(song);

        if (autoPlay) {
            Intent intent = new Intent(getContext(), MusicService.class);
            intent.setAction(MusicService.ACTION_PLAY);
            intent.putExtra("song", song);
            getContext().startService(intent);
        }

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showMiniPlayer(song,
                    MusicStateManager.getInstance().getIsPlaying().getValue() != null &&
                            MusicStateManager.getInstance().getIsPlaying().getValue());
        }
    }

    private void togglePlayPause() {
        Boolean isPlaying = MusicStateManager.getInstance().getIsPlaying().getValue();
        Intent intent = new Intent(getContext(), MusicService.class);
        intent.setAction(isPlaying != null && isPlaying
                ? MusicService.ACTION_PAUSE
                : MusicService.ACTION_RESUME);
        getContext().startService(intent);
    }

    private void playNext() {
        if (songList != null && !songList.isEmpty()) {
            if (isRandom) {
                int newIndex;
                do {
                    newIndex = (int) (Math.random() * songList.size());
                } while (newIndex == currentIndex && songList.size() > 1);
                currentIndex = newIndex;
            } else {
                currentIndex = (currentIndex + 1) % songList.size();
            }
            loadSong(currentIndex, true);
        }
    }

    private void playPrevious() {
        if (songList != null && !songList.isEmpty()) {
            if (isRandom) {
                int newIndex;
                do {
                    newIndex = (int) (Math.random() * songList.size());
                } while (newIndex == currentIndex && songList.size() > 1);
                currentIndex = newIndex;
            } else {
                currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
            }
            loadSong(currentIndex, true);
        }
    }

    // ✅ FIX: Lưu lịch sử đúng cách, không bị ghi đè
    private void saveToRecently(Song song) {
        if (song == null || dbRef == null) return;

        // Thêm mới mỗi lần nghe
        dbRef.push().setValue(song);

        // (Tùy chọn) Giữ tối đa 20 bài gần nhất
        dbRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.getChildrenCount() > 20) {
                DataSnapshot first = snapshot.getChildren().iterator().next();
                first.getRef().removeValue();
            }
        });
    }

    private void showAlbumSelectionDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference albumsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("albums");

        albumsRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(getContext(), "Bạn chưa có album nào!", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> albumNames = new ArrayList<>();
            List<String> albumIds = new ArrayList<>();

            for (DataSnapshot albumSnap : snapshot.getChildren()) {
                String id = albumSnap.getKey();
                String name = albumSnap.child("name").getValue(String.class);
                if (id != null && name != null) {
                    albumIds.add(id);
                    albumNames.add(name);
                }
            }

            if (albumNames.isEmpty()) {
                Toast.makeText(getContext(), "Bạn chưa có album nào!", Toast.LENGTH_SHORT).show();
                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Chọn album để thêm bài hát")
                    .setItems(albumNames.toArray(new String[0]), (dialog, which) -> {
                        String selectedAlbumId = albumIds.get(which);
                        addCurrentSongToAlbum(selectedAlbumId);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void addCurrentSongToAlbum(String albumId) {
        if (songList == null || songList.isEmpty()) return;
        Song currentSong = songList.get(currentIndex);
        if (currentSong == null) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference songRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("albums")
                .child(albumId)
                .child("songs")
                .child(currentSong.getId());

        songRef.setValue(currentSong)
                .addOnSuccessListener(unused -> {
                    isFavorite = true;
                    btnFavorite.setImageResource(R.drawable.heart__1_);
                    Toast.makeText(getContext(), "Đã thêm vào album!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
