package com.example.music.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.music.Models.Album;
import com.example.music.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CreateFragment extends Fragment {

    private EditText edtAlbumName;
    private Button btnCreate;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        edtAlbumName = view.findViewById(R.id.edtPlaylistName);
        btnCreate = view.findViewById(R.id.btnCreatePlaylist);

        btnCreate.setOnClickListener(v -> createAlbum());

        return view;
    }

    private void createAlbum() {
        String name = edtAlbumName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), "Vui lòng nhập tên album!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference albumsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("albums");

        // 🔍 KIỂM TRA TRÙNG TÊN ALBUM
        albumsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Album existing = snap.getValue(Album.class);
                    if (existing != null
                            && existing.getName() != null
                            && existing.getName().equalsIgnoreCase(name)) {

                        Toast.makeText(
                                getContext(),
                                "Album này đã tồn tại!",
                                Toast.LENGTH_SHORT
                        ).show();
                        return; // ❌ DỪNG TẠO
                    }
                }

                // ✅ KHÔNG TRÙNG → TẠO ALBUM
                String id = albumsRef.push().getKey();
                if (id == null) {
                    Toast.makeText(getContext(), "Không thể tạo album!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String defaultImageUri =
                        "android.resource://" + requireContext().getPackageName()
                                + "/" + R.drawable.img_1;

                Album album = new Album(id, name, defaultImageUri, new ArrayList<>());

                albumsRef.child(id).setValue(album)
                        .addOnSuccessListener(unused -> {
                            if (!isAdded()) return;

                            Toast.makeText(
                                    getContext(),
                                    "Đã tạo album thành công!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // 🔁 CHUYỂN VỀ MUSIC + MỞ TAB ALBUM
                            MusicFragment musicFragment = new MusicFragment();
                            Bundle b = new Bundle();
                            b.putBoolean(MusicFragment.ARG_OPEN_ALBUM_TAB, true);
                            musicFragment.setArguments(b);

                            requireActivity()
                                    .getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.frame_layout, musicFragment)
                                    .commit();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(
                                        getContext(),
                                        "Lỗi: " + e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        getContext(),
                        "Lỗi kiểm tra album!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
