package com.example.music.network;

import com.example.music.network.ChatRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ChatApi {

    @POST("/chat")
    Call<ChatResponse> chat(@Body ChatRequest request);

}