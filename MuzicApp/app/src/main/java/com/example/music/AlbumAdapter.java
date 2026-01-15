package com.example.music;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.Fragments.SongAlbumFragment;
import com.example.music.Models.Album;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private Context context;
    private List<Album> albums;

    public AlbumAdapter(Context context, List<Album> albums) {
        this.context = context;
        this.albums = albums;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.name.setText(album.getName());

        Glide.with(context)
                .load(album.getImageUrl())
                .placeholder(R.drawable.baseline_schedule_24)
                .into(holder.cover);

        Glide.with(context)
                .load(album.getImageUrl())
                .placeholder(R.drawable.baseline_schedule_24)
                .into(holder.background);

        // ⚠️ BẮT BUỘC cho phép long click
        holder.itemView.setLongClickable(true);

        // ✅ LONG CLICK → MENU CRUD ALBUM
        holder.itemView.setOnLongClickListener(v -> {
            showAlbumCrudMenu(v, album, holder.getAdapterPosition());
            return true; // QUAN TRỌNG
        });

        // ✅ CLICK THƯỜNG → MỞ ALBUM
        holder.itemView.setOnClickListener(v -> {
            SongAlbumFragment fragment = new SongAlbumFragment();
            Bundle args = new Bundle();
            args.putSerializable("album", album);
            fragment.setArguments(args);

            ((AppCompatActivity) context)
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
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
    private void showAlbumCrudMenu(View view, Album album, int position) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenu().add("Sửa album");
        menu.getMenu().add("Xóa album");

        menu.setOnMenuItemClickListener(item -> {

            if (item.getTitle().equals("Sửa album")) {
                showEditAlbumDialog(album, position);
            }

            if (item.getTitle().equals("Xóa album")) {
                deleteAlbum(album, position);
            }

            return true;
        });

        menu.show();
    }
    private void showEditAlbumDialog(Album album, int position) {
        EditText edt = new EditText(context);
        edt.setText(album.getName());

        new AlertDialog.Builder(context)
                .setTitle("Sửa album")
                .setView(edt)
                .setPositiveButton("Lưu", (d, w) -> {

                    String newName = edt.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(context, "Tên album không được rỗng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) return;

                    // 🔍 KIỂM TRA TRÙNG TÊN
                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(user.getUid())
                            .child("albums")
                            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {

                                    for (com.google.firebase.database.DataSnapshot snap : snapshot.getChildren()) {
                                        Album a = snap.getValue(Album.class);

                                        if (a == null || a.getName() == null) continue;

                                        // ❗ bỏ qua chính album đang sửa
                                        if (a.getId().equals(album.getId())) continue;

                                        if (a.getName().equalsIgnoreCase(newName)) {
                                            Toast.makeText(
                                                    context,
                                                    "Tên album đã tồn tại!",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                            return; // ❌ DỪNG
                                        }
                                    }

                                    // ✅ KHÔNG TRÙNG → UPDATE
                                    FirebaseDatabase.getInstance()
                                            .getReference("users")
                                            .child(user.getUid())
                                            .child("albums")
                                            .child(album.getId())
                                            .child("name")
                                            .setValue(newName);

                                    album.setName(newName);
                                    notifyItemChanged(position);

                                    Toast.makeText(context, "Đã cập nhật album", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                                    Toast.makeText(context, "Lỗi kiểm tra album", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteAlbum(Album album, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        new AlertDialog.Builder(context)
                .setTitle("Xóa album")
                .setMessage("Bạn có chắc muốn xóa album này?")
                .setPositiveButton("Xóa", (d, w) -> {

                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(user.getUid())
                            .child("albums")
                            .child(album.getId())
                            .removeValue();

                    albums.remove(position);
                    notifyItemRemoved(position);

                    Toast.makeText(context, "Đã xóa album", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


}
