package com.example.music.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.Fragments.NowPlayingFragment;
import com.example.music.Models.Song;
import com.example.music.R;

import java.util.List;

public class ChatSongAdapter extends RecyclerView.Adapter<ChatSongAdapter.SongVH> {

    private Context context;
    private List<Song> songs;

    public ChatSongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @Override
    public SongVH onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_song, parent, false);

        return new SongVH(view);
    }

    @Override
    public void onBindViewHolder(SongVH holder, int position) {

        Song song = songs.get(position);

        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());

        Glide.with(context)
                .load(song.getCoverUrl())
                .placeholder(R.drawable.splash)
                .into(holder.cover);

        Glide.with(context)
                .load(song.getCoverUrl())
                .placeholder(R.drawable.splash)
                .into(holder.background);

        holder.itemView.setOnClickListener(v -> {

            FragmentActivity activity = (FragmentActivity) context;

            // 👉 Tạo NowPlaying đúng chuẩn
            NowPlayingFragment fragment =
                    NowPlayingFragment.newInstance(songs, position);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();

            // 👉 ĐÓNG CHAT
            androidx.fragment.app.Fragment chatFragment =
                    activity.getSupportFragmentManager()
                            .findFragmentByTag("BotFragment");

            if (chatFragment instanceof com.google.android.material.bottomsheet.BottomSheetDialogFragment) {
                ((com.google.android.material.bottomsheet.BottomSheetDialogFragment) chatFragment).dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongVH extends RecyclerView.ViewHolder {

        ImageView cover;
        ImageView background;
        TextView title;
        TextView artist;

        public SongVH(View itemView) {
            super(itemView);

            cover = itemView.findViewById(R.id.song_cover);
            background = itemView.findViewById(R.id.song_background);
            title = itemView.findViewById(R.id.song_title);
            artist = itemView.findViewById(R.id.song_artist);
        }
    }
}