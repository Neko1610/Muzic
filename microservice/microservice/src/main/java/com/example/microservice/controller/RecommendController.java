package com.example.microservice.controller;

import com.example.microservice.model.Song;
import com.example.microservice.service.RecommendationEngine;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@CrossOrigin
public class RecommendController {

    private final RecommendationEngine engine;

    public RecommendController(RecommendationEngine engine) {
        this.engine = engine;
    }

    @GetMapping("/{uid}")
    public List<Song> recommend(@PathVariable String uid) throws Exception {
        return engine.getRecommendations(uid);
    }
}
