package pl.kurs.java.exchange.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kurs.java.exchange.service.ExchangeRateService;

@RestController
@RequestMapping("api/v1/rates")
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService;

    @GetMapping("/{currency}")
    public ResponseEntity<Double> getExchangeRate(@PathVariable String currency) {
        log.info("Getting exchange rate for currency: {}", currency);
        return ResponseEntity.ok(exchangeRateService.getExchangeRate(currency));
    }
}
