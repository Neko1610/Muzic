package com.example.music.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.music.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imgAvatar;
    private TextView tvName, tvEmail;
    private EditText edtName, edtPhone, edtAddress;
    private Button btnSave;

    private Uri imageUri;

    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvEmail = findViewById(R.id.tvEmail);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();

            userRef.child("name").setValue(name);
            userRef.child("phone").setValue(phone);
            userRef.child("address").setValue(address);
        });

        auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        loadUserInfo();

        // Bấm avatar để đổi ảnh
        imgAvatar.setOnClickListener(v -> openFileChooser());
    }

    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String name = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String avatarPath = snapshot.child("avatarPath").getValue(String.class);

                    if (name == null || name.isEmpty()) {
                        name = user.getEmail().split("@")[0];
                    }

                    edtName.setText(name);
                    edtPhone.setText(phone);
                    edtAddress.setText(address);

                    if (avatarPath != null) {
                        Glide.with(ProfileActivity.this)
                                .load(Uri.parse(avatarPath))
                                .placeholder(R.drawable.ic_avatar)
                                .circleCrop()
                                .into(imgAvatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }



    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Lưu trực tiếp đường dẫn ảnh vào Realtime Database
            userRef.child("avatarPath").setValue(imageUri.toString());

            // Hiển thị ngay ảnh mới (bo tròn)
            Glide.with(ProfileActivity.this)
                    .load(imageUri)
                    .circleCrop()   // ✅ bo tròn
                    .into(imgAvatar);
        }
    }
}
