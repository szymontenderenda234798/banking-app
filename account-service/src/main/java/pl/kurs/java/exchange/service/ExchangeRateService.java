package pl.kurs.java.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kurs.java.exchange.nbpapi.client.NbpExchangeRateApiClient;
import pl.kurs.java.exchange.config.SupportedCurrenciesConfig;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final NbpExchangeRateApiClient nbpExchangeRateApiClient;
    private final SupportedCurrenciesConfig supportedCurrenciesConfig;


    public double getExchangeRate(String currency) {
        if (!isCurrencySupported(currency)) {
            throw new IllegalArgumentException("Currency not supported");
        }
        return nbpExchangeRateApiClient.getExchangeRates(currency).getRates().get(0).getMid();
    }

    private boolean isCurrencySupported(String currency) {
        return supportedCurrenciesConfig.getForeignCurrencies().stream().map(String::toLowerCase).toList().contains(currency.toLowerCase());
    }
}
