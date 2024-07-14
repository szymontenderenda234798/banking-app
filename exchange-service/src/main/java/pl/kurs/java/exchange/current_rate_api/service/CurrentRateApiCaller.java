package pl.kurs.java.exchange.current_rate_api.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kurs.java.exchange.exception.ErrorDuringGetCurrentRateApiCallException;
import pl.kurs.java.exchange.current_rate_api.model.ApiResponse;

@Service
public class CurrentRateApiCaller {

    public double getCurrentRate(String currency) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.nbp.pl/api/exchangerates/rates/a/" + currency + "/?format=json"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                ObjectMapper objectMapper = new ObjectMapper();
                ApiResponse apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
                return apiResponse.getRates()[0].getMid();
            } else {
                throw new ErrorDuringGetCurrentRateApiCallException();
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new ErrorDuringGetCurrentRateApiCallException();
        }
    }
}
