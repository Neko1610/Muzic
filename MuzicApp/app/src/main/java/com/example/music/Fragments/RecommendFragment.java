package com.example.music.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.Adapters.SongAdapter;
import com.example.music.Models.Song;
import com.example.music.R;
import com.example.music.Service.RecommendationService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class RecommendFragment extends Fragment {

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<Song> recommendList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recommend, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewRecommend);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SongAdapter(getContext(), recommendList);
        recyclerView.setAdapter(adapter);

        loadRecommend();

        return view;
    }

    private void loadRecommend() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        RecommendationService service = new RecommendationService();

        service.getRecommendations(uid, songs -> {
            recommendList.clear();
            recommendList.addAll(songs);
            adapter.notifyDataSetChanged();
        });
    }
}
