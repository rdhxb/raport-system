package rdhxb.whitelistservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import rdhxb.whitelistservice.model.WlApiResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;


@Configuration
@RequiredArgsConstructor
public class WlClient {

    private final HttpClient wlClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final LocalDate date = LocalDate.now();

    public WlApiResponse getData(String nipNumber) throws IOException, InterruptedException {


        String url = "https://wl-api.mf.gov.pl/api/search/nip/";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url + nipNumber + "?date=" + date))
                .GET()
                .build();


        HttpResponse<String> response = wlClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200){
            throw new IllegalStateException("WL API status-code : " + response.statusCode() + "for : " + nipNumber);
        }

        return mapper.readValue(response.body(), WlApiResponse.class);


    }
}
