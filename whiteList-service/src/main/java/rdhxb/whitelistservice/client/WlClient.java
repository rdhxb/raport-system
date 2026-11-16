package rdhxb.whitelistservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rdhxb.common.exception.PermanentApiException;
import rdhxb.common.exception.TransientApiException;
import rdhxb.whitelistservice.model.WlApiResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;


@Component
public class WlClient {

    private final Duration timeoutDuration = Duration.ofSeconds(5);
    private final HttpClient wlClient = HttpClient.newBuilder().connectTimeout(timeoutDuration).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${wl.api.url}")
    private String baseUrl;


    public WlApiResponse getData(String nipNumber) throws IOException, InterruptedException, PermanentApiException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/search/nip/" + nipNumber + "?date=" + LocalDate.now()))
                .timeout(timeoutDuration)
                .GET()
                .build();

        HttpResponse<String> response = wlClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();

        // server problem trying again should help if its temporary problem
        if ( status >= 500){
            throw new TransientApiException("WL API returned " + status + " for nip number : " + nipNumber, null);
        }

        // daily limit for wl is 100 requests reset every midnight
        if (status == 429){
            throw new PermanentApiException("WL API daily request limit reached – try again after midnight");
        }
        // Any other non-200 (e.g. 400) means a bad request – retrying won't help
        if (status != 200){
            throw new PermanentApiException("WL API returned " + status + " for nip number :  " + nipNumber);
        }
        try {
            return mapper.readValue(response.body(), WlApiResponse.class);
        }catch (JacksonException e){
            throw new PermanentApiException("Wrong json from WL API " + e.getMessage());
        }




    }
}
