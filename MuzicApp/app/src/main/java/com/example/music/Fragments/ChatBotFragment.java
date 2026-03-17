package com.example.music.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Models.ChatMessage;
import com.example.music.Models.Song;
import com.example.music.R;
import com.example.music.Adapters.ChatAdapter;
import com.example.music.network.ChatApi;
import com.example.music.network.ChatRequest;
import com.example.music.network.ChatResponse;
import com.example.music.network.RetrofitClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBotFragment extends BottomSheetDialogFragment {

    private EditText edtMessage;
    private ImageButton btnSend;
    private RecyclerView chatRecycler;

    private ChatAdapter adapter;

    private List<ChatMessage> chatList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat_bot,
                container,false);

        edtMessage = view.findViewById(R.id.edtMessage);
        btnSend = view.findViewById(R.id.btnSend);
        chatRecycler = view.findViewById(R.id.chatRecycler);

        adapter = new ChatAdapter(getContext(),chatList);

        chatRecycler.setLayoutManager(
                new LinearLayoutManager(getContext()));

        chatRecycler.setAdapter(adapter);

        // Bot chào khi mở
        addBotMessage("Xin chào 👋\nBạn đang cảm thấy thế nào?");

        btnSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void sendMessage(){

        String message = edtMessage.getText().toString().trim();

        if(message.isEmpty()) return;

        addUserMessage(message);

        edtMessage.setText("");

        ChatApi api = RetrofitClient.getChatApi();

        api.chat(new ChatRequest(message))
                .enqueue(new Callback<ChatResponse>() {

                    @Override
                    public void onResponse(Call<ChatResponse> call,
                                           Response<ChatResponse> response) {

                        if(response.body()!=null){

                            String mood = response.body().mood != null
                                    ? response.body().mood
                                    : "chill";

                            addBotMessage("Mình cảm nhận bạn đang "
                                    + translateMood(mood)
                                    + " 🎧\nĐây là vài bài phù hợp:");

                            addSongList(response.body().songs);
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatResponse> call,
                                          Throwable t) {

                        addBotMessage("Bot đang lỗi 😢");
                    }
                });
    }

    private void addUserMessage(String msg){

        chatList.add(new ChatMessage(ChatMessage.TYPE_USER,msg));

        adapter.notifyItemInserted(chatList.size()-1);

        chatRecycler.scrollToPosition(chatList.size()-1);
    }

    private void addBotMessage(String msg){

        chatList.add(new ChatMessage(ChatMessage.TYPE_BOT,msg));

        adapter.notifyItemInserted(chatList.size()-1);

        chatRecycler.scrollToPosition(chatList.size()-1);
    }

    private void addSongList(List<Song> songs){

        chatList.add(new ChatMessage(songs));

        adapter.notifyItemInserted(chatList.size()-1);

        chatRecycler.scrollToPosition(chatList.size()-1);
    }

    private String translateMood(String mood){

        if(mood == null) return "thư giãn";

        switch (mood){

            case "sad":
                return "buồn";

            case "happy":
                return "vui";

            case "romantic":
                return "đang yêu";

            case "chill":
                return "thư giãn";

            case "energetic":
                return "tràn đầy năng lượng";

            case "focus":
                return "tập trung";

            default:
                return mood;
        }
    }
}