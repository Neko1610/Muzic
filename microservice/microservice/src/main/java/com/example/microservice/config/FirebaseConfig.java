package com.example.microservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    
    @PostConstruct
public void init() {
    try {
        System.out.println("🔥 Loading Firebase...");

        InputStream serviceAccount = getClass()
                .getClassLoader()
                .getResourceAsStream("firebase-service-account.json");

        if (serviceAccount == null) {
            throw new RuntimeException("❌ File JSON not found!");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://music-533b1-default-rtdb.firebaseio.com")
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            System.out.println("✅ Firebase initialized!");
        } else {
            System.out.println("⚠️ Firebase already initialized!");
        }

    } catch (Exception e) {
        System.err.println("❌ Firebase init failed!");
        e.printStackTrace(); // 🔥 phải có
    }
}
}