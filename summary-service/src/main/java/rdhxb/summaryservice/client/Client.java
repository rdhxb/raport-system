package rdhxb.summaryservice.client;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class Client {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper;

    @Value("${ollama.url}")
    private String baseUrl;

    @Value("${ollama.model}")
    private String model;

    public String sendData(String systemPrompt, String data) throws IOException, InterruptedException {

        // Map instead of a text block - Jackson escapes newlines/quotes in the report text
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", data)
                ),
                "stream", false,
                "think", false,
                "keep_alive", "30m",
                "options", Map.of(
                        "temperature", 0.2,
                        "num_ctx", 8192,
                        "num_predict", 800
                )
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/chat"))
                .header("Content-Type", "application/json")
                // generation on Jetson takes tens of seconds - without this it could hang forever
                .timeout(Duration.ofSeconds(180))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("Ollama status: {}", response.statusCode());

        return mapper.readTree(response.body()).path("message").path("content").asString();
    }
}
