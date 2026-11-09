package rdhxb.sudopservice.client;

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
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SudopClient {

    private final HttpClient sudopClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper;

    @Value("${sudop.api.url}")
    private String baseUrl;

    public void getData(String nipNumer) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/sudop-api/api/przypadki-pomocy?nip-beneficjenta="+ nipNumer))
                .GET()
                .build();

        HttpResponse<String> registered = sudopClient.send(request, HttpResponse.BodyHandlers.ofString());

        log.info(registered.headers().firstValue("location").orElseThrow());
        log.info(String.valueOf(registered.statusCode()));
        log.info("dla nip : " + nipNumer);

        if (registered.statusCode() == 303){
            String registeredUrl = registered.headers().firstValue("location").orElseThrow();
            HttpRequest registeredRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + registeredUrl))
                    .GET()
                    .build();
            log.info("requestResoult : " + registeredRequest.toString());

            for (int i = 0; i < 30; i++) {
                HttpResponse<String> response = sudopClient.send(registeredRequest, HttpResponse.BodyHandlers.ofString());

                log.info(String.valueOf(response.statusCode()));
                if (response.statusCode() == 303){
                    String resultUrl = response.headers().firstValue("location").orElseThrow();
                    HttpRequest resultRequest = HttpRequest.newBuilder()
                            .uri(URI.create(baseUrl + resultUrl))
                            .GET()
                            .build();

                    HttpResponse<String> result = sudopClient.send(resultRequest, HttpResponse.BodyHandlers.ofString());
                    log.info(result.body() + " oficjalny result !!!!!");
                    break;
                }
                if (response.statusCode() == 200){
                    log.info("status 200 continue !");
                    log.info(response.body());
                    TimeUnit.SECONDS.sleep(15);
                    continue;
                }
                if (response.statusCode() != 200 || response.statusCode() != 303){
                    log.error("failed !");
                    return;
                }

            }
        }


    }


}
