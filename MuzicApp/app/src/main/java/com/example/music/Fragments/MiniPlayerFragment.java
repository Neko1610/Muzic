package com.example.music.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.music.Service.MusicService;
import com.example.music.Models.Song;
import com.example.music.R;
import com.example.music.Service.MusicStateManager;

public class MiniPlayerFragment extends Fragment {

    private ImageView cover;
    private TextView titleTv, artistTv;
    private ImageButton btnPlayPause;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_mini_player, container, false);

        cover = view.findViewById(R.id.cover);
        titleTv = view.findViewById(R.id.title);
        artistTv = view.findViewById(R.id.artist);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);

        // ==========================
        // Observe current song
        // ==========================
        MusicStateManager.getInstance()
                .getCurrentSong()
                .observe(getViewLifecycleOwner(), song -> {

                    if (song != null) {
                        titleTv.setText(song.getTitle());
                        artistTv.setText(song.getArtist());

                        Glide.with(this)
                                .load(song.getCoverUrl())
                                .transform(new CircleCrop())
                                .into(cover);
                    }
                });

        // ==========================
        // Observe play state
        // ==========================
        MusicStateManager.getInstance()
                .getIsPlaying()
                .observe(getViewLifecycleOwner(), playing -> {

                    btnPlayPause.setImageResource(
                            playing ? R.drawable.ic_stop : R.drawable.ic_play
                    );
                });

        // ==========================
        // Play / Pause
        // ==========================
        btnPlayPause.setOnClickListener(v -> {

            Boolean isPlaying = MusicStateManager
                    .getInstance()
                    .getIsPlaying()
                    .getValue();

            Intent intent = new Intent(getContext(), MusicService.class);

            intent.setAction(isPlaying != null && isPlaying
                    ? MusicService.ACTION_PAUSE
                    : MusicService.ACTION_RESUME);

            requireContext().startService(intent);
        });

        // ==========================
        // CLICK MINI → OPEN NOW PLAYING
        // ==========================
        view.setOnClickListener(v -> {

            if (getActivity() == null) return;

            FragmentManager fm = getActivity().getSupportFragmentManager();
            Fragment existing = fm.findFragmentByTag("NOW_PLAYING");

            if (existing != null) {
                // 🔥 Bring fragment lên top mà không reset
                fm.beginTransaction()
                        .replace(R.id.frame_layout, existing, "NOW_PLAYING")
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
}
