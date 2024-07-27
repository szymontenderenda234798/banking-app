package pl.kurs.java.exchange.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import pl.kurs.java.exchange.config.properties.SupportedCurrenciesProperties;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SupportedCurrenciesConfig {
    private final SupportedCurrenciesProperties supportedCurrenciesProperties;

    public List<String> getBaseCurrency() {
        return supportedCurrenciesProperties.getBaseCurrency();
    }

    public List<String> getForeignCurrencies() {
        return supportedCurrenciesProperties.getForeignCurrencies();
    }
}
