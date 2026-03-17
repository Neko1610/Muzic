package com.example.microservice.service;

import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;

@Service
public class MusicService {

    public List<Map<String,String>> getMusicByMood(String mood){

        List<Map<String,String>> result = new ArrayList<>();

        try {

            DatabaseReference ref =
                    FirebaseDatabase.getInstance().getReference("songs");

            CountDownLatch latch = new CountDownLatch(1);

            ref.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    for (DataSnapshot songSnap : snapshot.getChildren()) {

                        String songMood =
                                songSnap.child("mood").getValue(String.class);

                        if(songMood != null && songMood.equals(mood)){

                            String title =
                                    songSnap.child("title").getValue(String.class);

                            String artist =
                                    songSnap.child("artist").getValue(String.class);

                            String cover =
                                    songSnap.child("coverUrl").getValue(String.class);

                            String mp3 =
                                    songSnap.child("mp3Url").getValue(String.class);

                            result.add(Map.of(
                                    "title", title,
                                    "artist", artist,
                                    "coverUrl", cover,
                                    "mp3Url", mp3
                            ));
                        }

                        if(result.size() >= 5) break;
                    }

                    latch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    error.toException().printStackTrace();
                    latch.countDown();
                }
            });

            latch.await(); // đợi Firebase trả dữ liệu

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}