package com.example.microservice.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class GeminiService {

    private final String API_KEY = "AIzaSyB71aFTbXO6gHD23zGXFpd-fVoZhk8abJY";

    public String detectMood(String message) {

        message = message.toLowerCase();

        // ===== KEYWORD FAST DETECT (giảm API call) =====

        if(message.contains("buồn") || message.contains("sad"))
            return "sad";

        if(message.contains("vui") || message.contains("happy"))
            return "happy";

        if(message.contains("yêu") || message.contains("love"))
            return "romantic";

        if(message.contains("chill") || message.contains("relax") || message.contains("thư giãn"))
            return "chill";

        if(message.contains("học") || message.contains("study") || message.contains("focus"))
            return "focus";

        if(message.contains("mệt") || message.contains("tired"))
            return "chill";

        if(message.contains("năng lượng") || message.contains("energy") || message.contains("gym"))
            return "energetic";

        // ===== nếu không detect được → gọi Gemini =====

        return detectMoodWithGemini(message);
    }


    private String detectMoodWithGemini(String message){

        try {

            String prompt = """
            Bạn là AI phân tích cảm xúc để gợi ý nhạc.

            Hãy xác định cảm xúc của tin nhắn.

            Các mood cho phép:

            happy
            sad
            romantic
            chill
            energetic
            focus

            Chỉ trả về JSON hợp lệ.
            Không thêm text.

            Ví dụ:

            {"mood":"sad"}

            Tin nhắn:
            """ + message;


            URL url = new URL(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = "{ \"contents\": [{\"parts\":[{\"text\":\"" + prompt + "\"}]}]}";

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes());
            os.flush();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String line;
            StringBuilder response = new StringBuilder();

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            br.close();

            String jsonResponse = response.toString();

            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            String text =
                    json.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();

            System.out.println("Gemini raw response: " + text);


            // ===== FIX JSON PARSE =====

            int start = text.indexOf("{");
            int end = text.lastIndexOf("}") + 1;

            if(start >= 0 && end > start){

                String jsonPart = text.substring(start, end);

                JsonObject moodJson =
                        JsonParser.parseString(jsonPart).getAsJsonObject();

                return moodJson.get("mood").getAsString();
            }

            return "chill";

        } catch (Exception e) {

            e.printStackTrace();

            return "chill";
        }
    }
}