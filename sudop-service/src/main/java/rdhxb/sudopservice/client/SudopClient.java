package rdhxb.sudopservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rdhxb.sudopservice.dto.SudopApiResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SudopClient {

    private static final int MAX_QUEUE_POLLS = 20;
    private static final int POLL_INTERVAL_SECONDS = 5;
    private static final int AID_HISTORY_YEARS = 3;

    private final HttpClient sudopClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${sudop.api.url}")
    private String baseUrl;

    public SudopApiResponse getData(String nipNumer) throws IOException, InterruptedException {

        LocalDate dzisiaj = LocalDate.now();
        LocalDate dataOd = dzisiaj.minusYears(AID_HISTORY_YEARS);

        String przypadkiPomocyUrl = "/sudop-api/v1/api/przypadki-pomocy?nip-beneficjenta=" + nipNumer
                + "&dzien-udzielenia-pomocy-od=" + dataOd
                + "&dzien-udzielenia-pomocy-do=" + dzisiaj;

        HttpResponse<String> registered = send(requestBuilder(przypadkiPomocyUrl));

        if (registered.statusCode() != 303){
            throw new IllegalStateException("SUDOP API registration status-code : " + registered.statusCode() + " dla : " + nipNumer);
        }

        HttpRequest queueRequest = requestBuilder(location(registered));
        log.info("SUDOP search registered for nip : {}", nipNumer);

        for (int i = 0; i < MAX_QUEUE_POLLS; i++) {
            HttpResponse<String> queue = send(queueRequest);

            if (queue.statusCode() == 200){
                TimeUnit.SECONDS.sleep(POLL_INTERVAL_SECONDS);
            } else if (queue.statusCode() == 303){
                log.info("Process data !");
                var apiData = fetchResult(location(queue), nipNumer);
                return mapper.readValue(apiData, SudopApiResponse.class);
            } else {
                throw new IllegalStateException("SUDOP API queue status-code : " + queue.statusCode() + " dla : " + nipNumer);
            }
        }

        throw new IllegalStateException("SUDOP API queue timeout dla : " + nipNumer);
    }

    private String fetchResult(String resultUrlPart, String nipNumer) throws IOException, InterruptedException {
        HttpResponse<String> result = send(requestBuilder(resultUrlPart));

        if (result.statusCode() != 200){
            throw new IllegalStateException("SUDOP API result status-code : " + result.statusCode() + " dla : " + nipNumer);
        }

        return result.body();
    }

    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return sudopClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String location(HttpResponse<String> response){
        return response.headers().firstValue("location")
                .orElseThrow(() -> new IllegalStateException("SUDOP API response without Location header"));
    }

    private HttpRequest requestBuilder(String urlPart){
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + urlPart))
                .GET()
                .build();
    }
}
