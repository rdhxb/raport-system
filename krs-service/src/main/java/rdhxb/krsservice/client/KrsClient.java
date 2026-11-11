package rdhxb.krsservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rdhxb.krsservice.dto.KrsApiResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class KrsClient {

    private final HttpClient krsClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${krs.api.url}")
    private String baseUrl;

    public KrsApiResponse getData(String krsNumber) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/OdpisAktualny/" + krsNumber + "?rejestr=P&format=json"))
                .GET()
                .build();

        HttpResponse<String> response = krsClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200){
            throw new IllegalStateException("KRS API status-code : " + response.statusCode() + " dla : " + krsNumber);
        }

        return mapper.readValue(response.body(), KrsApiResponse.class);

    }
}