package rdhxb.whitelistservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rdhxb.whitelistservice.model.WlApiResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;


@Component
public class WlClient {

    private final HttpClient wlClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${wl.api.url}")
    private String baseUrl;


    public WlApiResponse getData(String nipNumber) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/search/nip/" + nipNumber + "?date=" + LocalDate.now()))
                .GET()
                .build();


        HttpResponse<String> response = wlClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200){
            throw new IllegalStateException("WL API status-code : " + response.statusCode() + " for : " + nipNumber + " response : " + response.body());
        }

        return mapper.readValue(response.body(), WlApiResponse.class);


    }
}
