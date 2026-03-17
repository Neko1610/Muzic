package com.example.music.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Models.ChatMessage;
import com.example.music.Models.Song;
import com.example.music.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ChatMessage> chatList;

    public ChatAdapter(Context context, List<ChatMessage> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @Override
    public int getItemViewType(int position) {
        return chatList.get(position).type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == ChatMessage.TYPE_USER){

            View v = LayoutInflater.from(context)
                    .inflate(R.layout.item_user_chat,parent,false);

            return new UserVH(v);

        }else if(viewType == ChatMessage.TYPE_BOT){

            View v = LayoutInflater.from(context)
                    .inflate(R.layout.item_bot_chat,parent,false);

            return new BotVH(v);

        }else{

            View v = LayoutInflater.from(context)
                    .inflate(R.layout.item_song_list,parent,false);

            return new SongVH(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ChatMessage msg = chatList.get(position);

        if(holder instanceof UserVH){

            ((UserVH) holder).text.setText(msg.message);

        }
        else if(holder instanceof BotVH){

            ((BotVH) holder).text.setText(msg.message);

        }
        else if(holder instanceof SongVH){

            ChatSongAdapter adapter =
                    new ChatSongAdapter(context, msg.songs);

            ((SongVH) holder).songRecycler.setLayoutManager(
                    new LinearLayoutManager(context));

            ((SongVH) holder).songRecycler.setAdapter(adapter);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class UserVH extends RecyclerView.ViewHolder {

        TextView text;

        public UserVH(View v) {
            super(v);
            text = v.findViewById(R.id.txtUser);
        }
    }

    static class BotVH extends RecyclerView.ViewHolder {

        TextView text;

        public BotVH(View v) {
            super(v);
            text = v.findViewById(R.id.txtBot);
        }
    }

    static class SongVH extends RecyclerView.ViewHolder {

        RecyclerView songRecycler;

        public SongVH(View v) {
            super(v);
            songRecycler = v.findViewById(R.id.songRecycler);
        }
    }
}