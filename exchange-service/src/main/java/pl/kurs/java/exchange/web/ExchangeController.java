package pl.kurs.java.exchange.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kurs.java.exchange.current_rate_api.service.CurrentRateApiCaller;
import pl.kurs.java.exchange.model.ExchangeRequest;
import pl.kurs.java.exchange.model.command.CreateExchangeRequestCommand;
import pl.kurs.java.exchange.service.ExchangeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/exchange")
@Slf4j
public class ExchangeController {

    private final ExchangeService exchangeService;
    private final CurrentRateApiCaller currentRateApiCaller;

    @PostMapping("/{pesel}")
    public ResponseEntity<ExchangeRequest> processExchangeRequestCommand(@PathVariable String pesel, @RequestBody CreateExchangeRequestCommand createExchangeRequestCommand) {
        log.info("Processing exchange request for PESEL: {}", pesel);
        return ResponseEntity.ok().body(exchangeService.processExchangeRequestCommand(pesel, createExchangeRequestCommand));
    }

    @GetMapping("/{currency}")
    public void getCurrentRate(@PathVariable String currency) {
        log.info("Getting current rate for: {}", currency);
        currentRateApiCaller.getCurrentRate(currency);
    }
}
