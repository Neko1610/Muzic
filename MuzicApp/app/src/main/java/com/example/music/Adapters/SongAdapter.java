package com.example.music.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.music.Fragments.NowPlayingFragment;
import com.example.music.Models.Song;
import com.example.music.R;
import com.example.music.Service.PlayTrackerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private Context context;
    private String albumId;
    private boolean allowCrud;

    private List<Song> songs;

    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
        this.allowCrud = false;
    }

    public SongAdapter(Context context, List<Song> songs, String albumId) {
        this.context = context;
        this.songs = songs;
        this.albumId = albumId;
        this.allowCrud = true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());

        Glide.with(context)
                .load(song.getCoverUrl())
                .placeholder(R.drawable.baseline_schedule_24)
                .into(holder.cover);

        Glide.with(context)
                .load(song.getCoverUrl())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                .into(holder.bg);

        // CLICK → NowPlaying
        holder.itemView.setOnClickListener(v -> {

            PlayTrackerService tracker = new PlayTrackerService();
            tracker.trackPlay(song);

            FragmentManager fm =
                    ((AppCompatActivity) context).getSupportFragmentManager();

            Fragment existing = fm.findFragmentByTag("NOW_PLAYING");

            if (existing == null) {

                NowPlayingFragment fragment =
                        NowPlayingFragment.newInstance(songs, position);

                fm.beginTransaction()
                        .add(R.id.frame_layout, fragment, "NOW_PLAYING")
                        .addToBackStack(null)
                        .commit();

            } else {

                NowPlayingFragment now =
                        (NowPlayingFragment) existing;

                now.playNewSong(songs, position);   // 🔥 UPDATE BÀI MỚI

                fm.beginTransaction()
                        .replace(R.id.frame_layout, existing, "NOW_PLAYING")
                        .addToBackStack(null)
                        .commit();
            }


        });


        // LONG CLICK → CRUD
        if (allowCrud) {
            holder.itemView.setOnLongClickListener(v -> {
                showSongCrudMenu(v, song, holder.getAdapterPosition());
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
    public void updateList(List<Song> newSongs) {
        songs.clear();
        songs.addAll(newSongs);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover, bg;
        TextView title, artist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.song_cover);
            bg = itemView.findViewById(R.id.song_background);   // 👈 thêm background
            title = itemView.findViewById(R.id.song_title);
            artist = itemView.findViewById(R.id.song_artist);
        }
    }
    private void showSongCrudMenu(View view, Song song, int position) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenu().add("Xóa bài hát");

        menu.setOnMenuItemClickListener(item -> {

            if (item.getTitle().equals("Xóa bài hát")) {
                deleteSong(song, position);
            }

            return true;
        });

        menu.show();
    }



    private void deleteSong(Song song, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        new AlertDialog.Builder(context)
                .setTitle("Xóa bài hát")
                .setMessage("Bạn có chắc muốn xóa bài này?")
                .setPositiveButton("Xóa", (d, w) -> {

                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(user.getUid())
                            .child("albums")
                            .child(albumId)
                            .child("songs")
                            .child(song.getId())
                            .removeValue();

                    songs.remove(position);
                    notifyItemRemoved(position);

                    Toast.makeText(context, "Đã xóa bài hát", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


}
