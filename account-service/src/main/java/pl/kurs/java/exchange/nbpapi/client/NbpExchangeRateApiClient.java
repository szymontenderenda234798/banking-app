package pl.kurs.java.exchange.nbpapi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.kurs.java.exchange.nbpapi.model.NbpExchangeRateApiResponse;

@FeignClient(name = "nbpApiClient", url = "https://api.nbp.pl")
public interface NbpExchangeRateApiClient {

    @GetMapping(value = "/api/exchangerates/rates/a/{currency}/?format=json", produces = "application/json")
    NbpExchangeRateApiResponse getExchangeRates(@PathVariable("currency") String currency);
}