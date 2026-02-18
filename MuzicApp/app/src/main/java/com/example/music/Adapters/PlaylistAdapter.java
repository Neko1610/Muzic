package com.example.music.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.Fragments.SongPlaylistFragment;
import com.example.music.Models.Playlist;
import com.example.music.R;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private Context context;
    private List<Playlist> playlists;

    public PlaylistAdapter(Context context, List<Playlist> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.name.setText(playlist.getName());

        // Load ảnh cover chính
        Glide.with(context)
                .load(playlist.getCoverUrl())
                .placeholder(R.drawable.baseline_schedule_24)
                .into(holder.cover);

        // Load background mờ phía sau
        Glide.with(context)
                .load(playlist.getCoverUrl())
                .placeholder(R.drawable.baseline_schedule_24)
                .into(holder.background);

        // 👉 Khi click vào playlist -> mở SongPlaylistFragment
        holder.itemView.setOnClickListener(v -> {
            SongPlaylistFragment fragment = new SongPlaylistFragment();
            Bundle args = new Bundle();
            args.putSerializable("playlist", playlist); // truyền playlist sang fragment
            fragment.setArguments(args);

            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover, background;
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.playlist_cover);
            background = itemView.findViewById(R.id.playlist_background);
            name = itemView.findViewById(R.id.playlist_name);
        }
    }
}
