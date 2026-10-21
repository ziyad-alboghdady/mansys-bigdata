package org.sau.mansys.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Value("${ollama.url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    private final RestClient rest;

    public ChatController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofMinutes(5));
        this.rest = RestClient.builder().requestFactory(factory).build();
    }

    @GetMapping("/chat")
    public String page() {
        return "chat";
    }

    @PostMapping(value = "/api/chat",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> chat(@RequestBody Map<String, Object> body) {

        @SuppressWarnings("unchecked")
        List<Map<String, String>> messages = (List<Map<String, String>>) body.get("messages");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", ollamaModel);
        payload.put("messages", messages);
        payload.put("stream", false);

        try {
            Map<?, ?> response = rest.post()
                    .uri(ollamaUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            Map<?, ?> message = (Map<?, ?>) response.get("message");
            return Map.of("content", String.valueOf(message.get("content")));

        } catch (Exception e) {
            return Map.of("content", "Could not reach the local model (" + ollamaModel
                    + ") at " + ollamaUrl + " — " + e.getMessage());
        }
    }
}
