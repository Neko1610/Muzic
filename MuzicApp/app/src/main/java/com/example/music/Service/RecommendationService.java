package com.example.music.Service;

import com.example.music.Models.Song;
import com.example.music.network.RecommendApi;
import com.example.music.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendationService {

    public interface RecommendCallback {
        void onResult(List<Song> songs);
    }

    public void getRecommendations(String uid, RecommendCallback callback) {

        RecommendApi api = RetrofitClient
                .getInstance()
                .create(RecommendApi.class);

        api.getRecommendations(uid)
                .enqueue(new Callback<List<Song>>() {

                    @Override
                    public void onResponse(Call<List<Song>> call,
                                           Response<List<Song>> response) {

                        if (response.isSuccessful() &&
                                response.body() != null) {

                            callback.onResult(response.body());

                        } else {
                            callback.onResult(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Song>> call,
                                          Throwable t) {

                        t.printStackTrace();
                        callback.onResult(new ArrayList<>());
                    }
                });
    }
}
