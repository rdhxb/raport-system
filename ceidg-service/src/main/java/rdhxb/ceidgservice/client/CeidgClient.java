package rdhxb.ceidgservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rdhxb.ceidgservice.dto.CeidgApiResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Component
public class CeidgClient {

    private final HttpClient ceidgClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ceidg.api.url}")
    private String baseUrl;

    @Value("${ceidg.api.test.url}")
    private String baseUrlTest;

    @Value("${ceidg.api.token}")
    private String token;

    public CeidgApiResponse getData(String nipNumer) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .uri(URI.create(baseUrl + "/firma?nip=" + nipNumer))
                .GET()
                .build();

        HttpResponse<String> response = ceidgClient.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("Status code : " + response.statusCode());

        if (response.statusCode() == 200){
            return mapper.readValue(response.body(), CeidgApiResponse.class);

        } else if (response.statusCode() == 204) {
            return new CeidgApiResponse(null);
        }

        return null;

    }

}
