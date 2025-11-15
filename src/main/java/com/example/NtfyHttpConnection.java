package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public class NtfyHttpConnection implements NtfyConnection{

    private final String serverUrl;
    private final String topic;
    private final HttpClient client;


    public NtfyHttpConnection(String topic) {
        Dotenv dotenv = Dotenv.load();
        serverUrl = dotenv.get("NTFY_SERVER_URL");

        if (serverUrl == null || serverUrl.isBlank()){
            throw new IllegalStateException("NTFY_SERVER_URL not set in .env");
        }
        this.topic = topic;
        this.client = HttpClient.newHttpClient();
    }

    public NtfyHttpConnection(String baseUrl, String topic) {
        this.serverUrl = baseUrl;
        this.topic = topic;
        this.client = HttpClient.newHttpClient();
    }


    @Override
    public void sendMessage(String user, String message) {
        try {
            String jsonBody = new JSONObject()
                    .put("user", user)
                    .put("message", message)
                    .toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "/" + topic))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new RuntimeException("Failed to send message: " + response.statusCode());
        }catch (Exception e){
            throw new RuntimeException("Error sending message", e);
        }
    }

    @Override
    public void receiveMessage(Consumer<NtfyMessageDto> listener) {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(serverUrl + "/" + topic + "/json"))
                        .build();

                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))){
                    String line;
                    while ((line = reader.readLine()) != null){
                        if (line.isBlank()) continue;

                        JSONObject json = new JSONObject(line);
                        if (json.has("message")) {
                            String rawMessage = json.getString("message");
                            try {
                                // Try to parse nested JSON (for our app’s own messages)
                                JSONObject inner = new JSONObject(rawMessage);
                                String user = inner.optString("user", "unknown");
                                String msg  = inner.optString("message", rawMessage);
                                Platform.runLater(() -> listener.accept(new NtfyMessageDto(user, msg)));
                            } catch (Exception ex) {
                                // Not a nested JSON (e.g. plain text message)
                                Platform.runLater(() -> listener.accept(new NtfyMessageDto("unknown", rawMessage)));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("NTFY listener stopped: " + e.getMessage());
            }
        }, "ntfy-listener").start();
    }
}
