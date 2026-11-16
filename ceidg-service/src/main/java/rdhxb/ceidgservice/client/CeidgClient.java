package rdhxb.ceidgservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rdhxb.ceidgservice.dto.CeidgApiResponse;
import rdhxb.common.exception.PermanentApiException;
import rdhxb.common.exception.TransientApiException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
public class CeidgClient {

    private final Duration timeoutDuration = Duration.ofSeconds(5);
    private final HttpClient ceidgClient = HttpClient.newBuilder().connectTimeout(timeoutDuration).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ceidg.api.url}")
    private String baseUrl;

    @Value("${ceidg.api.test.url}")
    private String baseUrlTest;

    @Value("${ceidg.api.token}")
    private String token;

    public CeidgApiResponse getData(String nipNumer) throws IOException, InterruptedException, PermanentApiException {

        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .uri(URI.create(baseUrl + "/firma?nip=" + nipNumer))
                .timeout(timeoutDuration)
                .GET()
                .build();

        HttpResponse<String> response = ceidgClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();

        if (status == 401) {
            throw new PermanentApiException("CEIDG API token expired or invalid !");
        }
        // 50 req / 3 min  | 429 → 180 s block counted from the last request; retrying would extend it.
        if (status == 429){
            throw new PermanentApiException("CEIDG API too many request try again in 3 minutes");
        }
        // 5xx (500, 502, 503, 504) are temporary server problems – worth retrying
        if (status >= 500){
            throw new TransientApiException("CEIDG API returned " + status + " for nip number " + nipNumer, null);
        }
        if (status == 204){
            return null;
        }
        if (status != 200){
            throw new PermanentApiException("CEIDG API returned " + status + " for nip number " + nipNumer);
        }
        try {
            return mapper.readValue(response.body(), CeidgApiResponse.class);
        } catch (JacksonException e) {
            throw new PermanentApiException("Bad json from CEIDG API " + e.getMessage());
        }

    }

}
