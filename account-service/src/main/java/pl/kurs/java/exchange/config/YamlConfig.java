package pl.kurs.java.exchange.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import pl.kurs.java.exchange.config.properties.SupportedCurrenciesProperties;
import pl.kurs.java.exchange.util.yaml.YamlPropertySourceFactory;

@Configuration
@PropertySource(value = "classpath:exchange/supported-currencies.yml", factory = YamlPropertySourceFactory.class)
@EnableConfigurationProperties(SupportedCurrenciesProperties.class)
public class YamlConfig {
    // Required to make @PropertySource work with YAML
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

}
