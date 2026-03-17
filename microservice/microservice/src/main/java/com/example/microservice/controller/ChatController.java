package com.example.microservice.controller;
import com.example.microservice.service.GeminiService;
import com.example.microservice.service.MusicService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final GeminiService geminiService;
    private final MusicService musicService;

    public ChatController(GeminiService geminiService,
                          MusicService musicService) {

        this.geminiService = geminiService;
        this.musicService = musicService;
    }

    @PostMapping
    public Map<String,Object> chat(@RequestBody Map<String,String> body){

        String message = body.get("message");

        String mood = geminiService.detectMood(message);

        List<Map<String,String>> songs =
                musicService.getMusicByMood(mood);

        return Map.of(
                "mood",mood,
                "songs",songs
        );
    }
}