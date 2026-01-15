package com.example.music.Activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.music.Fragments.HomeFragment;
import com.example.music.Fragments.MiniPlayerFragment;
import com.example.music.Fragments.MusicFragment;
import com.example.music.Fragments.CreateFragment;
import com.example.music.Models.Song;
import com.example.music.R;
import com.example.music.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    public static MediaPlayer globalPlayer;   // MediaPlayer dùng chung
    public static Song currentSong;           // Bài hát hiện tại

    ActivityMainBinding binding;
    DrawerLayout drawerLayout;
    ImageView iconMenu, iconSearch;
    SearchView searchView;

    private ImageView imgAvatar; // giữ tham chiếu để reload trong onResume

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔹 Check user login
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        // 🔹 Layout chính
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        drawerLayout = binding.drawerLayout;
        iconMenu = binding.toolbar.findViewById(R.id.icon_menu);
        iconSearch = binding.toolbar.findViewById(R.id.icon_search);
        searchView = binding.searchView;

        // Mở menu
        iconMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Toggle Search
        iconSearch.setOnClickListener(v -> {
            if (searchView.getVisibility() == View.VISIBLE) {
                searchView.setVisibility(View.GONE);
            } else {
                searchView.setVisibility(View.VISIBLE);
                searchView.onActionViewExpanded();
            }
        });

        // Search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callHomeFilter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callHomeFilter(newText);
                return true;
            }
        });

        // Load HomeFragment mặc định
        replaceFragment(new HomeFragment());

        // BottomNavigation
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.music) {
                replaceFragment(new MusicFragment());
            } else if (id == R.id.setting) {
                replaceFragment(new CreateFragment());
            }
            return true;
        });

        // Drawer menu
        NavigationView navView = binding.navView;
        View headerView = navView.getHeaderView(0);
        imgAvatar = headerView.findViewById(R.id.imgAvatar);

        // 🔹 Load avatar từ Firebase
        loadUserAvatar(imgAvatar);

        // Click avatar → Profile
        imgAvatar.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        // Menu item
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                if (globalPlayer != null) {
                    if (globalPlayer.isPlaying()) {
                        globalPlayer.stop();
                    }
                    globalPlayer.release();
                    globalPlayer = null;
                    currentSong = null;
                }
                hideMiniPlayer();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // Hàm thay fragment
    public void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame_layout, fragment);
        ft.commit();
    }

    // 🔹 Gọi filter trong HomeFragment
    private void callHomeFilter(String query) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).filterSongs(query);
        }
    }

    // 🔹 MiniPlayer
    public void showMiniPlayer(Song song, boolean isPlaying) {
        currentSong = song;
        binding.miniPlayerContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mini_player_container, new MiniPlayerFragment())
                .commit();
    }

    public void hideMiniPlayer() {
        binding.miniPlayerContainer.setVisibility(View.GONE);
    }

    // 🔹 Load avatar từ Firebase
    private void loadUserAvatar(ImageView imgAvatar) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String avatarPath = snapshot.child("avatarPath").getValue(String.class);

                    if (avatarPath != null) {
                        Glide.with(MainActivity.this)
                                .load(Uri.parse(avatarPath))
                                .placeholder(R.drawable.ic_avatar)
                                .circleCrop()   // 🔹 bo tròn ở Drawer
                                .into(imgAvatar);
                    } else {
                        Glide.with(MainActivity.this)
                                .load(R.drawable.ic_avatar)
                                .circleCrop()   // vẫn bo tròn cho ảnh mặc định
                                .into(imgAvatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    // 🔹 Reload avatar khi quay lại từ ProfileActivity
    @Override
    protected void onResume() {
        super.onResume();
        if (imgAvatar != null) {
            loadUserAvatar(imgAvatar);
        }
    }
}
