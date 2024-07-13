package pl.kurs.java.exchange.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kurs.java.exchange.service.ExchangeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    @PostMapping
    public void exchange() {
        exchangeService.exchange();
    }
}
