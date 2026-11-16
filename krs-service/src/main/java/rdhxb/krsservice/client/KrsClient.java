package rdhxb.krsservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rdhxb.common.exception.PermanentApiException;
import rdhxb.common.exception.TransientApiException;
import rdhxb.krsservice.dto.KrsApiResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class KrsClient {

    private final Duration timeoutDuration = Duration.ofSeconds(5);
    private final HttpClient krsClient = HttpClient.newBuilder().connectTimeout(timeoutDuration).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${krs.api.url}")
    private String baseUrl;

    public KrsApiResponse getData(String krsNumber) throws IOException, InterruptedException, PermanentApiException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/OdpisAktualny/" + krsNumber + "?rejestr=P&format=json"))
                .timeout(timeoutDuration)
                .GET()
                .build();

        HttpResponse<String> response = krsClient.send(request, HttpResponse.BodyHandlers.ofString());


        int status = response.statusCode();

        if (status == 404){
            throw new PermanentApiException("KRS " + krsNumber + " not found");
        }
        // 429 (rate limit) and 5xx are temporary – worth retrying
        if (status == 429 || status >= 500){
            throw new TransientApiException("KRS API returned " + status + " for KRS " + krsNumber, null);
        }
        // Any other non-200 (e.g. 400) means a bad request – retrying won't help
        if (status != 200){
            throw new PermanentApiException("KRS API returned " + status + " for KRS " + krsNumber);
        }
        try {
            return mapper.readValue(response.body(), KrsApiResponse.class);
        }catch (JacksonException e){
            throw new PermanentApiException("Wrong json from KRS API " + e.getMessage());
        }



    }
}