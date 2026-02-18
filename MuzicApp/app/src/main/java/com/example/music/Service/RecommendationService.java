package com.example.music.Service;

import androidx.annotation.NonNull;

import com.example.music.Models.Song;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationService {

    private final DatabaseReference dbRef;

    public interface RecommendCallback {
        void onResult(List<Song> songs);
    }

    public RecommendationService() {
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    // ==============================
    // PUBLIC METHOD
    // ==============================

    public void getRecommendations(String uid, RecommendCallback callback) {

        DatabaseReference categoryRef = dbRef.child("users")
                .child(uid)
                .child("categoryScore");

        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String topCategory = null;
                int maxScore = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Integer score = snap.getValue(Integer.class);
                    if (score != null && score > maxScore) {
                        maxScore = score;
                        topCategory = snap.getKey();
                    }
                }

                if (topCategory != null) {
                    loadSongsByCategory(topCategory, callback);
                } else {
                    loadMostViewedSongs(callback);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(new ArrayList<>());
            }
        });
    }

    // ==============================
    // LOAD BY CATEGORY
    // ==============================

    private void loadSongsByCategory(String topCategory, RecommendCallback callback) {

        dbRef.child("songs")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        List<Song> sameCategory = new ArrayList<>();
                        List<Song> otherCategory = new ArrayList<>();

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Song s = snap.getValue(Song.class);

                            if (s != null && s.getCategory() != null) {

                                if (s.getCategory().trim()
                                        .equalsIgnoreCase(topCategory.trim())) {

                                    sameCategory.add(s);
                                } else {
                                    otherCategory.add(s);
                                }
                            }
                        }

                        Collections.shuffle(sameCategory);
                        Collections.shuffle(otherCategory);

                        List<Song> finalList = new ArrayList<>();

                        int half = 5; // 50% nếu tổng là 10 bài

                        // 🔥 50% theo category nhiều nhất
                        for (int i = 0; i < Math.min(half, sameCategory.size()); i++) {
                            finalList.add(sameCategory.get(i));
                        }

                        // 🔥 50% trộn category khác
                        for (int i = 0; i < Math.min(half, otherCategory.size()); i++) {
                            finalList.add(otherCategory.get(i));
                        }

                        Collections.shuffle(finalList); // trộn lại cho đẹp

                        callback.onResult(finalList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResult(new ArrayList<>());
                    }
                });
    }



    // ==============================
    // LOAD MOST VIEWED (User mới)
    // ==============================

    private void loadMostViewedSongs(RecommendCallback callback) {

        dbRef.child("songs")
                .orderByChild("viewCount")
                .limitToLast(10) // 🔥 lấy 10 bài view cao nhất
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        List<Song> result = new ArrayList<>();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Song s = snap.getValue(Song.class);
                            if (s != null) result.add(s);
                        }

                        // 🔥 Vì limitToLast trả về từ thấp → cao
                        // Ta đảo lại để bài cao nhất đứng đầu
                        Collections.reverse(result);

                        callback.onResult(result);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResult(new ArrayList<>());
                    }
                });
    }

}
