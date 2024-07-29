package pl.kurs.java.exchange.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "currencies")
@Getter
@Setter
public class SupportedCurrenciesProperties {
    private List<String> baseCurrency;
    private List<String> foreignCurrencies;
}
