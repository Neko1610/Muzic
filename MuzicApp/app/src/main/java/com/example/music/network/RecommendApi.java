package com.example.music.network;

import com.example.music.Models.Song;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RecommendApi {

    @GET("api/recommend/{uid}")
    Call<List<Song>> getRecommendations(
            @Path("uid") String uid
    );
}
