package com.example.microservice.service;

import com.example.microservice.model.Song;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class RecommendationEngine {

    public List<Song> getRecommendations(String uid) throws Exception {

        DatabaseReference rootRef =
                FirebaseDatabase.getInstance().getReference();

        CompletableFuture<List<Song>> future =
                new CompletableFuture<>();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Map<String, Long> userCategoryScore =
                        snapshot.child("users")
                                .child(uid)
                                .child("categoryScore")
                                .getValue(new GenericTypeIndicator<Map<String, Long>>() {});

                DataSnapshot songsSnapshot =
                        snapshot.child("songs");

                List<Song> allSongs = new ArrayList<>();

                for (DataSnapshot songSnap : songsSnapshot.getChildren()) {
                    Song song = songSnap.getValue(Song.class);
                    if (song != null) {
                        allSongs.add(song);
                    }
                }

                // ===== Nếu user chưa có categoryScore =====
                if (userCategoryScore == null || userCategoryScore.isEmpty()) {

                    allSongs.sort((a, b) ->
                            Integer.compare(b.getViewCount(), a.getViewCount())
                    );

                    if (allSongs.size() > 10) {
                        allSongs = allSongs.subList(0, 10);
                    }

                    future.complete(allSongs);
                    return;
                }

                // ===== Nếu user có categoryScore =====

                // 1️⃣ Lấy category nghe nhiều nhất
                String topCategory = null;
                int maxScore = 0;

                for (Map.Entry<String, Long> entry : userCategoryScore.entrySet()) {
                    if (entry.getValue() > maxScore) {
                        maxScore = entry.getValue().intValue();
                        topCategory = entry.getKey();
                    }
                }
                // 2️⃣ Lấy top viewCount
                List<Song> topViewSongs = new ArrayList<>(allSongs);
                topViewSongs.sort((a, b) ->
                        Integer.compare(b.getViewCount(), a.getViewCount())
                );

                int half = 5;

                if (topViewSongs.size() > half) {
                    topViewSongs = topViewSongs.subList(0, half);
                }

                // 🔥 Tạo Set ID để tránh trùng
                Set<String> usedIds = new HashSet<>();
                for (Song s : topViewSongs) {
                    usedIds.add(s.getId());
                }

                // 3️⃣ Lấy theo category nghe nhiều (KHÔNG TRÙNG)
                List<Song> categorySongs = new ArrayList<>();

                for (Song song : allSongs) {
                    if (song.getCategory() != null &&
                            song.getCategory().equalsIgnoreCase(topCategory) &&
                            !usedIds.contains(song.getId())) {

                        categorySongs.add(song);
                    }
                }

                // Sort categorySongs theo viewCount luôn cho đẹp
                categorySongs.sort((a, b) ->
                        Integer.compare(b.getViewCount(), a.getViewCount())
                );

                if (categorySongs.size() > half) {
                    categorySongs = categorySongs.subList(0, half);
                }

                // 4️⃣ Gộp lại (KHÔNG SHUFFLE)
                List<Song> finalList = new ArrayList<>();
                finalList.addAll(topViewSongs);
                finalList.addAll(categorySongs);

                future.complete(finalList);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future.get();
    }
}
