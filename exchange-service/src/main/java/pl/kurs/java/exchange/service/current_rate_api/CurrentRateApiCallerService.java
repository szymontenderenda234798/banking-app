package pl.kurs.java.exchange.service.current_rate_api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.kurs.java.exchange.exception.ErrorDuringGetCurrentRateApiCallException;
import pl.kurs.java.exchange.service.current_rate_api.model.ApiResponse;

@Service
@RequiredArgsConstructor
public class CurrentRateApiCallerService {

    private final HttpClient httpClient;

    public double getCurrentRate(String currency) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.nbp.pl/api/exchangerates/rates/a/" + currency + "/?format=json"))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                ObjectMapper objectMapper = new ObjectMapper();
                ApiResponse apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
                if (apiResponse.getRates() == null || apiResponse.getRates().length == 0) {
                    throw new ErrorDuringGetCurrentRateApiCallException();
                }
                return apiResponse.getRates()[0].getMid();
            } else {
                throw new ErrorDuringGetCurrentRateApiCallException();
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new ErrorDuringGetCurrentRateApiCallException();
        }
    }
}
