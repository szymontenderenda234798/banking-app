package pl.kurs.java.exchange.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kurs.java.exchange.model.command.CreateExchangeRequestCommand;
import pl.kurs.java.exchange.service.ExchangeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/exchange")
@Slf4j
public class ExchangeController {

    private final ExchangeService exchangeService;

    @PostMapping("/{pesel}")
    public void processExchangeRequestCommand(@PathVariable String pesel, @RequestBody CreateExchangeRequestCommand createExchangeRequestCommand) {
        log.info("Processing exchange request for PESEL: {}", pesel);
        exchangeService.processExchangeRequestCommand(pesel, createExchangeRequestCommand);
    }
}
