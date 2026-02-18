package com.example.music.Service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.music.Models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class PlayTrackerService {

    private DatabaseReference dbRef;

    public PlayTrackerService() {
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public void trackPlay(Song song) {

        if (song == null) return;
        increaseViewCount(song);
        increaseCategoryScore(song.getCategory());
    }

    // ======================
    // Tăng viewCount
    // ======================

    private void increaseViewCount(Song song) {

        dbRef.child("songs")
                .orderByChild("id")
                .equalTo(song.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) return;

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            snap.getRef().child("viewCount")
                                    .runTransaction(new Transaction.Handler() {

                                        @NonNull
                                        @Override
                                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {

                                            Integer current = currentData.getValue(Integer.class);

                                            if (current == null) {
                                                currentData.setValue(1);
                                            } else {
                                                currentData.setValue(current + 1);
                                            }

                                            return Transaction.success(currentData);
                                        }

                                        @Override
                                        public void onComplete(DatabaseError error,
                                                               boolean committed,
                                                               DataSnapshot currentData) {}
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }



    // ======================
    // Tăng categoryScore
    // ======================

    private void increaseCategoryScore(String category) {

        if (category == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = dbRef.child("users")
                .child(uid)
                .child("categoryScore")
                .child(category);

        ref.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                if (current == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue(current + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed,
                                   DataSnapshot currentData) {}
        });
    }
}
